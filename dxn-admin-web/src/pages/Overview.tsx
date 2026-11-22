import { useCallback, useEffect, useState } from "react"
import {
  Users,
  BarChart3,
  Flame,
  Award,
  TrendingUp,
  TrendingDown,
  Send,
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
import { api, ApiError } from "@/lib/api"
import type { DashboardOverview, Role } from "@/lib/types"

const iconMap: Record<string, React.ElementType> = {
  Users,
  BarChart3,
  Flame,
  Award,
}

const roleLabels: Record<Role, string> = {
  TRAINEE: "متدرب",
  LEADER: "قائد",
  ADMIN: "مدير",
}

const fieldCls =
  "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"

function errMessage(e: unknown) {
  if (e instanceof ApiError) {
    const first = Object.values(e.errors ?? {})[0]?.[0]
    return first && first !== e.message ? `${e.message} ${first}` : e.message
  }
  return "تعذر الاتصال بالخادم"
}

function dateWindow(days: number) {
  const to = new Date()
  const from = new Date(to)
  from.setDate(from.getDate() - (days - 1))
  const iso = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
  return { from: iso(from), to: iso(to) }
}

function Empty({ message }: { message: string }) {
  return (
    <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
      {message}
    </div>
  )
}

export default function Overview() {
  const [days, setDays] = useState<7 | 30 | 90>(30)
  const [data, setData] = useState<DashboardOverview | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoadErr(null)
    try {
      const params = new URLSearchParams(dateWindow(days))
      setData(await api<DashboardOverview>(`/api/admin/dashboard/overview?${params}`))
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [days])

  useEffect(() => {
    void load()
  }, [load])

  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">نظرة عامة</h1>
          <p className="text-muted-foreground">ملخص أداء التطبيق والمتدربين</p>
        </div>
        <div className="flex items-center gap-2">
          <select
            className={cn(fieldCls, "w-auto")}
            aria-label="الفترة الزمنية"
            value={days}
            onChange={(e) => setDays(Number(e.target.value) as 7 | 30 | 90)}
          >
            <option value={7}>آخر 7 أيام</option>
            <option value={30}>آخر 30 يوماً</option>
            <option value={90}>آخر 90 يوماً</option>
          </select>
          <Button className="gap-2">
            <Send size={16} />
            إرسال تذكير جماعي
          </Button>
        </div>
      </header>

      {loadErr && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
          <span>{loadErr}</span>
          <Button variant="outline" size="sm" onClick={() => void load()}>
            إعادة المحاولة
          </Button>
        </div>
      )}

      {!data ? (
        <p className="py-16 text-center text-muted-foreground">
          {loadErr ? "تعذر تحميل البيانات" : "جارٍ التحميل..."}
        </p>
      ) : (
        <>
          {/* KPI Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {data.stats.map((stat) => {
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
                        className={stat.changeType === "up" ? "text-dxn-success" : "text-dxn-error"}
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
                {data.bagsProgress.length === 0 ? (
                  <Empty message="لا توجد حقائب منشورة بعد" />
                ) : (
                  <div className="space-y-4">
                    {data.bagsProgress.map((bag) => (
                      <div key={bag.sectionId}>
                        <div className="flex items-center justify-between text-sm mb-1">
                          <div className="flex items-center gap-2">
                            <Layers size={16} className="text-dxn-accent" />
                            <span className="font-medium">{bag.title}</span>
                          </div>
                          <span className="text-dxn-accent">{bag.percent}%</span>
                        </div>
                        <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                          <div
                            className="h-full rounded-full transition-all bg-dxn-success"
                            style={{ width: `${bag.percent}%` }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            <Card className="border-0 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg">الشهادات الصادرة</CardTitle>
                <CardDescription>إجمالي الشهادات المُنَوَّرة لكل قسم</CardDescription>
              </CardHeader>
              <CardContent>
                {data.certificatesBySection.length === 0 ? (
                  <Empty message="لا توجد شهادات بعد" />
                ) : (
                  <div className="space-y-3">
                    {data.certificatesBySection.map((cert) => (
                      <div
                        key={cert.sectionId}
                        className="flex items-center justify-between p-3 rounded-lg border hover:border-dxn-secondary transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-dxn-secondary/10 text-dxn-secondary flex items-center justify-center">
                            <Award size={20} />
                          </div>
                          <p className="font-medium text-sm">{cert.title}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-xl font-bold text-dxn-accent">{cert.issuedCount}</p>
                          <p className="text-xs text-muted-foreground">شهادة</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Activity Chart */}
            <Card className="lg:col-span-2 border-0 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg">النشاط خلال آخر {days} يوماً</CardTitle>
                <CardDescription>عدد الجلسات والمستخدمين النشطين</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {data.activity.length === 0 ? (
                  <Empty message="لا يوجد نشاط في هذه الفترة" />
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={data.activity}>
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
                )}
              </CardContent>
            </Card>

            {/* Failed Skills */}
            <Card className="border-0 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg">أكثر المهارات رسوباً</CardTitle>
                <CardDescription>نسبة الرسوب حسب المهارة</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {data.failedSkills.length === 0 ? (
                  <Empty message="لا توجد مهارات رسوب في هذه الفترة" />
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data.failedSkills} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                      <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 12 }} />
                      <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
                      <Tooltip formatter={(value) => [`${value}%`, "نسبة الرسوب"]} />
                      <Bar dataKey="failureRate" fill="#DC3545" radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
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
                  {data.inactiveUsers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center text-muted-foreground py-10">
                        لا يوجد متدربون متوقفون
                      </TableCell>
                    </TableRow>
                  ) : (
                    data.inactiveUsers.map((user) => (
                      <TableRow key={user.id}>
                        <TableCell className="font-medium">{user.name}</TableCell>
                        <TableCell>{roleLabels[user.role] ?? user.role}</TableCell>
                        <TableCell>{user.lastActive ?? "—"}</TableCell>
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
                    ))
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
