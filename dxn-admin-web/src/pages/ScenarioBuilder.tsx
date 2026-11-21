import { useState } from "react"
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
import { sections } from "@/data/mockData"
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
  options: Option[]
}

const initialRounds: Round[] = [
  {
    id: "r1",
    title: "اعتراض السعر",
    message: "أحمد: المنتج غالي شوي… ما أظن أقدر أشتريه باستمرار.",
    options: [
      { id: "o1", text: "أتفهم تماماً. كنت أشعر بنفس الشعور. لكن لما حسبت الفائدة اليومية وجدت أنها توفر عليّ تكاليف صحية أخرى.", type: "correct", trust: 25, feedback: "رد احترافي يبني الثقة", xp: 10 },
      { id: "o2", text: "غالي؟! هذا أرخص منتج في السوق.", type: "wrong", trust: -20, feedback: "رد عدائي يخسر ثقة العميل", xp: 0 },
      { id: "o3", text: "ممكن نتحدث عن خطتك المالية لاحقاً.", type: "neutral", trust: 0, feedback: "رد محايد لا يؤثر", xp: 0 },
    ],
  },
  {
    id: "r2",
    title: "الشك في الفائدة",
    message: "أحمد: كيف أعرف أنه فعلاً ينفع؟",
    options: [
      { id: "o4", text: "شارك تجربة شخصية أو شهادة موثقة مع البيانات.", type: "correct", trust: 25, feedback: "دليل اجتماعي قوي", xp: 10 },
      { id: "o5", text: "الشركة تقول كذا وكفى.", type: "wrong", trust: -15, feedback: "رد ضعيف", xp: 0 },
    ],
  },
]

