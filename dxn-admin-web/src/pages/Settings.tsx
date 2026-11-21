import { useState } from "react"
import {
  Globe,
  Shield,
  Bell,
  Trophy,
  Moon,
  Save,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"

const tabs = [
  { id: "general", label: "إعدادات عامة", icon: Globe },
  { id: "gamification", label: "قواعد التلعيب", icon: Trophy },
  { id: "notifications", label: "الإشعارات", icon: Bell },
  { id: "security", label: "الأمان", icon: Shield },
]

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState("general")

  return (
    <div className="p-8 space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-dxn-accent">الإعدادات</h1>
        <p className="text-muted-foreground">إعدادات التطبيق العامة والأمان والتلعيب</p>
      </header>

      <div className="flex gap-4 border-b border-gray-200">
        {tabs.map((tab) => {
          const Icon = tab.icon
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab.id
                  ? "border-dxn-primary text-dxn-primary"
                  : "border-transparent text-gray-500 hover:text-gray-700"
              }`}
            >
              <Icon size={18} />
              {tab.label}
            </button>
          )
        })}
      </div>

      {activeTab === "general" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card className="border-0 shadow-sm">
            <CardHeader>
              <CardTitle className="text-lg">معلومات التطبيق</CardTitle>
              <CardDescription>اسم التطبيق والشعار واللغة الافتراضية</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">اسم التطبيق</label>
                <Input defaultValue="DXN Training" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">اللغة الافتراضية</label>
                <select className="w-full h-10 rounded-md border border-input bg-background px-3 text-sm">
                  <option value="ar">العربية</option>
                  <option value="en">English</option>
                </select>
              </div>
              <div className="flex items-center justify-between p-3 border rounded-lg">
                <div className="flex items-center gap-3">
                  <Moon size={18} />
                  <span className="text-sm font-medium">تفعيل الوضع الليلي للمستخدمين</span>
                </div>
                <input type="checkbox" defaultChecked className="w-5 h-5" />
              </div>
            </CardContent>
          </Card>

          <Card className="border-0 shadow-sm">
            <CardHeader>
              <CardTitle className="text-lg">إدارة المدراء</CardTitle>
              <CardDescription>المدراء الحاليون وصلاحياتهم</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {[
                { name: "محمد العمري", role: "Super Admin", email: "mohammed@example.com" },
                { name: "خالد العلي", role: "Admin", email: "khaled@example.com" },
              ].map((admin) => (
                <div key={admin.email} className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-dxn-accent text-white flex items-center justify-center font-bold">
                      {admin.name[0]}
                    </div>
                    <div>
                      <p className="text-sm font-medium">{admin.name}</p>
                      <p className="text-xs text-muted-foreground">{admin.email}</p>
                    </div>
                  </div>
                  <Badge variant="secondary">{admin.role}</Badge>
                </div>
              ))}
              <Button variant="outline" className="w-full">إضافة مدير</Button>
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
                <Input type="number" defaultValue={20} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">عدد القلوب الافتراضي</label>
                <Input type="number" defaultValue={3} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">مدة تجديد القلب (ساعات)</label>
                <Input type="number" defaultValue={4} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">حد الـ Streak للتذكير</label>
                <Input type="number" defaultValue={2} />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">إعدادات Leagues</label>
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
            <CardDescription>تفعيل/تعطيل أنواع الإشعارات</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {[
              { label: "تذكير Streak", desc: "قبل انتهاء اليوم بـ 2-4 ساعات" },
              { label: "إعادة المشاركة", desc: "لم يفتح التطبيق 24/48/72 ساعة" },
              { label: "نتائج Leagues", desc: "نهاية كل أسبوع" },
              { label: "تذكير القادة", desc: "أعضاء متوقفون > 3 أيام" },
            ].map((item) => (
              <div key={item.label} className="flex items-center justify-between p-3 border rounded-lg">
                <div>
                  <p className="text-sm font-medium">{item.label}</p>
                  <p className="text-xs text-muted-foreground">{item.desc}</p>
                </div>
                <input type="checkbox" defaultChecked className="w-5 h-5" />
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {activeTab === "security" && (
        <Card className="border-0 shadow-sm max-w-2xl">
          <CardHeader>
            <CardTitle className="text-lg">الأمان</CardTitle>
            <CardDescription>2FA، مدة الجلسة، سجل الدخول</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between p-3 border rounded-lg">
              <div>
                <p className="text-sm font-medium">2FA إجباري للمدراء</p>
                <p className="text-xs text-muted-foreground">إجبار كل المدراء على تفعيل المصادقة الثنائية</p>
              </div>
              <input type="checkbox" defaultChecked className="w-5 h-5" />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">مدة الجلسة (دقيقة)</label>
              <Input type="number" defaultValue={60} />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Rate Limit — محاولات الاختبار في الدقيقة</label>
              <Input type="number" defaultValue={10} />
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex justify-end">
        <Button className="gap-2">
          <Save size={16} />
          حفظ الإعدادات
        </Button>
      </div>
    </div>
  )
}
