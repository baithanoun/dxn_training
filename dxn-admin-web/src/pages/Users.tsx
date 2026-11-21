import { useState } from "react"
import {
  Search,
  Filter,
  MoreVertical,
  UserCog,
  Lock,
  Bell,
  Ban,
  Award,
  ChevronLeft,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { users, sections, certificates } from "@/data/mockData"

const roleBadge: Record<string, "default" | "secondary" | "warning" | "muted"> = {
  "متدرب": "default",
  "قائد": "secondary",
  "مدير": "warning",
}

export default function UsersPage() {
  const [selectedUser, setSelectedUser] = useState<(typeof users)[0] | null>(null)

  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">المستخدمون</h1>
          <p className="text-muted-foreground">إدارة حسابات المتدربين والقادة والمديرين</p>
        </div>
        <Button className="gap-2">
          <UserCog size={16} />
          إضافة مستخدم
        </Button>
      </header>

      <div className="flex gap-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={18} />
          <Input placeholder="بحث بالاسم أو البريد أو رقم العضوية..." className="pr-10" />
        </div>
        <Button variant="outline" className="gap-2">
          <Filter size={16} />
          فلتر
        </Button>
      </div>

      <Card className="border-0 shadow-sm">
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>الاسم</TableHead>
                <TableHead>رقم العضوية</TableHead>
                <TableHead>الدور</TableHead>
                <TableHead>القائد</TableHead>
                <TableHead>XP</TableHead>
                <TableHead>آخر نشاط</TableHead>
                <TableHead>الحالة</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((user) => (
                <TableRow
                  key={user.id}
                  className="cursor-pointer"
                  onClick={() => setSelectedUser(user)}
                >
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-dxn-primary/10 text-dxn-primary flex items-center justify-center font-bold">
                        {user.name[0]}
                      </div>
                      <div>
                        <p className="font-medium">{user.name}</p>
                        <p className="text-xs text-muted-foreground">{user.email}</p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{user.dxnId}</TableCell>
                  <TableCell>
                    <Badge variant={roleBadge[user.role]}>{user.role}</Badge>
                  </TableCell>
                  <TableCell>{user.leader}</TableCell>
                  <TableCell>{user.xp.toLocaleString("ar-SA")}</TableCell>
                  <TableCell>{user.lastActive}</TableCell>
                  <TableCell>
                    <Badge variant={user.status === "نشط" ? "success" : "warning"}>{user.status}</Badge>
                  </TableCell>
                  <TableCell>
                    <Button variant="ghost" size="icon" className="h-8 w-8">
                      <MoreVertical size={16} />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* User Detail Drawer */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 flex">
          <div
            className="flex-1 bg-black/30"
            onClick={() => setSelectedUser(null)}
          />
          <div className="w-[420px] bg-white shadow-xl p-6 overflow-y-auto">
            <button
              onClick={() => setSelectedUser(null)}
              className="flex items-center gap-1 text-sm text-muted-foreground mb-4"
            >
              <ChevronLeft size={16} />
              رجوع
            </button>

            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full bg-dxn-primary text-white flex items-center justify-center text-2xl font-bold">
                {selectedUser.name[0]}
              </div>
              <div>
                <h2 className="text-xl font-bold">{selectedUser.name}</h2>
                <p className="text-muted-foreground">{selectedUser.email}</p>
                <Badge variant={roleBadge[selectedUser.role]} className="mt-1">{selectedUser.role}</Badge>
              </div>
            </div>

            <div className="space-y-4">
              <Card className="border-0 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">معلومات الحساب</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">رقم العضوية DXN</span>
                    <span className="font-medium">{selectedUser.dxnId}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">القائد</span>
                    <span className="font-medium">{selectedUser.leader}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">إجمالي XP</span>
                    <span className="font-medium">{selectedUser.xp.toLocaleString("ar-SA")}</span>
                  </div>
                </CardContent>
              </Card>

              <Card className="border-0 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">تقدم الحقائب الخمس</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {sections.map((section) => {
                    // محاكاة تقدم المستخدم بشكل ثابت بناءً على XP ومعرف القسم
                    const baseProgress = Math.min(100, Math.round((selectedUser.xp / (section.id * 400)) * 100))
                    const progress = Math.min(100, Math.max(0, baseProgress))
                    return (
                      <div key={section.id}>
                        <div className="flex justify-between text-sm mb-1">
                          <div className="flex items-center gap-2">
                            <span className="text-xs text-muted-foreground">{section.bagId}</span>
                            <span className="truncate max-w-[180px]">{section.title}</span>
                          </div>
                          <span className="font-medium">{progress}%</span>
                        </div>
                        <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-dxn-success rounded-full"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                      </div>
                    )
                  })}
                </CardContent>
              </Card>

              <Card className="border-0 shadow-sm">
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">الشهادات المُنَوَّرة</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  {certificates.map((cert) => {
                    const earned = selectedUser.xp >= cert.sectionId * 500
                    const inProgress = selectedUser.xp >= (cert.sectionId - 1) * 500
                    return (
                      <div
                        key={cert.id}
                        className="flex items-center justify-between text-sm p-2 rounded-lg border"
                      >
                        <span className={earned ? "text-dxn-accent font-medium" : "text-muted-foreground"}>
                          {cert.title}
                        </span>
                        <Badge variant={earned ? "default" : inProgress ? "warning" : "muted"}>
                          {earned ? "منجزة" : inProgress ? "قيد التقدم" : "مقفل"}
                        </Badge>
                      </div>
                    )
                  })}
                </CardContent>
              </Card>

              <div className="grid grid-cols-2 gap-3">
                <Button variant="outline" className="gap-2">
                  <Lock size={16} />
                  إعادة تعيين كلمة المرور
                </Button>
                <Button variant="outline" className="gap-2">
                  <Bell size={16} />
                  إرسال إشعار
                </Button>
                <Button variant="outline" className="gap-2">
                  <Award size={16} />
                  الشهادات
                </Button>
                <Button variant="outline" className="gap-2 text-dxn-error hover:text-dxn-error">
                  <Ban size={16} />
                  تعطيل الحساب
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
