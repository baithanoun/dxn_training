# Plan 1 — Laravel API Foundation + Admin Content Management

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An admin can log in to the React dashboard and create, edit, order, publish and delete the training content (sections → skills → levels → lesson cards and chat/quiz rounds), stored in MySQL through a new Laravel API.

**Architecture:** New Laravel app in `dxn-api/` exposing a JSON REST API under `/api`, authenticated with Sanctum bearer tokens. Admin routes live under `/api/admin/*` behind a `role:admin` middleware. The existing React app (`dxn-admin-web/`) gets one fetch wrapper, an auth context, a login page, and its Content and Scenario Builder pages are rewired from `mockData.ts` to the API.

**Tech Stack:** PHP 8.3, Laravel (latest that `composer create-project laravel/laravel` installs on PHP 8.3), Sanctum, MySQL 8.4 (Laragon) for dev, SQLite in-memory for tests (Laravel default `phpunit.xml`), React 19 + TypeScript + Vite (existing, no new npm deps).

**Spec:** `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md` (stages 1–2 of §5.7). Later plans: Plan 2 users & teams, Plan 3 mobile API + Android, Plan 4 analytics/overview/settings/export, Plan 5 deployment to dockhosting.dev.

> **حالة التنفيذ (تحديث 2026-11-24):** جميع خطوات المهام 1–10 أُنجزت محلياً؛ الأدلة في سجل الالتزامات و`docs/superpowers/plans/2026-11-21-plan2-verification.md`. المهام 8–10 استُبدلت لاحقاً بـ Plan 2 A1–A3 ونُفّذت عبرها. الاستثناء الوحيد هو `git push` في Task 10 Step 4: الالتزام تم محلياً لكن الدفع لم يُنفَّذ وفق قاعدة المالك (لا push ولا merge).

## Global Constraints
- API JSON uses **camelCase** keys; every `id` and foreign id is serialized as a **string** (`"12"`).
- Roles stored lowercase `admin|leader|trainee`; serialized uppercase `ADMIN|LEADER|TRAINEE` (Android `UserRole`).
- Level `type` ∈ `lesson | quiz | chat_battle | match_pairs | order_sequence`. Content `status` ∈ `draft | published`.
- Sections, skills, levels use **soft deletes**; cards/rounds/options are hard-replaced by their parent's save.
- `dxn_id` stored with Laravel `encrypted` cast, plus `dxn_id_hash` = `hash_hmac('sha256', strtoupper(trim($dxnId)), config('app.key'))`, unique, for lookup.
- Admin routes require `auth:sanctum` + `role:admin`. Inactive users (`is_active=false`) cannot log in.
- No new npm dependencies; no axios. React talks to the API only through `src/lib/api.ts`.
- Images only, max 5 MB (`5120` KB), stored on Laravel `public` disk.
- Content is Arabic only; DB charset `utf8mb4` (Laravel default).

