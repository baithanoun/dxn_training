import { useState } from "react"
import {
  ChevronDown,
  ChevronLeft,
  Plus,
  Search,
  Filter,
  MoreVertical,
  Edit3,
  Eye,
  Layers,
  BookOpen,
  FileQuestion,
  MessageSquareText,
  Lock,
  Award,
  Unlock,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import { sections } from "@/data/mockData"

const typeIcons: Record<string, React.ElementType> = {
  lesson: BookOpen,
  quiz: FileQuestion,
  simulation: MessageSquareText,
}

const statusColors: Record<string, "default" | "secondary" | "warning" | "muted"> = {
  published: "default",
  draft: "warning",
  archived: "muted",
}

const bagColors: Record<number, string> = {
  1: "bg-dxn-primary/10 text-dxn-primary",
  2: "bg-dxn-success/10 text-dxn-success",
  3: "bg-dxn-secondary/10 text-dxn-secondary",
  4: "bg-dxn-accent/10 text-dxn-accent",
  5: "bg-dxn-warning/10 text-dxn-warning",
}

function getRequiredSectionTitle(id?: number | null) {
  if (!id) return null
  return sections.find((s) => s.id === id)?.title
}

export default function Content() {
  const [selectedSection, setSelectedSection] = useState<number | null>(sections[0]?.id ?? null)
  const [selectedSkill, setSelectedSkill] = useState<number | null>(sections[0]?.skills[0]?.id ?? null)
  const [selectedLevel, setSelectedLevel] = useState<number | null>(null)
  const [expanded, setExpanded] = useState<Record<number, boolean>>({
    [sections[0]?.id ?? 0]: true,
    [sections[0]?.skills[0]?.id ?? 0]: true,
  })


  const activeSection = sections.find((s) => s.id === selectedSection)
  const activeSkill = activeSection?.skills.find((s) => s.id === selectedSkill)

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
          <Button className="gap-2">
            <Plus size={16} />
            إضافة قسم
          </Button>
        </div>
      </header>

      <div className="flex-1 grid grid-cols-12 gap-6 min-h-0">
        {/* Tree */}
        <Card className="col-span-4 border-0 shadow-sm flex flex-col">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg">شجرة المحتوى</CardTitle>
              <div className="flex gap-1">
                <Button variant="ghost" size="icon" className="h-8 w-8">
                  <Plus size={16} />
                </Button>
              </div>
            </div>
            <CardDescription>
              {sections.length} حقائب · {sections.reduce((acc, s) => acc + s.skills.length, 0)} مهارات ·{" "}
              {sections.reduce((acc, s) => acc + s.skills.reduce((c, k) => c + k.levels.length, 0), 0)} مستويات
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1 overflow-y-auto">
            <div className="space-y-1">
              {sections.map((section) => {
                const isOpen = expanded[section.id]
                const isLocked = !!section.requiredSectionId
                const requiredTitle = getRequiredSectionTitle(section.requiredSectionId)
                return (
                  <div key={section.id}>
                    <button
                      onClick={() => {
                        setSelectedSection(section.id)
                        setSelectedSkill(null)
                        setSelectedLevel(null)
                        setExpanded((prev) => ({ ...prev, [section.id]: !prev[section.id] }))
                      }}
                      className={cn(
                        "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-semibold transition-colors",
                        selectedSection === section.id
                          ? "bg-dxn-primary/10 text-dxn-primary"
                          : "text-gray-700 hover:bg-gray-50"
                      )}
                    >
                      {isOpen ? <ChevronDown size={16} /> : <ChevronLeft size={16} />}
                      <Layers size={16} />
                      <span className="truncate">{section.title}</span>
                      <Badge
                        variant={statusColors[section.status]}
                        className="mr-auto shrink-0"
                      >
                        {section.status === "published" ? "منشور" : "مسودة"}
                      </Badge>
                    </button>

                    <div className="mr-6 pr-2 flex items-center gap-1 text-xs text-muted-foreground mt-0.5">
                      <span
                        className={cn(
                          "px-2 py-0.5 rounded-full font-medium",
                          bagColors[section.bagId] || "bg-gray-100 text-gray-600"
                        )}
                      >
                        الحقيبة {section.bagId}
                      </span>
                      {isLocked ? (
                        <span className="flex items-center gap-1 text-dxn-error">
                          <Lock size={12} />
                          يتطلب: {requiredTitle}
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
                        {section.skills.map((skill) => {
                          const skillOpen = expanded[skill.id]
                          return (
                            <div key={skill.id}>
                              <button
                                onClick={() => {
                                  setSelectedSkill(skill.id)
                                  setSelectedLevel(null)
                                  setExpanded((prev) => ({ ...prev, [skill.id]: !prev[skill.id] }))
                                }}
                                className={cn(
                                  "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors",
                                  selectedSkill === skill.id
                                    ? "bg-dxn-secondary/10 text-dxn-secondary"
                                    : "text-gray-600 hover:bg-gray-50"
                                )}
                              >
                                {skillOpen ? <ChevronDown size={14} /> : <ChevronLeft size={14} />}
                                {skill.title}
                                <Badge variant={statusColors[skill.status]} className="mr-auto">
                                  {skill.status === "published" ? "منشور" : "مسودة"}
                                </Badge>
                              </button>

                              {skillOpen && (
                                <div className="mr-5 mt-1 space-y-1">
                                  {skill.levels.map((level) => {
                                    const Icon = typeIcons[level.type]
                                    return (
                                      <button
                                        key={level.id}
                                        onClick={() => setSelectedLevel(level.id)}
                                        className={cn(
                                          "w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors",
                                          selectedLevel === level.id
                                            ? "bg-gray-100 text-dxn-accent font-medium"
                                            : "text-gray-500 hover:bg-gray-50"
                                        )}
                                      >
                                        <Icon size={14} />
                                        {level.title}
                                        <span className="mr-auto text-xs text-muted-foreground">{level.xp} XP</span>
                                      </button>
                                    )
                                  })}
                                </div>
                              )}
                            </div>
                          )
                        })}
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
                    {selectedLevel ? "تفاصيل المستوى" : activeSkill ? activeSkill.title : activeSection?.title}
                  </CardTitle>
                  <Badge variant={selectedLevel ? "default" : "secondary"}>
                    {selectedLevel ? "مستوى" : activeSkill ? "مهارة" : "قسم"}
                  </Badge>
                </div>
                <CardDescription>
                  {selectedLevel
                    ? "تعديل البطاقات التعليمية وجولات الاختبار والإعدادات"
                    : activeSkill
                    ? "عرض المستويات التابعة لهذه المهارة"
                    : activeSection
                    ? activeSection.description
                    : "اختر قسماً لعرض التفاصيل"}
                </CardDescription>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" className="gap-2">
                  <Eye size={16} />
                  معاينة
                </Button>
                <Button size="sm" className="gap-2">
                  <Edit3 size={16} />
                  تعديل
                </Button>
                <Button variant="ghost" size="icon" className="h-9 w-9">
                  <MoreVertical size={18} />
                </Button>
              </div>
            </div>
          </CardHeader>

          <CardContent className="flex-1 p-6 overflow-y-auto">
            {sections.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-center py-20">
                <Layers size={48} className="text-muted-foreground mb-4" />
                <p className="text-lg font-medium text-dxn-accent">لا توجد أقسام</p>
                <p className="text-sm text-muted-foreground">أضف قسماً جديداً لبدء بناء المحتوى التدريبي.</p>
              </div>
            ) : selectedLevel && activeSkill ? (
              (() => {
                const level = activeSkill.levels.find((l) => l.id === selectedLevel)
                return level ? (
                  <div key={level.id} className="space-y-6">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <label className="text-sm font-medium">عنوان المستوى</label>
                        <Input defaultValue={level.title} />
                      </div>
                      <div className="space-y-2">
                        <label className="text-sm font-medium">نوع المستوى</label>
                        <Input defaultValue={level.type} disabled />
                      </div>
                      <div className="space-y-2">
                        <label className="text-sm font-medium">حد الاجتياز (%)</label>
                        <Input type="number" defaultValue={80} />
                      </div>
                      <div className="space-y-2">
                        <label className="text-sm font-medium">مكافأة XP</label>
                        <Input type="number" defaultValue={level.xp} />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium">البطاقات التعليمية</label>
                      <div className="p-4 border border-dashed border-gray-300 rounded-lg text-center text-muted-foreground">
                        سيتم ربط البطاقات التعليمية هنا (Micro-Learning Cards)
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium">جولات الاختبار</label>
                      <div className="p-4 border border-dashed border-gray-300 rounded-lg text-center text-muted-foreground">
                        {level.type === "simulation"
                          ? "افتح منشئ السيناريوهات لإنشاء جولات المحادثة الحية"
                          : "إضافة أسئلة الاختبار هنا"}
                      </div>
                    </div>

                    <div className="flex justify-end gap-2">
                      <Button variant="outline">إلغاء</Button>
                      <Button>حفظ التغييرات</Button>
                    </div>
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center h-full text-center py-20">
                    <Layers size={48} className="text-muted-foreground mb-4" />
                    <p className="text-lg font-medium text-dxn-accent">اختر مستوى</p>
                    <p className="text-sm text-muted-foreground">اختر مستوى من المهارة الحالية لعرض التفاصيل.</p>
                  </div>
                )
              })()
            ) : activeSkill ? (
              <div className="space-y-4">
                <h3 className="font-semibold">مستويات المهارة</h3>
                {activeSkill.levels.map((level) => {
                  const Icon = typeIcons[level.type]
                  return (
                    <div
                      key={level.id}
                      onClick={() => setSelectedLevel(level.id)}
                      className="flex items-center gap-4 p-4 border rounded-lg hover:border-dxn-primary cursor-pointer transition-colors"
                    >
                      <div className="w-10 h-10 rounded-lg bg-dxn-primary/10 text-dxn-primary flex items-center justify-center">
                        <Icon size={20} />
                      </div>
                      <div className="flex-1">
                        <p className="font-medium">{level.title}</p>
                        <p className="text-sm text-muted-foreground">
                          {level.type === "lesson" ? "درس تفاعلي" : level.type === "quiz" ? "اختبار" : "محاكاة محادثة"}
                        </p>
                      </div>
                      <Badge variant="secondary">{level.xp} XP</Badge>
                      <ChevronLeft size={16} className="text-muted-foreground" />
                    </div>
                  )
                })}
              </div>
            ) : activeSection ? (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <Card className="border-0 shadow-sm">
                    <CardContent className="p-4 space-y-1">
                      <p className="text-sm text-muted-foreground">الحقيبة التدريبية</p>
                      <div className="flex items-center gap-2">
                        <span
                          className={cn(
                            "w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold",
                            bagColors[activeSection.bagId]
                          )}
                        >
                          {activeSection.bagId}
                        </span>
                        <span className="text-lg font-bold">الحقيبة {activeSection.bagId}</span>
                      </div>
                    </CardContent>
                  </Card>

                  <Card className="border-0 shadow-sm">
                    <CardContent className="p-4 space-y-1">
                      <p className="text-sm text-muted-foreground">الشهادة</p>
                      <div className="flex items-center gap-2 text-dxn-secondary">
                        <Award size={20} />
                        <span className="text-base font-bold truncate">{activeSection.certificateTitle}</span>
                      </div>
                    </CardContent>
                  </Card>

                  <Card className="border-0 shadow-sm">
                    <CardContent className="p-4 space-y-1">
                      <p className="text-sm text-muted-foreground">الشرط السابق</p>
                      {activeSection.requiredSectionId ? (
                        <div className="flex items-center gap-2 text-dxn-error">
                          <Lock size={20} />
                          <span className="text-sm font-medium truncate">
                            يتطلب إكمال: {getRequiredSectionTitle(activeSection.requiredSectionId)}
                          </span>
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
                    {activeSection.skills.map((skill) => (
                      <div
                        key={skill.id}
                        onClick={() => {
                          setSelectedSkill(skill.id)
                          setExpanded((prev) => ({ ...prev, [skill.id]: true }))
                        }}
                        className="flex items-center gap-4 p-4 border rounded-lg hover:border-dxn-accent cursor-pointer transition-colors"
                      >
                        <div className="w-10 h-10 rounded-lg bg-dxn-accent/10 text-dxn-accent flex items-center justify-center">
                          <Layers size={20} />
                        </div>
                        <div className="flex-1">
                          <p className="font-medium">{skill.title}</p>
                          <p className="text-sm text-muted-foreground">{skill.levels.length} مستويات</p>
                        </div>
                        <Badge variant={statusColors[skill.status]}>
                          {skill.status === "published" ? "منشور" : "مسودة"}
                        </Badge>
                        <ChevronLeft size={16} className="text-muted-foreground" />
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-20 text-muted-foreground">
                اختر قسماً أو مهارة أو مستوى من الشجرة لعرض التفاصيل
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
