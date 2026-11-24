# Plan 5 — تجهيز «حكاية تميّز» للنشر والاستخدام

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans أو superpowers:subagent-driven-development. خطوات `- [ ]`. **commit واحد لكل مهمة**، لا push ولا merge إلا في R10 وبموافقة المالك. المهام المعلَّمة **[المالك]** لا ينفّذها الوكيل. توقّف عند ⏸.

**Goal:** إغلاق بنود `docs/superpowers/reviews/2026-11-24-readiness-analysis.md` (K-1..K-5، I-1..I-9) ليُنشر الـ API واللوحة على DockHosting، ويُوزَّع تطبيق Android موقّعاً، ويبدأ إطلاق تجريبي.

**Architecture:** بلا تغيير: Laravel 13 (`dxn-api/`) يخدم `/api` واللوحة تحت `/admin/` من صورة Docker واحدة (`deploy/Dockerfile`)؛ MySQL مُدار من DockHosting؛ Android يتصل بـ `DXN_RELEASE_BASE_URL`.

**Tech Stack:** PHP 8.3 / Laravel 13 / Sanctum / MySQL 8 (مُدار)، React 19 + Vite، Kotlin/Compose/Retrofit، GitHub Actions (جديد، بلا تكلفة مكتبات).

**Spec:** `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md` + التحليل أعلاه + `deploy/README.md` (مصدر قدرات المضيف).

## Current state
- الفرع `feat/plan1-api-content`، 62 commit فوق `main`، شجرة نظيفة، آخر commit `c91cea4`.
- 135 اختباراً ناجحاً؛ اللوحة وAndroid debug يبنيان.
- Docker غير مثبت محلياً؛ النشر لم يُجرَّب.

## Global Constraints
- العقد كما في `AGENTS.md`: camelCase، ids نصية، JSON غير مغلّف (عدا `/auth/me`)، كل endpoint جديد له feature test.
- لا أسرار في المستودع أو السجلات أو رسائل الـ commit: keystore وكلمات مروره، `APP_KEY`، بيانات قاعدة الإنتاج، `ADMIN_PASSWORD`.
- لا مكتبات Composer/npm/Gradle جديدة دون موافقة المالك.
- لا migrate ولا seed على قاعدة الإنتاج من الوكيل؛ ذلك عبر `entrypoint.sh` أو المالك.
- أي اختصار مقصود: `shortcut: <limit>, <when to upgrade>`.
- قدرات DockHosting: فقط ما في `deploy/README.md` وروابطه؛ غير ذلك "verify with host docs".

## Review Focus
1. مستخدم يحذف حسابه → تختفي بياناته الشخصية ولا يستطيع الدخول بالتوكن القديم، ولا تنكسر لوحة القائد/التحليلات. *(R5 tests)*
2. `bundleRelease` بدون keystore → **يفشل** برسالة واضحة، لا ينتج AAB موقّعاً بـ debug. *(R0)*
3. استجابة HTML اللوحة تحمل CSP تمنع inline scripts خارجية؛ واستجابات `/api` تحمل `X-Content-Type-Options: nosniff`، والإنتاج يحمل HSTS. *(R2 tests)*
4. شهادة تُمنح → تظهر في Profile دون إعادة تشغيل التطبيق. *(R1 manual)*
5. بعد نشر جديد تبقى الصور المرفوعة سابقاً متاحة (أو تُمنع الرفع مع رسالة إن لم يتوفر تخزين دائم). *(R4)*

---

## R0 — توقيع release آمن

**Files:** Modify `DXNTrainingApp/app/build.gradle.kts` (`:61-70`, `:131-139`)، `DXNTrainingApp/README.md`.
- [ ] احذف fallback التوقيع بـ debug: `signingConfig = signingConfigs.getByName("release")` حين `hasKeystore`، وإلا لا تعيّن `signingConfig`.
- [ ] في `gradle.taskGraph.whenReady` (بجانب فحص الـ placeholder): إن كانت المهام تتضمن `bundleRelease` أو `assembleRelease` و`!hasKeystore` → `throw GradleException("Release signing keystore missing: copy keystore.properties.example to keystore.properties (outside git) — see README.")`.
- [ ] README: شرح `keystore.properties` (مسارات فقط، بلا قيم) والتنبيه بحفظ الـ keystore وكلمة مروره خارج المستودع مع نسخة احتياطية — فقدانه يمنع تحديث التطبيق على Play للأبد.
- [ ] تحقق: `.\gradlew.bat :app:assembleDebug` → PASS؛ `.\gradlew.bat :app:bundleRelease` بدون keystore و`DXN_RELEASE_BASE_URL=https://example.invalid/` → **FAIL** بالرسالة أعلاه.
- [ ] Commit `build(android): fail release builds without a signing keystore`

