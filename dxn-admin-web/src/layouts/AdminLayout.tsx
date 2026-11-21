import { Link, useLocation, Outlet } from "react-router-dom"
import {
  LayoutDashboard,
  BookOpen,
  MessageSquareText,
  Users,
  UsersRound,
  BarChart3,
  Settings,
  LogOut,
  Bell,
} from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/lib/auth"

const navItems = [
  { path: "/", label: "نظرة عامة", icon: LayoutDashboard },
  { path: "/content", label: "المحتوى التدريبي", icon: BookOpen },
  { path: "/scenario", label: "منشئ السيناريوهات", icon: MessageSquareText },
  { path: "/users", label: "المستخدمون", icon: Users },
  { path: "/teams", label: "الفرق", icon: UsersRound },
  { path: "/analytics", label: "التحليلات", icon: BarChart3 },
  { path: "/settings", label: "الإعدادات", icon: Settings },
]

export default function AdminLayout() {
  const location = useLocation()
  const { user, logout } = useAuth()

  return (
    <div dir="rtl" className="min-h-screen bg-dxn-bg flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-l border-gray-200 fixed inset-y-0 right-0 z-50 flex flex-col shadow-sm">
        <div className="p-6 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-dxn-primary flex items-center justify-center text-white font-bold text-sm">
              DXN
            </div>
            <div>
              <h1 className="font-bold text-dxn-accent text-lg leading-tight">DXN Training</h1>
              <p className="text-xs text-muted-foreground">لوحة الإدارة</p>
            </div>
          </div>
        </div>

        <div className="px-4 py-4 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-dxn-secondary/20 text-dxn-secondary flex items-center justify-center font-bold">{user?.fullName.charAt(0)}</div>
            <div>
              <p className="text-sm font-semibold">{user?.fullName}</p>
              <p className="text-xs text-muted-foreground">مدير</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto p-3 space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path
            const Icon = item.icon
            return (
              <Link
                key={item.path}
                to={item.path}
                className={cn(
                  "flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors",
                  isActive
                    ? "bg-dxn-primary/10 text-dxn-primary border-l-4 border-dxn-primary"
                    : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                )}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            )
          })}
        </nav>

        <div className="p-4 border-t border-gray-100 space-y-2">
          <Button variant="ghost" className="w-full justify-start gap-3 text-gray-600">
            <Bell size={18} />
            الإشعارات
          </Button>
          <Button variant="ghost" onClick={logout} className="w-full justify-start gap-3 text-dxn-error hover:text-dxn-error hover:bg-dxn-error/10">
            <LogOut size={18} />
            تسجيل الخروج
          </Button>
          <p className="text-center text-xs text-muted-foreground pt-2">الإصدار 2.5.0</p>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 mr-64 min-h-screen">
        <Outlet />
      </main>
    </div>
  )
}
