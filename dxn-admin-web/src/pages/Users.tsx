import { useCallback, useEffect, useState, type FormEvent } from "react"
import {
  Search,
  UserCog,
  Ban,
  Check,
  Award,
  ChevronLeft,
  X,
  Download,
  Plus,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { api, ApiError, TOKEN_KEY } from "@/lib/api"
import { cn } from "@/lib/utils"
import type {
  AdminUser,
  AdminUserDetail,
  Paginated,
  Role,
} from "@/lib/types"

type ApiRole = "admin" | "leader" | "trainee"
type RoleFilter = ApiRole | ""
type StatusFilter = "active" | "inactive" | "disabled" | ""
type GenderValue = "" | "male" | "female"
type StatusVariant = "success" | "warning" | "muted"

const PER_PAGE = 20

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

function genderLabel(gender: AdminUser["gender"]) {
  if (gender === "MALE") return "ذكر"
  if (gender === "FEMALE") return "أنثى"
  return "غير محدد"
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

// Same active/inactive/disabled split the API uses for status filtering and CSV export.
function statusOf(u: AdminUser): { label: string; variant: StatusVariant } {
  if (!u.isActive) return { label: "معطّل", variant: "muted" }
  const cutoff = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
  if (u.lastActivityDate && u.lastActivityDate >= cutoff) return { label: "نشط", variant: "success" }
  return { label: "متوقف", variant: "warning" }
}

function buildParams(query: string, role: RoleFilter, status: StatusFilter, gender: GenderValue, withPage?: number) {
  const p = new URLSearchParams()
  if (query) p.set("search", query)
  if (role) p.set("role", role)
  if (status) p.set("status", status)
  if (gender) p.set("gender", gender)
  if (withPage !== undefined) p.set("page", String(withPage))
  p.set("perPage", String(PER_PAGE))
  return p.toString()
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">{label}</label>
      {children}
    </div>
  )
}

export default function Users() {
  const [search, setSearch] = useState("")
  const [query, setQuery] = useState("")
  const [roleFilter, setRoleFilter] = useState<RoleFilter>("")
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("")
  const [genderFilter, setGenderFilter] = useState<GenderValue>("")
  const [page, setPage] = useState(1)
  const [rows, setRows] = useState<AdminUser[]>([])
  const [meta, setMeta] = useState<Paginated<AdminUser>["meta"] | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [detailId, setDetailId] = useState<string | null>(null)
  const [leaders, setLeaders] = useState<AdminUser[]>([])
  const [exporting, setExporting] = useState(false)

  useEffect(() => {
    const t = window.setTimeout(() => {
      setQuery(search.trim())
      setPage(1)
    }, 300)
    return () => window.clearTimeout(t)
  }, [search])

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api<Paginated<AdminUser>>(`/api/admin/users?${buildParams(query, roleFilter, statusFilter, genderFilter, page)}`)
      setRows(res.data)
      setMeta(res.meta)
      setLoadErr(null)
    } catch (e) {
      setLoadErr(errMessage(e))
    } finally {
      setLoading(false)
    }
  }, [query, roleFilter, statusFilter, genderFilter, page])

  useEffect(() => {
    void load()
  }, [load])

  // Leaders for both the create form and the detail panel's leader select.
  useEffect(() => {
    let live = true
    api<Paginated<AdminUser>>("/api/admin/users?role=leader&perPage=100")
      .then((res) => {
        if (live) setLeaders(res.data)
      })
      .catch(() => {
        // Non-blocking: the select simply stays empty if this request fails.
      })
    return () => {
      live = false
    }
  }, [])

  const exportCsv = async () => {
    setErr(null)
    setExporting(true)
    try {
      const token = localStorage.getItem(TOKEN_KEY)
      const res = await fetch(`${import.meta.env.VITE_API_URL}/api/admin/users/export?${buildParams(query, roleFilter, statusFilter, "")}`, {
        headers: {
          Accept: "text/csv",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      })
      if (res.status === 401) {
        localStorage.removeItem(TOKEN_KEY)
        window.location.assign("/login")
        return
      }
      if (!res.ok) throw new Error("تعذر تصدير المستخدمين")
      // The server CSV already carries its BOM and escaped cells: save the blob bytes unchanged.
      const blob = await res.blob()
      const url = URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = "users.csv"
      document.body.appendChild(link)
      link.click()
      link.remove()
      URL.revokeObjectURL(url)
    } catch (e) {
      setErr(errMessage(e))
    } finally {
      setExporting(false)
    }
  }

  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-brand-plum-500">المستخدمون</h1>
          <p className="text-muted-foreground">إدارة حسابات المتدربين والقادة والمديرين</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" className="gap-2" disabled={exporting} onClick={() => void exportCsv()}>
            <Download size={16} />
            {exporting ? "جارٍ التصدير..." : "تصدير CSV"}
          </Button>
          <Button className="gap-2" onClick={() => setCreating((v) => !v)}>
            <UserCog size={16} />
            إضافة مستخدم
          </Button>
        </div>
      </header>

      {err && (
        <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-4 py-2 text-sm text-semantic-error">
          <span>{err}</span>
          <button onClick={() => setErr(null)} aria-label="إغلاق">
            <X size={16} />
          </button>
        </div>
      )}

      {creating && (
        <CreateUserForm
          leaders={leaders}
          onCancel={() => setCreating(false)}
          onCreated={() => {
            setCreating(false)
            void load()
          }}
        />
      )}

      <div className="flex flex-wrap items-center gap-2">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <Search className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={18} />
          <Input
            placeholder="بحث بالاسم أو البريد أو رقم العضوية..."
            className="pr-10"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select
          className={cn(fieldCls, "w-auto")}
          aria-label="تصفية حسب الدور"
          value={roleFilter}
          onChange={(e) => {
            setRoleFilter(e.target.value as RoleFilter)
            setPage(1)
          }}
        >
          <option value="">الكل</option>
          <option value="trainee">متدرب</option>
          <option value="leader">قائد</option>
          <option value="admin">مدير</option>
        </select>
        <select
          className={cn(fieldCls, "w-auto")}
          aria-label="تصفية حسب الحالة"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as StatusFilter)
            setPage(1)
          }}
        >
          <option value="">الكل</option>
          <option value="active">نشط</option>
          <option value="inactive">متوقف</option>
          <option value="disabled">معطّل</option>
        </select>
        <select
          className={cn(fieldCls, "w-auto")}
          aria-label="تصفية حسب الجنس"
          value={genderFilter}
          onChange={(e) => {
            setGenderFilter(e.target.value as GenderValue)
            setPage(1)
          }}
        >
          <option value="">الكل</option>
          <option value="male">ذكر</option>
          <option value="female">أنثى</option>
        </select>
      </div>

      <Card className="border-0 shadow-sm">
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>الاسم</TableHead>
                <TableHead>رقم العضوية</TableHead>
                <TableHead>الدور</TableHead>
                <TableHead>الجنس</TableHead>
                <TableHead>القائد</TableHead>
                <TableHead>XP</TableHead>
                <TableHead>آخر نشاط</TableHead>
                <TableHead>الحالة</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((user) => {
                const status = statusOf(user)
                return (
                  <TableRow key={user.id} className="cursor-pointer" onClick={() => setDetailId(user.id)}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-brand-plum-500/10 text-brand-plum-500 flex items-center justify-center font-bold">
                          {user.fullName[0]}
                        </div>
                        <div>
                          <p className="font-medium">{user.fullName}</p>
                          <p className="text-xs text-muted-foreground">{user.email ?? user.phone ?? "—"}</p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{user.dxnId ?? "—"}</TableCell>
                    <TableCell>
                      <Badge variant={roleVariants[user.role]}>{roleLabels[user.role]}</Badge>
                    </TableCell>
                    <TableCell>{genderLabel(user.gender)}</TableCell>
                    <TableCell>{user.leaderName ?? "—"}</TableCell>
                    <TableCell>{user.totalXp.toLocaleString("ar-SA")}</TableCell>
                    <TableCell>{user.lastActivityDate ?? "—"}</TableCell>
                    <TableCell>
                      <Badge variant={status.variant}>{status.label}</Badge>
                    </TableCell>
                  </TableRow>
                )
              })}
              {!loading && rows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} className="text-center text-muted-foreground py-10">
                    {loadErr ? loadErr : "لا يوجد مستخدمون مطابقون"}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
          <div className="flex items-center justify-between border-t px-4 py-3">
            <p className="text-sm text-muted-foreground">
              صفحة {meta?.page ?? 1} من {meta?.lastPage ?? 1}
              {meta ? ` · ${meta.total} مستخدم` : ""}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={loading || !meta || meta.page <= 1}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
              >
                السابق
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={loading || !meta || meta.page >= meta.lastPage}
                onClick={() => setPage((p) => p + 1)}
              >
                التالي
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {detailId && (
        <div className="fixed inset-0 z-50 flex">
          <div className="flex-1 bg-black/30" onClick={() => setDetailId(null)} />
          <div className="w-[440px] max-w-full bg-white shadow-xl overflow-y-auto">
            <UserPanel
              id={detailId}
              leaders={leaders}
              onClose={() => setDetailId(null)}
              onChanged={() => void load()}
            />
          </div>
        </div>
      )}
    </div>
  )
}

