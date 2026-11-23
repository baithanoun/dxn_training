# Plan 2 — Complete the DXN Training System (Admin web, Users/Teams, Mobile API + Android, Analytics, Settings, Deployment)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. **One commit per task** (the owner reviews commit by commit). Do not push, do not merge.

**Goal:** Finish the whole system on top of what Plan 1 Tasks 1–7 delivered: admin content pages fully on the API, users & teams management, the mobile API with server-side scoring/XP/certificates, the Android app wired to it, analytics/overview/settings/export, and a deployable production build for dockhosting.dev.

**Architecture:** One Laravel 12+ app `dxn-api/` (JSON REST under `/api`, Sanctum bearer tokens) is the single source of truth. Business rules (unlocking, scoring, XP, streak, certificates) live in two service classes (`ProgressService`, `ScoringService`) used by the mobile controllers. The React admin (`dxn-admin-web/`) talks only through `src/lib/api.ts`. The Android app (`DXNTrainingApp/`, Kotlin/Compose/Retrofit) keeps its screens and swaps mock state for API state.

**Tech Stack:** PHP 8.3, Laravel (installed), Sanctum, MySQL 8.4 (Laragon, dev) / MySQL (prod), PHPUnit on SQLite `:memory:`; React 19 + TS + Vite + Tailwind + Recharts (installed, no new npm deps); Kotlin 1.9.24, Compose, Retrofit 2.11, OkHttp 4.12, Gson; AndroidX DataStore Preferences (the one new dependency allowed).

**Spec:** `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md` (binding — esp. §5.3 and §5.9). Plan 1 (`docs/superpowers/plans/2026-11-21-plan1-api-foundation-and-content-admin.md`) Tasks 8–10 are **superseded** by Tasks A1–A3 below.

---

## 0. Current state (read before Task A1)

Branch `feat/plan1-api-content` (local, not pushed). Commits so far:

| Commit | Content |
|---|---|
| 5fef70a | Laravel scaffold, Sanctum, users table (role, phone, dxn_id encrypted + dxn_id_hash, sponsor_id, total_xp, current_streak, last_activity_date, hearts, gems, is_active), `role` middleware, `/api/auth/login|logout|me`, AdminSeeder |
| 6986914 | Tables/models: sections, skills, levels (soft deletes), lesson_cards, game_rounds, round_options |
| 25834dd | ContentSeeder from `database/seeders/data/content.json` (5 sections, 11 levels, 16 cards, 11 rounds, 25 options; all published; last level per section `is_final_quiz`) |
| ab0fd78, 36b5c4b | `/api/admin` content tree, CRUD sections/skills/levels, `PUT levels/{id}/cards`, `POST reorder` |
| 76dca0b | `PUT /api/admin/levels/{id}/rounds` with validation |
| 08f0d29 | `POST /api/admin/media` (image ≤ 5 MB, public disk) |
| 19ac5cd | React: `src/lib/api.ts`, `src/lib/auth.tsx`, `src/lib/types.ts`, `src/pages/Login.tsx`, `RequireAuth` |

35 PHPUnit tests pass. **Uncommitted:** `dxn-admin-web/src/pages/Content.tsx` has an unreviewed, interrupted rewrite (≈850 lines changed) — handled in Task A1.

Existing patterns to reuse (read these files once; do not reinvent):
- `dxn-api/app/Http/Requests/Admin/AdminRequest.php` — base FormRequest: `req()` (required on POST, sometimes on PUT) and `columns()` (camelCase → snake_case).
- `dxn-api/app/Http/Resources/Admin/*Resource.php` — `$wrap = null`, string ids.
- `dxn-api/app/Http/Controllers/Admin/LevelController.php` — transaction + replace pattern.
- `dxn-api/tests/Feature/Admin/ContentCrudTest.php` — how admin/trainee users and tokens are created in tests.
- `dxn-admin-web/src/lib/api.ts` — `api<T>(path, {method, json, body})`, throws `ApiError {status, message, errors}`; 401 → clears token → `/login`.
- `/api/auth/me` returns `UserResource` **wrapped** in `{data: …}` (React unwraps it). Every NEW endpoint returns unwrapped JSON (`->resolve()` or `$wrap = null`).

