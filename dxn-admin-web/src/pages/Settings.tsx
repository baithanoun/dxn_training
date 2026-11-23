import { useCallback, useEffect, useState, type FormEvent } from "react"
import {
  Globe,
  Shield,
  Bell,
  Trophy,
  Save,
  Plus,
  X,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { api, ApiError } from "@/lib/api"
import type { AdminUser, Paginated } from "@/lib/types"

interface AppSettings {
  app_name: string
  default_language: "ar"
  maintenance_mode: boolean
  xp_lesson_default: number
  hearts_default: number
  heart_refill_hours: number
  streak_reminder_days: number
  xp_first_try_multiplier: number
  xp_replay_ratio: number
  notify_streak: boolean
  notify_reengage: boolean
  notify_leagues: boolean
  notify_leaders: boolean
  session_minutes: number
  quiz_rate_limit_per_minute: number
}

const DEFAULTS: AppSettings = {
  app_name: "DXN Training",
  default_language: "ar",
  maintenance_mode: false,
  xp_lesson_default: 20,
  hearts_default: 3,
  heart_refill_hours: 4,
  streak_reminder_days: 2,
  xp_first_try_multiplier: 1.5,
  xp_replay_ratio: 0.25,
  notify_streak: true,
  notify_reengage: true,
  notify_leagues: true,
  notify_leaders: true,
  session_minutes: 60,
  quiz_rate_limit_per_minute: 10,
}

const tabs = [
  { id: "general", label: "إعدادات عامة", icon: Globe },
  { id: "gamification", label: "قواعد التلعيب", icon: Trophy },
  { id: "notifications", label: "الإشعارات", icon: Bell },
  { id: "security", label: "الأمان", icon: Shield },
] as const

type TabId = (typeof tabs)[number]["id"]

const fieldCls =
  "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"

function errMessage(e: unknown) {
  if (e instanceof ApiError) {
    const first = Object.values(e.errors ?? {})[0]?.[0]
    return first && first !== e.message ? `${e.message}: ${first}` : e.message
  }
  return "تعذر الاتصال بالخادم"
}

function toNumber(value: string, fallback: number) {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<TabId>("general")
  const [settings, setSettings] = useState<AppSettings>(DEFAULTS)
  const [loading, setLoading] = useState(true)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveMsg, setSaveMsg] = useState<string | null>(null)
  const [saveErr, setSaveErr] = useState<string | null>(null)

  const [admins, setAdmins] = useState<AdminUser[]>([])
  const [adminsLoading, setAdminsLoading] = useState(true)
  const [adminsErr, setAdminsErr] = useState<string | null>(null)
  const [showAddAdmin, setShowAddAdmin] = useState(false)

  const loadSettings = useCallback(async () => {
    setLoading(true)
    try {
      const data = await api<AppSettings>("/api/admin/settings")
      setSettings({ ...DEFAULTS, ...data })
      setLoadErr(null)
    } catch (e) {
      setLoadErr(errMessage(e))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadAdmins = useCallback(async () => {
    setAdminsLoading(true)
    try {
      const res = await api<Paginated<AdminUser>>("/api/admin/users?role=admin&perPage=100")
      setAdmins(res.data)
      setAdminsErr(null)
    } catch (e) {
      setAdminsErr(errMessage(e))
    } finally {
      setAdminsLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadSettings()
  }, [loadSettings])

  useEffect(() => {
    void loadAdmins()
  }, [loadAdmins])

  const setField = <K extends keyof AppSettings>(key: K, value: AppSettings[K]) => {
    setSettings((prev) => ({ ...prev, [key]: value }))
    setSaveMsg(null)
    setSaveErr(null)
  }

  const saveSettings = async () => {
    setSaving(true)
    setSaveMsg(null)
    setSaveErr(null)
    try {
      const res = await api<{ message?: string }>("/api/admin/settings", {
        method: "PUT",
        json: settings,
      })
      setSaveMsg(res.message ?? "تم حفظ الإعدادات")
    } catch (e) {
      setSaveErr(errMessage(e))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="p-8 space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-brand-plum-500">الإعدادات</h1>
        <p className="text-muted-foreground">إعدادات التطبيق العامة والأمان والتلعيب</p>
      </header>

      {loadErr && (
        <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-4 py-2 text-sm text-semantic-error">
          <span>{loadErr}</span>
          <Button variant="outline" size="sm" onClick={() => void loadSettings()}>
            إعادة المحاولة
          </Button>
        </div>
      )}

      <div className="flex gap-4 border-b border-gray-200">
        {tabs.map((tab) => {
          const Icon = tab.icon
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab.id
                  ? "border-brand-plum-500 text-brand-plum-500"
                  : "border-transparent text-gray-500 hover:text-gray-700"
              }`}
            >
              <Icon size={18} />
              {tab.label}
            </button>
          )
        })}
      </div>

      {loading ? (
        <p className="text-center text-muted-foreground py-12">جارٍ تحميل الإعدادات...</p>
      ) : (
        <>
          {activeTab === "general" && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card className="border-0 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">معلومات التطبيق</CardTitle>
                  <CardDescription>اسم التطبيق واللغة الافتراضية وحالة الصيانة</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">اسم التطبيق</label>
                    <Input
                      value={settings.app_name}
                      onChange={(e) => setField("app_name", e.target.value)}
                      maxLength={100}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">اللغة الافتراضية</label>
                    <select
                      className={fieldCls}
                      value={settings.default_language}
                      onChange={(e) => setField("default_language", e.target.value as "ar")}
                    >
                      <option value="ar">العربية</option>
                    </select>
                  </div>
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <p className="text-sm font-medium">وضع الصيانة</p>
                      <p className="text-xs text-muted-foreground">إيقاف تطبيق الموبايل مؤقتاً مع إبقاء لوحة الإدارة تعمل</p>
                    </div>
                    <input
                      type="checkbox"
                      checked={settings.maintenance_mode}
                      onChange={(e) => setField("maintenance_mode", e.target.checked)}
                      className="w-5 h-5"
                    />
                  </div>
                </CardContent>
              </Card>

              <Card className="border-0 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg">إدارة المدراء</CardTitle>
                  <CardDescription>المدراء الحاليون وصلاحياتهم</CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  {adminsErr && (
                    <div className="flex items-center justify-between rounded-lg border border-semantic-error-border bg-semantic-error-bg px-3 py-2 text-sm text-semantic-error">
                      <span>{adminsErr}</span>
                      <Button variant="outline" size="sm" onClick={() => void loadAdmins()}>
                        إعادة المحاولة
                      </Button>
                    </div>
                  )}
                  {adminsLoading ? (
                    <p className="text-sm text-muted-foreground">جارٍ تحميل المدراء...</p>
                  ) : admins.length === 0 ? (
                    <p className="text-sm text-muted-foreground">لا يوجد مدراء.</p>
                  ) : (
                    admins.map((admin) => (
                      <div key={admin.id} className="flex items-center justify-between p-3 border rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-full bg-brand-plum-500 text-white flex items-center justify-center font-bold">
                            {admin.fullName[0]}
                          </div>
                          <div>
                            <p className="text-sm font-medium">{admin.fullName}</p>
                            <p className="text-xs text-muted-foreground">{admin.email ?? admin.phone ?? "—"}</p>
                          </div>
                        </div>
                        <Badge variant="secondary">مدير</Badge>
                      </div>
                    ))
                  )}

                  {showAddAdmin ? (
                    <AddAdminForm
                      onCancel={() => setShowAddAdmin(false)}
                      onCreated={() => {
                        setShowAddAdmin(false)
                        void loadAdmins()
                      }}
                    />
                  ) : (
                    <Button variant="outline" className="w-full gap-2" onClick={() => setShowAddAdmin(true)}>
                      <Plus size={16} />
                      إضافة مدير
                    </Button>
                  )}
                </CardContent>
              </Card>
            </div>
          )}

          {activeTab === "gamification" && (
            <Card className="border-0 shadow-sm max-w-2xl">
              <CardHeader>
                <CardTitle className="text-lg">قواعد التلعيب</CardTitle>
                <CardDescription>XP، القلوب، Streak، Leagues</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">XP افتراضي للدرس</label>
                    <Input
                      type="number"
                      min={0}
                      max={1000}
                      value={settings.xp_lesson_default}
                      onChange={(e) => setField("xp_lesson_default", toNumber(e.target.value, DEFAULTS.xp_lesson_default))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">
                      عدد القلوب الافتراضي <span className="text-xs text-muted-foreground">(قريباً)</span>
                    </label>
                    <Input
                      type="number"
                      min={1}
                      max={10}
                      value={settings.hearts_default}
                      onChange={(e) => setField("hearts_default", toNumber(e.target.value, DEFAULTS.hearts_default))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">
                      مدة تجديد القلب (ساعات) <span className="text-xs text-muted-foreground">(قريباً)</span>
                    </label>
                    <Input
                      type="number"
                      min={1}
                      max={48}
                      value={settings.heart_refill_hours}
                      onChange={(e) => setField("heart_refill_hours", toNumber(e.target.value, DEFAULTS.heart_refill_hours))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">حد الـ Streak للتذكير (أيام)</label>
                    <Input
                      type="number"
                      min={1}
                      max={30}
                      value={settings.streak_reminder_days}
                      onChange={(e) => setField("streak_reminder_days", toNumber(e.target.value, DEFAULTS.streak_reminder_days))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">مضاعف XP للمحاولة الأولى</label>
                    <Input
                      type="number"
                      min={1}
                      max={3}
                      step="0.1"
                      value={settings.xp_first_try_multiplier}
                      onChange={(e) => setField("xp_first_try_multiplier", toNumber(e.target.value, DEFAULTS.xp_first_try_multiplier))}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">نسبة XP عند إعادة المحاولة</label>
                    <Input
                      type="number"
                      min={0}
                      max={1}
                      step="0.05"
                      value={settings.xp_replay_ratio}
                      onChange={(e) => setField("xp_replay_ratio", toNumber(e.target.value, DEFAULTS.xp_replay_ratio))}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    إعدادات Leagues <span className="text-xs text-muted-foreground">(قريباً)</span>
                  </label>
                  <div className="p-3 border rounded-lg text-sm text-muted-foreground">
                    إعادة تعيين المجموعات أسبوعياً يوم الأحد 00:00 UTC · حجم المجموعة 30 متدرب
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {activeTab === "notifications" && (
            <Card className="border-0 shadow-sm max-w-2xl">
              <CardHeader>
                <CardTitle className="text-lg">الإشعارات</CardTitle>
                <CardDescription>تفعيل/تعطيل أنواع الإشعارات (قريباً)</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                {[
                  { key: "notify_streak" as const, label: "تذكير Streak", desc: "قبل انتهاء اليوم بـ 2-4 ساعات" },
                  { key: "notify_reengage" as const, label: "إعادة المشاركة", desc: "لم يفتح التطبيق 24/48/72 ساعة" },
                  { key: "notify_leagues" as const, label: "نتائج Leagues", desc: "نهاية كل أسبوع" },
                  { key: "notify_leaders" as const, label: "تذكير القادة", desc: "أعضاء متوقفون > 3 أيام" },
                ].map((item) => (
                  <div key={item.key} className="flex items-center justify-between p-3 border rounded-lg">
                    <div>
                      <p className="text-sm font-medium">
                        {item.label} <span className="text-xs text-muted-foreground">(قريباً)</span>
                      </p>
                      <p className="text-xs text-muted-foreground">{item.desc}</p>
                    </div>
                    <input
                      type="checkbox"
                      checked={settings[item.key]}
                      onChange={(e) => setField(item.key, e.target.checked)}
                      className="w-5 h-5"
                    />
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          {activeTab === "security" && (
            <Card className="border-0 shadow-sm max-w-2xl">
              <CardHeader>
                <CardTitle className="text-lg">الأمان</CardTitle>
                <CardDescription>مدة الجلسة وحدود الطلبات</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">مدة الجلسة (دقيقة)</label>
                  <Input
                    type="number"
                    min={15}
                    max={1440}
                    value={settings.session_minutes}
                    onChange={(e) => setField("session_minutes", toNumber(e.target.value, DEFAULTS.session_minutes))}
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-sm font-medium">Rate Limit — محاولات الاختبار في الدقيقة</label>
                  <Input
                    type="number"
                    min={1}
                    max={120}
                    value={settings.quiz_rate_limit_per_minute}
                    onChange={(e) =>
                      setField("quiz_rate_limit_per_minute", toNumber(e.target.value, DEFAULTS.quiz_rate_limit_per_minute))
                    }
                  />
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}

      <div className="flex items-center justify-end gap-3">
        {saveMsg && <p className="text-sm text-game-leaf-text">{saveMsg}</p>}
        {saveErr && <p className="text-sm text-semantic-error">{saveErr}</p>}
        <Button className="gap-2" onClick={() => void saveSettings()} disabled={saving || loading}>
          <Save size={16} />
          {saving ? "جارٍ الحفظ..." : "حفظ الإعدادات"}
        </Button>
      </div>
    </div>
  )
}

function AddAdminForm({
  onCancel,
  onCreated,
}: {
  onCancel: () => void
  onCreated: () => void
}) {
  const [f, setF] = useState({
    fullName: "",
    email: "",
    phone: "",
    password: "",
    dxnId: "",
  })
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement>) =>
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
          role: "admin",
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
    <form className="space-y-3 border rounded-lg p-3" onSubmit={(e) => void submit(e)}>
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium">إضافة مدير جديد</p>
        <button type="button" onClick={onCancel} className="text-muted-foreground hover:text-foreground" aria-label="إغلاق">
          <X size={16} />
        </button>
      </div>
      {err && <p className="text-sm text-semantic-error">{err}</p>}
      <div className="space-y-2">
        <Input placeholder="الاسم الكامل" required value={f.fullName} onChange={set("fullName")} />
        <Input placeholder="البريد الإلكتروني" type="email" value={f.email} onChange={set("email")} />
        <Input placeholder="الهاتف" value={f.phone} onChange={set("phone")} />
        <Input placeholder="رقم العضوية DXN (اختياري)" value={f.dxnId} onChange={set("dxnId")} />
        <Input placeholder="كلمة المرور" type="password" required minLength={8} value={f.password} onChange={set("password")} />
      </div>
      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={busy}>
          إلغاء
        </Button>
        <Button type="submit" disabled={busy}>
          {busy ? "جارٍ الإضافة..." : "إضافة مدير"}
        </Button>
      </div>
    </form>
  )
}