## R1 — تحديث الشهادات في Profile

**Files:** Modify `DXNTrainingApp/app/src/main/java/com/dxn/training/ui/viewmodel/ProfileViewModel.kt`، والمكان الذي يعرض Profile (verify: `ui/screens/ProfileScreen.kt`، `ui/navigation/AppNavigation.kt`)، وربما `data/repository/ContentRepository.kt`.
- [ ] أعد إنتاج الخلل (من `plan2-verification.md:84`): أكمل اختبار القسم النهائي → افتح Profile → 0 شهادات.
- [ ] السبب المتوقع (verify): `ProfileViewModel` يحمّل `sections-progress` مرة واحدة عند الإنشاء ويُحتفظ به. الإصلاح الأصغر: اجعل الشهادات مشتقة من حالة `ContentRepository` المشتركة (الـ singleton الذي يُحدَّث بعد كل إكمال عبر `refreshAll`) بدل نسخة محلية، أو أعد التحميل عند ظهور الشاشة (`LaunchedEffect(Unit) { viewModel.refresh() }`). اختر الأول إن كانت البيانات متاحة هناك.
- [ ] تحقق: `assembleDebug`؛ يدوياً على المحاكي: إكمال final quiz → Profile يعرض الشهادة فوراً.
- [ ] Commit `fix(android): profile reflects newly issued certificates`

## R2 — تقوية أمان الإنتاج في Laravel

**Files:** Create `dxn-api/app/Http/Middleware/SecurityHeaders.php`، `dxn-api/tests/Feature/SecurityHeadersTest.php`. Modify `dxn-api/bootstrap/app.php` (تسجيل عام)، `deploy/.env.production.example`، `deploy/README.md`.
- [ ] Middleware عام: `X-Content-Type-Options: nosniff`، `X-Frame-Options: DENY`، `Referrer-Policy: strict-origin-when-cross-origin`، `Permissions-Policy: camera=(), microphone=(), geolocation=()`؛ وإن `app()->isProduction()` و`$request->isSecure()`: `Strict-Transport-Security: max-age=31536000; includeSubDomains`.
- [ ] لاستجابات اللوحة (`AdminSpaController`، verify): `Content-Security-Policy: default-src 'self'; img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'` — `style-src 'unsafe-inline'` مطلوب غالباً لـ Recharts/Tailwind runtime styles (verify بفتح اللوحة المبنية وعدم ظهور أخطاء CSP في console؛ إن لزم أضف `// shortcut: unsafe-inline styles for chart lib, tighten with nonces later`).
- [ ] خلف proxy المضيف: `trustProxies(at: '*')` في `bootstrap/app.php` إن لم يكن موجوداً (verify with host docs أن الـ TLS ينتهي عند المضيف) كي يعمل `isSecure()` وروابط `APP_URL`.
- [ ] `.env.production.example`: تأكد `APP_DEBUG=false`، `APP_ENV=production`، `LOG_LEVEL=warning`، `SESSION_SECURE_COOKIE=true`؛ وفي README خطوة «بعد أول دخول: غيّر كلمة مرور المدير من اللوحة ثم احذف `ADMIN_PASSWORD` من Variables».
- [ ] قرار مسجَّل: توكنات الموبايل بلا انتهاء (`sanctum.expiration = null`) — مقبول لأن الخروج والتعطيل يلغيانها؛ أضف ذلك لـ README.
- [ ] Tests: `test_api_response_has_nosniff_and_frame_deny`، `test_admin_spa_has_csp`، `test_hsts_only_in_production_over_https`.
- [ ] `php artisan test` PASS؛ `npm run build` ثم `php artisan serve` وفتح `/admin/` → لا أخطاء CSP في console.
- [ ] Commit `feat(api): security headers and production config hardening`

### ⏸ نقطة توقف المالك 1 — مراجعة R0–R2

## R3 — CI على GitHub Actions

