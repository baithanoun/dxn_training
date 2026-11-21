# SYSTEM_SPEC.md — نظام تعلم تفاعلي بنمط Duolingo

> **Version**: 1.0.0  
> **Status**: Ready for Implementation  
> **Last Updated**: 2026-11-14  
> **Audience**: AI Coding Agents (Cursor / Claude Code / Copilot) + Dev Team  
> **Language**: Arabic (documentation) + English (code identifiers)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [Architecture](#4-architecture)
5. [Data Model](#5-data-model)
6. [API Specification](#6-api-specification)
7. [Core Algorithms](#7-core-algorithms)
8. [Mobile App Structure](#8-mobile-app-structure)
9. [Backend Module Structure](#9-backend-module-structure)
10. [Event Schema](#10-event-schema)
11. [Security Requirements](#11-security-requirements)
12. [Observability & Ops](#12-observability--ops)
13. [Testing Strategy](#13-testing-strategy)
14. [Deployment & Environments](#14-deployment--environments)
15. [Roadmap](#15-roadmap)
16. [MVP Definition of Done](#16-mvp-definition-of-done)
17. [Out of Scope](#17-out-of-scope)
18. [Instructions for AI Coding Agent](#18-instructions-for-ai-coding-agent)
19. [Changelog](#19-changelog)

---

## 1. Executive Summary

| البند | القيمة |
|---|---|
| **نوع النظام** | منصة تعلم تفاعلي mobile-first مع gamification |
| **الهدف** | إبقاء المستخدم في حلقة تعلّم يومية عبر micro-lessons + مكافآت |
| **المنصات** | iOS + Android (Flutter) + Web Admin Panel |
| **البنية** | Modular Monolith (قابل للتفكيك لاحقاً) |
| **الفريق المستهدف** | 2-5 مطورين |
| **MVP Duration** | 10-12 أسبوع |
| **المقياس الأساسي** | D7 Retention + Daily Active Lessons |

> ⚠️ **افتراض**: الموضوع (المجال) قابل للتغيير — النظام مصمم **generic** بحيث الموضوع يُعرَّف عبر Content Configuration مش عبر كود.

---

## 2. Functional Requirements

### 2.1 User Management
- **FR-1.1**: تسجيل عبر Email/Google/Apple (OAuth 2.0)
- **FR-1.2**: Guest mode مع إمكانية الترقية لحساب
- **FR-1.3**: Profile: `display_name`, `avatar`, `timezone`, `daily_goal`, `notification_prefs`
- **FR-1.4**: حذف الحساب (GDPR) — soft delete + 30 يوم grace period

### 2.2 Content System
- **FR-2.1**: هيكل هرمي: `Course → Unit → Skill → Lesson → Exercise`
- **FR-2.2**: دعم 5 أنواع تمارين في MVP:
  1. `MULTIPLE_CHOICE`
  2. `FILL_IN_BLANK`
  3. `MATCH_PAIRS`
  4. `ORDER_SEQUENCE`
  5. `LISTENING` (optional)
- **FR-2.3**: كل Exercise له `difficulty_score` (1-10) و `skill_tags[]`
- **FR-2.4**: Content versioning — immutable snapshots
- **FR-2.5**: Content يُقدَّم عبر CDN كـ JSON bundles (offline-capable)

### 2.3 Learning Engine
- **FR-3.1**: Session Generator يبني lesson من 8-12 exercise بناءً على:
  - mastery الحالية
  - أخطاء سابقة (SM-2)
  - difficulty target
- **FR-3.2**: تسجيل كل إجابة: `{exercise_id, user_answer, is_correct, time_ms, timestamp}`
- **FR-3.3**: mastery لكل skill: `0.0 → 1.0` (weighted accuracy + recency)
- **FR-3.4**: Hearts: 5 قلوب، تُفقد عند الخطأ، regen كل 4 ساعات (1 قلب)

### 2.4 Gamification
- **FR-4.1**: XP — 10 عادي، +5 perfect، +2 combo
- **FR-4.2**: Streak — يُحسب حسب **timezone المستخدم**
- **FR-4.3**: Leaderboard أسبوعي — مجموعات 30، يتجدد الأحد 00:00 UTC
- **FR-4.4**: Achievements — declarative JSON rules
- **FR-4.5**: Daily Goal قابل للتخصيص (10/20/30/50 XP)

### 2.5 Notifications
- **FR-5.1**: Streak reminder (قبل انتهاء اليوم بـ 2-4 ساعات)
- **FR-5.2**: Re-engagement (24/48/72 ساعة)
- **FR-5.3**: Leaderboard results (نهاية الأسبوع)
- **FR-5.4**: احترام quiet hours + timezone

### 2.6 Admin & Experimentation
- **FR-6.1**: Admin panel (web) لرفع/مراجعة المحتوى
- **FR-6.2**: Feature flags per-user
- **FR-6.3**: A/B test assignment service

---

## 3. Non-Functional Requirements

| المعيار | الهدف |
|---|---|
| **API Latency** (p95) | < 200ms read، < 500ms write |
| **Lesson Load Time** | < 1.5s (cached) |
| **Uptime** | 99.5% (MVP) → 99.9% (scale) |
| **Concurrent Users** | 10K (MVP) → 500K (target) |
| **Offline Support** | Lessons قابلة للتحميل offline |
| **Data Retention** | Events: 2 سنة، User data: lifetime |
| **Security** | OWASP Top 10، JWT rotation، rate limiting |
| **Localization** | RTL support من اليوم الأول |

---

## 4. Architecture

### 4.1 High-Level Diagram

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENTS                                                     │
│  ┌──────────┐  ┌──────────┐  ┌────────────────┐            │
│  │ Flutter  │  │ Flutter  │  │ React Admin    │            │
│  │  iOS     │  │ Android  │  │   (Web)        │            │
│  └────┬─────┘  └────┬─────┘  └───────┬────────┘            │
└───────┼─────────────┼────────────────┼──────────────────────┘
        │             │                │
        ▼             ▼                ▼
┌─────────────────────────────────────────────────────────────┐
│  API GATEWAY (Kong / AWS API GW)                            │
│  - Rate limiting  - Auth  - Routing                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Core API    │  │  Content API │  │  Admin API   │
│  (NestJS)    │  │  (NestJS)    │  │  (NestJS)    │
│              │  │              │  │              │
│ - Auth       │  │ - Lessons    │  │ - CMS        │
│ - Progress   │  │ - CDN signed │  │ - Approvals  │
│ - Gamify     │  │ - Bundles    │  │ - Analytics  │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                  │
       ▼                 ▼                  ▼
┌─────────────────────────────────────────────────┐
│  DATA LAYER                                     │
│  ┌──────────┐ ┌───────┐ ┌────────┐ ┌─────────┐ │
│  │PostgreSQL│ │ Redis │ │  S3    │ │ Kafka   │ │
│  │(primary) │ │(cache)│ │(assets)│ │(events) │ │
│  └──────────┘ └───────┘ └────────┘ └────┬────┘ │
└─────────────────────────────────────────┼───────┘
                                          ▼
┌─────────────────────────────────────────────────┐
│  ASYNC WORKERS                                  │
│  ┌──────────────┐ ┌──────────────┐ ┌─────────┐ │
│  │ Notifications│ │ Leaderboard  │ │Analytics│ │
│  │  Worker      │ │  Rollover    │ │ Ingest  │ │
│  └──────────────┘ └──────────────┘ └─────────┘ │
└─────────────────────────────────────────────────┘
```

### 4.2 Tech Stack Decisions

| Layer | Choice | Justification |
|---|---|---|
| **Mobile** | Flutter 3.x | كود واحد، أداء كافي، فريق صغير |
| **Backend** | NestJS + TypeScript | Type-safety مع mobile، ecosystem ناضج |
| **DB** | PostgreSQL 16 | ACID للـ progress/gamification |
| **Cache/Leaderboard** | Redis 7 | Sorted Sets للـ leaderboards |
| **Queue** | BullMQ (Redis) → Kafka لاحقاً | بسيط للـ MVP |
| **Storage** | S3 + CloudFront | CDN للـ content bundles |
| **Auth** | Supabase Auth أو custom JWT | سرعة + تحكم |
| **Push** | Firebase Cloud Messaging | مجاني + موثوق |
| **Analytics** | PostHog (self-hosted) | Feature flags + funnels |
| **Observability** | OpenTelemetry + Grafana | Vendor-neutral |
| **Infra** | Docker + AWS ECS Fargate → EKS | مش K8s من الأول |

### 4.3 قرارات مرفوضة صراحةً
- ❌ Microservices في MVP
- ❌ GraphQL (REST + typed SDK أسرع)
- ❌ Native iOS/Android (Flutter كافي)
- ❌ MongoDB (نحتاج joins/transactions)

---

## 5. Data Model

### 5.1 PostgreSQL Schema

```sql
-- ============ USERS ============
CREATE TABLE users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email           CITEXT UNIQUE,
  auth_provider   TEXT NOT NULL,        -- 'email'|'google'|'apple'|'guest'
  auth_provider_id TEXT,
  display_name    TEXT NOT NULL,
  avatar_url      TEXT,
  timezone        TEXT NOT NULL DEFAULT 'UTC',  -- IANA tz
  locale          TEXT NOT NULL DEFAULT 'ar',
  daily_goal_xp   INT  NOT NULL DEFAULT 20,
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email) WHERE is_deleted = FALSE;

-- ============ CONTENT ============
CREATE TABLE courses (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug        TEXT UNIQUE NOT NULL,
  title       TEXT NOT NULL,
  source_lang TEXT NOT NULL DEFAULT 'ar',
  target_lang TEXT NOT NULL DEFAULT 'ar',
  version     INT  NOT NULL DEFAULT 1,
  is_published BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE units (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  course_id  UUID NOT NULL REFERENCES courses(id),
  order_idx  INT  NOT NULL,
  title      TEXT NOT NULL,
  metadata   JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE skills (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  unit_id     UUID NOT NULL REFERENCES units(id),
  order_idx   INT  NOT NULL,
  slug        TEXT NOT NULL,
  title       TEXT NOT NULL,
  prerequisites UUID[] NOT NULL DEFAULT '{}',
  metadata    JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE lessons (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  skill_id     UUID NOT NULL REFERENCES skills(id),
  order_idx    INT  NOT NULL,
  title        TEXT NOT NULL,
  exercises    JSONB NOT NULL,
  difficulty   SMALLINT NOT NULL CHECK (difficulty BETWEEN 1 AND 10),
  version      INT NOT NULL DEFAULT 1,
  is_published BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============ PROGRESS ============
CREATE TABLE user_lesson_progress (
  user_id      UUID NOT NULL REFERENCES users(id),
  lesson_id    UUID NOT NULL REFERENCES lessons(id),
  status       TEXT NOT NULL DEFAULT 'available',
  best_score   SMALLINT NOT NULL DEFAULT 0,
  attempts     INT NOT NULL DEFAULT 0,
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (user_id, lesson_id)
);

CREATE TABLE user_skill_mastery (
  user_id     UUID NOT NULL REFERENCES users(id),
  skill_id    UUID NOT NULL REFERENCES skills(id),
  mastery     REAL NOT NULL DEFAULT 0,
  last_seen   TIMESTAMPTZ,
  next_review TIMESTAMPTZ,
  ease_factor REAL NOT NULL DEFAULT 2.5,
  interval_days INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE exercise_attempts (
  id           BIGSERIAL PRIMARY KEY,
  user_id      UUID NOT NULL,
  exercise_id  UUID NOT NULL,
  lesson_id    UUID NOT NULL,
  is_correct   BOOLEAN NOT NULL,
  user_answer  JSONB,
  time_ms      INT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_attempts_user_time ON exercise_attempts(user_id, created_at DESC);

-- ============ GAMIFICATION ============
CREATE TABLE user_stats (
  user_id         UUID PRIMARY KEY REFERENCES users(id),
  total_xp        BIGINT NOT NULL DEFAULT 0,
  current_streak  INT NOT NULL DEFAULT 0,
  longest_streak  INT NOT NULL DEFAULT 0,
  last_lesson_date DATE,
  gems            INT NOT NULL DEFAULT 0,
  hearts          SMALLINT NOT NULL DEFAULT 5,
  hearts_refilled_at TIMESTAMPTZ,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE xp_events (
  id         BIGSERIAL PRIMARY KEY,
  user_id    UUID NOT NULL,
  amount     INT NOT NULL,
  reason     TEXT NOT NULL,
  source_id  UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_xp_user_date ON xp_events(user_id, created_at);

CREATE TABLE streak_freezes (
  user_id     UUID NOT NULL,
  purchased_at TIMESTAMPTZ NOT NULL,
  used_at     TIMESTAMPTZ,
  used_for_date DATE
);

-- ============ LEADERBOARD ============
CREATE TABLE leaderboard_groups (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  week_start  DATE NOT NULL,
  tier        TEXT NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE leaderboard_members (
  group_id  UUID NOT NULL REFERENCES leaderboard_groups(id),
  user_id   UUID NOT NULL,
  xp_this_week INT NOT NULL DEFAULT 0,
  PRIMARY KEY (group_id, user_id)
);

-- ============ FLAGS / EXPERIMENTS ============
CREATE TABLE feature_flags (
  key         TEXT PRIMARY KEY,
  enabled     BOOLEAN NOT NULL DEFAULT FALSE,
  rollout_pct SMALLINT NOT NULL DEFAULT 0,
  target_rules JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE experiments (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key         TEXT UNIQUE NOT NULL,
  variants    JSONB NOT NULL,
  status      TEXT NOT NULL DEFAULT 'draft',
  started_at  TIMESTAMPTZ,
  ended_at    TIMESTAMPTZ
);

CREATE TABLE experiment_assignments (
  experiment_id UUID NOT NULL REFERENCES experiments(id),
  user_id       UUID NOT NULL,
  variant       TEXT NOT NULL,
  assigned_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (experiment_id, user_id)
);
```

### 5.2 ملاحظات حرجة
- `exercises` كـ JSONB → مرنة + immutable snapshots
- `xp_events` **append-only** — المصدر الوحيد للحقيقة
- `user_skill_mastery` تستخدم SM-2 (ease_factor / interval / next_review)

---

## 6. API Specification

### 6.1 Conventions
- Base: `https://api.example.com/v1`
- Auth: `Authorization: Bearer <JWT>` (access 15min + refresh 30d rotation)
- Errors: `{ "error": { "code": "STRING", "message": "...", "details": {} } }`
- Idempotency: header `Idempotency-Key` للـ POST الحساسة
- Pagination: cursor-based `?cursor=&limit=`
- Versioning: header `X-Client-Version`

### 6.2 Endpoints

```yaml
# --- Auth ---
POST   /auth/register
POST   /auth/login
POST   /auth/oauth/google
POST   /auth/oauth/apple
POST   /auth/refresh
POST   /auth/logout

# --- User ---
GET    /me
PATCH  /me
DELETE /me
GET    /me/stats

# --- Content (read) ---
GET    /courses
GET    /courses/:slug/tree
GET    /lessons/:id
GET    /content/bundle?since=

# --- Learning ---
POST   /lessons/:id/start
POST   /sessions/:id/answer
POST   /sessions/:id/complete
POST   /sessions/:id/abandon

# --- Gamification ---
GET    /leaderboard
POST   /streak/freeze
POST   /hearts/refill
GET    /achievements

# --- Notifications ---
POST   /devices
DELETE /devices/:token
PATCH  /me/notifications

# --- Experimentation ---
GET    /flags
POST   /events

# --- Admin ---
POST   /admin/lessons
PATCH  /admin/lessons/:id
POST   /admin/lessons/:id/publish
GET    /admin/analytics/funnel?range=
```

### 6.3 Example: `POST /sessions/:id/answer`

**Request**:
```json
{
  "exercise_id": "ex_789",
  "answer": { "choice_id": "b" },
  "time_ms": 4200
}
```

**Response**:
```json
{
  "is_correct": true,
  "correct_answer": { "choice_id": "b" },
  "feedback": "أحسنت! ✅",
  "xp_awarded": 2,
  "hearts_remaining": 5,
  "next_exercise_id": "ex_790",
  "session_progress": { "completed": 3, "total": 10 }
}
```

---

## 7. Core Algorithms

### 7.1 Session Generator

```pseudo
function generateLesson(userId, lessonId):
    lesson = fetchLesson(lessonId)
    candidates = lesson.exercises
    userMastery = getMastery(userId, lesson.skill_id)

    dueExercises = filter(candidates, ex =>
        hasSkillDueForReview(userId, ex.skill_tags))

    target = mapMasteryToDifficulty(userMastery)
    ordered = sortBy(candidates, ex => abs(ex.difficulty - target))

    selected = pickWithWeights(ordered, [0.6, 0.3, 0.1])
    return interleaveByType(selected)
```

### 7.2 Streak Update (Timezone-safe)

```pseudo
function onLessonComplete(userId, completedAt):
    user = getUser(userId)
    localDate = toDate(completedAt, user.timezone)
    stats = getStats(userId)

    if stats.last_lesson_date == localDate:
        return stats

    yesterday = localDate - 1 day
    if stats.last_lesson_date == yesterday:
        stats.current_streak += 1
    else if hasActiveFreeze(userId, yesterday):
        consumeFreeze(userId, yesterday)
        stats.current_streak += 1
    else:
        stats.current_streak = 1

    stats.last_lesson_date = localDate
    stats.longest_streak = max(stats.longest_streak, stats.current_streak)
    save(stats)
    publishEvent('streak.updated', {userId, streak: stats.current_streak})
```

### 7.3 SM-2 Spaced Repetition

```pseudo
function updateMastery(userId, skillId, quality /* 0-5 */):
    m = getMastery(userId, skillId)

    if quality < 3:
        m.repetitions = 0
        m.interval = 1
    else:
        if m.repetitions == 0: m.interval = 1
        elif m.repetitions == 1: m.interval = 6
        else: m.interval = round(m.interval * m.ease)
        m.repetitions += 1

    m.ease = max(1.3, m.ease + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    m.next_review = now() + m.interval days
    m.mastery = clamp(quality / 5, 0, 1)
    save(m)
```

### 7.4 Leaderboard Assignment

```pseudo
// Cron: Sunday 00:00 UTC
function assignLeaderboardGroups(weekStart):
    users = getActiveUsers(last 14 days)
    bucketizeByXP(users, tiers: bronze|silver|gold)
    for each tier:
        chunks = chunk(shuffle(tierUsers), size=30)
        for chunk in chunks:
            createGroup(weekStart, tier, chunk)

// XP updates → Redis sorted set:
// ZINCRBY lb:{group_id}:{week} {xp} {user_id}
```

### 7.5 Hearts Regeneration (Lazy)

```pseudo
function getCurrentHearts(userId):
    stats = getStats(userId)
    if stats.hearts >= 5: return 5
    elapsed = now() - stats.hearts_refilled_at
    regen = floor(elapsed / 4h)
    return min(5, stats.hearts + regen)

function consumeHeart(userId):
    current = getCurrentHearts(userId)
    if current == 0: throw HEARTS_EXHAUSTED
    setHearts(userId, current - 1, refill_anchor=now())
```

---

## 8. Mobile App Structure (Flutter)

```
lib/
├── main.dart
├── app/
│   ├── router.dart              # go_router
│   ├── theme/
│   └── di/                      # riverpod providers
├── core/
│   ├── api/                     # dio client + interceptors
│   ├── storage/                 # drift (sqlite) + secure storage
│   ├── analytics/               # posthog wrapper
│   └── errors/
├── features/
│   ├── auth/
│   │   ├── data/  domain/  presentation/
│   ├── learning/
│   │   ├── data/
│   │   │   ├── session_repository.dart
│   │   │   └── content_sync.dart
│   │   ├── domain/
│   │   │   ├── exercise.dart          # sealed class per type
│   │   │   └── session_state.dart
│   │   └── presentation/
│   │       ├── lesson_player.dart
│   │       ├── widgets/
│   │       │   ├── multiple_choice.dart
│   │       │   ├── fill_blank.dart
│   │       │   ├── match_pairs.dart
│   │       │   └── order_sequence.dart
│   │       └── session_summary.dart
│   ├── home/
│   ├── leaderboard/
│   ├── profile/
│   └── notifications/
└── l10n/
    ├── app_ar.arb
    └── app_en.arb
```

### 8.1 Offline Strategy
- أول فتح: تنزيل content bundle (~2MB) عبر `/content/bundle`
- الإجابات تُخزَّن في Drift → batch upload عند الاتصال
- Conflict resolution: server wins للـ gamification، client wins للـ in-progress session

### 8.2 State Management
- **Riverpod 2.x** (compile-safe, testable)
- **Exercise rendering**: كل نوع `sealed class` + widget منفصل + `ExerciseRenderer` يختار حسب `runtimeType`

---

## 9. Backend Module Structure (NestJS)

```
src/
├── main.ts
├── app.module.ts
├── common/
│   ├── guards/         # JwtGuard, AdminGuard, RateLimitGuard
│   ├── interceptors/   # LoggingInterceptor, IdempotencyInterceptor
│   ├── decorators/
│   └── pipes/
├── modules/
│   ├── auth/
│   │   ├── auth.controller.ts
│   │   ├── auth.service.ts
│   │   └── strategies/
│   ├── users/
│   ├── content/
│   │   ├── content.controller.ts
│   │   ├── content.service.ts
│   │   └── bundle.builder.ts
│   ├── learning/
│   │   ├── session.controller.ts
│   │   ├── session-generator.service.ts
│   │   ├── mastery.service.ts
│   │   └── answer-evaluator.service.ts
│   ├── gamification/
│   │   ├── xp.service.ts
│   │   ├── streak.service.ts
│   │   ├── hearts.service.ts
│   │   ├── leaderboard.service.ts
│   │   └── achievements.service.ts
│   ├── notifications/
│   │   ├── push.service.ts
│   │   └── scheduler.service.ts
│   ├── experiments/
│   │   ├── flags.service.ts
│   │   └── assignment.service.ts
│   └── analytics/
│       └── events.service.ts
├── workers/
│   ├── leaderboard.rollover.worker.ts
│   ├── xp.aggregator.worker.ts
│   └── notification.sender.worker.ts
└── infra/
    ├── db/               # prisma
    ├── redis/
    └── s3/
```

- **ORM**: Prisma (type-safety + clean migrations)
- **Validation**: `class-validator` + `class-transformer`
- **Testing**: Jest + Supertest (critical paths only)

---

## 10. Event Schema

```typescript
type Event = {
  event_id: string;
  user_id: string;
  session_id?: string;
  event_name: EventName;
  properties: Record<string, any>;
  client_ts: string;
  server_ts: string;
  app_version: string;
  platform: 'ios' | 'android' | 'web';
};

type EventName =
  | 'app_opened'
  | 'lesson_started'
  | 'exercise_answered'
  | 'lesson_completed'
  | 'lesson_abandoned'
  | 'streak_extended'
  | 'streak_frozen'
  | 'heart_lost'
  | 'hearts_refilled'
  | 'leaderboard_viewed'
  | 'notification_received'
  | 'notification_opened'
  | 'paywall_shown'
  | 'purchase_completed'
  | 'experiment_exposed';
```

**الأحداث الحرجة من اليوم الأول**: `lesson_started`, `lesson_completed`, `exercise_answered`, `streak_extended`

---

## 11. Security Requirements

| المجال | المتطلب |
|---|---|
| **Auth** | JWT RS256، access 15min، refresh 30d rotating |
| **Authorization** | RBAC: `user`, `content_editor`, `admin` |
| **Rate Limiting** | Redis: 60 req/min/user، 10/min على `/auth/*` |
| **Input Validation** | Whitelist + strict typing |
| **PII** | تشفير على مستوى column |
| **Anti-cheat** | server-side validation + max XP/min |
| **Secrets** | AWS Secrets Manager / Doppler |
| **GDPR** | `/me/export` + `/me` delete مع 30 يوم purge |

---

## 12. Observability & Ops

### Metrics (Prometheus)
- `http_request_duration_seconds{route,status}`
- `lesson_completion_total{result}`
- `xp_events_processed_total`
- `streak_updates_total{type}`
- `leaderboard_rollover_duration_seconds`
- `notification_delivery_total{status}`

### Logs (structured JSON)
`request_id`, `user_id`, `route`, `latency_ms`, `error_code`

### Alerts
- p95 latency > 500ms (5 min)
- error rate > 2% (5 min)
- leaderboard rollover فشل
- Redis memory > 80%

### Health
- `/health` (liveness)
- `/health/ready` (DB + Redis + queue)

---

## 13. Testing Strategy

| Type | Coverage | Tool |
|---|---|---|
| Unit | Services الحرجة — > 80% | Jest |
| Integration | Repos + DB | Jest + Testcontainers |
| E2E | Session flow, auth flow | Supertest |
| Contract | OpenAPI schema | Schemathesis |
| Mobile | Exercise widgets | flutter_test |
| Load | 10K concurrent | k6 |

**Definition of Done لأي feature**: unit + integration + event tracking + feature flag + metrics.

---

## 14. Deployment & Environments

| Env | Purpose | Infra |
|---|---|---|
| `local` | dev | docker-compose (postgres+redis+minio) |
| `staging` | QA | AWS ECS Fargate (2 tasks) |
| `prod` | live | AWS ECS Fargate (auto-scale 3-20) |

### CI/CD (GitHub Actions)
1. Lint + type-check
2. Unit + integration tests
3. Build Docker → ECR
4. Deploy staging (auto)
5. Deploy prod (manual approval)
6. DB migrations: Prisma migrate (forward-only)

**Migration rule**: كل migration backward-compatible مع نسخة API السابقة (expand-contract).

---

## 15. Roadmap

### Sprint 1-2: Foundation
- [ ] Monorepo setup (`apps/mobile`, `apps/api`, `apps/admin`, `packages/shared-types`)
- [ ] Auth (email + guest) + `/me`
- [ ] DB schema + migrations
- [ ] Docker compose + CI pipeline

### Sprint 3-4: Content & Learning
- [ ] Content API (courses/tree/lesson)
- [ ] Session generator + answer evaluator
- [ ] Flutter: skill tree + lesson player (3 exercise types)
- [ ] Core events

### Sprint 5-6: Gamification
- [ ] XP + streak (timezone-safe)
- [ ] Hearts (lazy regen)
- [ ] Daily goal widget
- [ ] 2 exercise types إضافية

### Sprint 7-8: Social & Notifications
- [ ] Weekly leaderboard + rollover worker
- [ ] Push (FCM) + preferences
- [ ] Achievements (5 badges)

### Sprint 9-10: Hardening & Launch
- [ ] Offline content sync
- [ ] Feature flags + 2 A/B tests
- [ ] Load test + security review
- [ ] Store submission

---

## 16. MVP Definition of Done

النظام MVP-ready عندما:
1. مستخدم جديد: signup → daily goal → lesson → XP → streak=1 (بدون أخطاء)
2. Streak تاني يوم يشتغل صح مع 20/26 ساعة (timezone)
3. Leaderboard أسبوعي يظهر بمجموعة 30 + XP صحيح
4. 15 حدث تحليلي مسجّل في PostHog
5. p95 < 300ms على `/sessions/:id/answer` في load test 1K RPS
6. كل الإشعارات تحترم quiet hours + timezone

---

## 17. Out of Scope

- **Monetization details** (SKUs, paywall logic, receipts) — مستند منفصل
- **AI content generation** — v1.5
- **Server-Driven UI** — v2
- **Social (friends, feed)** — v2
- **Web app للطلاب** — mobile-first فقط في MVP

---

## 18. Instructions for AI Coding Agent

عند استلام هذا المستند، اتبع الترتيب التالي:

1. **اقرأ كامل المستند أولاً** — لا تبدأ توليد كود قبل فهم sections 5 و 7.
2. **ابدأ بـ monorepo skeleton** بالهيكل في section 9 + `packages/shared-types` يحتوي types مشتركة (Exercise, Lesson, SessionState).
3. **نفّذ DB migrations** بترتيب section 5: users → content → progress → gamification.
4. **نفّذ endpoints** بترتيب section 15. أي endpoint جديد لازم يضيف:
   - DTO + validation
   - unit test
   - event tracking call (section 10)
5. **Core algorithms (section 7) تُنفّذ كـ services معزولة مع unit tests قبل أي integration**.
6. **Flutter**: ابدأ بـ `features/learning/domain/exercise.dart` (sealed class) قبل أي UI.
7. **كل feature خلف feature flag** — لا تطلق شيئاً مفعّل 100% من أول commit.
8. **اسأل قبل الافتراض** — أضف `// TODO(clarify): ...` بدل اختراع قرار.
9. **لا تبنِ microservices أو GraphQL أو K8s** — قرارات مرفوضة صراحةً (section 4.3).
10. **PR DoD**: كود + test + event + flag + metric + doc delta.

---

## 19. Changelog

| Version | Date | Changes |
|---|---|---|
| 1.0.0 | 2026-11-14 | Initial specification |

---

**End of Document.**