import { useCallback, useEffect, useState } from "react"
import { Link } from "react-router-dom"
import {
  ChevronDown,
  ChevronLeft,
  ChevronUp,
  Plus,
  Search,
  Filter,
  Layers,
  BookOpen,
  FileQuestion,
  MessageSquareText,
  Shuffle,
  ListOrdered,
  Lock,
  Award,
  Unlock,
  Trash2,
  Edit3,
  ImagePlus,
  X,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button, buttonVariants } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import { api, ApiError } from "@/lib/api"
import type { ContentStatus, LevelDetail, LevelType, Section, Skill } from "@/lib/types"

const typeIcons: Record<LevelType, React.ElementType> = {
  lesson: BookOpen,
  quiz: FileQuestion,
  chat_battle: MessageSquareText,
  match_pairs: Shuffle,
  order_sequence: ListOrdered,
}

const typeLabels: Record<LevelType, string> = {
  lesson: "درس",
  quiz: "اختبار",
  chat_battle: "محاكي محادثة",
  match_pairs: "مطابقة",
  order_sequence: "ترتيب",
}

const statusColors: Record<ContentStatus, "default" | "warning"> = {
  published: "default",
  draft: "warning",
}
const statusLabel = (s: ContentStatus) => (s === "published" ? "منشور" : "مسودة")

const bagColors: Record<number, string> = {
  1: "bg-dxn-primary/10 text-dxn-primary",
  2: "bg-dxn-success/10 text-dxn-success",
  3: "bg-dxn-secondary/10 text-dxn-secondary",
  4: "bg-dxn-accent/10 text-dxn-accent",
  5: "bg-dxn-warning/10 text-dxn-warning",
}

const fieldCls =
  "flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"

function errMessage(e: unknown) {
  if (e instanceof ApiError) {
    const first = Object.values(e.errors ?? {})[0]?.[0]
    return first && first !== e.message ? `${e.message} ${first}` : e.message
  }
  return "تعذر الاتصال بالخادم"
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">{label}</label>
      {children}
    </div>
  )
}

const move = <T,>(arr: T[], i: number, d: number) => {
  const a = [...arr]
  ;[a[i], a[i + d]] = [a[i + d], a[i]]
  return a
}

function Arrows({ i, n, onMove }: { i: number; n: number; onMove: (d: number) => void }) {
  return (
    <span className="flex shrink-0">
      <button disabled={i === 0} onClick={(e) => { e.stopPropagation(); onMove(-1) }} className="p-0.5 disabled:opacity-20 hover:text-dxn-primary" aria-label="أعلى">
        <ChevronUp size={14} />
      </button>
      <button disabled={i === n - 1} onClick={(e) => { e.stopPropagation(); onMove(1) }} className="p-0.5 disabled:opacity-20 hover:text-dxn-primary" aria-label="أسفل">
        <ChevronDown size={14} />
      </button>
    </span>
  )
}

type Creating = { kind: "section" } | { kind: "skill"; sectionId: string } | { kind: "level"; skillId: string } | null