**Files:** Create `.github/workflows/ci.yml`. Verify/decide: `QA_DELIVERABLES/dxn-playwright/.github/workflows/playwright.yml` (داخل مجلد فرعي فلا يعمل؛ اتركه وسجّل ذلك).
- [ ] jobs (على `push` و`pull_request`):
  - `api`: `shivammathur/setup-php@v2` (php 8.3، extensions `mbstring, intl, pdo_sqlite`) → `composer install` في `dxn-api` → `cp .env.example .env && php artisan key:generate` → `php artisan test`.
  - `web`: `actions/setup-node@v4` (node 22) → `npm ci && npm run build && npm run lint` في `dxn-admin-web` (مع `VITE_API_URL=http://localhost`).
  - `android`: `actions/setup-java@v4` (temurin 17) → `./gradlew :app:assembleDebug` في `DXNTrainingApp` (احتاج `chmod +x gradlew`).
  - `docker`: `docker build -f deploy/Dockerfile .` (بدون push) — **هذا يغلق K-3 جزئياً**.
- [ ] لا أسرار في الـ workflow.
- [ ] تحقق: لا يمكن تشغيله قبل الدفع — سجّل **NOT RUN** حتى R10؛ افحص صياغة YAML محلياً (`python -c "import yaml…"` إن توفر، أو قراءة دقيقة).
- [ ] Commit `ci: build and test api, admin web, android and docker image`

## R4 — استمرارية الوسائط

**قرار المالك أولاً:** (أ) تفعيل Persistent storage المدفوع في DockHosting (الأبسط، بلا كود)، أو (ب) تخزين خارجي S3-compatible (يحتاج `league/flysystem-aws-s3-v3` — مكتبة جديدة تحتاج موافقة).
- [ ] **(أ)** `deploy/README.md`: نقطة التركيب = `/var/www/html/dxn-api/storage/app/public` (verify المسار في `deploy/Dockerfile`)، و`storage:link` في `entrypoint.sh` قائم. تحقق بعد النشر: ارفع صورة → أعد النشر → الصورة ما زالت تُعرض.
- [ ] **(ب)** إن اختيرت: `FILESYSTEM_DISK=s3` + متغيرات `AWS_*` في `.env.production.example`، و`MediaService` (موجود) يستخدم `Storage::disk(config('filesystems.default'))` — verify أنه لا يفترض `public`؛ test بـ `Storage::fake('s3')`.
- [ ] في الحالتين: إعداد `MEDIA_UPLOADS_ENABLED` (افتراضي true)؛ إن false يعيد `POST /api/admin/media` 503 `{message:'رفع الصور غير مفعّل على هذا الخادم'}` واللوحة تعرض الرسالة. Test: `test_media_upload_disabled_returns_503`.
- [ ] Commit `feat(api): media persistence configuration`

## R5 — الخصوصية والشروط وحذف الحساب

