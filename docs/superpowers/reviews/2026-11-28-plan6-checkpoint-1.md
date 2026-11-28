# Plan 6 — تقرير نقطة التوقف 1

التاريخ: 2026-11-28  
الفرع: `feat/plan6-trainer-notes`  
الأساس: `origin/main` @ `aacbb35bb3dc9a671fc40a88a8fccdb9941e463d`  
الحالة: إيقاف عند ⏸ نقطة توقف 1 (R1) بانتظار مراجعة المالك. لا push ولا merge ولا إصدار.

## 1. الـ commits

| # | Hash | الرسالة |
|---|---|---|
| A1 | `4218f80169a6760b6990c763f4e5c11362dfd259` | `fix(android): keep content above system bars and keyboard` |
| A2 | `8d1c2d8ea00a4c90dc1dbd300f77a6c49caeb653` | `fix(android): readable input text in light and dark mode` |
| A3 | `2142d1255f1f3842030cd64268df5e12bff8224b` | `fix(android): profile screen no longer crashes on open` |
| A4 | `fc0941af000e89be619bfa4a45f2e7e567209e82` | `fix(android): launcher icon shows the team logo` |
| A5 | `45cc4ffb88e61db7466e97ee835e5a3fe1248e8c` | `fix(android): retry button works after a failed attempt` |

كل commit يحمل السطر: `Co-Authored-By: Big Pickle <noreply@anthropic.com>`.

## 2. نتائج أوامر التحقق كما ظهرت

### الأساس (قبل أي تعديل)
- `dxn-admin-web`: `npm ci` نجح (190 حزمة، 0 ثغرات)؛ `npm run build` نجح؛ `npm run lint` نجح مع تحذيرات oxlint سابقة غير متعلقة بهذه الخطة.
- `dxn-api`: `php artisan test` / `composer test` فشلا أولاً بـ 3 إخفاقات existing في `origin/main`:
  1. `AppConfigTest::test_download_link_and_version_are_editable_and_public` — توقّع `1.2.0` وظهر `1.0.0`.
  2. `AppConfigTest::test_download_link_defaults_to_null` — توقّع `null` وظهر رابط APK من `public/downloads/latest.json`.
  3. `SecurityHeadersTest::test_admin_spa_has_csp` — 500 لأن `public/admin/index.html` غير مبني في worktree جديد.
