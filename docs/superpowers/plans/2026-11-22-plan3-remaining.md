# Plan 3 — المتبقي من النظام (اختبارات، إصلاحات، واكتمال الواجهات والنشر)

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development أو superpowers:executing-plans. خطوات بصيغة `- [ ]`. **commit واحد لكل مهمة**، لا push ولا merge. توقّف للمالك عند نقاط التوقف المحددة.

**Goal:** حماية ما بُني باختبارات، إصلاح أخطاء المراجعة، ثم إكمال A2/B3/D1–D3/E1–E3/F1–F2.

**Architecture:** بلا تغيير عن Plan 2: Laravel API (`dxn-api/`) مصدر الحقيقة، منطق التقدّم في `App\Services\ProgressService` والتقييم في `App\Services\ScoringService`؛ React (`dxn-admin-web/`) عبر `src/lib/api.ts` فقط؛ Android (`DXNTrainingApp/`) عبر Retrofit.

**Tech Stack:** Laravel 13 / PHP 8.3 / Sanctum / MySQL 8.4 (PHPUnit على SQLite `:memory:`)، React 19 + Vite 8 + TS، Kotlin/Compose/Retrofit + DataStore 1.1.1.

**Spec:** `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md` (ملزمة). **Plan 2:** `docs/superpowers/plans/2026-11-21-plan2-complete-system.md` — مهامه غير المكتملة تُنفَّذ من هنا؛ حين تقول هذه الخطة "كما في Plan 2 Task X" فالنص هناك ملزم حرفياً مع الفروقات المذكورة هنا. **المراجعة:** `docs/superpowers/reviews/2026-11-22-plan2-progress-review.md` (رموز C-/I-/M- تشير إليها).

> **حالة التنفيذ (تحديث 2026-11-24):** أُنجزت T0–T12 محلياً؛ الأدلة في `docs/superpowers/plans/2026-11-21-plan2-verification.md`. بناء صورة Docker والنشر الفعلي على DockHosting **`NOT RUN`** ومسؤولية المالك (انظر `deploy/README.md`)؛ لا push ولا merge.

## Current state
| Commit | المحتوى |
|---|---|
| 93e8d01 | A1 Content page |
| 759e67c | C1+C2+C3 (register, tree, progress, completion, ProgressService, ScoringService, settings table) |
| a8fd4e0, 82f7b69 | D1 جزئي: BASE_URL debug، SessionStore، ApiClient interceptor، auth endpoints في ApiService |
| 06ebb27 | B1 users API |
| 04c02af | C4 leader team + events |
| af6c756 | B2 teams API |
| d7a6d6b | E3 settings API |
| f6125df | E1 dashboard/overview فقط |

غير مُلتزَم: `DXNTrainingApp/.../ApiService.kt`, `ContentRepository.kt`, `QuizScreen.kt` (D2 WIP). غير مُتتبَّع: `AGENTS.md`, ملفات الخطط/المراجعة.
`php artisan test` = 35 passed. صفحات على mockData: ScenarioBuilder, Users, Teams, Overview, Analytics.

## Global Constraints
تُنسخ من Plan 2 حرفياً (قسم "Global Constraints") وتبقى ملزمة، مع الإضافات:
- رسائل الأخطاء الموجّهة للمستخدم في الـ API الجديد بالعربية كما هي مكتوبة في Plan 2 (مثل "لم يتم العثور على قائد بهذا الرقم"، "رقم العضوية مسجّل مسبقاً").
- كل CSV يُكتب بـ `fputcsv` مع BOM، وكل خلية تبدأ بأحد `= + - @` تُسبق بـ `'`.
- أي اختصار مقصود يحمل تعليق `shortcut: <limit>, <when to upgrade>`.
- لا Composer/npm deps جديدة.

