# DXN Training — agent notes

One git repo, three independent apps; there is no root build. Work is driven by the plans in `docs/superpowers/plans/` and the binding spec `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md`, not by the READMEs.

## Workflow
- Plan 2 (`docs/superpowers/plans/2026-11-21-plan2-complete-system.md`) is binding and supersedes Plan 1 Tasks 8–10. Execution order: `A1 → A2 → B1 → C1 → C2 → B2 → B3 → C3 → C4 → D1 → D2 → D3 → E1 → E2 → E3 → F1 → F2`.
- Plan 3 (`docs/superpowers/plans/2026-11-22-plan3-remaining.md`) is the current execution plan (T0–T12); it finishes Plan 2 and only overrides it where stated. Final verification record: `docs/superpowers/plans/2026-11-21-plan2-verification.md` — mark manual checks **NOT RUN** unless actually performed.
- One commit per task; **do not push or merge**. Stop for owner review after **A2, C3, D2, F1** (tests green + list of commits + anything skipped).
- Plan 2 asks for a `Co-Authored-By: <model> <noreply@anthropic.com>` trailer; existing commits omit it — follow the plan.
- `dxn-api/AGENTS.md` (mirrored in `dxn-api/CLAUDE.md`) holds Laravel Boost bootstrap instructions — read it before changing that app.
- OpenCode config is `opencode.jsonc` (not `opencode.json`); it defines the MCP servers and Tavily websearch provider.

## API contract (non-default conventions)
- JSON keys camelCase; every id/foreign id is a **string**. Roles stored `admin|leader|trainee`, serialized `ADMIN|LEADER|TRAINEE`.
- Level `type` ∈ `lesson|quiz|chat_battle|match_pairs|order_sequence`; content `status` ∈ `draft|published`; mobile sees published, non-trashed only.
- `/api/admin/*` requires `auth:sanctum` + `role:admin`; leaders use mobile `/api/leader/team`. `/api/users/{id}/*` is allowed only for self, that user's leader (`sponsor_id`), or admin → 403 otherwise.
- Server owns score/pass/XP/streak/certificates. Completion needs `answers`; the old `{userId,score,passed}` body → 422, and client `score`/`passed` are ignored.
- `GET /api/auth/me` is wrapped in `{data: …}`; every newer endpoint returns unwrapped JSON. React unwraps only `/me`.
- Every new PHP endpoint needs a PHPUnit feature test.

## dxn-api (Laravel 13 / PHP 8.3 / Sanctum / MySQL 8.4)
- Test: `php artisan test` (SQLite `:memory:` via `phpunit.xml`); `composer test` also clears config first. Keep the suite green.
- Dev server: `php artisan serve --host=0.0.0.0 --port=8000`. Dev DB: Laragon MySQL `dxn_training`, user `root`, empty password; start `mysqld` if connections are refused.
- Manual API requests **must** send `Accept: application/json`; without it, unauthenticated API calls can 500 with `Route [login] not defined`.
- Login/register throttled `5,1`, events `60,1`. Completion uses the named `completion` limiter (`throttle:completion`) whose per-minute rate is read from the `quiz_rate_limit_per_minute` setting (default 10) — repeated manual logins return 429.
- Default admin `admin@dxn.local` / `change-me-locally` (from `.env`). `ContentSeeder` is idempotent; run `php artisan storage:link` once for media uploads.
- SQLite tests don't catch MySQL charset issues; Arabic round-trip must be checked manually on MySQL.

## dxn-admin-web (React 19 / Vite 8 / TS / Tailwind 3)
- `npm run dev` (port 5173), `npm run build` = `tsc -b && vite build`, `npm run lint` = `oxlint` (no ESLint, no test script).
- All API calls go through `src/lib/api.ts`; token in `localStorage['dxn_admin_token']`; 401 clears it and redirects to `/admin/login` (the router `basename`).
- `VITE_API_URL` is required in dev (e.g. `http://127.0.0.1:8000`); copy the committed `.env.example` to `.env.local`. Production `.env.production` sets it empty for same-origin `/api` calls.
- No mock data / NestJS backend: all pages call Laravel through `src/lib/api.ts`.

## DXNTrainingApp (Kotlin / Compose / Retrofit)
- Build: set `JAVA_HOME=D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7`, then `.\gradlew.bat :app:assembleDebug` (wrapper 8.7). `local.properties` is gitignored but already points at that JDK and the Android SDK.
- `BASE_URL` is in `app/build.gradle.kts` `buildTypes`: debug = `http://10.0.2.2:8000/`, release = placeholder `https://<domain>/` that the owner must replace before publishing. Emulator reaches the host at `10.0.2.2:8000`; cleartext is allowed only in `app/src/debug/AndroidManifest.xml`.
- `ContentRepository` is a singleton; `MockData` is only an offline display fallback, not a source of truth. XP/streak/certificates/unlocks come from the server.

## Production / admin SPA
- The React admin is built into `dxn-api/public/admin/` (generated and gitignored) and served by Laravel under `/admin/` with an SPA fallback; release images build it with `VITE_API_URL=""` for same-origin `/api` calls. See `deploy/README.md`.