export default function Content() {
  const [tree, setTree] = useState<Section[] | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [selectedSection, setSelectedSection] = useState<string | null>(null)
  const [selectedSkill, setSelectedSkill] = useState<string | null>(null)
  const [selectedLevel, setSelectedLevel] = useState<string | null>(null)
  const [expanded, setExpanded] = useState<Record<string, boolean>>({})
  const [creating, setCreating] = useState<Creating>(null)
  const [editing, setEditing] = useState(false)

  const load = useCallback(async () => {
    try {
      const t = await api<Section[]>("/api/admin/content/tree")
      setTree(t)
      setLoadErr(null)
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  // Runs a mutation, shows API errors in the banner, then refetches the tree (store/update responses lack counts).
  const run = async (fn: () => Promise<unknown>) => {
    setErr(null)
    setBusy(true)
    try {
      await fn()
      await load()
      return true
    } catch (e) {
      setErr(errMessage(e))
      return false
    } finally {
      setBusy(false)
    }
  }

  const sections = tree ?? []
  const activeSection = sections.find((s) => s.id === selectedSection)
  const activeSkill = activeSection?.skills?.find((s) => s.id === selectedSkill)
  const sectionTitle = (id: string | null) => sections.find((s) => s.id === id)?.title

  const reorder = (type: "sections" | "skills" | "levels", ids: string[], parentId?: string) =>
    run(() => api("/api/admin/reorder", { json: { type, parentId, ids } }))

  const remove = async (path: string, what: string, after: () => void) => {
    if (!window.confirm(`حذف ${what}؟ لا يمكن التراجع عن هذا الإجراء.`)) return
    if (await run(() => api(path, { method: "DELETE" }))) after()
  }

  const select = (sec: string | null, skill: string | null, level: string | null) => {
    setSelectedSection(sec)
    setSelectedSkill(skill)
    setSelectedLevel(level)
    setCreating(null)
    setEditing(false)
  }

  const toggle = (id: string) => setExpanded((p) => ({ ...p, [id]: !p[id] }))

  const startCreate = (c: Creating) => {
    setCreating(c)
    setEditing(false)
    setSelectedLevel(null)
  }

  const counts = {
    skills: sections.reduce((a, s) => a + (s.skills?.length ?? 0), 0),
    levels: sections.reduce((a, s) => a + (s.skills ?? []).reduce((c, k) => c + (k.levels?.length ?? 0), 0), 0),
  }

  return (
    <div className="p-8 space-y-6 h-screen flex flex-col">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">المحتوى التدريبي</h1>
          <p className="text-muted-foreground">إدارة الأقسام المبنية على الحقائب التدريبية والشهادات</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" className="gap-2">
            <Search size={16} />
            بحث
          </Button>
          <Button variant="outline" className="gap-2">
            <Filter size={16} />
            فلتر
          </Button>
          <Button className="gap-2" onClick={() => startCreate({ kind: "section" })}>
            <Plus size={16} />
            إضافة قسم
          </Button>
        </div>
      </header>

      {err && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
          <span>{err}</span>
          <button onClick={() => setErr(null)} aria-label="إغلاق">
            <X size={16} />
          </button>
        </div>
      )}

      {tree === null ? (
        <div className="flex-1 flex flex-col items-center justify-center gap-3 text-muted-foreground">
          {loadErr ? (
            <>
              <p className="text-red-600">{loadErr}</p>
              <Button variant="outline" onClick={() => void load()}>
                إعادة المحاولة
              </Button>
            </>
          ) : (
            <p>جارٍ التحميل...</p>
          )}
        </div>
      ) : (
        <div className="flex-1 grid grid-cols-12 gap-6 min-h-0">
          {/* Tree */}
          <Card className="col-span-4 border-0 shadow-sm flex flex-col">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">شجرة المحتوى</CardTitle>
                <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => startCreate({ kind: "section" })}>
                  <Plus size={16} />
                </Button>
              </div>
              <CardDescription>
                {sections.length} حقائب · {counts.skills} مهارات · {counts.levels} مستويات
              </CardDescription>
            </CardHeader>
            <CardContent className="flex-1 overflow-y-auto">
              <div className="space-y-1">
                {sections.map((section, si) => {
                  const isOpen = expanded[section.id]
                  const isLocked = !!section.requiredSectionId
                  const skills = section.skills ?? []
                  return (
                    <div key={section.id}>
                      <div
                        className={cn(
                          "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-semibold transition-colors cursor-pointer",
                          selectedSection === section.id && !selectedSkill && !selectedLevel && !creating
                            ? "bg-dxn-primary/10 text-dxn-primary"
                            : "text-gray-700 hover:bg-gray-50"
                        )}
                        onClick={() => {
                          select(section.id, null, null)
                          toggle(section.id)
                        }}
                      >
                        {isOpen ? <ChevronDown size={16} /> : <ChevronLeft size={16} />}
                        <Layers size={16} />
                        <span className="truncate">{section.title}</span>
                        <Badge variant={statusColors[section.status]} className="mr-auto shrink-0">
                          {statusLabel(section.status)}
                        </Badge>
                        <Arrows i={si} n={sections.length} onMove={(d) => reorder("sections", move(sections, si, d).map((s) => s.id))} />
                      </div>

                      <div className="mr-6 pr-2 flex items-center gap-1 text-xs text-muted-foreground mt-0.5">
                        {section.bagId && (
                          <span className={cn("px-2 py-0.5 rounded-full font-medium", bagColors[Number(section.bagId)] || "bg-gray-100 text-gray-600")}>
                            الحقيبة {section.bagId}
                          </span>
                        )}
                        {isLocked ? (
                          <span className="flex items-center gap-1 text-dxn-error">
                            <Lock size={12} />
                            يتطلب: {sectionTitle(section.requiredSectionId)}
                          </span>
                        ) : (
                          <span className="flex items-center gap-1 text-dxn-success">
                            <Unlock size={12} />
                            متاح من البداية
                          </span>
                        )}
                      </div>

                      {isOpen && (
                        <div className="mr-6 mt-1 space-y-1 border-r-2 border-gray-100 pr-2">
                          {skills.map((skill, ki) => {
                            const skillOpen = expanded[skill.id]
                            const levels = skill.levels ?? []
                            return (
                              <div key={skill.id}>
                                <div
                                  onClick={() => {
                                    select(section.id, skill.id, null)
                                    toggle(skill.id)
                                  }}
                                  className={cn(
                                    "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors cursor-pointer",
                                    selectedSkill === skill.id && !selectedLevel && !creating
                                      ? "bg-dxn-secondary/10 text-dxn-secondary"
                                      : "text-gray-600 hover:bg-gray-50"
                                  )}
                                >
                                  {skillOpen ? <ChevronDown size={14} /> : <ChevronLeft size={14} />}
                                  {skill.title}
                                  <Badge variant={statusColors[skill.status]} className="mr-auto">
                                    {statusLabel(skill.status)}
                                  </Badge>
                                  <Arrows i={ki} n={skills.length} onMove={(d) => reorder("skills", move(skills, ki, d).map((s) => s.id), section.id)} />
                                </div>

                                {skillOpen && (
                                  <div className="mr-5 mt-1 space-y-1">
                                    {levels.map((level, li) => {
                                      const Icon = typeIcons[level.type]
                                      return (
                                        <div
                                          key={level.id}
                                          onClick={() => {
                                            select(section.id, skill.id, level.id)
                                          }}
                                          className={cn(
                                            "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors cursor-pointer",
                                            selectedLevel === level.id ? "bg-gray-100 text-dxn-accent font-medium" : "text-gray-500 hover:bg-gray-50"
                                          )}
                                        >
                                          <Icon size={14} />
                                          {level.title}
                                          <span className="mr-auto text-xs text-muted-foreground">{level.xpReward} XP</span>
                                          <Arrows i={li} n={levels.length} onMove={(d) => reorder("levels", move(levels, li, d).map((l) => l.id), skill.id)} />
                                        </div>
                                      )
                                    })}
                                    <button
                                      onClick={() => {
                                        select(section.id, skill.id, null)
                                        startCreate({ kind: "level", skillId: skill.id })
                                      }}
                                      className="w-full flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs text-dxn-primary hover:bg-gray-50"
                                    >
                                      <Plus size={12} /> إضافة مستوى
                                    </button>
                                  </div>
                                )}
                              </div>
                            )
                          })}
                          <button
                            onClick={() => {
                              select(section.id, null, null)
                              startCreate({ kind: "skill", sectionId: section.id })
                            }}
                            className="w-full flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs text-dxn-primary hover:bg-gray-50"
                          >
                            <Plus size={12} /> إضافة مهارة
                          </button>
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            </CardContent>
          </Card>

          {/* Details */}
          <Card className="col-span-8 border-0 shadow-sm flex flex-col">
            <CardHeader className="pb-3 border-b border-gray-100">
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <CardTitle className="text-xl">
                      {creating
                        ? { section: "قسم جديد", skill: "مهارة جديدة", level: "مستوى جديد" }[creating.kind]
                        : selectedLevel
                        ? "تفاصيل المستوى"
                        : activeSkill
                        ? activeSkill.title
                        : activeSection?.title}
                    </CardTitle>
                    {!creating && (activeSection || activeSkill) && (
                      <Badge variant={selectedLevel ? "default" : "secondary"}>
                        {selectedLevel ? "مستوى" : activeSkill ? "مهارة" : "قسم"}
                      </Badge>
                    )}
                  </div>
                  <CardDescription>
                    {creating
                      ? "أدخل البيانات ثم اضغط حفظ"
                      : selectedLevel
                      ? "تعديل البطاقات التعليمية والإعدادات"
                      : activeSkill
                      ? "عرض المستويات التابعة لهذه المهارة"
                      : activeSection
                      ? activeSection.description
                      : "اختر قسماً لعرض التفاصيل"}
                  </CardDescription>
                </div>
                {!creating && !selectedLevel && (activeSection || activeSkill) && (
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={busy}
                      onClick={() => {
                        const item = activeSkill ?? activeSection!
                        const status: ContentStatus = item.status === "published" ? "draft" : "published"
                        const base = activeSkill ? `/api/admin/skills/${item.id}` : `/api/admin/sections/${item.id}`
                        void run(() => api(base, { method: "PUT", json: { title: item.title, status } }))
                      }}
                    >
                      {(activeSkill ?? activeSection)!.status === "published" ? "إلغاء النشر" : "نشر"}
                    </Button>
                    <Button size="sm" className="gap-2" onClick={() => setEditing((v) => !v)}>
                      <Edit3 size={16} />
                      تعديل
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={busy}
                      className="gap-2 text-red-600"
                      onClick={() =>
                        activeSkill
                          ? void remove(`/api/admin/skills/${activeSkill.id}`, "المهارة", () => select(selectedSection, null, null))
                          : void remove(`/api/admin/sections/${activeSection!.id}`, "القسم", () => select(null, null, null))
                      }
                    >
                      <Trash2 size={16} />
                      حذف
                    </Button>
                  </div>
                )}
              </div>
            </CardHeader>

            <CardContent className="flex-1 p-6 overflow-y-auto">
              {creating?.kind === "section" || (editing && activeSection && !activeSkill) ? (
                <SectionForm
                  key={creating ? "new" : activeSection!.id}
                  initial={creating ? undefined : activeSection}
                  others={sections.filter((s) => s.id !== activeSection?.id)}
                  busy={busy}
                  onCancel={() => {
                    setCreating(null)
                    setEditing(false)
                  }}
                  onSave={async (data) => {
                    if (creating) {
                      const created = await (async () => {
                        let id: string | null = null
                        const ok = await run(async () => {
                          id = (await api<Section>("/api/admin/sections", { json: data })).id
                        })
                        return ok ? id : null
                      })()
                      if (created) select(created, null, null)
                    } else if (await run(() => api(`/api/admin/sections/${activeSection!.id}`, { method: "PUT", json: data }))) setEditing(false)
                  }}
                />
              ) : creating?.kind === "skill" || (editing && activeSkill && !selectedLevel) ? (
                <SkillForm
                  key={creating ? "new" : activeSkill!.id}
                  initial={creating ? undefined : activeSkill}
                  busy={busy}
                  onCancel={() => {
                    setCreating(null)
                    setEditing(false)
                  }}
                  onSave={async (data) => {
                    if (creating?.kind === "skill") {
                      if (await run(() => api("/api/admin/skills", { json: { ...data, sectionId: creating.sectionId } }))) {
                        setExpanded((p) => ({ ...p, [creating.sectionId]: true }))
                        setCreating(null)
                      }
                    } else if (await run(() => api(`/api/admin/skills/${activeSkill!.id}`, { method: "PUT", json: data }))) setEditing(false)
                  }}
                />
              ) : creating?.kind === "level" ? (
                <NewLevelForm
                  busy={busy}
                  onCancel={() => setCreating(null)}
                  onSave={async (data) => {
                    let id: string | null = null
                    const ok = await run(async () => {
                      id = (await api<{ id: string }>("/api/admin/levels", { json: { ...data, skillId: creating.skillId } })).id
                    })
                    if (ok && id) {
                      setExpanded((p) => ({ ...p, [creating.skillId]: true }))
                      select(selectedSection, selectedSkill, id)
                    }
                  }}
                />
              ) : sections.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-center py-20">
                  <Layers size={48} className="text-muted-foreground mb-4" />
                  <p className="text-lg font-medium text-dxn-accent">لا توجد أقسام</p>
                  <p className="text-sm text-muted-foreground">أضف قسماً جديداً لبدء بناء المحتوى التدريبي.</p>
                </div>
              ) : selectedLevel ? (
                <LevelPanel
                  key={selectedLevel}
                  id={selectedLevel}
                  run={run}
                  busy={busy}
                  onDeleted={() => select(selectedSection, selectedSkill, null)}
                  remove={remove}
                />
              ) : activeSkill ? (
                <SkillView skill={activeSkill} onPick={(id) => select(selectedSection, selectedSkill, id)} />
              ) : activeSection ? (
                <SectionView section={activeSection} requiredTitle={sectionTitle(activeSection.requiredSectionId)} onPick={(id) => {
                  select(activeSection.id, id, null)
                  setExpanded((p) => ({ ...p, [id]: true }))
                }} />
              ) : (
                <div className="text-center py-20 text-muted-foreground">اختر قسماً أو مهارة أو مستوى من الشجرة لعرض التفاصيل</div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}

function FormButtons({ busy, onCancel, label = "حفظ" }: { busy: boolean; onCancel: () => void; label?: string }) {
  return (
    <div className="flex justify-end gap-2">
      <Button type="button" variant="outline" onClick={onCancel} disabled={busy}>
        إلغاء
      </Button>
      <Button type="submit" disabled={busy}>
        {label}
      </Button>
    </div>
  )
}

function SectionForm({
  initial,
  others,
  busy,
  onSave,
  onCancel,
}: {
  initial?: Section
  others: Section[]
  busy: boolean
  onSave: (d: object) => void
  onCancel: () => void
}) {
  const [f, setF] = useState({
    title: initial?.title ?? "",
    description: initial?.description ?? "",
    bagId: initial?.bagId ?? "",
    certificateTitle: initial?.certificateTitle ?? "",
    requiredSectionId: initial?.requiredSectionId ?? "",
  })
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setF((p) => ({ ...p, [k]: e.target.value }))
  return (
    <form
      className="space-y-4"
      onSubmit={(e) => {
        e.preventDefault()
        onSave({
          title: f.title,
          description: f.description || null,
          bagId: f.bagId ? Number(f.bagId) : null,
          certificateTitle: f.certificateTitle || null,
          requiredSectionId: f.requiredSectionId || null,
        })
      }}
    >
      <div className="grid grid-cols-2 gap-4">
        <Field label="عنوان القسم">
          <Input required value={f.title} onChange={set("title")} />
        </Field>
        <Field label="رقم الحقيبة">
          <Input type="number" min={1} max={99} value={f.bagId} onChange={set("bagId")} />
        </Field>
        <Field label="عنوان الشهادة">
          <Input value={f.certificateTitle} onChange={set("certificateTitle")} />
        </Field>
        <Field label="القسم المطلوب سابقاً">
          <select className={cn(fieldCls, "h-10")} value={f.requiredSectionId} onChange={set("requiredSectionId")}>
            <option value="">لا يوجد</option>
            {others.map((s) => (
              <option key={s.id} value={s.id}>
                {s.title}
              </option>
            ))}
          </select>
        </Field>
      </div>
      <Field label="الوصف">
        <textarea className={fieldCls} rows={3} value={f.description} onChange={set("description")} />
      </Field>
      <FormButtons busy={busy} onCancel={onCancel} />
    </form>
  )
}

function SkillForm({
  initial,
  busy,
  onSave,
  onCancel,
}: {
  initial?: Skill
  busy: boolean
  onSave: (d: object) => void
  onCancel: () => void
}) {
  const [title, setTitle] = useState(initial?.title ?? "")
  const [icon, setIcon] = useState(initial?.icon ?? "")
  return (
    <form
      className="space-y-4"
      onSubmit={(e) => {
        e.preventDefault()
        onSave({ title, icon: icon || null })
      }}
    >
      <div className="grid grid-cols-2 gap-4">
        <Field label="عنوان المهارة">
          <Input required value={title} onChange={(e) => setTitle(e.target.value)} />
        </Field>
        <Field label="الأيقونة">
          <Input value={icon} onChange={(e) => setIcon(e.target.value)} />
        </Field>
      </div>
      <FormButtons busy={busy} onCancel={onCancel} />
    </form>
  )
}

const TYPES = Object.keys(typeLabels) as LevelType[]

function NewLevelForm({ busy, onSave, onCancel }: { busy: boolean; onSave: (d: object) => void; onCancel: () => void }) {
  const [title, setTitle] = useState("")
  const [type, setType] = useState<LevelType>("lesson")
  return (
    <form
      className="space-y-4"
      onSubmit={(e) => {
        e.preventDefault()
        onSave({ title, type })
      }}
    >
      <div className="grid grid-cols-2 gap-4">
        <Field label="عنوان المستوى">
          <Input required value={title} onChange={(e) => setTitle(e.target.value)} />
        </Field>
        <Field label="نوع المستوى">
          <select className={cn(fieldCls, "h-10")} value={type} onChange={(e) => setType(e.target.value as LevelType)}>
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {typeLabels[t]}
              </option>
            ))}
          </select>
        </Field>
      </div>
      <FormButtons busy={busy} onCancel={onCancel} />
    </form>
  )
}

function SectionView({ section, requiredTitle, onPick }: { section: Section; requiredTitle?: string; onPick: (id: string) => void }) {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-0 shadow-sm">
          <CardContent className="p-4 space-y-1">
            <p className="text-sm text-muted-foreground">الحقيبة التدريبية</p>
            {section.bagId ? (
              <div className="flex items-center gap-2">
                <span className={cn("w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold", bagColors[Number(section.bagId)])}>
                  {section.bagId}
                </span>
                <span className="text-lg font-bold">الحقيبة {section.bagId}</span>
              </div>
            ) : (
              <span className="text-sm text-muted-foreground">غير محددة</span>
            )}
          </CardContent>
        </Card>
        <Card className="border-0 shadow-sm">
          <CardContent className="p-4 space-y-1">
            <p className="text-sm text-muted-foreground">الشهادة</p>
            <div className="flex items-center gap-2 text-dxn-secondary">
              <Award size={20} />
              <span className="text-base font-bold truncate">{section.certificateTitle ?? "—"}</span>
            </div>
          </CardContent>
        </Card>
        <Card className="border-0 shadow-sm">
          <CardContent className="p-4 space-y-1">
            <p className="text-sm text-muted-foreground">الشرط السابق</p>
            {section.requiredSectionId ? (
              <div className="flex items-center gap-2 text-dxn-error">
                <Lock size={20} />
                <span className="text-sm font-medium truncate">يتطلب إكمال: {requiredTitle}</span>
              </div>
            ) : (
              <div className="flex items-center gap-2 text-dxn-success">
                <Unlock size={20} />
                <span className="text-sm font-medium">متاح من البداية</span>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
      <div>
        <h3 className="font-semibold mb-3">المهارات والمستويات</h3>
        <div className="space-y-3">
          {(section.skills ?? []).map((skill) => (
            <div key={skill.id} onClick={() => onPick(skill.id)} className="flex items-center gap-4 p-4 border rounded-lg hover:border-dxn-accent cursor-pointer transition-colors">
              <div className="w-10 h-10 rounded-lg bg-dxn-accent/10 text-dxn-accent flex items-center justify-center">
                <Layers size={20} />
              </div>
              <div className="flex-1">
                <p className="font-medium">{skill.title}</p>
                <p className="text-sm text-muted-foreground">{skill.levels?.length ?? 0} مستويات</p>
              </div>
              <Badge variant={statusColors[skill.status]}>{statusLabel(skill.status)}</Badge>
              <ChevronLeft size={16} className="text-muted-foreground" />
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

function SkillView({ skill, onPick }: { skill: Skill; onPick: (id: string) => void }) {
  return (
    <div className="space-y-4">
      <h3 className="font-semibold">مستويات المهارة</h3>
      {(skill.levels ?? []).map((level) => {
        const Icon = typeIcons[level.type]
        return (
          <div key={level.id} onClick={() => onPick(level.id)} className="flex items-center gap-4 p-4 border rounded-lg hover:border-dxn-primary cursor-pointer transition-colors">
            <div className="w-10 h-10 rounded-lg bg-dxn-primary/10 text-dxn-primary flex items-center justify-center">
              <Icon size={20} />
            </div>
            <div className="flex-1">
              <p className="font-medium">{level.title}</p>
              <p className="text-sm text-muted-foreground">{typeLabels[level.type]}</p>
            </div>
            <Badge variant="secondary">{level.xpReward} XP</Badge>
            <ChevronLeft size={16} className="text-muted-foreground" />
          </div>
        )
      })}
    </div>
  )
}

interface CardRow {
  key: number
  title: string
  body: string
  tip: string
  mediaUrl: string | null
}
let cardKey = 0

function LevelPanel({
  id,
  run,
  busy,
  onDeleted,
  remove,
}: {
  id: string
  run: (fn: () => Promise<unknown>) => Promise<boolean>
  busy: boolean
  onDeleted: () => void
  remove: (path: string, what: string, after: () => void) => Promise<void>
}) {
  const [detail, setDetail] = useState<LevelDetail | null>(null)
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [f, setF] = useState({ title: "", type: "lesson" as LevelType, passThreshold: 0, xpReward: 0, isFinalQuiz: false, status: "draft" as ContentStatus })
  const [cards, setCards] = useState<CardRow[]>([])
  const [uploadErr, setUploadErr] = useState<string | null>(null)

  const apply = useCallback((d: LevelDetail) => {
    setDetail(d)
    setF({ title: d.title, type: d.type, passThreshold: d.passThreshold, xpReward: d.xpReward, isFinalQuiz: d.isFinalQuiz, status: d.status })
    setCards(d.cards.map((c) => ({ key: ++cardKey, title: c.title, body: c.body, tip: c.tip ?? "", mediaUrl: c.mediaUrl })))
  }, [])

  const fetchDetail = useCallback(async () => {
    try {
      apply(await api<LevelDetail>(`/api/admin/levels/${id}`))
      setLoadErr(null)
    } catch (e) {
      setLoadErr(errMessage(e))
    }
  }, [id, apply])

  useEffect(() => {
    void fetchDetail()
  }, [fetchDetail])

  if (!detail)
    return loadErr ? (
      <div className="text-center py-20 space-y-3">
        <p className="text-red-600">{loadErr}</p>
        <Button variant="outline" onClick={() => void fetchDetail()}>
          إعادة المحاولة
        </Button>
      </div>
    ) : (
      <p className="text-center py-20 text-muted-foreground">جارٍ التحميل...</p>
    )

  const setCard = (i: number, patch: Partial<CardRow>) => setCards((cs) => cs.map((c, j) => (j === i ? { ...c, ...patch } : c)))

  const upload = async (i: number, file: File) => {
    setUploadErr(null)
    const fd = new FormData()
    fd.append("file", file)
    try {
      const { url } = await api<{ url: string }>("/api/admin/media", { body: fd })
      setCard(i, { mediaUrl: url })
    } catch (e) {
      setUploadErr(errMessage(e))
    }
  }

  const save = async () => {
    const ok = await run(async () => {
      await api(`/api/admin/levels/${id}`, { method: "PUT", json: { ...f, passThreshold: Number(f.passThreshold), xpReward: Number(f.xpReward) } })
      apply(
        await api<LevelDetail>(`/api/admin/levels/${id}/cards`, {
          method: "PUT",
          json: { cards: cards.map((c) => ({ title: c.title, body: c.body, tip: c.tip || null, mediaUrl: c.mediaUrl })) },
        }),
      )
    })
    if (!ok) void fetchDetail() // PUT level may have succeeded before cards failed; resync
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-4">
        <Field label="عنوان المستوى">
          <Input value={f.title} onChange={(e) => setF({ ...f, title: e.target.value })} />
        </Field>
        <Field label="نوع المستوى">
          <select className={cn(fieldCls, "h-10")} value={f.type} onChange={(e) => setF({ ...f, type: e.target.value as LevelType })}>
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {typeLabels[t]}
              </option>
            ))}
          </select>
        </Field>
        <Field label="حد الاجتياز (%)">
          <Input type="number" min={0} max={100} value={f.passThreshold} onChange={(e) => setF({ ...f, passThreshold: e.target.valueAsNumber })} />
        </Field>
        <Field label="مكافأة XP">
          <Input type="number" min={0} max={1000} value={f.xpReward} onChange={(e) => setF({ ...f, xpReward: e.target.valueAsNumber })} />
        </Field>
        <Field label="الحالة">
          <select className={cn(fieldCls, "h-10")} value={f.status} onChange={(e) => setF({ ...f, status: e.target.value as ContentStatus })}>
            <option value="draft">مسودة</option>
            <option value="published">منشور</option>
          </select>
        </Field>
        <label className="flex items-center gap-2 text-sm font-medium self-end pb-2">
          <input type="checkbox" checked={f.isFinalQuiz} onChange={(e) => setF({ ...f, isFinalQuiz: e.target.checked })} />
          اختبار نهائي
        </label>
      </div>

      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label className="text-sm font-medium">البطاقات التعليمية</label>
          <Button type="button" variant="outline" size="sm" className="gap-1" onClick={() => setCards((cs) => [...cs, { key: ++cardKey, title: "", body: "", tip: "", mediaUrl: null }])}>
            <Plus size={14} /> إضافة بطاقة
          </Button>
        </div>
        {uploadErr && <p className="text-sm text-red-600">{uploadErr}</p>}
        {cards.length === 0 && (
          <div className="p-4 border border-dashed border-gray-300 rounded-lg text-center text-muted-foreground">لا توجد بطاقات</div>
        )}
        {cards.map((c, i) => (
          <div key={c.key} className="border rounded-lg p-3 space-y-2">
            <div className="flex items-center gap-2">
              <span className="text-xs text-muted-foreground">#{i + 1}</span>
              <Input placeholder="عنوان البطاقة" value={c.title} onChange={(e) => setCard(i, { title: e.target.value })} />
              <Arrows i={i} n={cards.length} onMove={(d) => setCards((cs) => move(cs, i, d))} />
              <button type="button" onClick={() => setCards((cs) => cs.filter((_, j) => j !== i))} className="text-red-600 p-1" aria-label="حذف البطاقة">
                <Trash2 size={16} />
              </button>
            </div>
            <textarea className={fieldCls} rows={3} placeholder="المحتوى" value={c.body} onChange={(e) => setCard(i, { body: e.target.value })} />
            <Input placeholder="نصيحة (اختياري)" value={c.tip} onChange={(e) => setCard(i, { tip: e.target.value })} />
            <div className="flex items-center gap-3">
              <label className="inline-flex items-center gap-1 text-sm text-dxn-primary cursor-pointer">
                <ImagePlus size={16} /> {c.mediaUrl ? "تغيير الصورة" : "رفع صورة"}
                <input
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={(e) => {
                    const file = e.target.files?.[0]
                    e.target.value = ""
                    if (file) void upload(i, file)
                  }}
                />
              </label>
              {c.mediaUrl && (
                <>
                  <img src={c.mediaUrl} alt="" className="h-12 w-12 rounded object-cover" />
                  <button type="button" onClick={() => setCard(i, { mediaUrl: null })} className="text-xs text-red-600">
                    إزالة
                  </button>
                </>
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="flex justify-between gap-2">
        <div className="flex gap-2">
          <Button variant="outline" className="gap-2 text-red-600" disabled={busy} onClick={() => void remove(`/api/admin/levels/${id}`, "المستوى", onDeleted)}>
            <Trash2 size={16} /> حذف المستوى
          </Button>
          {detail.type !== "lesson" && (
            <Link
              to={`/scenario?levelId=${encodeURIComponent(id)}`}
              className={buttonVariants({ variant: "outline", size: "default", className: "gap-2" })}
            >
              <MessageSquareText size={16} /> فتح في منشئ السيناريوهات
            </Link>
          )}
        </div>
        <div className="flex gap-2">
          <Button variant="outline" disabled={busy} onClick={() => apply(detail)}>
            إلغاء
          </Button>
          <Button disabled={busy} onClick={() => void save()}>
            حفظ التغييرات
          </Button>
        </div>
      </div>
    </div>
  )
}