## Review Focus
1. فشل ثم نجاح في نفس المستوى → `xpAwarded = xp_reward` كاملاً (لا 25%). *(T1)*
2. `answers` بعنصر ينقصه `optionId`، أو `roundId` من مستوى آخر، أو جولة مكررة، أو جولة ناقصة → 422 لا 500. *(T1/T2)*
3. عضو نشط أمس → `lastActiveDays = 1` و`ACTIVE`؛ قبل 10 أيام → `10` و`INACTIVE`. *(T1/T2)*
4. اسم مستخدم `=HYPERLINK("x")` أو `علي, محمد` في CSV → خلية واحدة صحيحة بلا تنفيذ صيغة. *(T1/T2)*
5. `PUT admin/settings` بمفتاح `foo` → 422؛ `maintenance_mode=true` → `/api/sections/tree` 503 و`/api/admin/*` تعمل. *(T1/T3)*

---

## T0 — قرار D2 WIP (قبل أي شيء)
- [x] `git diff -- DXNTrainingApp` واقرأه. ثم في `DXNTrainingApp`: `set JAVA_HOME=D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7` و`.\gradlew.bat :app:assembleDebug`.
- [x] إن نجح البناء: `git stash push -m "D2-wip" -- DXNTrainingApp` (يُستعاد في T7). إن فشل: نفس الـ stash، وسجّل الخطأ في رسالة T7. **لا commit هنا.**

## T1 — Test backfill (Critical C-1) — اختبارات فقط
**Files (Create):**
- `dxn-api/tests/Feature/RegisterTest.php`
- `dxn-api/tests/Feature/Mobile/TreeTest.php`, `ProgressTest.php`, `CompletionTest.php`, `LeaderTeamTest.php`, `EventsTest.php`
- `dxn-api/tests/Unit/ScoringServiceTest.php`, `dxn-api/tests/Unit/ProgressServiceTest.php`
- `dxn-api/tests/Feature/Admin/UsersTest.php`, `TeamsTest.php`, `SettingsTest.php`, `DashboardTest.php`

**Interfaces (موجودة — verify in project قبل الكتابة):** routes في `dxn-api/routes/api.php:22-54`؛ `ProgressService::{orderedLevels, finalLevel, levelStatuses, sectionStatuses, isUnlocked, completionPercent}`؛ `ScoringService::{score(Level,array):int, passed(Level,int):bool}`؛ `Setting::get($key,$default)`؛ نمط إنشاء المستخدمين والتوكن من `tests/Feature/Admin/ContentCrudTest.php`. للمحتوى استخدم `$this->seed(ContentSeeder::class)` ثم اقرأ المستويات من DB (لا ids ثابتة).

