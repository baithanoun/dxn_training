import {
  Users,
  BarChart3,
  Flame,
  Award,
  TrendingUp,
  TrendingDown,
  Send,
  Lock,
  Layers,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
} from "recharts"
import {
  overviewStats,
  activityData,
  failedSkills,
  inactiveUsers,
  sections,
  certificates,
} from "@/data/mockData"

const iconMap: Record<string, React.ElementType> = {
  Users,
  BarChart3,
  Flame,
  Award,
}

export default function Overview() {
  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">نظرة عامة</h1>
          <p className="text-muted-foreground">ملخص أداء التطبيق والمتدربين</p>
        </div>
        <Button className="gap-2">
          <Send size={16} />
          إرسال تذكير جماعي
        </Button>
      </header>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {overviewStats.map((stat) => {
          const Icon = iconMap[stat.icon] ?? Users
          const ChangeIcon = stat.changeType === "up" ? TrendingUp : TrendingDown
          return (
            <Card key={stat.title} className="border-0 shadow-sm">
              <CardHeader className="pb-2">
                <CardDescription className="text-base">{stat.title}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-dxn-accent">{stat.value}</span>
                  <div className="w-10 h-10 rounded-lg bg-dxn-primary/10 text-dxn-primary flex items-center justify-center">
                    <Icon size={20} />
                  </div>
                </div>
                <div className="flex items-center gap-1 text-sm">
                  <ChangeIcon
                    size={16}
                    className={stat.changeType === "up" ? "text-dxn-success" : "text-dxn-error"}
                  />
                  <span
                    className={
                      stat.changeType === "up" ? "text-dxn-success" : "text-dxn-error"
                    }
                  >
                    {stat.change}%
                  </span>
                  <span className="text-muted-foreground">منذ الأسبوع الماضي</span>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {/* Section / Certificate Progress */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">تقدم الحقائب الخمس</CardTitle>
            <CardDescription>نسبة الإكمال حسب الحقيبة التدريبية</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {sections.map((section) => {
                const cert = certificates.find((c) => c.sectionId === section.id)
                const isLocked = !!section.requiredSectionId
                // نسبة الإكمال محسوبة من الشهادات المُصدَّرة بشكل تقريبي
                // نستخدم إجمالي الشهادات كقاعدة قصوى افتراضية بدلاً من عدد المستخدمين لتجنب اختلاف الأنواع
                const issuedCount = cert?.issuedCount ?? 0
                const totalCerts = certificates.reduce((sum, c) => sum + c.issuedCount, 0)
                const completion = isLocked
                  ? 0
                  : totalCerts > 0
                  ? Math.min(100, Math.round((issuedCount / totalCerts) * 100 * 3))
                  : 0
                return (
                  <div key={section.id}>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <div className="flex items-center gap-2">
                        <Layers size={16} className="text-dxn-accent" />
                        <span className="font-medium">{section.title}</span>
                        <span className="text-xs text-muted-foreground">(الحقيبة {section.bagId})</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {isLocked && <Lock size={14} className="text-dxn-error" />}
                        <span className={isLocked ? "text-dxn-error" : "text-dxn-accent"}>
                          {isLocked ? "مقفل" : `${completion}%`}
                        </span>
                      </div>
                    </div>
                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={cn(
                          "h-full rounded-full transition-all",
                          isLocked ? "bg-gray-300" : "bg-dxn-success"
                        )}
                        style={{ width: isLocked ? "0%" : `${completion}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </CardContent>
        </Card>

        <Card className="border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">الشهادات الصادرة</CardTitle>
            <CardDescription>إجمالي الشهادات المُنَوَّرة لكل قسم</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {certificates.map((cert) => (
                <div
                  key={cert.id}
                  className="flex items-center justify-between p-3 rounded-lg border hover:border-dxn-secondary transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-dxn-secondary/10 text-dxn-secondary flex items-center justify-center">
                      <Award size={20} />
                    </div>
                    <div>
                      <p className="font-medium text-sm">{cert.title}</p>
                      <p className="text-xs text-muted-foreground">
                        الحقيبة {cert.sectionId}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-xl font-bold text-dxn-accent">{cert.issuedCount}</p>
                    <p className="text-xs text-muted-foreground">شهادة</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Activity Chart */}
        <Card className="lg:col-span-2 border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">النشاط خلال آخر 30 يوماً</CardTitle>
            <CardDescription>عدد الجلسات والمستخدمين النشطين</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={activityData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip />
                <Line
                  type="monotone"
                  dataKey="sessions"
                  stroke="#E52F12"
                  strokeWidth={3}
                  dot={{ r: 4, fill: "#E52F12" }}
                  name="الجلسات"
                />
                <Line
                  type="monotone"
                  dataKey="activeUsers"
                  stroke="#D4A017"
                  strokeWidth={3}
                  dot={{ r: 4, fill: "#D4A017" }}
                  name="المستخدمون النشطون"
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Failed Skills */}
        <Card className="border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">أكثر المهارات رسوباً</CardTitle>
            <CardDescription>نسبة الرسوب حسب المهارة</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={failedSkills} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 12 }} />
                <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
                <Tooltip formatter={(value) => [`${value}%`, "نسبة الرسوب"]} />
                <Bar dataKey="failureRate" fill="#DC3545" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Inactive Users */}
      <Card className="border-0 shadow-sm">
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-lg">المتدربون المتوقفون أكثر من 3 أيام</CardTitle>
            <CardDescription>قائمة بالأعضاء الذين يحتاجون لتذكير</CardDescription>
          </div>
          <Button variant="outline" size="sm">عرض الكل</Button>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>الاسم</TableHead>
                <TableHead>الدور</TableHead>
                <TableHead>آخر نشاط</TableHead>
                <TableHead>إجمالي XP</TableHead>
                <TableHead>الحالة</TableHead>
                <TableHead>إجراء</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {inactiveUsers.map((user) => (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">{user.name}</TableCell>
                  <TableCell>{user.role}</TableCell>
                  <TableCell>{user.lastActive}</TableCell>
                  <TableCell>{user.xp.toLocaleString("ar-SA")}</TableCell>
                  <TableCell>
                    <Badge variant="warning">متوقف</Badge>
                  </TableCell>
                  <TableCell>
                    <Button variant="ghost" size="sm" className="text-dxn-primary">
                      إرسال تذكير
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}