## Review Focus
1. Deleting a section that another section lists as `requiredSectionId` → expect **422** with a message naming the dependent section, not a dangling reference. *(test in Task 4)*
2. Saving a chat/quiz round that has **no correct option**, or an option with empty text → expect **422** with per-field errors that the Scenario Builder shows. *(test in Task 5)*
3. Reorder request containing an id from a **different parent** (or a missing id) → expect **422**, nothing reordered. *(test in Task 4)*
4. Expired/invalid token while the dashboard is open → any request returns 401 and the user lands on `/login`, never a blank page. *(manual check in Task 7)*
5. Arabic text round-trips through **MySQL** unchanged (tests run on SQLite and won't catch charset issues). *(verification step in Task 10)*

---

### Task 1: Scaffold `dxn-api` with Sanctum, user fields, admin login

**Files:**
- Create: `dxn-api/` (via composer), `dxn-api/app/Http/Middleware/EnsureRole.php`, `dxn-api/app/Http/Controllers/AuthController.php`, `dxn-api/app/Http/Resources/UserResource.php`, `dxn-api/database/seeders/AdminSeeder.php`
- Modify: `dxn-api/database/migrations/0001_01_01_000000_create_users_table.php`, `dxn-api/app/Models/User.php`, `dxn-api/bootstrap/app.php`, `dxn-api/routes/api.php`, `dxn-api/config/cors.php`, `dxn-api/.env.example`, `dxn-api/database/seeders/DatabaseSeeder.php`
- Test: `dxn-api/tests/Feature/AuthTest.php`

**Interfaces:**
- Produces: `POST /api/auth/login` body `{login: string, password: string}` (login = email or phone) → `200 {token: string, user: UserResource}`; `401` wrong credentials; `403` inactive. `POST /api/auth/logout` (revokes current token) → `204`. `GET /api/auth/me` → `UserResource`.
- Produces: `UserResource` = `{id, fullName, email, phone, dxnId, role, sponsorId, totalXp, currentStreak, hearts, gems, isActive}`.
- Produces: middleware alias `role` → `EnsureRole::handle($request, $next, string ...$roles)` returns 403 if `$request->user()->role` not in `$roles`.
- Produces: `User::hashDxnId(string $dxnId): string` (static) — the HMAC from Global Constraints.

- [x] **Step 1: Create the project and install Sanctum**

Run (from `D:\webserver\appp`): `composer create-project laravel/laravel dxn-api` then `cd dxn-api && php artisan install:api` (answer "no" to running migrations).
Expected: `routes/api.php` exists, `laravel/sanctum` in `composer.json`.

- [x] **Step 2: Configure MySQL (Laragon) for dev**

Start MySQL: run `C:\laragon\laragon.exe` and click *Start All* (or start `mysqld` from `C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin`). Create DB: `C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql -u root -e "CREATE DATABASE dxn_training CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"`.
In `.env` and `.env.example`: `DB_CONNECTION=mysql`, `DB_DATABASE=dxn_training`, `DB_USERNAME=root`, `DB_PASSWORD=`, plus `ADMIN_EMAIL=admin@dxn.local`, `ADMIN_PASSWORD=change-me-locally`, `FRONTEND_URL=http://localhost:5173`. Leave `phpunit.xml` on SQLite `:memory:`.

- [x] **Step 3: Write the failing tests** in `tests/Feature/AuthTest.php`

```php
test_admin_can_login_with_email(): create user role=admin, password 'secret123';
  POST /api/auth/login {login: email, password: 'secret123'} → 200, json has 'token', 'user.role' === 'ADMIN', 'user.id' is string
test_user_can_login_with_phone(): phone '+970599000000' → 200
test_wrong_password_returns_401()
test_inactive_user_returns_403(): is_active=false → 403
test_me_requires_token(): GET /api/auth/me without token → 401
test_logout_revokes_token(): login, POST /api/auth/logout with token → 204; then GET /api/auth/me with same token → 401
test_login_is_rate_limited(): 6 wrong attempts within a minute → 6th is 429
test_dxn_id_is_encrypted_and_hashed(): create user dxn_id 'dxn-123'; raw DB column dxn_id !== 'dxn-123'; dxn_id_hash === User::hashDxnId('DXN-123')
test_role_middleware_blocks_non_admin(): trainee token on a temp route Route::get('/api/admin/ping')->middleware(['auth:sanctum','role:admin']) → 403; admin → 200
```

- [x] **Step 4: Run tests to verify they fail**

Run: `php artisan test --filter=AuthTest` — Expected: FAIL (routes/columns missing).

- [x] **Step 5: Implement**
- Users migration: keep `name`; make `email` nullable+unique; add `phone` nullable unique, `dxn_id` text nullable, `dxn_id_hash` string(64) nullable unique, `role` string(16) default `trainee` indexed, `sponsor_id` nullable FK→users nullOnDelete, `total_xp` uint default 0, `current_streak` uint default 0, `last_activity_date` date nullable, `hearts` tinyint default 3, `gems` uint default 0, `is_active` bool default true.
- `User`: `HasApiTokens`; casts `dxn_id => 'encrypted'`, `password => 'hashed'`; a `saving` hook sets `dxn_id_hash` from `dxn_id`.
- `AuthController@login`: find by email or phone; `Hash::check`; 403 if inactive; `createToken('dashboard')`. Route throttled with `throttle:5,1`.
- Register `role` alias in `bootstrap/app.php`. CORS `allowed_origins` = `[env('FRONTEND_URL')]`, paths `api/*`.
- `AdminSeeder`: `updateOrCreate` by `ADMIN_EMAIL` with role admin; called from `DatabaseSeeder`.
- Remove the temp ping route after tests are rewritten to use a real admin route in Task 4 (keep it until then, under `/api/admin/ping`).

- [x] **Step 6: Run tests to verify they pass**

Run: `php artisan test` — Expected: all PASS. Then `php artisan migrate --seed` against MySQL — Expected: no errors.

- [x] **Step 7: Commit**

```bash
git add dxn-api
git commit -m "feat(api): scaffold Laravel API with Sanctum auth and admin role"
```

---

### Task 2: Content schema and models

**Files:**
- Create: migrations `create_sections_table`, `create_skills_table`, `create_levels_table`, `create_lesson_cards_table`, `create_game_rounds_table`, `create_round_options_table`; models `Section`, `Skill`, `Level`, `LessonCard`, `GameRound`, `RoundOption` in `dxn-api/app/Models/`
- Test: `dxn-api/tests/Feature/ContentModelTest.php`

**Interfaces:**
- Produces tables/columns:
  - `sections`: title, description text nullable, order_index uint, bag_id uint nullable, certificate_title nullable, required_section_id FK→sections nullable, status default draft, version uint default 1, softDeletes
  - `skills`: section_id FK cascade, title, icon nullable, order_index, status default draft, softDeletes
  - `levels`: skill_id FK cascade, title, type, pass_threshold tinyint default 80, xp_reward uint default 40, order_index, is_final_quiz bool default false, status default draft, softDeletes
  - `lesson_cards`: level_id FK cascade, title, body text, tip text nullable, media_url nullable, order_index
  - `game_rounds`: level_id FK cascade, title nullable, prompt text, initial_trust tinyint default 50, order_index
  - `round_options`: round_id FK→game_rounds cascade, text text, is_correct bool, is_neutral bool default false, trust_impact smallint default 0, feedback text nullable, xp_bonus uint default 0, order_index
- Produces relations (all ordered by `order_index`): `Section::skills()`, `Section::requiredSection()`, `Skill::levels()`, `Skill::section()`, `Level::cards()`, `Level::rounds()`, `Level::skill()`, `GameRound::options()`.
- Produces constants: `Level::TYPES = ['lesson','quiz','chat_battle','match_pairs','order_sequence']`, `Section::STATUSES = ['draft','published']`.

- [x] **Step 1: Write the failing test** `test_relations_are_ordered_and_soft_delete_hides_children_from_tree()`: create section with 2 skills (order 2,1) → `$section->skills->pluck('order_index')` === `[1,2]`; soft-delete a level → `$skill->levels()->count()` drops by 1 and `Level::withTrashed()->count()` unchanged.
- [x] **Step 2: Run** `php artisan test --filter=ContentModelTest` — Expected: FAIL.
- [x] **Step 3: Implement** migrations + models (`$fillable`, casts for bools, `SoftDeletes` on the three parents).
- [x] **Step 4: Run** `php artisan test` — Expected: PASS; `php artisan migrate` on MySQL OK.
- [x] **Step 5: Commit** `git commit -m "feat(api): content schema and models"`

---

### Task 3: Seed content from the Android mock data

**Files:**
- Create: `dxn-api/database/seeders/data/content.json`, `dxn-api/database/seeders/ContentSeeder.php`
- Modify: `dxn-api/database/seeders/DatabaseSeeder.php`
- Test: `dxn-api/tests/Feature/ContentSeederTest.php`

**Interfaces:**
- Consumes: Task 2 models.
- `content.json` = array of sections in the **same camelCase shape** as Android `SectionResponse` (see `DXNTrainingApp/app/src/main/java/com/dxn/training/data/remote/NetworkModels.kt`), converted by hand from `DXNTrainingApp/app/src/main/java/com/dxn/training/data/repository/MockData.kt` (`MockData.sections`, all 5 sections, all cards, rounds and options). Android string ids are dropped; `requiredSectionId` becomes the **orderIndex** of the required section.

- [x] **Step 1: Write the failing tests**
  - `test_seeds_five_published_sections_with_levels_cards_and_rounds()`: after `$this->seed(ContentSeeder::class)` → `Section::count() === 5`, all `status === 'published'`, every level has `cards` ≥ 1, every non-lesson level has `rounds` ≥ 1 with ≥ 1 `is_correct` option; section with orderIndex 2 has `required_section_id` = id of section orderIndex 1.
  - `test_seeder_is_idempotent()`: seed twice → `Section::count() === 5`.
  - `test_last_level_of_each_section_is_final_quiz()`.
- [x] **Step 2: Run** — Expected: FAIL.
- [x] **Step 3: Write `content.json`** (mechanical conversion of `MockData.sections`; options keep their list order as `order_index`, which is also the correct order for `order_sequence`).
- [x] **Step 4: Implement `ContentSeeder::run()`**: return early if `Section::exists()`; otherwise insert in a transaction; set `is_final_quiz=true` on the last level (by skill then level order) of each section; all statuses `published`.
- [x] **Step 5: Run** `php artisan test` — PASS; `php artisan db:seed` on MySQL OK.
- [x] **Step 6: Commit** `git commit -m "feat(api): seed training content from Android mock data"`

---

### Task 4: Admin content CRUD API (sections, skills, levels, cards, reorder)

**Files:**
- Create: `dxn-api/app/Http/Controllers/Admin/{ContentTreeController,SectionController,SkillController,LevelController,ReorderController}.php`, `dxn-api/app/Http/Requests/Admin/{SectionRequest,SkillRequest,LevelRequest,CardsRequest,ReorderRequest}.php`, `dxn-api/app/Http/Resources/Admin/{SectionResource,SkillResource,LevelResource,LevelDetailResource}.php`
- Modify: `dxn-api/routes/api.php` (remove `/api/admin/ping`; update the role test in `AuthTest` to hit `GET /api/admin/content/tree`)
- Test: `dxn-api/tests/Feature/Admin/ContentCrudTest.php`

**Interfaces** (all under `auth:sanctum, role:admin`, prefix `/api/admin`):
- `GET content/tree` → `SectionResource[]`: `{id, title, description, orderIndex, bagId, certificateTitle, requiredSectionId, status, skills: [{id, sectionId, title, icon, orderIndex, status, levels: [{id, skillId, title, type, passThreshold, xpReward, orderIndex, isFinalQuiz, status, cardsCount, roundsCount}]}]}` (drafts included, trashed excluded).
- `POST sections` / `PUT sections/{id}` / `DELETE sections/{id}` — fields `title`(required, ≤255), `description`, `bagId`(int 1–99), `certificateTitle`, `requiredSectionId`(exists, ≠ self), `status`. New items get `order_index = max+1` in their parent.
- `POST skills` (`sectionId` required) / `PUT skills/{id}` / `DELETE skills/{id}` — `title`, `icon`, `status`.
- `POST levels` (`skillId` required) / `PUT levels/{id}` / `DELETE levels/{id}` — `title`, `type`(in `Level::TYPES`), `passThreshold`(0–100), `xpReward`(0–1000), `isFinalQuiz`, `status`. Setting `isFinalQuiz=true` clears it on other levels of the same section.
- `GET levels/{id}` → `LevelDetailResource` = level fields + `cards: [{id,title,body,tip,mediaUrl,orderIndex}]` + `rounds: [{id,title,prompt,initialTrust,orderIndex,options:[{id,text,isCorrect,isNeutral,trustImpact,feedback,xpBonus,orderIndex}]}]`.
- `PUT levels/{id}/cards` body `{cards: [{title(≤255, required), body(required, ≤5000), tip, mediaUrl}]}` → replaces all cards in list order; returns `LevelDetailResource`.
- `POST reorder` body `{type: 'sections'|'skills'|'levels', parentId?: string, ids: string[]}` → sets `order_index` = position+1; `204`.

- [x] **Step 1: Write the failing tests**
  - `test_tree_requires_admin()`: no token 401, trainee 403, admin 200.
  - `test_tree_includes_drafts_and_excludes_deleted()`.
  - `test_create_section_appends_order_index()`.
  - `test_update_level_validates_type_and_threshold()`: `type='video'` → 422 `errors.type`; `passThreshold=150` → 422.
  - `test_setting_final_quiz_clears_other_final_in_section()`.
  - `test_delete_section_required_by_another_returns_422()` *(Review Focus 1)*: s2.required=s1; `DELETE sections/s1` → 422, message contains s2's title; s1 not trashed.
  - `test_delete_level_soft_deletes()`.
  - `test_put_cards_replaces_in_order()`: PUT 2 cards → GET level shows exactly those 2 with orderIndex 1,2.
  - `test_reorder_rejects_foreign_ids()` *(Review Focus 3)*: reorder skills of section A including a skill of section B → 422, original order unchanged.
  - `test_reorder_rejects_partial_list()`: ids missing one sibling → 422.
- [x] **Step 2: Run** `php artisan test --filter=ContentCrudTest` — FAIL.
- [x] **Step 3: Implement** controllers, FormRequests (validation above; reorder validates `ids` equals exactly the set of sibling ids), resources (`(string)` ids).
- [x] **Step 4: Run** `php artisan test` — all PASS.
- [x] **Step 5: Commit** `git commit -m "feat(api): admin content CRUD and reorder"`

---

### Task 5: Scenario/quiz rounds save API

**Files:**
- Create: `dxn-api/app/Http/Controllers/Admin/RoundsController.php`, `dxn-api/app/Http/Requests/Admin/RoundsRequest.php`
- Modify: `dxn-api/routes/api.php`
- Test: `dxn-api/tests/Feature/Admin/RoundsTest.php`

**Interfaces:**
- `PUT /api/admin/levels/{id}/rounds` body `{rounds: [{title?, prompt, initialTrust, options: [{text, isCorrect, isNeutral, trustImpact, feedback?, xpBonus?}]}]}` → replaces all rounds+options in one DB transaction, list order = `order_index`; returns `LevelDetailResource` (Task 4).
- Validation: `rounds` array min 1; `prompt` required ≤2000; `initialTrust` int 0–100; `options` min 2; option `text` required ≤1000; `trustImpact` int −100..100; `xpBonus` int 0–100; `isCorrect` and `isNeutral` not both true; for level types `quiz|chat_battle|match_pairs` each round needs ≥1 `isCorrect=true` (error key `rounds.{i}.options`). `lesson` levels → 422 (lessons have no rounds).

- [x] **Step 1: Write the failing tests**
  - `test_put_rounds_replaces_and_orders()`.
  - `test_round_without_correct_option_returns_422()` *(Review Focus 2)*: error key `rounds.0.options`.
  - `test_empty_option_text_returns_422()`: error key `rounds.0.options.1.text`.
  - `test_failed_save_keeps_previous_rounds()`: existing 2 rounds, invalid PUT → still 2 rounds.
  - `test_lesson_level_rejects_rounds()`.
- [x] **Step 2: Run** — FAIL.
- [x] **Step 3: Implement.**
- [x] **Step 4: Run** `php artisan test` — PASS.
- [x] **Step 5: Commit** `git commit -m "feat(api): save chat/quiz rounds with validation"`

---

### Task 6: Media upload

**Files:**
- Create: `dxn-api/app/Http/Controllers/Admin/MediaController.php`
- Modify: `dxn-api/routes/api.php`
- Test: `dxn-api/tests/Feature/Admin/MediaTest.php`

**Interfaces:**
- `POST /api/admin/media` multipart `file` → `201 {url: string}` (absolute URL via `Storage::disk('public')->url`). Rule: `required|image|max:5120`.

- [x] **Step 1: Tests** (`Storage::fake('public')`): `test_uploads_image_returns_url()`; `test_rejects_non_image()` (pdf → 422); `test_rejects_over_5mb()` (`UploadedFile::fake()->image('a.jpg')->size(6000)` → 422); `test_requires_admin()`.
- [x] **Step 2: Run** — FAIL. **Step 3: Implement** (store under `media/`). Run `php artisan storage:link` once on dev.
- [x] **Step 4: Run** `php artisan test` — PASS.
- [x] **Step 5: Commit** `git commit -m "feat(api): admin image upload"`

---

### Task 7: React — API client, auth and login page

**Files:**
- Create: `dxn-admin-web/src/lib/api.ts`, `dxn-admin-web/src/lib/auth.tsx`, `dxn-admin-web/src/lib/types.ts`, `dxn-admin-web/src/pages/Login.tsx`, `dxn-admin-web/.env.example`
- Modify: `dxn-admin-web/src/App.tsx`, `dxn-admin-web/src/layouts/AdminLayout.tsx` (show `user.fullName`, logout button)

**Interfaces:**
- `api.ts`: `export class ApiError extends Error { status: number; errors: Record<string,string[]> }`; `export async function api<T>(path: string, opts?: { method?: string; json?: unknown; body?: FormData }): Promise<T>` — base `import.meta.env.VITE_API_URL` (e.g. `http://127.0.0.1:8000`), adds `Accept: application/json` and `Authorization: Bearer <localStorage 'dxn_admin_token'>`; on 401 removes the token and `window.location.assign('/login')`; non-2xx throws `ApiError` with Laravel `message`/`errors`; 204 returns `undefined`.
- `auth.tsx`: `AuthProvider`, `useAuth(): { user: User | null; loading: boolean; login(login: string, password: string): Promise<void>; logout(): Promise<void> }` (loads `/api/auth/me` on start if a token exists); `RequireAuth` wraps routes and redirects to `/login`. Non-ADMIN user after login → show "هذا الحساب لا يملك صلاحية الإدارة" and drop the token.
- `types.ts`: TS types mirroring Task 1 `UserResource` and Task 4 `SectionResource`/`LevelDetailResource` exactly.
- `Login.tsx`: RTL card in the existing UI kit (`Card`, `Input`, `Button`), fields "البريد الإلكتروني أو الهاتف" / "كلمة المرور", shows API error text, disables button while submitting.

- [x] **Step 1: Implement** the files above; in `App.tsx` add `<Route path="/login">` outside the layout and wrap the layout route with `RequireAuth`.
- [x] **Step 2: Type-check/build** — Run: `npm run build` in `dxn-admin-web` — Expected: success.
- [x] **Step 3: Manual check** (API: `php artisan serve`; web: `npm run dev`): wrong password shows error; admin login lands on `/`; reload keeps session; logout returns to `/login`; then in DevTools set `localStorage.dxn_admin_token='bad'`, reload `/content` → lands on `/login` *(Review Focus 4)*.
- [x] **Step 4: Commit** `git commit -m "feat(admin-web): API client, auth context and login page"`

---

### Task 8: React — Content page on the API

**Files:**
- Modify: `dxn-admin-web/src/pages/Content.tsx`

**Interfaces:**
- Consumes: Task 4 endpoints, Task 6 upload, Task 7 `api()` + types.

- [x] **Step 1: Implement**
  - Load `GET /api/admin/content/tree`; loading, error (with retry) and existing empty states.
  - "إضافة قسم" opens an inline form (title, description, bag, certificate title, required section) → `POST sections`; add skill / add level buttons inside the tree (`POST skills` / `POST levels`, type select from `Level::TYPES` with Arabic labels: درس، اختبار، محاكي محادثة، مطابقة، ترتيب).
  - Level panel: controlled inputs (replace `defaultValue`) for title, type, passThreshold, xpReward, isFinalQuiz, status → "حفظ التغييرات" = `PUT levels/{id}`; "إلغاء" reverts to loaded values.
  - Cards editor in the level panel: list with add/remove/move up-down, image upload button per card (`POST media` → `mediaUrl`) → save = `PUT levels/{id}/cards`.
  - Section/skill edit + publish toggle (`PUT` with `status`), delete with `window.confirm` → `DELETE`; show API 422 `message` (Review Focus 1) in a red banner.
  - Up/down arrows on siblings → `POST reorder`.
  - Remove the `sections` import from `@/data/mockData` in this file.
- [x] **Step 2: Build** `npm run build` — success.
- [x] **Step 3: Manual check:** create section → skill → level → 2 cards with an image; reload page, all persist; reorder two skills, reload, order kept; try deleting a section another depends on → red banner with message.
- [x] **Step 4: Commit** `git commit -m "feat(admin-web): content management on live API"`

---

### Task 9: React — Scenario Builder on the API

**Files:**
- Modify: `dxn-admin-web/src/pages/ScenarioBuilder.tsx`, `dxn-admin-web/src/pages/Content.tsx` (level panel gets "فتح في منشئ السيناريوهات" link for non-lesson levels → `/scenario?levelId=<id>`)

**Interfaces:**
- Consumes: `GET levels/{id}`, `PUT levels/{id}/rounds`, `PUT levels/{id}` (Task 4/5).
- Mapping between the page's existing local types and the API: `Round.message ↔ prompt`, `Round.title ↔ title`, `Option.type 'correct'|'wrong'|'neutral' ↔ isCorrect/isNeutral`, `Option.trust ↔ trustImpact`, `Option.xp ↔ xpBonus`, scenario name ↔ level `title`, `passThreshold` ↔ level `passThreshold`, publish/unpublish ↔ level `status`.

- [x] **Step 1: Implement**
  - Read `levelId` from the query string; without it, show a level picker (non-lesson levels from `content/tree`, grouped by section/skill) instead of `initialRounds`.
  - Load the level → fill rounds, name, threshold; "حفظ مسودة" → `PUT levels/{id}` (title, passThreshold) then `PUT levels/{id}/rounds`; "نشر/إلغاء النشر" (keep existing confirm) → `PUT levels/{id}` with `status`.
  - Server 422 `errors` (`rounds.{i}.options...`) are shown under the matching round/option, alongside the existing client validation and `hasXss` check.
  - Settings drawer: linked section/skill becomes read-only text from the level's location (moving a level is out of scope); keep the threshold input.
  - Export button: download the current rounds as JSON (client-side `Blob`); remove the unused mock `initialRounds` and the `sections` mock import.
- [x] **Step 2: Build** `npm run build` — success.
- [x] **Step 3: Manual check:** open a seeded chat_battle level, edit an option's text and trust, save, reload → persisted; remove all correct options from a round, save → error shown under that round (Review Focus 2); publish toggle persists.
- [x] **Step 4: Commit** `git commit -m "feat(admin-web): scenario builder on live API"`

---

### Task 10: End-to-end verification on MySQL and docs

**Files:**
- Create: `dxn-api/README.md` (Arabic: requirements, Laragon MySQL start, `.env`, `migrate --seed`, `serve`, `test`, default admin from `.env`)
- Modify: `dxn-admin-web/README.md` (add `VITE_API_URL` and login step)

- [x] **Step 1:** `php artisan migrate:fresh --seed` on MySQL → OK; `php artisan test` → all PASS; `npm run build` → OK.
- [x] **Step 2 (Review Focus 5):** via the dashboard create a section titled `اختبار الترميز — ١٢٣ ✓`, then `mysql -u root dxn_training -e "SELECT title FROM sections ORDER BY id DESC LIMIT 1"` shows the same text; reload dashboard shows it unchanged. Delete it.
- [x] **Step 3:** Write the two READMEs.
- [x] **Step 4: Commit** `git commit -m "docs: run instructions for API and admin web"` (نُفّذ محلياً؛ غطّاه لاحقاً `8ffca03 docs: run and verification docs for the full system`).
- [ ] `git push` — **NOT RUN**: الالتزامات محلية على `feat/plan1-api-content` وفق قاعدة المالك (لا push ولا merge).
