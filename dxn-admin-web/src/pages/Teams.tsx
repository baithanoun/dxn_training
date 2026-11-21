import {
  UsersRound,
  TrendingUp,
  Send,
  FileBarChart,
  ChevronLeft,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { teams } from "@/data/mockData"

// Inline Progress component since we haven't created it yet
const ProgressBar = ({ value }: { value: number }) => (
  <div className="h-2.5 w-full bg-gray-100 rounded-full overflow-hidden">
    <div
      className="h-full bg-dxn-success rounded-full transition-all"
      style={{ width: `${value}%` }}
    />
  </div>
)

export default function TeamsPage() {
  return (
    <div className="p-8 space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-dxn-accent">الفرق</h1>
          <p className="text-muted-foreground">متابعة أداء القادة والأعضاء التابعين</p>
        </div>
        <Button variant="outline" className="gap-2">
          <FileBarChart size={16} />
          تصدير تقرير
        </Button>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {[
          { title: "إجمالي الفرق", value: teams.length, icon: UsersRound },
          { title: "إجمالي الأعضاء", value: teams.reduce((a, b) => a + b.members, 0), icon: UsersRound },
          { title: "متوسط الإكمال", value: "62%", icon: TrendingUp },
        ].map((stat, idx) => {
          const Icon = stat.icon
          return (
            <Card key={idx} className="border-0 shadow-sm">
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">{stat.title}</p>
                    <p className="text-2xl font-bold text-dxn-accent">{stat.value}</p>
                  </div>
                  <div className="w-10 h-10 rounded-lg bg-dxn-secondary/10 text-dxn-secondary flex items-center justify-center">
                    <Icon size={20} />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {teams.map((team) => (
          <Card key={team.id} className="border-0 shadow-sm">
            <CardHeader className="pb-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full bg-dxn-accent/10 text-dxn-accent flex items-center justify-center text-lg font-bold">
                    {team.leader[0]}
                  </div>
                  <div>
                    <CardTitle className="text-lg">{team.leader}</CardTitle>
                    <CardDescription>{team.members} عضو · {team.activeToday} نشط اليوم</CardDescription>
                  </div>
                </div>
                <Button variant="ghost" size="icon">
                  <ChevronLeft size={18} />
                </Button>
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

              <div className="flex gap-2">
                <Button variant="outline" size="sm" className="gap-2 flex-1">
                  <Send size={14} />
                  تذكير جماعي
                </Button>
                <Button size="sm" className="flex-1">
                  عرض الأعضاء
                </Button>
              </div>

              <div className="pt-2 border-t border-gray-100">
                <p className="text-sm font-medium mb-2">أبرز الأعضاء</p>
                <div className="flex -space-x-2 space-x-reverse">
                  {Array.from({ length: Math.min(5, team.members) }).map((_, i) => (
                    <div
                      key={i}
                      className="w-8 h-8 rounded-full bg-gray-200 border-2 border-white flex items-center justify-center text-xs font-bold"
                    >
                      {String.fromCharCode(1575 + i)}
                    </div>
                  ))}
                  {team.members > 5 && (
                    <div className="w-8 h-8 rounded-full bg-gray-100 border-2 border-white flex items-center justify-center text-xs">
                      +{team.members - 5}
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
