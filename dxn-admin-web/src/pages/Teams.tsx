import { useCallback, useEffect, useState } from "react"
import {
  UsersRound,
  TrendingUp,
  FileBarChart,
  ChevronLeft,
  X,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { api, ApiError } from "@/lib/api"
import type { Role, TeamDetail, TeamSummary } from "@/lib/types"

const roleLabels: Record<Role, string> = {
  TRAINEE: "متدرب",
  LEADER: "قائد",
  ADMIN: "مدير",
}
const roleVariants: Record<Role, "default" | "secondary" | "warning"> = {
  TRAINEE: "default",
  LEADER: "secondary",
  ADMIN: "warning",
}

function errMessage(e: unknown) {
  if (e instanceof ApiError) {
    const first = Object.values(e.errors ?? {})[0]?.[0]
    return first && first !== e.message ? `${e.message} ${first}` : e.message
  }
  return "تعذر الاتصال بالخادم"
}

const ProgressBar = ({ value }: { value: number }) => (
  <div className="h-2.5 w-full bg-gray-100 rounded-full overflow-hidden">
    <div
      className="h-full bg-game-leaf rounded-full transition-all"
      style={{ width: `${value}%` }}
    />
  </div>
)

// Same safety discipline as the server CSV: UTF-8 BOM and a leading ' on formula cells.
function csvCell(value: string | number | null | undefined): string {
  const raw = value === null || value === undefined ? "" : String(value)
  const safe = raw !== "" && "=+-@".includes(raw[0]) ? `'${raw}` : raw
  return `"${safe.replace(/"/g, '""')}"`
}

export default function Teams() {
  const [teams, setTeams] = useState<TeamSummary[] | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [detailId, setDetailId] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setTeams(await api<TeamSummary[]>("/api/admin/teams"))
      setLoadErr(null)
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const exportReport = () => {
    const rows: (string | number)[][] = [
      ["القائد", "عدد الأعضاء", "نسبة الإكمال", "نشط اليوم"],
      ...(teams ?? []).map((t) => [t.leaderName, t.members, `${t.completion}%`, t.activeToday]),
    ]
    const csv = "\uFEFF" + rows.map((row) => row.map(csvCell).join(",")).join("\r\n")
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }))
    const link = document.createElement("a")
    link.href = url
    link.download = "teams.csv"
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  }

  const list = teams ?? []
  const totalMembers = list.reduce((a, t) => a + t.members, 0)
  const avgCompletion = list.length ? Math.round(list.reduce((a, t) => a + t.completion, 0) / list.length) : 0

  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-brand-plum-500">الفرق</h1>
          <p className="text-muted-foreground">متابعة أداء القادة والأعضاء التابعين</p>
        </div>
        <Button variant="outline" className="gap-2" disabled={!list.length} onClick={exportReport}>
          <FileBarChart size={16} />
          تصدير تقرير
        </Button>
      </header>

      {(err || loadErr) && (
        <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-4 py-2 text-sm text-semantic-error">
          <span>{err ?? loadErr}</span>
          <button onClick={() => { setErr(null); setLoadErr(null) }} aria-label="إغلاق">
            <X size={16} />
          </button>
        </div>
      )}

      {teams === null ? (
        <p className="text-center text-muted-foreground py-20">جارٍ التحميل...</p>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {[
              { title: "إجمالي الفرق", value: String(list.length), icon: UsersRound },
              { title: "إجمالي الأعضاء", value: String(totalMembers), icon: UsersRound },
              { title: "متوسط الإكمال", value: `${avgCompletion}%`, icon: TrendingUp },
            ].map((stat, idx) => {
              const Icon = stat.icon
              return (
                <Card key={idx} className="border-0 shadow-sm">
                  <CardContent className="p-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-muted-foreground">{stat.title}</p>
                        <p className="text-2xl font-bold text-brand-plum-500">{stat.value}</p>
                      </div>
                      <div className="w-10 h-10 rounded-lg bg-brand-rose-600/10 text-brand-rose-600 flex items-center justify-center">
                        <Icon size={20} />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>

          {list.length === 0 ? (
            <Card className="border-0 shadow-sm">
              <CardContent className="py-16 text-center text-muted-foreground">لا توجد فرق مسجّلة بعد.</CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {list.map((team) => (
                <Card
                  key={team.leaderId}
                  className="border-0 shadow-sm cursor-pointer hover:border-brand-plum-500 transition-colors"
                  onClick={() => setDetailId(team.leaderId)}
                >
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-full bg-brand-plum-500/10 text-brand-plum-500 flex items-center justify-center text-lg font-bold">
                          {team.leaderName[0]}
                        </div>
                        <div>
                          <CardTitle className="text-lg">{team.leaderName}</CardTitle>
                          <CardDescription>{team.members} عضو · {team.activeToday} نشط اليوم</CardDescription>
                        </div>
                      </div>
                      <ChevronLeft size={18} className="text-muted-foreground" />
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>نسبة إكمال الفريق</span>
                        <span className="font-bold">{team.completion}%</span>
                      </div>
                      <ProgressBar value={team.completion} />
                    </div>
                    <Button
                      size="sm"
                      className="w-full"
                      onClick={(e) => {
                        e.stopPropagation()
                        setDetailId(team.leaderId)
                      }}
                    >
                      عرض الأعضاء
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {detailId && (
        <div className="fixed inset-0 z-50 flex">
          <div className="flex-1 bg-black/30" onClick={() => setDetailId(null)} />
          <div className="w-[560px] max-w-full bg-white shadow-xl overflow-y-auto">
            <TeamDetailPanel id={detailId} onClose={() => setDetailId(null)} />
          </div>
        </div>
      )}
    </div>
  )
}

function TeamDetailPanel({ id, onClose }: { id: string; onClose: () => void }) {
  const [detail, setDetail] = useState<TeamDetail | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)

  const fetchDetail = useCallback(async () => {
    setLoadErr(null)
    try {
      setDetail(await api<TeamDetail>(`/api/admin/teams/${id}`))
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [id])

  useEffect(() => {
    void fetchDetail()
  }, [fetchDetail])

  return (
    <div className="p-6 space-y-4">
      <button onClick={onClose} className="flex items-center gap-1 text-sm text-muted-foreground mb-4">
        <ChevronLeft size={16} />
        رجوع
      </button>

      {!detail ? (
        loadErr ? (
          <div className="text-center space-y-3">
            <p className="text-semantic-error">{loadErr}</p>
            <Button variant="outline" onClick={() => void fetchDetail()}>
              إعادة المحاولة
            </Button>
          </div>
        ) : (
          <p className="text-center text-muted-foreground">جارٍ التحميل...</p>
        )
      ) : (
        <>
          <div>
            <h2 className="text-xl font-bold text-brand-plum-500">{detail.leader.fullName}</h2>
            <p className="text-sm text-muted-foreground">قائد الفريق · {detail.members.length} عضو</p>
          </div>

          <Card className="border-0 shadow-sm">
            <CardHeader className="pb-2">
              <CardTitle className="text-base">أعضاء الفريق</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {detail.members.length === 0 ? (
                <p className="text-sm text-muted-foreground p-4">لا يوجد أعضاء تابعون لهذا القائد.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>العضو</TableHead>
                      <TableHead>الدور</TableHead>
                      <TableHead>XP</TableHead>
                      <TableHead>التقدم</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {detail.members.map((m) => (
                      <TableRow key={m.id}>
                        <TableCell>
                          <p className="font-medium">{m.fullName}</p>
                          <p className="text-xs text-muted-foreground">{m.lastActivityDate ?? "—"}</p>
                        </TableCell>
                        <TableCell>
                          <Badge variant={roleVariants[m.role]}>{roleLabels[m.role]}</Badge>
                        </TableCell>
                        <TableCell>{m.totalXp.toLocaleString("ar-SA")}</TableCell>
                        <TableCell className="min-w-[120px]">
                          <div className="flex items-center gap-2">
                            <ProgressBar value={m.progressPercent} />
                            <span className="text-xs font-medium">{m.progressPercent}%</span>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
