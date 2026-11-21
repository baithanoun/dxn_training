// Mirrors dxn-api Resources (UserResource, Admin/*Resource). All ids are strings.
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
