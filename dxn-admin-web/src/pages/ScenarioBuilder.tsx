import { useCallback, useEffect, useRef, useState } from "react"
import { useSearchParams } from "react-router-dom"
import {
  Undo2,
  Redo2,
  Plus,
  Upload,
  Download,
  Copy,
  Save,
  Send,
  GripVertical,
  MoreVertical,
  ChevronDown,
  ChevronLeft,
  Settings,
  Heart,
  MessageCircle,
  Trash2,
  AlertCircle,
  Layers,
  Target,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import { api, ApiError } from "@/lib/api"
import type { ContentStatus, LevelDetail, LevelType, Round as ApiRound, Section, Skill } from "@/lib/types"
import { hasXss } from "@/lib/sanitize"

interface Option {
  id: string
  text: string
  type: "correct" | "wrong" | "neutral"
  trust: number
  feedback: string
  xp?: number
}

interface Round {
  id: string
  title: string
  message: string
  initialTrust: number
  options: Option[]
}

type ApiErrors = Record<string, string[]>

const levelTypeLabels: Record<Exclude<LevelType, "lesson">, string> = {
  quiz: "اختبار",
  chat_battle: "محاكي محادثة",
  match_pairs: "مطابقة",
  order_sequence: "ترتيب",
}

const fromApiRound = (r: ApiRound): Round => ({
  id: r.id,
  title: r.title ?? "",
  message: r.prompt,
  initialTrust: r.initialTrust ?? 50,
  options: r.options.map((o) => ({
    id: o.id,
    text: o.text,
    type: o.isCorrect ? "correct" : o.isNeutral ? "neutral" : "wrong",
    trust: o.trustImpact,
    feedback: o.feedback ?? "",
    xp: o.xpBonus ?? 0,
  })),
})

const toApiRound = (r: Round) => ({
  title: r.title,
  prompt: r.message,
  initialTrust: r.initialTrust,
  options: r.options.map((o) => ({
    text: o.text,
    isCorrect: o.type === "correct",
    isNeutral: o.type === "neutral",
    trustImpact: o.trust,
    feedback: o.feedback || null,
    xpBonus: o.xp ?? 0,
  })),
})

function errMessage(e: unknown) {
  if (e instanceof ApiError) {
    const first = Object.values(e.errors ?? {})[0]?.[0]
    return first && first !== e.message ? `${e.message}: ${first}` : e.message
  }
  return "تعذر الاتصال بالخادم"
}

/** Laravel keys (rounds.0.options.1.text) → keys of the local round/option ids. */
function mapApiErrors(errors: Record<string, string[]>, rounds: Round[]): ApiErrors {
  const mapped: ApiErrors = {}
  const add = (key: string, messages: string[]) => {
    mapped[key] = [...(mapped[key] ?? []), ...messages]
  }
  for (const [key, messages] of Object.entries(errors ?? {})) {
    const parts = key.split(".")
    if (parts[0] !== "rounds" || parts.length === 1) {
      add(key, messages)
      continue
    }
    const round = rounds[Number(parts[1])]
    if (!round) {
      add("rounds", messages)
      continue
    }
    if (parts[2] === "options" && parts.length >= 4) {
      const option = round.options[Number(parts[3])]
      add(option ? `option:${round.id}:${option.id}` : `round:${round.id}`, messages)
    } else {
      add(`round:${round.id}`, messages)
    }
  }
  return mapped
}

// shortcut: import accepts this builder's exported round shape; upgrade to accept raw API round JSON if external files are needed.
function readRoundsFile(value: unknown): Round[] | null {
  if (!Array.isArray(value)) return null
  const candidates = value as Array<Partial<Round> & { options?: Array<Partial<Option>> }>
  const valid = candidates.every(
    (r) =>
      r &&
      typeof r.message === "string" &&
      Array.isArray(r.options) &&
      (r.options ?? []).every((o) => o && typeof o.text === "string"),
  )
  if (!valid) return null
  return candidates.map((r, i) => ({
    id: r.id || `r${Date.now()}-${i}`,
    title: r.title ?? "",
    message: r.message ?? "",
    initialTrust: typeof r.initialTrust === "number" ? r.initialTrust : 50,
    options: (r.options ?? []).map((o, j) => ({
      id: o?.id || `o${Date.now()}-${i}-${j}`,
      text: o?.text ?? "",
      type: o?.type === "correct" || o?.type === "neutral" || o?.type === "wrong" ? o.type : "neutral",
      trust: typeof o?.trust === "number" ? o.trust : 0,
      feedback: typeof o?.feedback === "string" ? o.feedback : "",
      xp: typeof o?.xp === "number" ? o.xp : 0,
    })),
  }))
}

// shortcut: location comes from admin content/tree; upgrade with a dedicated lookup endpoint if tree pagination is added.
function findLocation(tree: Section[], levelId: string | null): { section?: Section; skill?: Skill } {
  for (const section of tree) {
    for (const skill of section.skills ?? []) {
      if (skill.levels?.some((l) => l.id === levelId)) return { section, skill }
    }
  }
  return {}
}

function ErrorList({ messages }: { messages?: string[] }) {
  if (!messages?.length) return null
  return (
    <div className="space-y-1 text-xs text-semantic-error" role="alert">
      {messages.map((m, i) => (
        <p key={i} className="flex items-start gap-1">
          <AlertCircle size={13} className="mt-0.5 shrink-0" />
          {m}
        </p>
      ))}
    </div>
  )
}

export default function ScenarioBuilder() {
  const [searchParams, setSearchParams] = useSearchParams()
  const levelId = searchParams.get("levelId")
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [tree, setTree] = useState<Section[]>([])
  const [treeErr, setTreeErr] = useState<string | null>(null)
  const [level, setLevel] = useState<LevelDetail | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const [saveMessage, setSaveMessage] = useState<string | null>(null)
  const [apiErrors, setApiErrors] = useState<ApiErrors>({})
  const [invalidFile, setInvalidFile] = useState(false)

  const [scenarioName, setScenarioName] = useState("")
  const [status, setStatus] = useState<ContentStatus>("draft")
  const [rounds, setRounds] = useState<Round[]>([])
  const [selectedRound, setSelectedRound] = useState<string | null>(null)
  const [expanded, setExpanded] = useState<Record<string, boolean>>({})
  const [previewRound, setPreviewRound] = useState<string | null>(null)
  const [previewTrust, setPreviewTrust] = useState(50)
  const [previewHearts, setPreviewHearts] = useState(3)
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [passThreshold, setPassThreshold] = useState(80)
  const [showValidation, setShowValidation] = useState(false)

  useEffect(() => {
    let active = true
    void (async () => {
      try {
        setTree(await api<Section[]>("/api/admin/content/tree"))
      } catch (e) {
        if (active) setTreeErr(errMessage(e))
      }
    })()
    return () => {
      active = false
    }
  }, [])

  const applyLevel = useCallback((d: LevelDetail) => {
    setLevel(d)
    setScenarioName(d.title)
    setStatus(d.status)
    setPassThreshold(d.passThreshold)
    const loaded = d.rounds.map(fromApiRound)
    setRounds(loaded)
    const firstId = loaded[0]?.id ?? null
    setSelectedRound(firstId)
    setExpanded(Object.fromEntries(loaded.map((r) => [r.id, true])))
    setPreviewRound(firstId)
    setPreviewTrust(loaded[0]?.initialTrust ?? 50)
    setPreviewHearts(3)
    setApiErrors({})
    setShowValidation(false)
    setInvalidFile(false)
    setLoadErr(null)
  }, [setLevel, setScenarioName, setStatus, setPassThreshold, setRounds, setSelectedRound, setExpanded, setPreviewRound, setPreviewTrust, setPreviewHearts, setApiErrors, setShowValidation, setInvalidFile, setLoadErr])

  useEffect(() => {
    if (!levelId) return
    let active = true
    void (async () => {
      try {
        const d = await api<LevelDetail>(`/api/admin/levels/${levelId}`)
        if (active) applyLevel(d)
      } catch (e) {
        if (active) setLoadErr(errMessage(e))
      }
    })()
    return () => {
      active = false
    }
  }, [levelId, reloadKey, applyLevel])

  const location = findLocation(tree, levelId)

  const validationErrors = (() => {
    const errors: string[] = []
    if (!levelId) errors.push("اختر مستوى أولاً")
    if (!scenarioName.trim()) errors.push("اسم السيناريو مطلوب")
    if (hasXss(scenarioName)) errors.push("اسم السيناريو يحتوي على محتوى غير آمن")
    if (passThreshold < 0 || passThreshold > 100) errors.push("حد الاجتياز يجب أن يكون بين 0 و 100")
    if (rounds.length === 0) errors.push("السيناريو يحتاج جولة واحدة على الأقل")
    rounds.forEach((round) => {
      if (!round.title.trim()) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج عنواناً`)
      if (hasXss(round.title) || hasXss(round.message)) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتوي على محتوى غير آمن`)
      if (!round.message.trim()) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج رسالة عميل`)
      if (round.initialTrust < 0 || round.initialTrust > 100) errors.push(`الثقة الابتدائية في "${round.title || "غير مسمّاة"}" يجب أن تكون بين 0 و 100`)
      if (round.options.length === 0) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج خياراً واحداً على الأقل`)
      const hasCorrect = round.options.some((o) => o.type === "correct")
      if (!hasCorrect) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج خياراً صحيحاً واحداً على الأقل`)
      round.options.forEach((option) => {
        if (!option.text.trim()) errors.push(`خيار في "${round.title || "غير مسمّاة"}" يحتاج نصاً`)
        if (hasXss(option.text) || hasXss(option.feedback)) errors.push(`خيار في "${round.title || "غير مسمّاة"}" يحتوي على محتوى غير آمن`)
        if (option.trust < -100 || option.trust > 100) errors.push(`تأثير الثقة في "${round.title || "غير مسمّاة"}" يجب أن يكون بين -100 و +100`)
      })
    })
    return errors
  })()

  const serverErrors = Object.entries(apiErrors).flatMap(([key, messages]) =>
    messages.map((message) => ({ key, message })),
  )
  const activePreviewRound = rounds.find((r) => r.id === previewRound) || rounds[0]

  const handleOptionPreview = (option: Option) => {
    setPreviewTrust((prev) => Math.min(100, Math.max(0, prev + option.trust)))
    if (option.type === "wrong") {
      setPreviewHearts((prev) => Math.max(0, prev - 1))
    }
  }

  const addRound = () => {
    const newRound: Round = {
      id: `r${Date.now()}`,
      title: "جولة جديدة",
      message: "",
      initialTrust: 50,
      options: [{ id: `o${Date.now()}`, text: "", type: "correct", trust: 0, feedback: "" }],
    }
    setRounds([...rounds, newRound])
    setExpanded((prev) => ({ ...prev, [newRound.id]: true }))
    setSelectedRound(newRound.id)
  }

  const updateRound = (id: string, updates: Partial<Round>) => {
    setRounds((prev) => prev.map((r) => (r.id === id ? { ...r, ...updates } : r)))
  }

  const updateOption = (roundId: string, optionId: string, updates: Partial<Option>) => {
    setRounds((prev) =>
      prev.map((r) =>
        r.id === roundId
          ? { ...r, options: r.options.map((o) => (o.id === optionId ? { ...o, ...updates } : o)) }
          : r
      )
    )
  }

  const addOption = (roundId: string) => {
    setRounds((prev) =>
      prev.map((r) =>
        r.id === roundId
          ? {
              ...r,
              options: [
                ...r.options,
                { id: `o${Date.now()}`, text: "", type: "neutral", trust: 0, feedback: "" },
              ],
            }
          : r
      )
    )
  }

  const deleteOption = (roundId: string, optionId: string) => {
    if (!window.confirm("هل أنت متأكد من حذف هذا الخيار؟")) return
    setRounds((prev) =>
      prev.map((r) => (r.id === roundId ? { ...r, options: r.options.filter((o) => o.id !== optionId) } : r))
    )
  }

  const save = async () => {
    setShowValidation(true)
    if (!levelId || validationErrors.length > 0) return false
    setBusy(true)
    setSaveMessage(null)
    setApiErrors({})
    try {
      await api(`/api/admin/levels/${levelId}`, {
        method: "PUT",
        json: { title: scenarioName, passThreshold: Number(passThreshold) },
      })
      const detail = await api<LevelDetail>(`/api/admin/levels/${levelId}/rounds`, {
        method: "PUT",
        json: { rounds: rounds.map(toApiRound) },
      })
      applyLevel(detail)
      setSaveMessage("تم الحفظ")
      return true
    } catch (e) {
      if (e instanceof ApiError && e.status === 422) setApiErrors(mapApiErrors(e.errors, rounds))
      setSaveMessage(errMessage(e))
      return false
    } finally {
      setBusy(false)
    }
  }

  const toggleStatus = async () => {
    setShowValidation(true)
    if (!levelId || validationErrors.length > 0) return
    const nextStatus: ContentStatus = status === "published" ? "draft" : "published"
    if (!window.confirm(nextStatus === "published" ? "نشر السيناريو؟" : "إلغاء النشر؟")) return
    setBusy(true)
    setSaveMessage(null)
    setApiErrors({})
    try {
      await api(`/api/admin/levels/${levelId}`, { method: "PUT", json: { status: nextStatus } })
      setStatus(nextStatus)
      setSaveMessage("تم الحفظ")
    } catch (e) {
      if (e instanceof ApiError && e.status === 422) setApiErrors(mapApiErrors(e.errors, rounds))
      setSaveMessage(errMessage(e))
    } finally {
      setBusy(false)
    }
  }

  const exportRounds = () => {
    if (!levelId) return
    const blob = new Blob([JSON.stringify(rounds, null, 2)], { type: "application/json" })
    const url = URL.createObjectURL(blob)
    const link = document.createElement("a")
    link.href = url
    link.download = `scenario-${levelId}.json`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 0)
  }

  const importRounds = async (file: File | undefined) => {
    if (!file) return
    setInvalidFile(false)
    setApiErrors({})
    try {
      const imported = readRoundsFile(JSON.parse(await file.text()) as unknown)
      if (!imported) {
        setInvalidFile(true)
        return
      }
      setRounds(imported)
      const firstId = imported[0]?.id ?? null
      setSelectedRound(firstId)
      setExpanded(Object.fromEntries(imported.map((r) => [r.id, true])))
      setPreviewRound(firstId)
      setPreviewTrust(imported[0]?.initialTrust ?? 50)
      setPreviewHearts(3)
      setShowValidation(false)
    } catch {
      setInvalidFile(true)
    }
  }

  if (!levelId) {
    return (
      <div className="p-8 space-y-6 overflow-y-auto">
        <div>
          <h1 className="text-2xl font-bold text-brand-plum-500">منشئ السيناريوهات</h1>
          <p className="text-muted-foreground">اختر مستوى (غير درس) لبدء تحرير السيناريو</p>
        </div>
        {treeErr && <p className="text-sm text-semantic-error">{treeErr}</p>}
        {!treeErr && tree.length === 0 && <p className="text-sm text-muted-foreground">جارٍ تحميل المستويات...</p>}
        {tree.map((section) => (
          <Card key={section.id} className="border-0 shadow-sm">
            <CardHeader className="pb-2">
              <CardTitle className="text-lg">{section.title}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {(section.skills ?? []).map((skill) => {
                const levels = (skill.levels ?? []).filter((l) => l.type !== "lesson")
                if (levels.length === 0) return null
                return (
                  <div key={skill.id} className="space-y-2">
                    <h3 className="text-sm font-semibold text-muted-foreground">
                      {section.title} › {skill.title} › المستوى
                    </h3>
                    {levels.map((l) => (
                      <button
                        key={l.id}
                        onClick={() => {
                          setLoadErr(null)
                          setSaveMessage(null)
                          setInvalidFile(false)
                          setSearchParams({ levelId: l.id })
                        }}
                        className="w-full flex items-center justify-between rounded-lg border px-4 py-3 text-sm hover:border-brand-plum-500 hover:bg-brand-plum-500/5 transition-colors"
                      >
                        <span className="font-medium">{l.title}</span>
                        <Badge variant="secondary">
                          {l.type === "lesson" ? "درس" : levelTypeLabels[l.type]}
                        </Badge>
                      </button>
                    ))}
                  </div>
                )
              })}
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  if (loadErr) {
    return (
      <div className="p-8 space-y-4 text-center">
        <p className="text-semantic-error">{loadErr}</p>
        <Button variant="outline" onClick={() => { setLoadErr(null); setReloadKey((n) => n + 1) }}>
          إعادة المحاولة
        </Button>
      </div>
    )
  }

  if (!level || level.id !== levelId) {
    return <div className="p-8 text-center text-muted-foreground">جارٍ التحميل...</div>
  }

  return (
    <div className="h-screen flex flex-col">
      {/* Toolbar */}
      <div className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between gap-3">
        <div className="flex items-center gap-4">
          <Input
            value={scenarioName}
            onChange={(e) => setScenarioName(e.target.value)}
            className="w-72 font-semibold text-lg h-10 border-0 bg-transparent px-0 focus-visible:ring-0"
          />
          <Badge variant={status === "published" ? "default" : "warning"}>
            {status === "published" ? "منشور" : "مسودة"}
          </Badge>
          {saveMessage && (
            <span className={cn("text-xs", saveMessage === "تم الحفظ" ? "text-game-leaf-text" : "text-semantic-error")}>
              {saveMessage}
            </span>
          )}
          {invalidFile && <span className="text-xs text-semantic-error">ملف غير صالح</span>}
        </div>

        <div className="flex items-center gap-2">
          {/* shortcut: undo/redo/copy buttons remain placeholders as before this task; upgrade with an editor history. */}
          <Button variant="ghost" size="icon">
            <Undo2 size={18} />
          </Button>
          <Button variant="ghost" size="icon">
            <Redo2 size={18} />
          </Button>
          <div className="w-px h-6 bg-gray-200 mx-1" />
          <Button variant="outline" size="sm" className="gap-2" onClick={addRound}>
            <Plus size={16} />
            جولة
          </Button>
          <input
            ref={fileInputRef}
            type="file"
            accept="application/json,.json"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0]
              e.target.value = ""
              void importRounds(file)
            }}
          />
          <Button variant="outline" size="sm" className="gap-2" onClick={() => fileInputRef.current?.click()}>
            <Upload size={16} />
            استيراد
          </Button>
          <Button variant="outline" size="sm" className="gap-2" onClick={exportRounds}>
            <Download size={16} />
            تصدير
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Copy size={16} />
            نسخ
          </Button>
          <Button variant="outline" size="sm" className="gap-2" disabled={busy} onClick={() => void save()}>
            <Save size={16} />
            حفظ مسودة
          </Button>
          {showValidation && validationErrors.length + serverErrors.length > 0 && (
            <Badge variant="destructive" className="gap-1">
              <AlertCircle size={14} />
              {validationErrors.length + serverErrors.length} خطأ
            </Badge>
          )}
          <Button
            size="sm"
            className="gap-2"
            disabled={busy}
            onClick={() => void toggleStatus()}
          >
            <Send size={16} />
            {status === "published" ? "إلغاء النشر" : "نشر"}
          </Button>
        </div>
      </div>

      <div className="flex-1 flex min-h-0">
        {/* Canvas */}
        <div className="flex-1 overflow-y-auto p-6 bg-brand-ivory">
          <div className="max-w-3xl mx-auto space-y-4">
            <ErrorList messages={apiErrors["rounds"]} />
            {rounds.map((round, idx) => {
              const isOpen = expanded[round.id]
              const isSelected = selectedRound === round.id
              return (
                <Card
                  key={round.id}
                  className={cn(
                    "border-0 shadow-sm transition-all",
                    isSelected && "ring-2 ring-brand-plum-500"
                  )}
                  onClick={() => setSelectedRound(round.id)}
                >
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-brand-plum-500 text-white flex items-center justify-center text-sm font-bold">
                        {idx + 1}
                      </div>
                      <Input
                        value={round.title}
                        onChange={(e) => updateRound(round.id, { title: e.target.value })}
                        className="flex-1 font-semibold border-0 bg-transparent px-0 focus-visible:ring-0"
                        placeholder="عنوان الجولة"
                      />
                      <button
                        onClick={(e) => {
                          e.stopPropagation()
                          setExpanded((prev) => ({ ...prev, [round.id]: !isOpen }))
                        }}
                      >
                        {isOpen ? <ChevronDown size={18} /> : <ChevronLeft size={18} />}
                      </button>
                      <GripVertical size={18} className="text-muted-foreground cursor-grab" />
                      <Button variant="ghost" size="icon" className="h-8 w-8">
                        <MoreVertical size={16} />
                      </Button>
                    </div>
                    <ErrorList messages={apiErrors[`round:${round.id}`]} />
                  </CardHeader>

                  {isOpen && (
                    <CardContent className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium">رسالة العميل</label>
                        <textarea
                          value={round.message}
                          onChange={(e) => updateRound(round.id, { message: e.target.value })}
                          className="w-full min-h-[80px] rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                          placeholder="اكتب رسالة العميل هنا..."
                        />
                      </div>

                      <div className="space-y-1">
                        <label className="text-xs text-muted-foreground">الثقة الابتدائية</label>
                        <Input
                          type="number"
                          min={0}
                          max={100}
                          value={round.initialTrust}
                          onChange={(e) => updateRound(round.id, { initialTrust: Number(e.target.value) })}
                          className="h-8 w-32"
                        />
                      </div>

                      <div className="space-y-3">
                        <div className="flex items-center justify-between">
                          <label className="text-sm font-medium">خيارات الرد</label>
                        </div>

                        {round.options.map((option) => (
                          <div
                            key={option.id}
                            className="border rounded-lg p-4 space-y-3 bg-gray-50/50"
                          >
                            <div className="flex items-center gap-3">
                              <Input
                                value={option.text}
                                onChange={(e) => updateOption(round.id, option.id, { text: e.target.value })}
                                placeholder="نص الخيار"
                                className="flex-1"
                              />
                              <select
                                value={option.type}
                                onChange={(e) =>
                                  updateOption(round.id, option.id, { type: e.target.value as Option["type"] })
                                }
                                className="h-10 rounded-md border border-input bg-background px-2 text-sm"
                              >
                                <option value="correct">صحيح</option>
                                <option value="wrong">خاطئ</option>
                                <option value="neutral">محايد</option>
                              </select>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-9 w-9 text-semantic-error"
                                onClick={() => deleteOption(round.id, option.id)}
                              >
                                <Trash2 size={16} />
                              </Button>
                            </div>

                            <div className="grid grid-cols-3 gap-3">
                              <div className="space-y-1">
                                <label className="text-xs text-muted-foreground">تأثير الثقة</label>
                                <Input
                                  type="number"
                                  value={option.trust}
                                  onChange={(e) =>
                                    updateOption(round.id, option.id, { trust: Number(e.target.value) })
                                  }
                                  className="h-8"
                                />
                              </div>
                              <div className="space-y-1 col-span-2">
                                <label className="text-xs text-muted-foreground">رسالة التغذية الراجعة</label>
                                <Input
                                  value={option.feedback}
                                  onChange={(e) =>
                                    updateOption(round.id, option.id, { feedback: e.target.value })
                                  }
                                  className="h-8"
                                  placeholder="تعليق يظهر للمتدرب"
                                />
                              </div>
                            </div>
                            <ErrorList messages={apiErrors[`option:${round.id}:${option.id}`]} />
                          </div>
                        ))}

                        <Button
                          variant="outline"
                          size="sm"
                          className="gap-2"
                          onClick={() => addOption(round.id)}
                        >
                          <Plus size={16} />
                          إضافة خيار
                        </Button>
                      </div>
                    </CardContent>
                  )}
                </Card>
              )
            })}

            <Button variant="outline" className="w-full gap-2 py-6" onClick={addRound}>
              <Plus size={18} />
              إضافة جولة جديدة
            </Button>
          </div>
        </div>

        {/* Preview Panel */}
        <div className="w-[380px] border-r border-gray-200 bg-white flex flex-col">
          <CardHeader className="border-b border-gray-100">
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg">المعاينة الحية</CardTitle>
              <Button variant="ghost" size="icon" onClick={() => setSettingsOpen(true)}>
                <Settings size={18} />
              </Button>
            </div>
            <CardDescription>جرب التدفق كما يراه المتدرب</CardDescription>
          </CardHeader>

          <div className="flex-1 p-4 space-y-4 overflow-y-auto">
            {/* Trust Bar */}
            <div className="bg-gray-50 rounded-xl p-4 space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium">شريط الثقة</span>
                <span className={cn(
                  "font-bold",
                  previewTrust >= 70 ? "text-game-leaf-text" : previewTrust >= 40 ? "text-semantic-warning" : "text-semantic-error"
                )}>
                  {previewTrust}%
                </span>
              </div>
              <div className="h-3 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className={cn(
                    "h-full transition-all duration-500",
                    previewTrust >= 70 ? "bg-game-leaf" : previewTrust >= 40 ? "bg-semantic-warning" : "bg-semantic-error"
                  )}
                  style={{ width: `${previewTrust}%` }}
                />
              </div>
              <div className="flex items-center gap-1 text-semantic-error">
                {Array.from({ length: 3 }).map((_, i) => (
                  <Heart
                    key={i}
                    size={18}
                    fill={i < previewHearts ? "currentColor" : "none"}
                    className={i < previewHearts ? "" : "text-gray-300"}
                  />
                ))}
              </div>
            </div>

            {/* Chat */}
            {activePreviewRound ? (
              <div className="space-y-4">
                <div className="flex items-center gap-3 pb-3 border-b border-gray-100">
                  <div className="w-10 h-10 rounded-full bg-brand-plum-500 text-white flex items-center justify-center font-bold">أ</div>
                  <div>
                    <p className="font-semibold">أحمد</p>
                    <p className="text-xs text-muted-foreground">صديق مهتم بالصحة</p>
                  </div>
                </div>

                <div className="flex justify-start">
                  <div className="max-w-[85%] bg-gray-100 rounded-2xl rounded-tr-sm px-4 py-3 text-sm">
                    <div className="flex items-center gap-2 text-muted-foreground mb-1">
                      <MessageCircle size={14} />
                      <span className="text-xs">{activePreviewRound.title}</span>
                    </div>
                    {activePreviewRound.message}
                  </div>
                </div>

                <div className="space-y-2 pt-2">
                  {activePreviewRound.options.map((option) => (
                    <button
                      key={option.id}
                      onClick={() => handleOptionPreview(option)}
                      className="w-full text-right bg-white border-2 border-gray-100 hover:border-brand-plum-500 rounded-xl px-4 py-3 text-sm transition-colors"
                    >
                      {option.text}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">لا توجد جولات للمعاينة</p>
            )}

            <div className="pt-4 border-t border-gray-100">
              <p className="text-sm font-medium mb-2">اختيار الجولة للمعاينة</p>
              <div className="space-y-1">
                {rounds.map((round, idx) => (
                  <button
                    key={round.id}
                    onClick={() => {
                      setPreviewRound(round.id)
                      setPreviewTrust(round.initialTrust)
                      setPreviewHearts(3)
                    }}
                    className={cn(
                      "w-full text-right px-3 py-2 rounded-lg text-sm transition-colors",
                      previewRound === round.id ? "bg-brand-plum-500/10 text-brand-plum-500" : "hover:bg-gray-50"
                    )}
                  >
                    {idx + 1}. {round.title}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Settings Drawer */}
      {settingsOpen && (
        <div className="fixed inset-0 z-50 flex">
          <div
            className="flex-1 bg-black/30"
            onClick={() => setSettingsOpen(false)}
          />
          <div className="w-[420px] bg-white shadow-xl p-6 overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold flex items-center gap-2">
                <Settings size={20} />
                إعدادات السيناريو
              </h2>
              <Button variant="ghost" size="icon" onClick={() => setSettingsOpen(false)}>
                ×
              </Button>
            </div>

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">اسم السيناريو</label>
                <Input
                  value={scenarioName}
                  onChange={(e) => setScenarioName(e.target.value)}
                />
                <ErrorList messages={apiErrors["title"]} />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">القسم المرتبط</label>
                <div className="w-full min-h-10 rounded-md border border-input bg-gray-50 px-3 py-2 text-sm">
                  {location.section?.title ?? "—"}
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">المهارة المرتبطة</label>
                <div className="w-full min-h-10 rounded-md border border-input bg-gray-50 px-3 py-2 text-sm">
                  {location.skill?.title ?? "—"}
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">حد الاجتياز (%)</label>
                <Input
                  type="number"
                  min={0}
                  max={100}
                  value={passThreshold}
                  onChange={(e) => setPassThreshold(Number(e.target.value))}
                />
                <ErrorList messages={apiErrors["passThreshold"]} />
              </div>

              <Card className="border-0 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-base flex items-center gap-2">
                    <Layers size={16} />
                    المهارة المرتبطة
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">القسم</span>
                    <span className="font-medium">{location.section?.title ?? "—"}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">المهارة</span>
                    <span className="font-medium">{location.skill?.title ?? "—"}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">عدد الجولات</span>
                    <span className="font-medium">{rounds.length}</span>
                  </div>
                </CardContent>
              </Card>

              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm font-medium">
                  <Target size={16} />
                  قواعد التحقق
                </div>
                {showValidation && (validationErrors.length > 0 || serverErrors.length > 0) ? (
                  <div className="space-y-2">
                    {validationErrors.map((err, idx) => (
                      <div key={idx} className="flex items-start gap-2 text-sm text-semantic-error">
                        <AlertCircle size={16} className="shrink-0 mt-0.5" />
                        {err}
                      </div>
                    ))}
                    {serverErrors.map(({ key, message }, idx) => (
                      <div key={`server-${idx}`} className="flex items-start gap-2 text-sm text-semantic-error">
                        <AlertCircle size={16} className="shrink-0 mt-0.5" />
                        الخادم ({key}): {message}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-game-leaf-text flex items-center gap-2">
                    <AlertCircle size={16} />
                    السيناريو جاهز للنشر
                  </p>
                )}
              </div>

              <div className="pt-4 flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setSettingsOpen(false)}>
                  إلغاء
                </Button>
                <Button
                  className="flex-1"
                  disabled={busy}
                  onClick={() => {
                    void save().then((ok) => {
                      if (ok) setSettingsOpen(false)
                    })
                  }}
                >
                  تحقق واحفظ
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