**الاختبارات (الاسم → التأكيد):**
- RegisterTest: `test_register_trainee_returns_token` (201، `token`، `user.role==='TRAINEE'`)، `test_register_requires_email_or_phone` (422)، `test_duplicate_dxn_id_422` (422 `errors.dxnId`)، `test_blank_dxn_id_422` (`'  '` → 422)، `test_leader_dxn_id_links_sponsor` (sponsor_id = id القائد)، `test_leader_dxn_id_of_trainee_rejected` (422 `errors.leaderDxnId`)، `test_register_cannot_set_role` (`role:'admin'` → TRAINEE).
- TreeTest: `test_tree_returns_only_published_with_exact_keys` (مفاتيح SectionResponse/Skill/Level/Card/Round/Option كما في Plan 2 C2، ids نصية)، `test_tree_hides_trashed_level`.
- ProgressTest: `test_new_user_first_level_unlocked_rest_locked`، `test_completed_level_unlocks_next`، `test_certificate_unlocks_next_section`، `test_self_200`، `test_own_leader_200`، `test_admin_200`، `test_other_trainee_403`، `test_progress_contains_every_published_level`.
- ScoringServiceTest (unit): quiz 2/3 صحيح → 67؛ chat: initial 50 + 25 −20 + 25 → 80، وclamp عند >100 و<0؛ order_sequence ترتيب صحيح → 100، معكوس → 0؛ match_pairs كـ quiz؛ lesson بلا جولات → 100.
- CompletionTest: `test_first_try_pass_awards_multiplied_xp` (xp 40 → 60)، `test_repeat_same_score_awards_zero` (attempts=2، best ثابت)، `test_improved_replay_awards_quarter` (40 → 10)، **`test_first_pass_after_fail_awards_full_xp` (40 → 40)** *(Review Focus 1 — متوقع أن يفشل الآن)*، `test_fail_awards_zero_status_attempted`، `test_locked_level_409_no_row`، `test_old_body_shape_422`، **`test_missing_option_id_422`**، **`test_foreign_round_id_422`**، **`test_duplicate_round_422`**، **`test_unanswered_round_422`** *(Review Focus 2 — متوقع فشلها)*، `test_final_level_issues_certificate_once`، `test_streak_yesterday_increments` / `test_streak_today_unchanged` / `test_streak_older_resets` (`Carbon::setTestNow`)، **`test_completion_writes_level_event`** (صف `level_completed` أو `level_failed` في `analytics_events` — متوقع فشله، I-3).
- LeaderTeamTest: `test_trainee_forbidden` (403)، **`test_last_active_days_and_status`** (أمس → 1/ACTIVE، قبل 10 أيام → 10/INACTIVE، null → 999) *(Review Focus 3)*، `test_progress_percent`.
- EventsTest: `test_accepts_allowed_events_202`، `test_rejects_unknown_event_422`، `test_rejects_more_than_100`، `test_client_cannot_send_level_completed` (422).
- UsersTest: `test_list_paginates_and_filters_by_role_and_search`، `test_search_by_exact_dxn_id`، `test_status_filter_inactive_uses_3_days`، `test_create_user_validates_unique_email_and_phone`، `test_leader_must_have_leader_role`، `test_deactivate_revokes_tokens`، `test_admin_cannot_demote_self`، `test_export_csv_has_bom_and_header`، **`test_export_escapes_commas_and_formulas`** *(Review Focus 4 — متوقع فشله)*، `test_non_admin_forbidden`.
- TeamsTest: `test_team_aggregates` (أرقام دقيقة لقائدين)، `test_team_detail_404_for_non_leader`.
- SettingsTest: `test_defaults_returned`، `test_invalid_value_422`، **`test_unknown_key_422`**، **`test_maintenance_blocks_mobile_not_admin`**، **`test_completion_limiter_honors_setting`** *(Review Focus 5 — متوقع فشلها)*.
- DashboardTest: `test_overview_shape` (مفاتيح الكتل)، `test_overview_empty_db_no_division_by_zero`.

- [x] **Step 1:** اكتب الملفات. **Step 2:** `cd dxn-api && php artisan test` — سجّل قائمة الاختبارات الفاشلة.
- [x] **Step 3:** الاختبارات الفاشلة المتوقعة (المعلَّمة أعلاه) تُعلَّم `$this->markTestIncomplete('fixed in T2/T3')` **فقط** إن كان إصلاحها في T2/T3؛ أي فشل غير متوقع يُصلَح في T2 ويُسجَّل سببه.
- [x] **Step 4:** Commit `test(api): backfill feature/unit tests for plan 2 endpoints`

## T2 — إصلاحات المراجعة في الـ API
**Files (Modify):** `app/Http/Controllers/Mobile/CompletionController.php`، `app/Services/ScoringService.php`، `app/Http/Controllers/Mobile/LeaderTeamController.php`، `app/Http/Controllers/Admin/UserController.php`، `app/Http/Controllers/Admin/TeamController.php`، `app/Http/Controllers/Mobile/EventController.php`، `app/Services/ProgressService.php`، `app/Models/User.php`، `app/Http/Requests/RegisterRequest.php` و/أو `AuthController.php`. **Create:** `app/Http/Requests/CompletionRequest.php`.

