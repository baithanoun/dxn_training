// Mirrors API Resources (UserResource, Admin/*Resource). All ids are strings.
export type Role = "ADMIN" | "LEADER" | "TRAINEE"
export type ContentStatus = "draft" | "published"
export type LevelType = "lesson" | "quiz" | "chat_battle" | "match_pairs" | "order_sequence"

export interface User {
  id: string
  fullName: string
  email: string | null
  phone: string | null
  dxnId: string | null
  role: Role
  sponsorId: string | null
  totalXp: number
  currentStreak: number
  hearts: number
  gems: number
  isActive: boolean
}

export interface Level {
  id: string
  skillId: string
  title: string
  type: LevelType
  passThreshold: number
  xpReward: number
  orderIndex: number
  isFinalQuiz: boolean
  status: ContentStatus
  cardsCount?: number
  roundsCount?: number
}

export interface Skill {
  id: string
  sectionId: string
  title: string
  icon: string | null
  orderIndex: number
  status: ContentStatus
  levels?: Level[]
}

export interface Section {
  id: string
  title: string
  description: string | null
  orderIndex: number
  bagId: string | null
  certificateTitle: string | null
  requiredSectionId: string | null
  status: ContentStatus
  skills?: Skill[]
}

export interface Card {
  id: string
  title: string
  body: string
  tip: string | null
  mediaUrl: string | null
  orderIndex: number
}

export interface RoundOption {
  id: string
  text: string
  isCorrect: boolean
  isNeutral: boolean
  trustImpact: number
  feedback: string | null
  xpBonus: number
  orderIndex: number
}

export interface Round {
  id: string
  title: string
  prompt: string
  initialTrust: number
  orderIndex: number
  options: RoundOption[]
}

export interface LevelDetail extends Level {
  cards: Card[]
  rounds: Round[]
}

// Mirrors Admin\AdminUserResource. Roles are serialized uppercase; ids stay strings.
export interface AdminUser {
  id: string
  fullName: string
  email: string | null
  phone: string | null
  dxnId: string | null
  role: Role
  leaderId: string | null
  leaderName: string | null
  totalXp: number
  currentStreak: number
  lastActivityDate: string | null
  isActive: boolean
  certificatesCount: number
  createdAt: string | null
}

export interface AdminCertificate {
  sectionId: string
  sectionTitle: string | null
  code: string
  issuedAt: string | null
}

// Shape expected from GET admin/users/{id}; sectionsProgress is currently [] in the API.
export interface SectionProgress {
  sectionId: string
  sectionTitle?: string | null
  progressPercent: number
}

export interface AdminUserDetail extends AdminUser {
  certificates: AdminCertificate[]
  sectionsProgress: SectionProgress[]
}

export interface Paginated<T> {
  data: T[]
  meta: {
    page: number
    perPage: number
    total: number
    lastPage: number
  }
}

// Mirrors Admin\TeamController@index (leaderName added alongside the leader object).
export interface TeamSummary {
  leaderId: string
  leader: AdminUser
  leaderName: string
  members: number
  completion: number
  activeToday: number
}

export interface TeamMember extends AdminUser {
  progressPercent: number
}

export interface TeamDetail {
  leader: AdminUser
  members: TeamMember[]
}

// Mirrors Admin\DashboardController@overview.
export interface OverviewStat {
  title: string
  value: number | string
  change: number
  changeType: "up" | "down"
  icon: string
}

export interface OverviewActivity {
  date: string
  sessions: number
  activeUsers: number
}

export interface FailedSkill {
  name: string
  failureRate: number
}

export interface InactiveUser {
  id: string
  name: string
  lastActive: string | null
  xp: number
  role: Role
}

export interface CertificateCount {
  sectionId: string
  title: string
  issuedCount: number
}

export interface BagProgress {
  sectionId: string
  title: string
  percent: number
}

export interface DashboardOverview {
  stats: OverviewStat[]
  activity: OverviewActivity[]
  failedSkills: FailedSkill[]
  inactiveUsers: InactiveUser[]
  certificatesBySection: CertificateCount[]
  bagsProgress: BagProgress[]
}

// Mirrors Admin\DashboardController@analytics.
export interface AnalyticsSummary {
  title: string
  value: string
  change: number
}

export interface FunnelStep {
  name: string
  value: number
}

export interface FailedQuestion {
  id: string
  question: string
  attempts: number
  errorRate: number
  avgTime: string
}

export interface RetentionDay {
  day: string
  rate: number
}

export interface AnalyticsData {
  summary: AnalyticsSummary[]
  funnel: FunnelStep[]
  failedQuestions: FailedQuestion[]
  retention: RetentionDay[]
}