**Files:** Create `dxn-api/resources/views/legal/{privacy,terms}.blade.php`، routes ويب `GET /privacy`, `GET /terms` في `dxn-api/routes/web.php` (قبل SPA fallback)، migration `add_consent_and_deleted_fields_to_users_table` (`terms_accepted_at` timestamp nullable)، `app/Http/Controllers/AccountController.php`، tests `tests/Feature/AccountDeletionTest.php`، `tests/Feature/LegalPagesTest.php`. Modify `RegisterRequest.php`، `AuthController.php`، Android `ui/screens/RegisterScreen.kt`، `ui/screens/ProfileScreen.kt`، `data/remote/ApiService.kt`.
- [ ] نصوص الصفحات: **مسودة يكتبها الوكيل ويعتمدها المالك** (ليست استشارة قانونية): البيانات المجمعة (الاسم، البريد/الهاتف، رقم عضوية DXN مشفّر، الجنس، التقدّم، أحداث الاستخدام)، الغرض، مدة الاحتفاظ، الحذف، جهة التواصل (`<owner-contact>` placeholder يملؤه المالك). عربية، RTL، بهوية «حكاية تميّز».
- [ ] التسجيل: `acceptTerms` `accepted` إلزامي → يُخزَّن `terms_accepted_at = now()`؛ Android: checkbox «أوافق على سياسة الخصوصية والشروط» مع رابطين يفتحان `BASE_URL/privacy` و`/terms`؛ الزر معطّل حتى الموافقة.
- [ ] `DELETE /api/account` (auth، throttle `3,1`) بجسم `{password}`: يتحقق من كلمة المرور، يحذف كل التوكنات، **يُخفي الهوية** (anonymize) بدل الحذف الصلب للحفاظ على سلامة التحليلات والفرق: `name = 'مستخدم محذوف'`، `email/phone/dxn_id/dxn_id_hash/gender = null`، `password = random`، `is_active = false`، `sponsor_id = null`؛ ويحذف صفوفه في `analytics_events` و`user_level_progress` و`certificates` (قرار المالك: حذف أم إبقاء مجهول — الافتراضي: حذف) → 204. المدير لا يستطيع حذف نفسه بهذه الطريقة (422).
- [ ] صفحة ويب `GET /account/delete` تشرح طريقة الحذف من التطبيق (Google Play يطلب رابط ويب لطلب الحذف — verify with Play policy).
- [ ] Android Profile: زر «حذف الحساب» → تأكيد + إدخال كلمة المرور → `DELETE api/account` → مسح الجلسة → شاشة الدخول.
- [ ] Tests: `test_register_requires_accept_terms`، `test_delete_account_anonymizes_and_revokes_tokens`، `test_delete_requires_correct_password`، `test_deleted_user_cannot_login`، `test_admin_cannot_self_delete`، `test_leader_team_excludes_deleted_member`، `test_privacy_and_terms_pages_200`.
- [ ] `php artisan test` PASS؛ `assembleDebug`؛ فحص يدوي على المحاكي.
- [ ] Commit `feat: privacy policy, terms consent and account deletion`

## R6 — إصدار Android

- [ ] **[الوكيل]** `targetSdk`/`compileSdk`: تحقق من الحد الأدنى الحالي لـ Google Play (verify in Play Console / وثائق Android الرسمية) وارفعه إن لزم مع ترقية AGP/مكتبات compose المطلوبة فقط؛ `assembleDebug` + فحص يدوي سريع للشاشات الرئيسية بعد الرفع.
- [ ] **[الوكيل]** `versionCode = 2`، `versionName = "1.0.0"` (وتوثيق قاعدة الرفع لكل إصدار في README).
- [ ] **[الوكيل، بعد R7]** تحقق R8: release يمنع cleartext، لذا يُختبر مقابل خادم الإنتاج HTTPS. ابنِ `assembleRelease` بمفتاح اختبار مؤقت يُنشأ في مجلد مؤقت خارج المستودع ويُحذف بعدها، مع `DXN_RELEASE_BASE_URL=https://<domain>/`. افحص على جهاز/محاكي: الدخول، الشجرة، إكمال مستوى، Profile (Gson + R8 أكثر ما يُكسر). أي `ClassCastException` أو حقول null → قواعد keep في `proguard-rules.pro`.
- [ ] **[المالك]** إنشاء keystore الإنتاج بـ `keytool` (أمر في README)، حفظه وكلمة مروره خارج المستودع + نسخة احتياطية آمنة؛ إنشاء `keystore.properties` محلياً.
- [ ] **[المالك]** `.\gradlew.bat :app:bundleRelease` مع `DXN_RELEASE_BASE_URL=https://<domain>/` → رفع AAB إلى Play Console (Internal testing أولاً) مع رابط سياسة الخصوصية ورابط حذف الحساب.
- [ ] Commit (للجزء الآلي) `build(android): release version and target sdk`

### ⏸ نقطة توقف المالك 2 — قبل النشر

## R7 — النشر على DockHosting **[المالك، بإرشاد الوكيل]**
الوكيل يحدّث `deploy/README.md` بقائمة تحقق مرقّمة؛ المالك ينفّذ:
- [ ] إنشاء قاعدة MySQL 8.0 المُدارة؛ نسخ `deploy/.env.production.example` إلى Variables (`APP_KEY` جديد عبر `php artisan key:generate --show` محلياً، `APP_URL`/`FRONTEND_URL` = النطاق، `DB_*` من المضيف).
- [ ] تفعيل التخزين الدائم حسب R4.
- [ ] أول نشر من جذر المستودع (يبني `deploy/Dockerfile`) → السجلات تُظهر `migrate --force` بنجاح.
- [ ] مرة واحدة من console المضيف: `php artisan db:seed --class=AdminSeeder --force` ثم `--class=ContentSeeder --force` (و`SettingsSeeder` إن لزم — verify).
- [ ] الدخول إلى `/admin/` → تغيير كلمة مرور المدير → حذف `ADMIN_PASSWORD` من Variables → إعادة النشر.
- [ ] DNS وHTTPS (verify with host docs).
- [ ] Smoke test (الوكيل يكتب الأوامر في README؛ كلها مع `-H "Accept: application/json"`): `GET /up` → 200؛ `GET /api/app/config` → 200 والاسم «حكاية تميّز»؛ `POST /api/auth/login` بمستخدم خاطئ → 401؛ رؤوس R2 موجودة (`curl -I`)؛ `/admin/` يفتح ويسجّل الدخول؛ `/privacy` 200.
- [ ] Commit (README فقط) `docs(deploy): production go-live checklist and smoke tests`

