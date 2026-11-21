import {
  Calendar,
  Filter,
  Download,
  BarChart3,
  Clock,
  Target,
  TrendingDown,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  LineChart,
  Line,
  Tooltip,
} from "recharts"
import {
  analyticsSummary,
  funnelData,
  failedQuestions,
  retentionData,
} from "@/data/mockData"

const icons = [
  BarChart3,
  Clock,
  Target,
  TrendingDown,
]

export default function AnalyticsPage() {
  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">التحليلات</h1>
          <p className="text-muted-foreground">أداء المحتوى والمتدربين والفرق</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="gap-2">
            <Calendar size={16} />
            آخر 30 يوم
          </Button>
          <Button variant="outline" className="gap-2">
            <Filter size={16} />
            فلتر
          </Button>
          <Button className="gap-2">
            <Download size={16} />
            تصدير
          </Button>
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {analyticsSummary.map((stat, idx) => {
          const Icon = icons[idx]
          return (
            <Card key={idx} className="border-0 shadow-sm">
              <CardContent className="p-5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">{stat.title}</p>
                    <p className="text-2xl font-bold text-dxn-accent">{stat.value}</p>
                    <p className={cn("text-xs", stat.change > 0 ? "text-dxn-success" : "text-dxn-error")}>
                      {stat.change > 0 ? "+" : ""}
                      {stat.change}% من الفترة السابقة
                    </p>
                  </div>
                  <div className="w-10 h-10 rounded-lg bg-dxn-primary/10 text-dxn-primary flex items-center justify-center">
                    <Icon size={20} />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2 border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">مسار الإكمال — Funnel</CardTitle>
            <CardDescription>من فتح المستوى إلى المستوى التالي</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={funnelData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" />
                <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="value" fill="#E52F12" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">Retention</CardTitle>
            <CardDescription>Day 1 / 7 / 30</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={retentionData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="day" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => [`${value}%`, "Retention"]} />
                <Line type="monotone" dataKey="rate" stroke="#D4A017" strokeWidth={3} dot={{ r: 5, fill: "#D4A017" }} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="border-0 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg">أكثر الأسئلة رسوباً</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={failedQuestions} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" domain={[0, 100]} />
                <YAxis type="category" dataKey="question" width={140} tick={{ fontSize: 11 }} />
                <Tooltip formatter={(value) => [`${value}%`, "نسبة الخطأ"]} />
                <Bar dataKey="errorRate" fill="#DC3545" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="border-0 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-lg">تفاصيل الأداء</CardTitle>
              <CardDescription>أسئلة الأداء الأسوأ</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>السؤال</TableHead>
                  <TableHead>المحاولات</TableHead>
                  <TableHead>نسبة الخطأ</TableHead>
                  <TableHead>متوسط الوقت</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {failedQuestions.map((q) => (
                  <TableRow key={q.id}>
                    <TableCell className="font-medium">{q.question}</TableCell>
                    <TableCell>{q.attempts}</TableCell>
                    <TableCell>
                      <Badge variant="destructive">{q.errorRate}%</Badge>
                    </TableCell>
                    <TableCell>{q.avgTime}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

function cn(...classes: (string | boolean | undefined)[]) {
  return classes.filter(Boolean).join(" ")
}