- [x] **I-1 XP:** في الـ transaction: `$firstAttempt = ! $row; $wasCompleted = $row?->status === 'completed'; $prevBest = (int)($row?->best_score ?? 0);` ثم:
  - `passed && ! $wasCompleted` → `$firstAttempt ? round(xp×mult) : xp_reward`
  - `passed && $wasCompleted && $score > $prevBest` → `floor(xp×ratio)`
  - غير ذلك 0.
- [x] **I-2 تحقق الإجابات:** `CompletionRequest` (FormRequest) يُحمَّل بالمستوى من الـ route؛ القواعد: `answers` array (مطلوب لغير lesson)؛ `answers.*.roundId` required string؛ لـ quiz/match_pairs/chat_battle `answers.*.optionId` required؛ لـ order_sequence `answers.*.orderedOptionIds` required array. في `after()`: كل roundId ∈ جولات المستوى، لا تكرار، عدد الإجابات = عدد الجولات، كل optionId ∈ خيارات جولته، و`orderedOptionIds` تبديل كامل لخيارات الجولة → وإلا خطأ على `answers` أو `answers.{i}.*`. يبقى فحص القفل (409) قبل التحقق كما الآن (المستوى المقفل يعيد 409 حتى لو الجسم خاطئ). `ScoringService` يفترض مدخلات صالحة.
- [x] **I-3 أحداث الخادم:** داخل الـ transaction بعد حفظ التقدّم: `AnalyticsEvent::create(['user_id'=>…, 'event_name'=> $passed ? 'level_completed' : 'level_failed', 'level_id'=>…, 'payload'=>['score'=>$score,'attempt'=>$row->attempts], 'created_at'=>now()])`.
- [x] **M-3:** انقل فحص `isUnlocked` إلى داخل الـ transaction بعد `lockForUpdate` (أبقِ الرد 409).
- [x] **I-4 CSV:** استبدل البناء اليدوي بـ `fputcsv` على `fopen('php://temp','r+')`، بادئة BOM، وتهريب الخلايا الخطرة بدالة خاصة `private function csvSafe(?string $v): string`. الأعمدة كما في Plan 2 B1: الاسم، البريد، الهاتف، رقم العضوية، الدور، القائد، XP، آخر نشاط، الحالة.
- [x] **I-5:** `lastActiveDays = $m->last_activity_date ? (int) $m->last_activity_date->startOfDay()->diffInDays(now()->startOfDay()) : 999`.
- [x] **I-6:** في `TeamController::index` أضف `'leaderName' => $leader->name` وأبقِ `leader` (كائن). حدّث Plan 2 B3 ضمنياً: React يستخدم `leaderName`.
- [x] **M-2:** في `sectionStatuses`: COMPLETED فقط عند وجود شهادة؛ وإلا UNLOCKED/LOCKED.
- [x] **M-1:** احسب `levelStatuses($user)` مرة واحدة قبل حلقة الأقسام، واجلب الشهادات مرة واحدة (`Certificate::where('user_id',…)->pluck('code','section_id')`).
- [x] **M-4:** `EventController`: `created_at = now()`؛ خزّن `occurredAt` داخل `payload['occurredAt']`.
- [x] **M-5/C1:** في `User::booted` saving: `if (is_string($u->dxn_id) && trim($u->dxn_id) === '') $u->dxn_id = null;`؛ `RegisterRequest`: `dxnId` → `required|string|max:64` + `prepareForValidation` يعمل `trim`؛ الرسائل العربية: dxnId المكرر "رقم العضوية مسجّل مسبقاً"، القائد "لم يتم العثور على قائد بهذا الرقم"، و`messages()` للحقول الإلزامية.
- [x] أزل `markTestIncomplete` من اختبارات I-1/I-2/I-3/I-4/I-5. `php artisan test` → كلها PASS (عدا اختبارات Settings الخاصة بـ T3).
- [x] Commit `fix(api): completion XP/validation/events, CSV escaping, team activity, progress perf`

