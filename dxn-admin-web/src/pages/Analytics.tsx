import { useCallback, useEffect, useState } from "react"
import {
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
import { api, ApiError, TOKEN_KEY } from "@/lib/api"
import { chartColors } from "@/lib/chartColors"
import type { AdminUser, AnalyticsData, Paginated, Section } from "@/lib/types"

const icons = [
  BarChart3,
  Clock,
  Target,
  TrendingDown,
]

const summaryIcons: Record<string, React.ElementType> = {
  "Completion Rate": BarChart3,
  "Average Time": Clock,
  "First-Attempt Success": Target,
  "Drop-off Rate": TrendingDown,
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

function buildQuery(days: number, sectionId: string, leaderId: string) {
  const params = new URLSearchParams(dateWindow(days))
  if (sectionId) params.set("sectionId", sectionId)
  if (leaderId) params.set("leaderId", leaderId)
  return params.toString()
}

function Empty({ message }: { message: string }) {
  return (
    <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
      {message}
    </div>
  )
}

export default function AnalyticsPage() {
  const [days, setDays] = useState<7 | 30 | 90>(30)
  const [sectionId, setSectionId] = useState("")
  const [leaderId, setLeaderId] = useState("")
  const [showFilters, setShowFilters] = useState(false)
  const [data, setData] = useState<AnalyticsData | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [sections, setSections] = useState<Section[]>([])
  const [leaders, setLeaders] = useState<AdminUser[]>([])
  const [exportErr, setExportErr] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)

  const load = useCallback(async () => {
    setLoadErr(null)
    try {
      setData(await api<AnalyticsData>(`/api/admin/analytics?${buildQuery(days, sectionId, leaderId)}`))
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [days, sectionId, leaderId])

  useEffect(() => {
    void load()
  }, [load])

  // Filter options are non-blocking: an empty select simply means "all" for that dimension.
  useEffect(() => {
    let live = true
    api<Section[]>("/api/admin/content/tree")
      .then((tree) => {
        if (live) setSections(tree)
      })
      .catch(() => {})
    api<Paginated<AdminUser>>("/api/admin/users?role=leader&perPage=100")
      .then((res) => {
        if (live) setLeaders(res.data)
      })
      .catch(() => {})
    return () => {
      live = false
    }
  }, [])

  const exportCsv = async () => {
    setExportErr(null)
    setExporting(true)
    try {
      const token = localStorage.getItem(TOKEN_KEY)
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/api/admin/analytics/export?${buildQuery(days, sectionId, leaderId)}`,
        {
          headers: {
            Accept: "text/csv",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        },
      )
      if (res.status === 401) {
        localStorage.removeItem(TOKEN_KEY)
        window.location.assign("/login")
        return
      }
      if (!res.ok) throw new Error("تعذر تصدير التحليلات")
      // The server CSV already carries its BOM and escaped cells: save the blob bytes unchanged.
      const blob = await res.blob()
      const url = URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = "analytics.csv"
      document.body.appendChild(link)
      link.click()
      link.remove()
      URL.revokeObjectURL(url)
    } catch (e) {
      setExportErr(errMessage(e))
    } finally {
      setExporting(false)
    }
  }

  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-brand-plum-500">التحليلات</h1>
          <p className="text-muted-foreground">أداء المحتوى والمتدربين والفرق</p>
        </div>
        <div className="flex gap-2">
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
          <Button
            variant="outline"
            className="gap-2"
            onClick={() => setShowFilters((v) => !v)}
          >
            <Filter size={16} />
            فلتر
          </Button>
          <Button className="gap-2" disabled={exporting} onClick={() => void exportCsv()}>
            <Download size={16} />
            {exporting ? "جارٍ التصدير..." : "تصدير"}
          </Button>
        </div>
      </header>

      {loadErr && (
        <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-4 py-2 text-sm text-semantic-error">
          <span>{loadErr}</span>
          <Button variant="outline" size="sm" onClick={() => void load()}>
            إعادة المحاولة
          </Button>
        </div>
      )}
      {exportErr && (
        <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-4 py-2 text-sm text-semantic-error">
          <span>{exportErr}</span>
          <button onClick={() => setExportErr(null)} aria-label="إغلاق">
            ×
          </button>
        </div>
      )}

      {showFilters && (
        <Card className="border-0 shadow-sm">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg">تصفية التحليلات</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-wrap items-end gap-4">
            <div className="space-y-2 min-w-[220px] flex-1">
              <label className="text-sm font-medium">الحقيبة التدريبية</label>
              <select
                className={fieldCls}
                aria-label="تصفية حسب الحقيبة"
                value={sectionId}
                onChange={(e) => setSectionId(e.target.value)}
              >
                <option value="">كل الحقائب</option>
                {sections.map((section) => (
                  <option key={section.id} value={section.id}>
                    {section.title}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-2 min-w-[220px] flex-1">
              <label className="text-sm font-medium">القائد</label>
              <select
                className={fieldCls}
                aria-label="تصفية حسب القائد"
                value={leaderId}
                onChange={(e) => setLeaderId(e.target.value)}
              >
                <option value="">كل القادة</option>
                {leaders.map((leader) => (
                  <option key={leader.id} value={leader.id}>
                    {leader.fullName}
                  </option>
                ))}
              </select>
            </div>
            <Button
              variant="outline"
              onClick={() => {
                setSectionId("")
                setLeaderId("")
              }}
            >
              إعادة تعيين
            </Button>
          </CardContent>
        </Card>
      )}

      {!data ? (
        <p className="py-16 text-center text-muted-foreground">
          {loadErr ? "تعذر تحميل البيانات" : "جارٍ التحميل..."}
        </p>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {data.summary.map((stat, idx) => {
              const Icon = summaryIcons[stat.title] ?? icons[idx] ?? BarChart3
              const changeClass =
                stat.change > 0 ? "text-game-leaf-text" : stat.change < 0 ? "text-semantic-error" : "text-muted-foreground"
              return (
                <Card key={stat.title} className="border-0 shadow-sm">
                  <CardContent className="p-5">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-muted-foreground">{stat.title}</p>
                        <p className="text-2xl font-bold text-brand-plum-500">{stat.value}</p>
                        <p className={cn("text-xs", changeClass)}>
                          {stat.change > 0 ? "+" : ""}
                          {stat.change}% من الفترة السابقة
                        </p>
                      </div>
                      <div className="w-10 h-10 rounded-lg bg-brand-plum-500/10 text-brand-plum-500 flex items-center justify-center">
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
                {data.funnel.length === 0 ? (
                  <Empty message="لا توجد بيانات مسار في هذه الفترة" />
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data.funnel} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                      <XAxis type="number" />
                      <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="value" fill={chartColors.primary} radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            <Card className="border-0 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg">Retention</CardTitle>
                <CardDescription>Day 1 / 7 / 30</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {data.retention.length === 0 ? (
                  <Empty message="لا توجد بيانات احتفاظ في هذه الفترة" />
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={data.retention}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="day" tick={{ fontSize: 12 }} />
                      <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                      <Tooltip formatter={(value) => [`${value}%`, "Retention"]} />
                      <Line
                        type="monotone"
                        dataKey="rate"
                        stroke={chartColors.secondary}
                        strokeWidth={3}
                        dot={{ r: 5, fill: chartColors.secondary }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card className="border-0 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg">أكثر الأسئلة رسوباً</CardTitle>
              </CardHeader>
              <CardContent className="h-72">
                {data.failedQuestions.length === 0 ? (
                  <Empty message="لا توجد أسئلة رسوب في هذه الفترة" />
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data.failedQuestions} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                      <XAxis type="number" domain={[0, 100]} />
                      <YAxis type="category" dataKey="question" width={140} tick={{ fontSize: 11 }} />
                      <Tooltip formatter={(value) => [`${value}%`, "نسبة الخطأ"]} />
                      <Bar dataKey="errorRate" fill={chartColors.error} radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
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
                    {data.failedQuestions.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} className="text-center text-muted-foreground py-10">
                          لا توجد بيانات أسئلة في هذه الفترة
                        </TableCell>
                      </TableRow>
                    ) : (
                      data.failedQuestions.map((q) => (
                        <TableRow key={q.id}>
                          <TableCell className="font-medium">{q.question}</TableCell>
                          <TableCell>{q.attempts}</TableCell>
                          <TableCell>
                            <Badge variant="destructive">{q.errorRate}%</Badge>
                          </TableCell>
                          <TableCell>{q.avgTime}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  )
}

function cn(...classes: (string | boolean | undefined)[]) {
  return classes.filter(Boolean).join(" ")
}