export default function ScenarioBuilder() {
  const [scenarioName, setScenarioName] = useState("اعتراض السعر والمنتجات")
  const [status, setStatus] = useState("draft")
  const [rounds, setRounds] = useState<Round[]>(initialRounds)
  const [selectedRound, setSelectedRound] = useState<string | null>("r1")
  const [expanded, setExpanded] = useState<Record<string, boolean>>({ r1: true, r2: true })
  const [previewRound, setPreviewRound] = useState<string | null>("r1")
  const [previewTrust, setPreviewTrust] = useState(50)
  const [previewHearts, setPreviewHearts] = useState(3)
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [linkedBag, setLinkedBag] = useState<number | "">(5)
  const [linkedSectionId, setLinkedSectionId] = useState<number | "">(5)
  const [linkedSkillId, setLinkedSkillId] = useState<number | "">(53)
  const [passThreshold, setPassThreshold] = useState(80)
  const [showValidation, setShowValidation] = useState(false)

  const activeLinkedSection = sections.find((s) => s.id === linkedSectionId)
  const activeLinkedSkill = activeLinkedSection?.skills.find((s) => s.id === linkedSkillId)

  const validationErrors = (() => {
    const errors: string[] = []
    if (!scenarioName.trim()) errors.push("اسم السيناريو مطلوب")
    if (hasXss(scenarioName)) errors.push("اسم السيناريو يحتوي على محتوى غير آمن")
    if (!linkedSectionId) errors.push("يجب ربط السيناريو بقسم")
    if (!linkedSkillId) errors.push("يجب ربط السيناريو بمهارة")
    if (passThreshold < 0 || passThreshold > 100) errors.push("حد الاجتياز يجب أن يكون بين 0 و 100")
    if (rounds.length === 0) errors.push("السيناريو يحتاج جولة واحدة على الأقل")
    rounds.forEach((round) => {
      if (!round.title.trim()) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج عنواناً`)
      if (hasXss(round.title) || hasXss(round.message)) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتوي على محتوى غير آمن`)
      if (!round.message.trim()) errors.push(`الجولة "${round.title || "غير مسمّاة"}" تحتاج رسالة عميل`)
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

  return (
    <div className="h-screen flex flex-col">
      {/* Toolbar */}
      <div className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Input
            value={scenarioName}
            onChange={(e) => setScenarioName(e.target.value)}
            className="w-72 font-semibold text-lg h-10 border-0 bg-transparent px-0 focus-visible:ring-0"
          />
          <Badge variant={status === "published" ? "default" : "warning"}>
            {status === "published" ? "منشور" : "مسودة"}
          </Badge>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="ghost" size="icon">
            <Undo2 size={18} />
          </Button>
          <Button variant="ghost" size="icon">
            <Redo2 size={18} />
          </Button>
          <div className="w-px h-6 bg-gray-200 mx-1" />
          <Button variant="outline" size="sm" className="gap-2">
            <Plus size={16} />
            جولة
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Upload size={16} />
            استيراد
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Download size={16} />
            تصدير
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Copy size={16} />
            نسخ
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Save size={16} />
            حفظ مسودة
          </Button>
          {showValidation && validationErrors.length > 0 && (
            <Badge variant="destructive" className="gap-1">
              <AlertCircle size={14} />
              {validationErrors.length} خطأ
            </Badge>
          )}
          <Button
            size="sm"
            className="gap-2"
            onClick={() => {
              setShowValidation(true)
              if (validationErrors.length === 0) {
                const nextStatus = status === "published" ? "draft" : "published"
                if (window.confirm(nextStatus === "published" ? "نشر السيناريو؟" : "إلغاء النشر؟")) {
                  setStatus(nextStatus)
                }
              }
            }}
          >
            <Send size={16} />
            {status === "published" ? "إلغاء النشر" : "نشر"}
          </Button>
        </div>
      </div>

      <div className="flex-1 flex min-h-0">
        {/* Canvas */}
        <div className="flex-1 overflow-y-auto p-6 bg-dxn-bg">
          <div className="max-w-3xl mx-auto space-y-4">
            {rounds.map((round, idx) => {
              const isOpen = expanded[round.id]
              const isSelected = selectedRound === round.id
              return (
                <Card
                  key={round.id}
                  className={cn(
                    "border-0 shadow-sm transition-all",
                    isSelected && "ring-2 ring-dxn-primary"
                  )}
                  onClick={() => setSelectedRound(round.id)}
                >
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-dxn-primary text-white flex items-center justify-center text-sm font-bold">
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
                                className="h-9 w-9 text-dxn-error"
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
                  previewTrust >= 70 ? "text-dxn-success" : previewTrust >= 40 ? "text-dxn-warning" : "text-dxn-error"
                )}>
                  {previewTrust}%
                </span>
              </div>
              <div className="h-3 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className={cn(
                    "h-full transition-all duration-500",
                    previewTrust >= 70 ? "bg-dxn-success" : previewTrust >= 40 ? "bg-dxn-warning" : "bg-dxn-error"
                  )}
                  style={{ width: `${previewTrust}%` }}
                />
              </div>
              <div className="flex items-center gap-1 text-dxn-error">
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
            <div className="space-y-4">
              <div className="flex items-center gap-3 pb-3 border-b border-gray-100">
                <div className="w-10 h-10 rounded-full bg-dxn-accent text-white flex items-center justify-center font-bold">أ</div>
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
                    className="w-full text-right bg-white border-2 border-gray-100 hover:border-dxn-primary rounded-xl px-4 py-3 text-sm transition-colors"
                  >
                    {option.text}
                  </button>
                ))}
              </div>
            </div>

            <div className="pt-4 border-t border-gray-100">
              <p className="text-sm font-medium mb-2">اختيار الجولة للمعاينة</p>
              <div className="space-y-1">
                {rounds.map((round, idx) => (
                  <button
                    key={round.id}
                    onClick={() => {
                      setPreviewRound(round.id)
                      setPreviewTrust(50)
                      setPreviewHearts(3)
                    }}
                    className={cn(
                      "w-full text-right px-3 py-2 rounded-lg text-sm transition-colors",
                      previewRound === round.id ? "bg-dxn-primary/10 text-dxn-primary" : "hover:bg-gray-50"
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
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">الحقيبة التدريبية</label>
                <select
                  className="w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
                  value={linkedBag}
                  onChange={(e) => {
                    const bag = Number(e.target.value)
                    setLinkedBag(bag)
                    const firstSection = sections.find((s) => s.bagId === bag)
                    setLinkedSectionId(firstSection?.id || "")
                    setLinkedSkillId(firstSection?.skills[0]?.id || "")
                  }}
                >
                  {Array.from(new Set(sections.map((s) => s.bagId))).map((bagId) => (
                    <option key={bagId} value={bagId}>الحقيبة {bagId}</option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">القسم المرتبط</label>
                <select
                  className="w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
                  value={linkedSectionId}
                  onChange={(e) => {
                    const sectionId = Number(e.target.value)
                    setLinkedSectionId(sectionId)
                    const section = sections.find((s) => s.id === sectionId)
                    setLinkedSkillId(section?.skills[0]?.id || "")
                  }}
                >
                  <option value="">اختر قسماً</option>
                  {sections.map((section) => (
                    <option key={section.id} value={section.id}>{section.title}</option>
                  ))}
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">المهارة المرتبطة</label>
                <select
                  className="w-full h-10 rounded-md border border-input bg-background px-3 text-sm"
                  value={linkedSkillId}
                  onChange={(e) => setLinkedSkillId(Number(e.target.value))}
                >
                  <option value="">اختر مهارة</option>
                  {activeLinkedSection?.skills.map((skill) => (
                    <option key={skill.id} value={skill.id}>{skill.title}</option>
                  ))}
                </select>
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
                    <span className="font-medium">{activeLinkedSection?.title || "—"}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">المهارة</span>
                    <span className="font-medium">{activeLinkedSkill?.title || "—"}</span>
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
                {showValidation && validationErrors.length > 0 ? (
                  <div className="space-y-2">
                    {validationErrors.map((err, idx) => (
                      <div key={idx} className="flex items-start gap-2 text-sm text-dxn-error">
                        <AlertCircle size={16} className="shrink-0 mt-0.5" />
                        {err}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-dxn-success flex items-center gap-2">
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
                  onClick={() => {
                    setShowValidation(true)
                    if (validationErrors.length === 0) {
                      setSettingsOpen(false)
                    }
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