## T3 — Settings: رفض المفاتيح المجهولة وربط السلوك (I-7, I-8)
**Files:** `app/Http/Controllers/Admin/SettingsController.php`، `app/Http/Controllers/AuthController.php`، `app/Providers/AppServiceProvider.php`، `routes/api.php`، **Create** `app/Http/Middleware/BlockWhenMaintenance.php`، `bootstrap/app.php` (alias `maintenance`).
- [x] `update`: إن وُجدت مفاتيح خارج `RULES` → 422 `{message:'مفتاح غير معروف', errors:{<key>:['مفتاح غير معروف']}}`.
- [x] نقل `DEFAULTS` إلى `Setting::DEFAULTS` (أو ثابت مشترك) واستعمال `Setting::get` بالقيمة الافتراضية منه في كل مكان (CompletionController يقرأ xp_* بالفعل).
- [x] `session_minutes`: في `AuthController::login` لتوكن `dashboard` فقط: `createToken('dashboard', ['*'], now()->addMinutes((int) Setting::get('session_minutes', 60)))`. توكن `mobile` بلا انتهاء.
- [x] `quiz_rate_limit_per_minute`: في `AppServiceProvider::boot`: `RateLimiter::for('completion', fn (Request $r) => Limit::perMinute((int) Setting::get('quiz_rate_limit_per_minute', 10))->by($r->user()?->id ?: $r->ip()))`؛ في routes استبدل `throttle:30,1` بـ `throttle:completion`. (ملاحظة: AGENTS.md يذكر `30,1` — حدّثه في F2.)
- [x] `maintenance_mode`: middleware يعيد 503 `{message:'التطبيق تحت الصيانة'}` إذا `Setting::get('maintenance_mode', false)`؛ يُطبَّق على مجموعة الـ routes الموبايل (`sections/tree`, `users/*`, `levels/*/complete`, `leader/team`, `events`) و`auth/register` — **لا** على `auth/login|logout|me` ولا `/api/admin/*`.
- [x] أزل `markTestIncomplete` من اختبارات Settings؛ `php artisan test` كله PASS.
- [x] Commit `feat(api): settings validation and behavioral wiring`

### ⏸ نقطة توقف المالك 1
سلّم: عدد الاختبارات، قائمة commits T1–T3، أي اختبار بقي incomplete ولماذا.

---

## T4 — A2 Scenario Builder على الـ API
كما في Plan 2 Task A2 حرفياً. التحقق: `npm run build` + الفحوص اليدوية المذكورة هناك. Commit `feat(admin-web): scenario builder on live API`.

## T5 — E1 المتبقي: analytics + export + certificates
كما في Plan 2 Task E1 للأجزاء غير المنفذة فقط:
- **Files:** Modify `app/Http/Controllers/Admin/DashboardController.php` (أضف `analytics`, `analyticsExport`, `certificates`)، routes تحت `/api/admin`: `GET analytics`, `GET analytics/export`, `GET certificates`. Tests: أضف إلى `tests/Feature/Admin/DashboardTest.php`.
- أشكال JSON وتعريفات funnel/retention/failedQuestions كما في Plan 2 E1 حرفياً. `failedQuestions` تأتي من أحداث `question_answered` بـ payload `{roundId, optionId, correct, timeMs}`.
- CSV عبر نفس نمط T2 (fputcsv + BOM + تهريب).
- الاختبارات: `test_analytics_funnel_counts`، `test_failed_questions_error_rate`، `test_retention_d1`، `test_analytics_empty_db_zeros`، `test_analytics_export_csv`، `test_certificates_issued_count`.
- Commit `feat(api): analytics, analytics export and certificates endpoints`

## T6 — B3 Users & Teams pages
كما في Plan 2 Task B3 حرفياً، مع: الفرق تعرض `leaderName` (T2/I-6)؛ أنواع `AdminUser`, `Paginated<T>`, `TeamSummary` في `src/lib/types.ts` مطابقة لـ `AdminUserResource` و`TeamController` الحاليين (اقرأهما). Commit `feat(admin-web): users and teams on live API`.