## R8 — النسخ الاحتياطي والمراقبة
- [ ] **[المالك]** تفعيل النسخ الاحتياطي اليومي لـ MySQL المُدار إن وفّره المضيف (verify with host docs)؛ وإلا: الوكيل يكتب `deploy/backup.sh` (`mysqldump --single-transaction` مضغوط) مع خطوات تشغيله من جهاز المالك/مهمة مجدولة، بدون كلمات مرور داخل الملف (تُقرأ من env).
- [ ] **[المالك]** اختبار استعادة واحد على قاعدة محلية (`dxn_restore_test`) وتسجيل النتيجة.
- [ ] مراقبة uptime خارجية على `/up` (خدمة يختارها المالك)؛ السجلات عبر `LOG_CHANNEL=stderr` في لوحة المضيف.
- [ ] `deploy/README.md`: قسم «النسخ والاستعادة» و«ماذا تفعل عند العطل» (rollback = إعادة نشر commit سابق؛ تحذير: migrations لا تُعكس تلقائياً).
- [ ] Commit `docs(deploy): backup, restore and monitoring runbook`

## R9 — مراجعة المحتوى والإطلاق التجريبي
- [ ] **[المالك]** مراجعة كل المحتوى المبذور من اللوحة (الأقسام الخمسة، البطاقات، الأسئلة، أرقام PV/SV والعمولات، المعلومات الصحية) وتعديله أو إخفاؤه (`draft`).
- [ ] **[المالك]** تعديل «هوية الفريق» (الاسم، النبذة، الترحيب) من الإعدادات.
- [ ] **[المالك]** إنشاء قائد واحد ورقم عضويته، و5–10 متدربين تجريبيين عبر Internal testing في Play.
- [ ] Checklist قبول (الوكيل يكتبه في `docs/superpowers/plans/2026-11-24-pilot-checklist.md`): تسجيل بالقائد، إكمال قسم وإصدار شهادة، ظهورها في Profile، لوحة القائد، تقارير اللوحة، حذف حساب تجريبي، تغيير النبذة وظهورها.
- [ ] Commit `docs: pilot acceptance checklist`

### ⏸ نقطة توقف المالك 3 — قبل الدمج

## R10 — الدمج والدفع **[بموافقة المالك فقط]**
- [ ] بعد موافقة صريحة: `git checkout main && git merge --no-ff feat/plan1-api-content` → `git push origin main` (المستودع الخاص `baithanoun/dxn_training`).
- [ ] تأكد أن CI (R3) نجح على `main`؛ حدّث `AGENTS.md` (الفرع الرئيسي، CI) وسجّل نتيجة R3 في التحليل.
- [ ] لا force push، ولا حذف للفرع قبل نجاح CI.

## ترتيب التنفيذ
R0 → R1 → R2 → ⏸ → R3 → R4 → R5 → R6 → ⏸ → R7 → R8 → R9 → ⏸ → R10

## من ينفّذ
| المهمة | الوكيل | المالك |
|---|---|---|
| R0–R3 | ✓ | مراجعة |
| R4 | الكود/التوثيق | اختيار (أ)/(ب) |
| R5 | ✓ | اعتماد النصوص وجهة التواصل، قرار حذف البيانات |
| R6 | الجزء الآلي | keystore، bundleRelease، Play Console |
| R7 | README | التنفيذ كاملاً |
| R8 | السكربت/التوثيق | تفعيل النسخ، اختبار الاستعادة، المراقبة |
| R9 | checklist | المراجعة والتجربة |
| R10 | التنفيذ | الموافقة |