## Global Constraints
- JSON keys **camelCase**; every id/foreign id serialized as **string**.
- Roles stored `admin|leader|trainee`; serialized `ADMIN|LEADER|TRAINEE`.
- Level `type` ∈ `lesson | quiz | chat_battle | match_pairs | order_sequence`. Content status ∈ `draft | published`. Mobile sees **published** and non-trashed only.
- The server computes score, pass, XP, unlocks, streak and certificates. Client-sent `score`/`passed` are **ignored**; old body shape `{userId, score, passed}` without `answers` → **422**.
- Score is an integer **0–100**.
- `/api/users/{userId}/…` is allowed only when `userId` = current user, or current user is that user's leader (`sponsor_id`), or current user is admin → otherwise **403**.
- Admin web (`/api/admin/*`) is **admin-only**. Leaders use the mobile app (`/api/leader/team`).
- Trainee links to leader by the **leader's DXN membership number** (`leaderDxnId`), looked up via `dxn_id_hash` among `role=leader`; not found → **422** `errors.leaderDxnId`.
- Registration creates `trainee` only. Email unique, phone unique, at least one of them required; login by either.
- Hearts are per-attempt on the device (3); the server does not enforce them.
- XP: first pass → `xp_reward`; first pass **on the first attempt** → `round(xp_reward × xp_first_try_multiplier)`; later pass with a **higher** score than best → `floor(xp_reward × xp_replay_ratio)`; otherwise 0. Defaults: `xp_first_try_multiplier = 1.5`, `xp_replay_ratio = 0.25` (stored in `settings`).
- Streak: on any completion request: `last_activity_date` = today → unchanged; = yesterday → +1; else → 1. Then set today. Use `now()->toDateString()` with `config('app.timezone')`.
- Unlock order inside a section: skills by `order_index`, then levels by `order_index` (published only). First level of an unlocked section is unlocked. A level is unlocked if the previous one in that order is completed. A section is unlocked if it has no `required_section_id`, or the user holds a certificate for it.
- Certificate is issued when the user passes the section's `is_final_quiz` level (if none is flagged, the last level in order). One certificate per (user, section).
- No new npm dependencies. No new Composer dependencies. Android: only `androidx.datastore:datastore-preferences:1.1.1` may be added.
- Every new PHP endpoint has PHPUnit feature tests. React tasks verify with `npm run build` + the listed manual browser checks. Android tasks verify with `gradlew :app:assembleDebug` (JDK at `D:\webserver\appp\jdk17`) + listed manual checks on an emulator.
- Arabic UI strings exactly as written in this plan.
- Commit messages end with `Co-Authored-By: <the executing model> <noreply@anthropic.com>` (or the project's own trailer rule).

## Review Focus
1. A trainee calls `GET /api/users/{otherId}/progress` → **403**, not data (IDOR). *(Task C2 test)*
2. A completion request for a **locked** level → **409** `{message:"المستوى مقفل"}`; nothing recorded. *(Task C3 test)*
3. Submitting the same passing answers twice → second response `xpAwarded` = 0 (no XP farming); `attempts` = 2; `best_score` unchanged. *(Task C3 test)*
4. Register with a `leaderDxnId` that belongs to a **trainee** (not a leader) → 422. *(Task C1 test)*
5. Android with the server **down**: content shows, finishing a level shows "لم تُحفظ النتيجة — تحقق من الاتصال", and no local XP/unlock is applied. *(Task D3 manual check)*

---

## Phase A — Finish the admin content pages (replaces Plan 1 Tasks 8–10)

### Task A1: Content page on the API

**Files:** Modify `dxn-admin-web/src/pages/Content.tsx` only.

**Interfaces (consumes, all `/api/admin`, admin token):**
- `GET content/tree` → `Section[]` (see `src/lib/types.ts`).
- `POST sections` `{title, description?, bagId?, certificateTitle?, requiredSectionId?, status?}` · `PUT sections/{id}` (same) · `DELETE sections/{id}` → 422 `{message}` when another section requires it.
- `POST skills` `{sectionId, title, icon?, status?}` · `PUT skills/{id}` `{title, icon, status}` (**never send sectionId** on PUT → 422) · `DELETE skills/{id}`.
- `POST levels` `{skillId, title, type, passThreshold?, xpReward?, isFinalQuiz?, status?}` · `PUT levels/{id}` `{title, type, passThreshold, xpReward, isFinalQuiz, status}` (**never send skillId**) · `GET levels/{id}` → `LevelDetail` · `DELETE levels/{id}`.
- `PUT levels/{id}/cards` `{cards:[{title, body, tip?, mediaUrl?}]}` → `LevelDetail`.
- `POST reorder` `{type:'sections'|'skills'|'levels', parentId?, ids}` → 204; `ids` must be **all** siblings in new order.
- `POST media` multipart `file` → `{url}`.
- After any mutation: **refetch the tree** (store/update responses omit `cardsCount`/`roundsCount`).

- [ ] **Step 1: Decide about the uncommitted WIP.** Run `git diff --stat dxn-admin-web/src/pages/Content.tsx` and `npm run build`. If it builds AND satisfies every bullet of Step 2 when checked in the browser, keep it and fix gaps. Otherwise `git checkout -- dxn-admin-web/src/pages/Content.tsx` and implement from the committed version. State which in the commit body.
- [ ] **Step 2: Implement**
  - Load tree; loading text "جارٍ التحميل…", error banner with "إعادة المحاولة" button, keep the existing empty state.
  - "إضافة قسم" → inline form (title*, description, bag number, certificate title, required section select) → POST.
  - In the tree: "إضافة مهارة" per section, "إضافة مستوى" per skill (type select labels: `lesson`=درس، `quiz`=اختبار، `chat_battle`=محاكي محادثة، `match_pairs`=مطابقة، `order_sequence`=ترتيب).
  - Level panel: **controlled** inputs (title, type, passThreshold, xpReward, isFinalQuiz checkbox, status) → "حفظ التغييرات" PUT; "إلغاء" restores loaded values. Show 422 field errors under fields.
  - Cards editor: list, add, remove, move up/down, per-card image upload ("رفع صورة") → `mediaUrl` + thumbnail; "حفظ البطاقات" → PUT cards.
  - Section/skill: edit title (and section fields), publish toggle ("منشور"/"مسودة"), delete with `window.confirm("هل أنت متأكد من الحذف؟")`; API 422 `message` in a red banner.
  - Up/down arrows on sections, skills, levels → POST reorder with the full sibling list.
  - Non-lesson level panel: link "فتح في منشئ السيناريوهات" → `/scenario?levelId=<id>`.
  - No import from `@/data/mockData`.
- [ ] **Step 3:** `npm run build` → success.
- [ ] **Step 4: Manual (API `php artisan serve`, web `npm run dev`, open `http://localhost:5173`):** create section → skill → level → 2 cards with an image; reload → persisted. Reorder two skills; reload → kept. Delete a section another one requires → red banner. Edit a level's passThreshold to 150 → field error.
- [ ] **Step 5: Commit** `feat(admin-web): content management on live API`

### Task A2: Scenario Builder on the API

**Files:** Modify `dxn-admin-web/src/pages/ScenarioBuilder.tsx`.

**Interfaces:** `GET levels/{id}`, `PUT levels/{id}` (title, passThreshold, status), `PUT levels/{id}/rounds` body `{rounds:[{title?, prompt, initialTrust, options:[{text, isCorrect, isNeutral, trustImpact, feedback?, xpBonus?}]}]}`; 422 error keys like `rounds.0.options`, `rounds.0.options.1.text`.

Mapping (page local type ↔ API): `Round.title↔title`, `Round.message↔prompt`, (new local field) `Round.initialTrust↔initialTrust` default 50, `Option.type 'correct'|'wrong'|'neutral' ↔ isCorrect/isNeutral`, `Option.trust↔trustImpact`, `Option.xp↔xpBonus`, `Option.feedback↔feedback`, scenario name↔level `title`, `passThreshold`↔level `passThreshold`, status `published|draft`↔level `status`.

- [ ] **Step 1: Implement**
  - Read `levelId` from `useSearchParams()`. Without it: a picker listing non-lesson levels from `content/tree` grouped "القسم › المهارة › المستوى"; choosing one sets `?levelId=`.
  - Load level → fill state; delete the mock `initialRounds` and the `sections` mock import.
  - "حفظ مسودة": `PUT levels/{id}` (title, passThreshold) then `PUT levels/{id}/rounds`; success toast/text "تم الحفظ".
  - "نشر"/"إلغاء النشر" (keep existing confirm) → `PUT levels/{id}` `{status}`.
  - Map server 422 keys onto the matching round/option and show them next to the existing client validation and `hasXss` check.
  - Settings drawer: section/skill shown read-only from the level's location; threshold editable.
  - "تصدير" downloads current rounds as `scenario-<levelId>.json` via `Blob`; "استيراد" (Upload icon) reads such a file into state (validate it is an array of rounds; otherwise show "ملف غير صالح").
- [ ] **Step 2:** `npm run build` → success.
- [ ] **Step 3: Manual:** open seeded chat_battle level, change an option's text/trust, save, reload → persisted. Remove all correct options in a round, save → error under that round. Toggle publish → persisted after reload. Export then import the same file → identical rounds.
- [ ] **Step 4: Commit** `feat(admin-web): scenario builder on live API`

---

## Phase B — Users & Teams (API + admin pages)

### Task B1: Admin users API

**Files:** Create `dxn-api/app/Http/Controllers/Admin/UserController.php`, `dxn-api/app/Http/Requests/Admin/UserRequest.php`, `dxn-api/app/Http/Resources/Admin/AdminUserResource.php`; modify `dxn-api/routes/api.php`; test `dxn-api/tests/Feature/Admin/UsersTest.php`.

**Interfaces (`/api/admin`):**
- `GET users?search=&role=&status=&leaderId=&page=&perPage=` → `{data: AdminUser[], meta: {page, perPage, total, lastPage}}` (Laravel paginator mapped to this shape; `perPage` default 20, max 100). `search` matches name/email/phone (LIKE) or exact DXN id via `dxn_id_hash`. `role` ∈ admin|leader|trainee. `status` ∈ `active` (last_activity_date ≥ today−3) | `inactive` (older or null) | `disabled` (is_active=false).
- `AdminUser` = `{id, fullName, email, phone, dxnId, role, leaderId, leaderName, totalXp, currentStreak, lastActivityDate (Y-m-d|null), isActive, certificatesCount, createdAt}`.
- `GET users/{id}` → `AdminUser` + `certificates: [{sectionId, sectionTitle, code, issuedAt}]` + `sectionsProgress: [{sectionId, title, completedLevels, totalLevels}]` (computed via `ProgressService` once Task C2 exists; until then return `[]` and fill it in C2).
- `POST users` `{fullName, email?, phone?, password (min 8), dxnId?, role, leaderId?}` → 201 `AdminUser`.
- `PUT users/{id}` `{fullName?, email?, phone?, password?, dxnId?, role?, leaderId?, isActive?}` → `AdminUser`. Setting `isActive=false` deletes all the user's tokens. `leaderId` must be a user with role leader and ≠ self. An admin cannot change their own role or deactivate themselves (422 "لا يمكنك تعديل صلاحياتك").
- `DELETE users/{id}` → not provided (deactivate instead).
- `GET users/export` → CSV download (`text/csv; charset=UTF-8` with BOM), columns: الاسم، البريد، الهاتف، رقم العضوية، الدور، القائد، XP، آخر نشاط، الحالة. Same filters as list. Register this route **before** `users/{id}`.

- [ ] **Step 1: Tests** — `test_list_paginates_and_filters_by_role_and_search()`, `test_search_by_exact_dxn_id()`, `test_status_filter_inactive_uses_3_days()`, `test_create_user_validates_unique_email_and_phone()`, `test_leader_must_have_leader_role()`, `test_deactivate_revokes_tokens()` (old token → 401 on `/api/auth/me`), `test_admin_cannot_demote_self()`, `test_export_csv_has_bom_and_header()`, `test_non_admin_forbidden()`.
- [ ] **Step 2:** run `php artisan test --filter=UsersTest` → FAIL. **Step 3:** implement (role/is_active are not mass-assignable — set them explicitly). **Step 4:** full suite PASS. **Step 5: Commit** `feat(api): admin users management`

### Task B2: Admin teams API

**Files:** Create `dxn-api/app/Http/Controllers/Admin/TeamController.php`; modify routes; test `dxn-api/tests/Feature/Admin/TeamsTest.php`.

**Interfaces:**
- `GET /api/admin/teams` → `[{leaderId, leader, members, completion, activeToday}]` — `members` = count of users with `sponsor_id = leader`; `completion` = average over members of `completedLevels / totalPublishedLevels × 100` (integer, 0 if no members); `activeToday` = members with `last_activity_date = today`.
- `GET /api/admin/teams/{leaderId}` → `{leader: AdminUser, members: AdminUser[] each + progressPercent}`.
- Until Task C2 lands, "completed levels" = count of `user_level_progress` rows with `status='completed'`; this task therefore **depends on C2's table** — implement B2 after C2 if executing strictly in order, or create the `user_level_progress` migration here (Task C2 then reuses it). Executor's choice; record it in the commit body.

- [ ] Tests: `test_team_aggregates()` (2 leaders, members with progress → exact numbers), `test_team_detail_404_for_non_leader()`. Implement, suite PASS, **Commit** `feat(api): admin teams overview`

### Task B3: Users & Teams pages

**Files:** Modify `dxn-admin-web/src/pages/Users.tsx`, `dxn-admin-web/src/pages/Teams.tsx`, `dxn-admin-web/src/lib/types.ts` (add `AdminUser`, `Paginated<T>`, `TeamSummary`).

- [ ] **Users:** table from `GET users` with search box (debounced 300 ms), role select (الكل/متدرب/قائد/مدير), status select (الكل/نشط/متوقف/معطّل), pagination ("السابق"/"التالي", "صفحة X من Y"). Role labels: TRAINEE=متدرب, LEADER=قائد, ADMIN=مدير. "إضافة مستخدم" opens a form (POST). Row click → side panel from `GET users/{id}`: edit role, leader (select from `GET users?role=leader&perPage=100`), activate/deactivate ("تعطيل"/"تفعيل" with confirm), certificates list, sections progress bars. "تصدير CSV" → fetch `users/export` with the token and save the blob as `users.csv` (cannot use a plain link because of the bearer token). Remove mock imports.
- [ ] **Teams:** cards/table from `GET teams`; click → members from `GET teams/{leaderId}`; "تصدير تقرير" → CSV built client-side from the loaded data. Remove mock import.
- [ ] `npm run build`; manual: create a leader and a trainee linked to them; deactivate the trainee; filters and export work. **Commit** `feat(admin-web): users and teams on live API`

---

## Phase C — Mobile API

### Task C1: Registration and mobile auth

**Files:** Modify `dxn-api/app/Http/Controllers/AuthController.php`, routes; create `dxn-api/app/Http/Requests/RegisterRequest.php`; test `dxn-api/tests/Feature/RegisterTest.php`.

**Interfaces:**
- `POST /api/auth/register` (throttle `5,1`) `{fullName, email?, phone?, password (min 8, confirmed not required), dxnId, leaderDxnId?}` → 201 `{token, user}` (user = `UserResource->resolve()`, unwrapped). `dxnId` required, trimmed; blank → 422; must be unique via `dxn_id_hash` → 422 `errors.dxnId` "رقم العضوية مسجّل مسبقاً". `leaderDxnId` → find `User::where('dxn_id_hash', User::hashDxnId(...))->where('role','leader')` else 422 `errors.leaderDxnId` "لم يتم العثور على قائد بهذا الرقم". Email/phone: `required_without` each other, unique.
- `POST /api/auth/login` unchanged; tokens named `mobile` when header `X-Client: android` is sent, else `dashboard`.
- Also fix parked minor: `User` saving hook normalizes blank `dxn_id` (`trim() === ''`) to null.

- [ ] Tests: `test_register_trainee_returns_token()`, `test_register_requires_email_or_phone()`, `test_duplicate_dxn_id_422()`, `test_leader_dxn_id_links_sponsor()`, `test_leader_dxn_id_of_trainee_rejected()` *(Review Focus 4)*, `test_register_cannot_set_role()` (sending `role:"admin"` is ignored → TRAINEE). **Commit** `feat(api): trainee registration with leader link`

### Task C2: Progress model, tree and read endpoints

**Files:**
- Create migrations `create_user_level_progress_table` (`user_id` FK cascade, `level_id` FK cascade, `status` string `completed|attempted`, `score` tinyint unsigned, `best_score` tinyint unsigned, `attempts` uint, `passed_final_quiz` bool, `completed_at` timestamp null, timestamps, unique(user_id, level_id)) and `create_certificates_table` (`user_id`, `section_id` FKs, `code` string unique, `issued_at` timestamp, unique(user_id, section_id)).
- Create models `UserLevelProgress`, `Certificate`; `app/Services/ProgressService.php`; controllers `app/Http/Controllers/Mobile/{TreeController,ProgressController}.php`; resources `app/Http/Resources/Mobile/{SectionTreeResource,...}` (or arrays built in the controller — executor's choice, keep shapes exact); policy-style check in `ProgressController`.
- Tests: `tests/Feature/Mobile/TreeTest.php`, `tests/Feature/Mobile/ProgressTest.php`, `tests/Unit/ProgressServiceTest.php`.

**Interfaces:**
- `ProgressService`:
  - `orderedLevels(Section $s): Collection<Level>` — published, non-trashed, skill order then level order.
  - `finalLevel(Section $s): ?Level` — flagged `is_final_quiz`, else last of `orderedLevels`.
  - `levelStatuses(User $u): array<int levelId, 'LOCKED'|'UNLOCKED'|'COMPLETED'>` — per Global Constraints unlock rules.
  - `sectionStatuses(User $u): array<int sectionId, 'LOCKED'|'UNLOCKED'|'COMPLETED'>` — COMPLETED iff certificate exists.
  - `isUnlocked(User $u, Level $l): bool`.
  - `completionPercent(User $u): int` (completed published levels / all published levels × 100).
- `GET /api/sections/tree` (auth any role) → array matching Android `SectionResponse` exactly: `{id, title, orderIndex, bagId, description, certificateTitle, requiredSectionId, skills:[{id, title, orderIndex, levels:[{id, title, orderIndex, xpReward, passThreshold, type, cards:[{id, title, body, tip, mediaUrl}], rounds:[{id, prompt, initialTrust, options:[{id, text, isCorrect, isNeutral, trustImpact, feedback, xpBonus}]}]}]}]}` — published only; options in `order_index` order.
- `GET /api/users/{userId}/progress` → `{ "<levelId>": {status, score, attempts, bestScore, sectionCompleted, passedFinalQuiz} }` for **every** published level (LOCKED ones included, zeros).
- `GET /api/users/{userId}/sections-progress` → `{ "<sectionId>": {status, certificateIssued, certificateId (code|null), completedAt (Y-m-d|null)} }`.
- `GET /api/users/{userId}/profile` → `UserResource->resolve()` (unwrapped; superset of Android `UserProfileResponse`).
- Access rule from Global Constraints → 403 otherwise; unknown user → 404.

- [ ] Also fill `sectionsProgress` and `certificates` in `GET /api/admin/users/{id}` (Task B1) using `ProgressService`, and extend `UsersTest` with one assertion for it.
- [ ] Tests: tree hides drafts/trashed and has exact keys; new user: first level of section 1 UNLOCKED, everything else LOCKED, section 1 UNLOCKED, others LOCKED; after inserting a `completed` row for level 1 → level 2 UNLOCKED; certificate for section 1 → section 2 UNLOCKED; access: self 200, own leader 200, admin 200, other trainee **403** *(Review Focus 1)*. **Commit** `feat(api): mobile tree and progress endpoints`

### Task C3: Level completion with server-side scoring

**Files:** Create `app/Services/ScoringService.php`, `app/Http/Controllers/Mobile/CompletionController.php`, `app/Http/Requests/CompletionRequest.php`, migration `create_settings_table` (`key` string primary, `value` json) + `app/Models/Setting.php` with `Setting::get(string $key, mixed $default)`; seed `xp_first_try_multiplier=1.5`, `xp_replay_ratio=0.25`. Tests `tests/Unit/ScoringServiceTest.php`, `tests/Feature/Mobile/CompletionTest.php`.

**Interfaces:**
- `POST /api/levels/{levelId}/complete` (auth; throttle `30,1`). Body by level type:
  - `lesson`: `{}` (answers optional/ignored) → score 100.
  - `quiz` | `match_pairs`: `{answers:[{roundId, optionId}]}` — one per round; score = round(correct / rounds × 100); pass = score ≥ `pass_threshold`.
  - `chat_battle`: `{answers:[{roundId, optionId}]}` — trust = `initial_trust` of the **first** round + Σ `trust_impact` of chosen options, clamped 0–100; score = trust; pass = trust ≥ `pass_threshold`.
  - `order_sequence`: `{answers:[{roundId, orderedOptionIds:[...]}]}` — per round correct iff the ids equal the options ordered by `order_index`; score = round(correct rounds / rounds × 100).
  - Validation: every `roundId` belongs to the level, every `optionId` belongs to its round, no duplicate rounds, all rounds answered → else 422. Missing `answers` for non-lesson → 422.
- `ScoringService::score(Level $level, array $answers): int` (0–100) and `passed(Level $level, int $score): bool`.
- Controller flow (one DB transaction, lock the progress row `lockForUpdate`):
  1. Level must be published and `ProgressService::isUnlocked` → else **409** `{message:"المستوى مقفل"}` *(Review Focus 2)*.
  2. Score/pass; upsert progress: attempts+1, score, best_score=max, status `completed` if passed or already completed else `attempted`, completed_at set on first pass, passed_final_quiz if final level passed.
  3. XP per Global Constraints → add to `users.total_xp`.
  4. Streak per Global Constraints.
  5. If passed and level is the section's final level and no certificate → create with `code = 'DXN-'.strtoupper(Str::random(10))`.
  6. Write `analytics_events` row `level_completed` or `level_failed` (table from Task E1 — if E1 is not done yet, create its migration here and E1 reuses it).
- Response: `{success: true, score, passed, xpAwarded, totalXp, currentStreak, certificateIssued, certificateTitle (section certificate_title|null), certificateId (code|null)}`.

- [ ] Tests (unit): each type scored correctly incl. chat clamp and order mismatch. Tests (feature): first-try pass gives `round(40×1.5)=60`; second identical pass gives 0 *(Review Focus 3)*; improved replay gives `floor(40×0.25)=10`; fail gives 0 and status stays/attempted; locked level 409 with no row; final level pass issues certificate once and next section unlocks; streak yesterday→+1, today→same, older→1 (use `Carbon::setTestNow`); old body `{score:100, passed:true}` → 422; foreign optionId → 422. **Commit** `feat(api): server-side level completion, XP, streak, certificates`

### Task C4: Leader team endpoint and events intake

**Files:** `app/Http/Controllers/Mobile/{LeaderTeamController,EventController}.php`; tests `tests/Feature/Mobile/{LeaderTeamTest,EventsTest}.php`.

**Interfaces:**
- `GET /api/leader/team` (role leader) → `[{id, name, progressPercent, lastActiveDays, status}]` matching Android `TeamMember`; `status`: `COMPLETED` if progressPercent = 100, `ACTIVE` if lastActiveDays ≤ 3, else `INACTIVE`; `lastActiveDays` = days since `last_activity_date` (999 if null).
- `POST /api/events` (auth; throttle `60,1`) `{events:[{eventName, levelId?, payload?, deviceInfo?, occurredAt?}]}` max 100; `eventName` ∈ `app_opened | level_started | question_answered | share_achievement | certificate_downloaded`; unknown names rejected 422. `level_completed/level_failed` are written only by the server (C3). → 202 `{accepted: n}`.
- [ ] Tests: trainee → 403 on leader/team; aggregates/status exact; events validation and insertion. **Commit** `feat(api): leader team and analytics events intake`

---

## Phase D — Android app on the API

Build: `cd DXNTrainingApp && set JAVA_HOME=D:\webserver\appp\jdk17 && gradlew :app:assembleDebug`. Emulator reaches the dev API at `http://10.0.2.2:8000/` (`php artisan serve --host=0.0.0.0`).

### Task D1: Auth, token storage, real user id

**Files:** `app/build.gradle.kts` (add `implementation("androidx.datastore:datastore-preferences:1.1.1")`; debug `buildConfigField BASE_URL "http://10.0.2.2:8000/"`, release keeps a placeholder to be set in Task F1); `app/src/debug/AndroidManifest.xml` or `res/xml/network_security_config.xml` allowing cleartext **only** to `10.0.2.2` in debug; create `data/local/SessionStore.kt`; modify `data/remote/ApiClient.kt` (interceptor adds `Authorization: Bearer <token>` and `X-Client: android`; on 401 clears session and emits a logout event), `data/remote/ApiService.kt` (+ `login`, `register`, `logout`), `ui/viewmodel/AuthViewModel.kt`, `ui/screens/LoginScreen.kt`, `ui/screens/RegisterScreen.kt`, navigation start destination.

**Interfaces:**
- `SessionStore(context)`: `val session: Flow<Session?>`, `suspend fun save(token: String, userId: String, role: String)`, `suspend fun clear()`. `data class Session(val token: String, val userId: String, val role: String)`.
- `ApiService`: `@POST("api/auth/login") login(@Body LoginRequest(login, password)): Response<AuthResponse>`, `@POST("api/auth/register") register(@Body RegisterRequest(fullName, email?, phone?, password, dxnId, leaderDxnId?)): Response<AuthResponse>`, `@POST("api/auth/logout") logout(): Response<Unit>`. `AuthResponse(token: String, user: UserProfileResponse)`. Make `UserProfileResponse.email` and `phone` nullable.
- `AuthViewModel(api, sessionStore)`: `state: StateFlow<AuthUiState>` (`Idle | Loading | Error(message) | LoggedIn`), `login(login, password)`, `register(...)`, `logout()`. Error text: show server `message`/first field error; network failure → "تعذر الاتصال بالخادم".
- LoginScreen: bind the two fields, disable button while loading, show error. RegisterScreen: fields الاسم الكامل، البريد/الهاتف (contains `@` → email else phone)، **كلمة المرور (new field)**، رقم عضوية DXN، **رقم عضوية القائد (اختياري)** (rename label from "كود القائد").
- App start: if a session exists → main screen; else onboarding/login.
- Replace `DEFAULT_USER_ID` usages with the session `userId` (pass it into `ContentRepository.loadContent(userId)` and `UserRepository.loadUserProfile(userId)`).
- [ ] Build passes; manual: register new trainee with the seeded leader's DXN number (create a leader in admin first), wrong leader number shows the server message, login/logout, app restart keeps session. **Commit** `feat(android): real auth with token storage`

### Task D2: Content, progress and completion from the server

**Files:** `data/repository/ContentRepository.kt`, `data/remote/ApiService.kt`, `data/remote/NetworkModels.kt`, `ui/screens/{QuizScreen,ChatBattleScreen,MatchPairsScreen,OrderSequenceScreen,CompletionScreen,LessonScreen}.kt`, `ui/viewmodel/HomeViewModel.kt`.

**Interfaces:**
- `LevelCompletionRequest(val answers: List<AnswerDto>)`; `AnswerDto(roundId: String, optionId: String? = null, orderedOptionIds: List<String>? = null)` (Gson omits nulls).
- `LevelCompletionResponse(success, score, passed, xpAwarded, totalXp, currentStreak, certificateIssued, certificateTitle?, certificateId?)`.
- `ContentRepository.submitLevelCompletion(levelId: String, answers: List<AnswerDto>): CompletionResult` where `sealed class CompletionResult { data class Ok(val response: LevelCompletionResponse); data class Locked(val message: String); object Offline; data class Failed(val message: String) }`. On `Ok`: call `refreshAll(userId)` (reload progress + section progress from the server) and update `UserRepository` totals from the response. **Delete** the local unlock/certificate logic in `updateLevelProgress` (`canSectionBeUnlocked`, `computeSectionStatus` as a source of truth, `issueCertificateIfNeeded`, `recomputeAllSections`) — statuses come only from the server maps; keep only what the UI needs to read them.
- Each game screen collects answers: Quiz/MatchPairs/ChatBattle → `(roundId, selectedOptionId)` per round; OrderSequence → `(roundId, orderedIds)`; Lesson → empty list. They call a ViewModel/repository `submit` and navigate to `CompletionScreen` with the **server** `score`, `xpAwarded`, `passed`, certificate fields. The screens keep local hearts/feedback UI exactly as now.
- Offline/failed: `CompletionScreen` shows "لم تُحفظ النتيجة — تحقق من الاتصال" with "إعادة المحاولة" (resubmits the same answers); no local XP/unlock *(Review Focus 5)*. Locked → show the message.
- `MockData` remains only as display fallback for content when the tree cannot load (banner "وضع غير متصل — المحتوى للعرض فقط").
- [ ] Build; manual on emulator against local API: pass level 1 → level 2 unlocks after refresh, XP increases by the server amount; repeat same level → XP +0; stop the API → finish a level → offline message, no unlock; restart API → "إعادة المحاولة" succeeds. **Commit** `feat(android): server-authoritative progress and completion`

### Task D3: Profile, team, events

**Files:** `data/repository/UserRepository.kt`, `data/repository/TeamRepository.kt`, `ui/viewmodel/{ProfileViewModel,TeamViewModel}.kt`, `ui/screens/{ProfileScreen,TeamScreen}.kt`, `ApiService.kt`, small `data/remote/EventSender.kt`.

- `TeamRepository(api)`: `suspend fun load(): Result<List<TeamMember>>` from `GET api/leader/team`; TeamScreen shown only when session role = LEADER (hide the entry otherwise); error/empty states "لا يوجد أعضاء بعد".
- Profile: values from `/api/users/{id}/profile`; certificates list from sections-progress (`certificateIssued`); logout button → `AuthViewModel.logout()`.
- `EventSender.send(name: String, levelId: String? = null, payload: Map<String, Any?> = emptyMap())` — fire-and-forget batch POST `api/events`; send `app_opened` on start, `level_started` when a game screen opens. Failures ignored (no offline queue — spec non-goal).
- Remove remaining `MockData.currentUser` / `MockData.teamMembers` usages outside the offline content fallback.
- [ ] Build; manual: leader sees team with the trainee from D1; trainee does not see team; events rows appear in `analytics_events`. **Commit** `feat(android): profile, leader team and events from API`

---

## Phase E — Analytics, Overview, Settings, Certificates

### Task E1: Analytics events table and admin dashboard API

**Files:** migration `create_analytics_events_table` (`id`, `user_id` FK null, `event_name` string(50) index, `level_id` FK null, `payload` json null, `device_info` string null, `created_at` index) unless already created in C3; `app/Http/Controllers/Admin/DashboardController.php`; tests `tests/Feature/Admin/DashboardTest.php`.

**Interfaces (`/api/admin`, all accept `?from=Y-m-d&to=Y-m-d`, default last 30 days):**
- `GET dashboard/overview` → `{stats:[{title, value, change, changeType:'up'|'down', icon}], activity:[{date (Y-m-d), sessions, activeUsers}], failedSkills:[{name, failureRate}], inactiveUsers:[{id, name, lastActive, xp, role}], certificatesBySection:[{sectionId, title, issuedCount}], bagsProgress:[{sectionId, title, percent}]}`.
  - stats (exact titles): "المستخدمون النشطون اليوم" (distinct users with any event today, icon `Users`), "معدل الإكمال العام" (avg `completionPercent` of trainees, `"68.4%"` format, icon `BarChart3`), "متوسط طول الـ Streak" (avg current_streak, `"5.2 يوم"`, icon `Flame`), "الشهادات الصادرة اليوم" (icon `Award`). `change` = % vs previous equal-length period (0 if no prior data).
  - activity: per day `sessions` = count `app_opened`, `activeUsers` = distinct users with any event.
  - failedSkills: top 5 skills by `level_failed / (level_completed+level_failed)` %.
  - inactiveUsers: trainees with last_activity_date older than 3 days, top 10 by oldest.
- `GET analytics?from&to&sectionId&leaderId` → `{summary:[{title, value, change}] (titles: "Completion Rate", "Average Time", "First-Attempt Success", "Drop-off Rate"), funnel:[{name, value}] (names: "فتح المستوى" = level_started, "إكمال الدرس" = distinct users with any completion attempt, "اجتياز الاختبار" = level_completed, "المستوى التالي" = users who started a later level), failedQuestions:[{id (roundId), question (prompt), attempts, errorRate, avgTime}], retention:[{day:"اليوم 1"|"اليوم 7"|"اليوم 30", rate}]}`.
  - failedQuestions comes from `question_answered` events with payload `{roundId, optionId, correct: bool, timeMs}`; `avgTime` formatted `"12 ث"`.
  - Retention D-n = % of users registered n+ days ago who had any event on day n after registration.
  - "Average Time" from `timeMs` sums per level session if present, else "—".
- `GET analytics/export?...` → CSV of failedQuestions + funnel (UTF-8 BOM).
- `GET certificates` → `[{sectionId, title, issuedCount, isActive}]` (isActive = section published).
- [ ] Tests with seeded events produce exact numbers for each block; empty DB returns zeros/empty arrays (no division by zero). **Commit** `feat(api): dashboard and analytics endpoints`

### Task E2: Overview & Analytics pages

**Files:** `dxn-admin-web/src/pages/Overview.tsx`, `dxn-admin-web/src/pages/Analytics.tsx`, `src/lib/types.ts`.
- [ ] Replace every `@/data/mockData` import with `GET dashboard/overview` / `GET analytics`; keep existing charts/layout; date range selector (آخر 7 أيام / 30 يوماً / 90 يوماً) mapping to `from/to`; analytics filters section and leader (from `content/tree` and `users?role=leader`); "تصدير" downloads `analytics/export` via blob. Loading/error/empty states. After this, delete `src/data/mockData.ts` if nothing imports it (`grep -r mockData src`).
- [ ] `npm run build`; manual: numbers change after completing levels on the emulator. **Commit** `feat(admin-web): overview and analytics on live API`

### Task E3: Settings API and page

**Files:** `app/Http/Controllers/Admin/SettingsController.php`, test `tests/Feature/Admin/SettingsTest.php`; `dxn-admin-web/src/pages/Settings.tsx`.
- `GET /api/admin/settings` → object of all keys with defaults; `PUT /api/admin/settings` partial object, validated whitelist:
  `app_name` string ≤ 100 (default "DXN Training"), `default_language` in `ar` (only), `maintenance_mode` bool, `xp_lesson_default` int 0–1000 (20), `hearts_default` int 1–10 (3), `heart_refill_hours` int 1–48 (4), `streak_reminder_days` int 1–30 (2), `xp_first_try_multiplier` numeric 1–3 (1.5), `xp_replay_ratio` numeric 0–1 (0.25), `notify_streak` / `notify_reengage` / `notify_leagues` / `notify_leaders` bool (true), `session_minutes` int 15–1440 (60), `quiz_rate_limit_per_minute` int 1–120 (10). Unknown keys → 422.
- Wire: `session_minutes` → Sanctum token expiry for **dashboard** tokens (`expires_at` on createToken); `quiz_rate_limit_per_minute` → the completion route limiter (`RateLimiter::for('completion', …)` reading the setting); `xp_*` already used by C3; `maintenance_mode` → mobile routes return 503 `{message:"التطبيق تحت الصيانة"}` (admin routes unaffected). Notification/leagues/heart refill keys are stored only (features are spec non-goals) — label them in the UI with a small "(قريباً)".
- "إدارة المدراء" card: list admins (`GET users?role=admin`), "إضافة مدير" → POST users with role admin.
- "حفظ الإعدادات" → PUT; success text "تم حفظ الإعدادات".
- [ ] Tests: defaults returned; invalid value 422; unknown key 422; maintenance blocks `/api/sections/tree` but not `/api/admin/*`; completion limiter honors the setting. Build + manual. **Commit** `feat: settings API and page`

---

## Phase F — Production build and deployment

### Task F1: Production build & deploy kit

**Files:** `dxn-api/routes/web.php` (SPA fallback), `dxn-admin-web/vite.config.ts` (`base: '/admin/'`), `dxn-admin-web/src/App.tsx` (`<BrowserRouter basename="/admin">`), `deploy/Dockerfile`, `deploy/nginx.conf` or Apache vhost, `deploy/entrypoint.sh`, `deploy/README.md` (Arabic), root `.dockerignore`.

- [ ] **Step 1: Confirm the host.** Open https://app.dockhosting.dev/ docs (or ask the owner) and record in `deploy/README.md`: does it run a Dockerfile, which PHP version, managed MySQL or own container, persistent volume for `storage/`, env var UI, cron/queue availability. **If the host does not accept Docker, stop and report** — do not guess another target.
- [ ] **Step 2: Admin served by Laravel.** Build React with `VITE_API_URL=""` (same origin) into `dxn-api/public/admin/` (script `npm run build -- --outDir ../dxn-api/public/admin --emptyOutDir`). `routes/web.php`: `Route::get('/admin/{any?}', fn () => response()->file(public_path('admin/index.html')))->where('any', '.*');`. Add `dxn-api/public/admin/` to `.gitignore` (built in the image, not committed).
- [ ] **Step 3: Dockerfile** (multi-stage): stage 1 `node:22-alpine` builds `dxn-admin-web`; stage 2 `composer:2` installs `--no-dev --optimize-autoloader`; final `php:8.3-apache` (or the host's required base) with extensions `pdo_mysql`, `mbstring`, `intl`, `opcache`, `gd`, `zip`; document root `dxn-api/public`; `a2enmod rewrite`; copy `public/admin` from stage 1. `entrypoint.sh`: `php artisan config:cache && php artisan route:cache && php artisan migrate --force && php artisan storage:link || true` then start Apache. **Never** run `db:seed` automatically; document a one-time `php artisan db:seed --class=AdminSeeder` and `--class=ContentSeeder`.
- [ ] **Step 4: Production env template** `deploy/.env.production.example`: `APP_ENV=production`, `APP_DEBUG=false`, `APP_URL=https://<domain>`, `DB_*`, `FRONTEND_URL=https://<domain>`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` (set once, then remove), `SESSION_DRIVER=database`, `LOG_CHANNEL=stderr`. CORS: same origin, so `FRONTEND_URL` = app URL.
- [ ] **Step 5: Android release URL:** release `BASE_URL` = `"https://<domain>/"` via `buildConfigField` in the `release` build type; cleartext stays debug-only.
- [ ] **Step 6: Verify locally:** `docker build -f deploy/Dockerfile .` if Docker is available; otherwise verify `npm run build` into `public/admin`, `php artisan serve`, open `http://127.0.0.1:8000/admin/` → login works same-origin; `php artisan test` all green. Record which was possible.
- [ ] **Step 7: Commit** `build: production build and dockhosting deploy kit`. The actual deploy (creating the app on dockhosting, setting secrets, DNS) is done by the owner following `deploy/README.md` — do not log in to the host or enter credentials.

### Task F2: Final verification and docs

- [ ] `php artisan migrate:fresh --seed` on local MySQL; full `php artisan test`; `npm run build`; `gradlew :app:assembleDebug`.
- [ ] Arabic round-trip on MySQL: create section `اختبار الترميز — ١٢٣ ✓` in the admin, read it back with `mysql -u root dxn_training -e "SELECT title FROM sections ORDER BY id DESC LIMIT 1"`, then delete it.
- [ ] End-to-end script (manual, record results in `docs/superpowers/plans/2026-11-21-plan2-verification.md`): admin login → create leader → Android register trainee with leader number → complete level 1 (XP from server) → leader sees trainee in app → admin Users shows XP/last activity → Overview/Analytics reflect it → certificate after final quiz of section 1 → section 2 unlocked.
- [ ] Update `dxn-api/README.md` and `dxn-admin-web/README.md` (Arabic: requirements, run, test, env vars, default admin from `.env`), `DXNTrainingApp/README.md` (BASE_URL for emulator/release).
- [ ] **Commit** `docs: run and verification docs for the full system`

---

## Execution order and dependencies

A1 → A2 → B1 → C1 → C2 → B2 → B3 → C3 → C4 → D1 → D2 → D3 → E1 → E2 → E3 → F1 → F2.
(B2 needs C2's progress table; C3 writes analytics events — create that migration in C3 if E1 has not run, as noted.)

## Owner review checkpoints
Stop and hand back to the owner for review after: **A2** (admin content complete), **C3** (scoring/XP core), **D2** (Android on server), **F1** (deploy kit). Each checkpoint: all tests green, list of commits, anything skipped.