- بعد موافقة المالك على الحل المحلي: بُنيت الواجهة إلى `dxn-api/public/admin` وإُزيل `public/downloads/latest.json` مؤقتاً أثناء اختبار الـ API فقط ثم أُعيد. النتيجة: **155 اختباراً / 706 تأكيداً — PASS**. لم يُلتزم أي ملف مولّد.
- `DXNTrainingApp`: `$env:JAVA_HOME = "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"` ثم `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL** (44 مهمة، تحذير AGP 8.7 مع compileSdk 36 موجود سابقاً).

### بعد A1–A5
- نفس أمر Android → **BUILD SUCCESSFUL** (44 مهمة: 1 executed، 43 up-to-date).
- `git diff --check origin/main..HEAD` → نظيف.
- مراجعة متكاملة بواسطة `superpawers-reviewer` → **APPROVED**، بلا findings مادية.
- لم تتغير ملفات الـ API أو الواجهة في المرحلة A، لذا بقيت أدلة الأساس صالحة لهما.

## 3. تفاصيل المهام

### A1 — الأزرار فوق شريط النظام
- نُفّذ: لفّ `AppNavigation` في `MainActivity` بـ `Box(Modifier.fillMaxSize().background(BrandIvory).windowInsetsPadding(WindowInsets.safeDrawing))`؛ ضبط `Scaffold(contentWindowInsets = WindowInsets(0))` و`NavigationBar(windowInsets = WindowInsets(0))` في `MainScreen`.
- التحقق: قراءة الشاشات التسع خارج `MainScreen` التي لها أزرار سفلية؛ لم يلزم تعديلها. بناء Android ناجح.
- انحراف: لا يوجد.
- NOT RUN: فحص محاكي Pixel API 35 بالتنقّل بالأزرار وبالإيماءات، والتأكد من عدم تغطية زر «إنشاء الحساب» للوحة المفاتيح — لا يوجد adb/emulator متصل.
- ملاحظة مقبولة: `HomeScreen` ما زال يستخدم `Scaffold` افتراضياً داخل الشاشة، وقد يضيف حشوة علوية مكررة، لكنه لا يخفي الأزرار؛ إصلاحه يتجاوز نطاق A1.

### A2 — وضوح نص الإدخال
- نُفّذ: أُضيف `dxTextFieldColors()` في `ui/components/Common.kt` بالألوان المطلوبة، واستُعمل في جميع حقول `OutlinedTextField` الثلاثة (`LoginScreen`, `RegisterScreen`, حقل حذف الحساب في `ProfileScreen`).
- التحقق: grep يؤكد عدم وجود حقل رابع؛ بناء Android ناجح.
- انحراف: لا يوجد.
- NOT RUN: فحص الوضع الداكن على المحاكي (`adb shell cmd uimode night yes`) — لا يوجد جهاز.

### A3 — «ملفي» يغلق التطبيق
- نُفّذ: استُبدل `LifecycleResumeEffect` بـ `DisposableEffect(LocalLifecycleOwner.current)` مع `LifecycleEventObserver` على `ON_RESUME` وتنظيف المراقب، مع تعليق `shortcut` المطلوب.
- التحقق: grep يؤكد عدم بقاء `LifecycleResumeEffect`؛ بناء Android ناجح.
- انحراف: تعذّر تأكيد السبب بـ logcat قبل الإصلاح؛ نُفّذ الإصلاح استناداً إلى الدليل الثابت في الخطة/الكود، وسُجّل ذلك في رسالة الـ commit.
- NOT RUN: collection of `adb logcat -s AndroidRuntime`؛ فتح/إغلاق «ملفي» 5 مرات؛ الرجوع من الخلفية؛ تدوير الشاشة؛ فحص نسخة release/R8. لا يوجد جهاز/محاكي ولا مفتاح إصدار.
- سبب الانهيار كما ظهر في logcat: **NOT RUN** — لم يُرصد أي نص استثناء لأن logcat لم يُجمع؛ المتوقع وفق الخطة هو `IllegalStateException: CompositionLocal LocalLifecycleOwner not present`.

### A4 — الشعار على أيقونة التطبيق
- نُفّذ: توسيع `docs/brand/build_logo_assets.py` لتوليد foreground شفاف بأحجام 108/162/216/324/432 مع شعار بمساحة 66/108 في الوسط، وأيقونات legacy مربعة ودائرية بأحجام 48/72/96/144/192 على `#F8F8F4`؛ تحديث XMLs إلى `@mipmap/ic_launcher_foreground` مع الإبقاء على `@drawable/ic_launcher_background`؛ حذف `res/drawable/ic_launcher_foreground.xml`.
- التحقق: فاحص Pillow حدد 0 فشل من 15 PNG (الأبعاد، الشفافية، توسيط 66/108، خلفية عاجية، زوايا دائرية شفافة)؛ تشغيل Generator مرتين أعطى ملفات متطابقة بايتاً؛ بناء Android ناجح.
- انحراف: استُخدم Pillow من `.venv` الموجود في `D:\webserver\appp\.venv` لأن worktree لا يحتوي `.venv` محلياً؛ لا مكتبة جديدة أُضيفت للمشروع.
- NOT RUN: تثبيت على المحاكي وفحص المشغّل (دائري/مربع) و«التطبيقات الأخيرة» — لا يوجد جهاز متصل.

### A5 — إعادة المحاولة بعد الرسوب
- نُفّذ: إزالة `enabled = result.response.passed`؛ زر «أعد المحاولة» مفعّل عند الرسوب ويستدعي `onReview`.
- التحقق: `AppNavigation` يعرّف `onReview` كـ `popBackStack(Screen.Lesson.route, false)`؛ مسارات اللعب تُزال inclusively ويبقى `Lesson` في الـ back stack، فيعود المستخدم إلى نفس المستوى. بناء Android ناجح.
- انحراف: لا يوجد؛ استُخدم مسار `onReview` المسموح به في الخطة بدل مسار `onRetry` جديد.
- NOT RUN: فحص إعادة المحاولة يدوياً على المحاكي — لا يوجد جهاز.

## 4. لقطات شاشة المحاكي

**NOT RUN** — لا توجد لقطات شاشة لأنه لا يوجد adb/emulator متصل في هذه البيئة. المسارات: لا شيء.

## 5. ما لم يُنفّذ عند نقطة التوقف

- R1 / إصدار 1.0.1: رفع `versionCode = 3` و`versionName = "1.0.1"`، تشغيل `.\scripts\release-android.ps1`، وcommit الإصدار — كلها **NOT RUN** لأنها تتطلب موافقة المالك والمفتاح/keystore، والخطة تمنع push وmerge دون إذن جديد.
- Review Focus 5 (ترقية مستخدم 1.0.0 إلى 1.0.1 مع بقاء الدخول) — **NOT RUN**.
- المرحلة B فما بعدها — لم تبدأ؛ تتطلب قرارات المالك D1–D6 عند نقاطها المحددة.

## 6. مراجعة المالك المطلوبة

افحص: `git diff origin/main...feat/plan6-trainer-notes`، الـ commits الخمسة، وأدلة البناء أعلاه. بعد الموافقة فقط يمكن المتابعة إلى R1 ثم المرحلة B.