## T7 — D1 (إكمال) + D2
- [x] `git stash pop` لـ "D2-wip" (من T0)؛ حل التعارضات إن وُجدت.
- [x] D1 المتبقي كما في Plan 2 D1: ربط `AuthViewModel`, `LoginScreen`, `RegisterScreen` (حقل كلمة المرور، تسمية "رقم عضوية القائد (اختياري)")، وجهة البداية حسب الجلسة، إزالة `DEFAULT_USER_ID` من `ContentRepository.kt` واستعمال `userId` من `SessionStore`.
- [x] نقل `BASE_URL` من `defaultConfig` إلى `buildTypes { debug { "http://10.0.2.2:8000/" } release { "https://CHANGE-ME/" } }` + تعليق `shortcut: release URL placeholder, set in F1`.
- [x] D2 كما في Plan 2 D2 حرفياً (CompletionResult، جمع الإجابات من الشاشات الأربع، حذف منطق الفتح المحلي، شاشة غير متصل). شكل الإجابات يجب أن يطابق تحقق T2 (كل الجولات، `optionId` أو `orderedOptionIds`).
- [x] تحقق: `gradlew :app:assembleDebug` + الفحوص اليدوية في Plan 2 D1 وD2 على المحاكي مقابل `php artisan serve --host=0.0.0.0 --port=8000`.
- [x] Commit واحد لكل جزء: `feat(android): real auth with token storage` ثم `feat(android): server-authoritative progress and completion`.

### ⏸ نقطة توقف المالك 2
سلّم: نتيجة البناء، الفحوص اليدوية المنجزة/غير المنجزة، commits.

## T8 — D3 Profile/Team/Events
كما في Plan 2 D3 حرفياً؛ أحداث العميل المسموحة فقط: `app_opened | level_started | question_answered | share_achievement | certificate_downloaded` — QuizScreen/MatchPairs/ChatBattle ترسل `question_answered` بـ `{roundId, optionId, correct, timeMs}` (لتغذية T5). Commit `feat(android): profile, leader team and events from API`.

## T9 — E2 Overview & Analytics pages
كما في Plan 2 E2 حرفياً، ثم احذف `src/data/mockData.ts` إن لم يعد مستورداً (`grep -r mockData src`). Commit `feat(admin-web): overview and analytics on live API`.

## T10 — صفحة Settings
verify in project: إن كانت `src/pages/Settings.tsx` ما زالت بقيم ثابتة → اربطها كما في Plan 2 E3 (GET/PUT، "(قريباً)" لمفاتيح الإشعارات/Leagues/heart refill، بطاقة "إدارة المدراء"). وإلا تخطَّ وسجّل ذلك. Commit `feat(admin-web): settings page on live API`.

## T11 — F1 Deploy kit
كما في Plan 2 F1 حرفياً (الخطوة 1: تأكيد قدرات dockhosting.dev والتوقف إن لم يدعم Docker). release `BASE_URL` يُحدَّث هنا. Commit `build: production build and dockhosting deploy kit`.

### ⏸ نقطة توقف المالك 3

## T12 — F2 Verification & docs
كما في Plan 2 F2 حرفياً، مع تحديث `AGENTS.md` (throttle الـ completion أصبح من الإعدادات؛ BASE_URL في buildTypes) وREADMEs الثلاثة. Commit `docs: run and verification docs for the full system`.

---

## ترتيب التنفيذ
T0 → T1 → T2 → T3 → ⏸ → T4 → T5 → T6 → T7 → ⏸ → T8 → T9 → T10 → T11 → ⏸ → T12

## التحقق في كل مهمة
- API: `cd dxn-api && php artisan test` (يجب أن يزيد العدد ولا ينقص).
- Web: `cd dxn-admin-web && npm run build && npm run lint`.
- Android: `.\gradlew.bat :app:assembleDebug` مع JAVA_HOME أعلاه.
- يدوياً على MySQL حيث تطلب الخطة (Arabic round-trip في T12).
- لا تدّعِ نجاح أي أمر لم يُشغَّل.