function CreateUserForm({
  leaders,
  onCancel,
  onCreated,
}: {
  leaders: AdminUser[]
  onCancel: () => void
  onCreated: () => void
}) {
  const [f, setF] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    dxnId: "",
    role: "trainee" as ApiRole,
    gender: "" as GenderValue,
    leaderId: "",
  })
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setF((p) => ({ ...p, [k]: e.target.value }))

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setBusy(true)
    setErr(null)
    try {
      await api("/api/admin/users", {
        json: {
          fullName: f.fullName,
          email: f.email || null,
          phone: f.phone || null,
          password: f.password,
          dxnId: f.dxnId || null,
          role: f.role,
          gender: f.gender || null,
          leaderId: f.leaderId || null,
        },
      })
      onCreated()
    } catch (e) {
      setErr(errMessage(e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Card className="border-0 shadow-sm">
      <CardHeader className="pb-2">
        <CardTitle className="text-lg">إضافة مستخدم</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={(e) => void submit(e)}>
          {err && <p className="text-sm text-semantic-error">{err}</p>}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Field label="الاسم الكامل">
              <Input required value={f.fullName} onChange={set("fullName")} />
            </Field>
            <Field label="رقم العضوية DXN">
              <Input value={f.dxnId} onChange={set("dxnId")} />
            </Field>
            <Field label="البريد الإلكتروني">
              <Input type="email" value={f.email} onChange={set("email")} />
            </Field>
            <Field label="الهاتف">
              <Input value={f.phone} onChange={set("phone")} />
            </Field>
            <Field label="كلمة المرور">
              <Input type="password" required minLength={8} value={f.password} onChange={set("password")} />
            </Field>
            <Field label="الدور">
              <select className={fieldCls} value={f.role} onChange={set("role")}>
                <option value="trainee">متدرب</option>
                <option value="leader">قائد</option>
                <option value="admin">مدير</option>
              </select>
            </Field>
            <Field label="الجنس">
              <select className={fieldCls} value={f.gender} onChange={set("gender")}>
                <option value="">غير محدد</option>
                <option value="male">ذكر</option>
                <option value="female">أنثى</option>
              </select>
            </Field>
            <Field label="القائد">
              <select className={fieldCls} value={f.leaderId} onChange={set("leaderId")}>
                <option value="">لا يوجد</option>
                {leaders.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.fullName}
                  </option>
                ))}
              </select>
            </Field>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onCancel} disabled={busy}>
              إلغاء
            </Button>
            <Button type="submit" disabled={busy}>
              <Plus size={16} />
              إنشاء المستخدم
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function UserPanel({
  id,
  leaders,
  onClose,
  onChanged,
}: {
  id: string
  leaders: AdminUser[]
  onClose: () => void
  onChanged: () => void
}) {
  const [detail, setDetail] = useState<AdminUserDetail | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [role, setRole] = useState<ApiRole>("trainee")
  const [gender, setGender] = useState<GenderValue>("")
  const [leaderId, setLeaderId] = useState("")

  const fetchDetail = useCallback(async () => {
    setLoadErr(null)
    try {
      setDetail(await api<AdminUserDetail>(`/api/admin/users/${id}`))
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [id])

  useEffect(() => {
    void fetchDetail()
  }, [fetchDetail])

  useEffect(() => {
    if (!detail) return
    setRole(detail.role.toLowerCase() as ApiRole)
    setGender(detail.gender ? (detail.gender.toLowerCase() as GenderValue) : "")
    setLeaderId(detail.leaderId ?? "")
  }, [detail])

  const save = async () => {
    if (!detail) return
    setBusy(true)
    setErr(null)
    try {
      await api(`/api/admin/users/${id}`, { method: "PUT", json: { role, gender: gender || null, leaderId: leaderId || null } })
      await fetchDetail()
      onChanged()
    } catch (e) {
      setErr(errMessage(e))
    } finally {
      setBusy(false)
    }
  }

  const toggleActive = async () => {
    if (!detail) return
    const next = !detail.isActive
    if (!window.confirm(next ? "تفعيل هذا الحساب؟" : "تعطيل هذا الحساب؟ سيتم إلغاء جميع جلساته.")) return
    setBusy(true)
    setErr(null)
    try {
      await api(`/api/admin/users/${id}`, { method: "PUT", json: { isActive: next } })
      await fetchDetail()
      onChanged()
    } catch (e) {
      setErr(errMessage(e))
    } finally {
      setBusy(false)
    }
  }

  if (!detail)
    return (
      <div className="p-6 space-y-3 text-center">
        <button onClick={onClose} className="flex items-center gap-1 text-sm text-muted-foreground">
          <ChevronLeft size={16} />
          رجوع
        </button>
        {loadErr ? (
          <>
            <p className="text-semantic-error">{loadErr}</p>
            <Button variant="outline" onClick={() => void fetchDetail()}>
              إعادة المحاولة
            </Button>
          </>
        ) : (
          <p className="text-muted-foreground">جارٍ التحميل...</p>
        )}
      </div>
    )

  const status = statusOf(detail)
  return (
    <div className="p-6 space-y-4">
      <button onClick={onClose} className="flex items-center gap-1 text-sm text-muted-foreground mb-4">
        <ChevronLeft size={16} />
        رجوع
      </button>

      {err && <p className="text-sm text-semantic-error">{err}</p>}

      <div className="flex items-center gap-4">
        <div className="w-16 h-16 rounded-full bg-brand-plum-500 text-white flex items-center justify-center text-2xl font-bold">
          {detail.fullName[0]}
        </div>
        <div>
          <h2 className="text-xl font-bold">{detail.fullName}</h2>
          <p className="text-muted-foreground">{detail.email ?? detail.phone ?? "—"}</p>
          <div className="flex gap-2 mt-1">
            <Badge variant={roleVariants[detail.role]}>{roleLabels[detail.role]}</Badge>
            <Badge variant={status.variant}>{status.label}</Badge>
          </div>
        </div>
      </div>

      <Card className="border-0 shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base">معلومات الحساب</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">رقم العضوية DXN</span>
            <span className="font-medium">{detail.dxnId ?? "—"}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">الجنس</span>
            <span className="font-medium">{genderLabel(detail.gender)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">القائد</span>
            <span className="font-medium">{detail.leaderName ?? "—"}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">إجمالي XP</span>
            <span className="font-medium">{detail.totalXp.toLocaleString("ar-SA")}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">الـ Streak الحالي</span>
            <span className="font-medium">{detail.currentStreak}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">آخر نشاط</span>
            <span className="font-medium">{detail.lastActivityDate ?? "—"}</span>
          </div>
        </CardContent>
      </Card>

      <Card className="border-0 shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base">تعديل الحساب</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Field label="الدور">
            <select className={fieldCls} value={role} disabled={busy} onChange={(e) => setRole(e.target.value as ApiRole)}>
              <option value="trainee">متدرب</option>
              <option value="leader">قائد</option>
              <option value="admin">مدير</option>
            </select>
          </Field>
          <Field label="الجنس">
            <select className={fieldCls} value={gender} disabled={busy} onChange={(e) => setGender(e.target.value as GenderValue)}>
              <option value="">غير محدد</option>
              <option value="male">ذكر</option>
              <option value="female">أنثى</option>
            </select>
          </Field>
          <Field label="القائد">
            <select className={fieldCls} value={leaderId} disabled={busy} onChange={(e) => setLeaderId(e.target.value)}>
              <option value="">لا يوجد</option>
              {leaders.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.fullName}
                </option>
              ))}
            </select>
          </Field>
          <div className="flex gap-2 pt-1">
            <Button className="flex-1" disabled={busy} onClick={() => void save()}>
              حفظ التغييرات
            </Button>
            <Button
              variant={detail.isActive ? "destructive" : "default"}
              className="flex-1 gap-2"
              disabled={busy}
              onClick={() => void toggleActive()}
            >
              {detail.isActive ? <Ban size={16} /> : <Check size={16} />}
              {detail.isActive ? "تعطيل" : "تفعيل"}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card className="border-0 shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base">تقدم الحقائب التدريبية</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {/* shortcut: AdminUserResource returns sectionsProgress: [] today; this empty state covers it and the same markup renders bars once the API populates the field. */}
          {detail.sectionsProgress.length === 0 ? (
            <p className="text-sm text-muted-foreground">لا توجد بيانات تقدم لهذا المستخدم بعد.</p>
          ) : (
            detail.sectionsProgress.map((p) => (
              <div key={p.sectionId}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="truncate max-w-[260px]">{p.sectionTitle ?? p.sectionId}</span>
                  <span className="font-medium">{p.progressPercent}%</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-game-leaf rounded-full"
                    style={{ width: `${p.progressPercent}%` }}
                  />
                </div>
              </div>
            ))
          )}
        </CardContent>
      </Card>

      <Card className="border-0 shadow-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-base flex items-center gap-2">
            <Award size={18} />
            الشهادات المُنَوَّرة ({detail.certificatesCount})
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {detail.certificates.length === 0 ? (
            <p className="text-sm text-muted-foreground">لم يحصل على شهادات بعد.</p>
          ) : (
            detail.certificates.map((c) => (
              <div key={c.code} className="flex items-center justify-between text-sm p-2 rounded-lg border">
                <span className="text-brand-plum-500 font-medium">{c.sectionTitle ?? "—"}</span>
                <div className="text-left text-xs text-muted-foreground">
                  <p dir="ltr">{c.code}</p>
                  <p>{c.issuedAt ?? "—"}</p>
                </div>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  )
}
