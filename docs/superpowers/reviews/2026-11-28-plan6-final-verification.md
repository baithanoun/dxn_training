# Plan 6 — تقرير التحقق النهائي

التاريخ: 2026-11-28
الفرع: `feat/plan6-trainer-notes`
الأساس: `origin/main` @ `aacbb35bb3dc9a671fc40a88a8fccdb9941e463d`
الإصدار: `1.2.0` (`versionCode = 6`)
الحالة: اكتملت كل مراحل Plan 6 وقرارات المالك. لا يوجد merge ولا نشر إلى الخادم/Google Play. الرفع إلى `origin` مسموح من المالك ويُنفّذ بعد هذا الـ commit.

## 1. الـ commits بعد نقطة التوقف 3

| # | Hash | الرسالة |
|---|---|---|
| D3 | `eea237e` | `feat(android): issue certificates as PDF` |
| D4 | `c3e94ea` | `feat(android): play correct-answer sound when asset is available` |
| Nitpick | `779485b` | `fix(android): clear stale invite id after logout` |
| R4 | `929c2c4` | `release(android): 1.2.0` |
| التقرير | (هذا الـ commit) | `docs: add plan6 final verification` |

المراحل A–C وإصدارات 1.0.1/1.0.2/1.1.0 موثّقة في تقارير checkpoint السابقة. كل commit يحمل السطر: `Co-Authored-By: Big Pickle <noreply@anthropic.com>`.

## 2. قرارات المالك المطبّقة

- **D1 Coil:** `io.coil-kt:coil-compose:2.6.0`، نجح بناء release/R8 بدون قواعد ProGuard إضافية.
- **D2 صور المحتوى:** المسار المتفق عليه `dxn-api/public/content-images/`؛ لم تُضف ملفات صور فعلية لأن المالك لم يرفعها بعد.
- **D3 PDF:** الشهادات تُحفظ وتُشارك كملف PDF أفقي A4 عبر `PdfDocument` المدمج في Android، بدون مكتبة جديدة. على API 29+ تُحفظ في `Downloads/HikayaTamayoz`؛ وعلى API 24–28 تبقى داخل التطبيق وتُشارك عبر `FileProvider`.
- **D4 الصوت:** `CorrectAnswerSound` يبحث وقت التشغيل عن `res/raw/correct.ogg`. غياب الملف حالياً no-op آمن، والاهتزاز والقصاصات يعملان.
- **D5 الأوسمة:** نُفّذ الخيار (أ) المقترح: أوسمة تلقائية محسوبة بلا جدول أو endpoint جديد.
- **D6 رابط الدعوة:** `https://dxn-train.dockhosting.cloud/join/<leaderDxnId>`؛ صفحة الويب تعرض الرقم وزر التحميل، والتطبيق يملأ حقل القائد عند فتح الرابط.

## 3. نتائج التحقق

| الفحص | النتيجة |
|---|---|
| `DXNTrainingApp`: `:app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug --no-daemon` | **BUILD SUCCESSFUL** |
| اختبارات instrumented عبر `adb shell am instrument` المباشر | **OK (3 tests)** — PDF header، أبعاد الرسم، وno-op عند غياب الصوت |
| `:app:connectedDebugAndroidTest` عبر Gradle | **فشل بيئي**: `Unable to start the UTP test results listener gRPC server`؛استُبدل بالrunner المباشر أعلاه |
| `dxn-api`: `php artisan test` مع إزالة مؤقتة لـ `latest.json` وإعادته | **PASS: 161 test / 725 assertions** |
| `dxn-admin-web`: `npm run build` | **PASS** مع تحذير حجم chunk فقط |
| `dxn-admin-web`: `npm run lint` | **PASS** مع تحذيراتFast refresh/set-state سابقة غير متعلقة بـ Plan 6 |
| `git diff --check origin/main..HEAD` | نظيف |
| مراجعة تكاملية ل commits ما بعد checkpoint 3 | **APPROVED**، بلا findings جوهرية |
| إصدار 1.2.0 | `BUILD SUCCESSFUL in 1m 46s`؛APK 2.4 MB؛SHA-256 `aa16e7a6d1b2a27b3f90b4d57610b7e91449b619810f7957ecc5d3e6d132cb39`؛حُذف ملف 1.1.0 محلياً؛حُذف ملفات التوقيع المؤقتة |

### فحوص المحاكي (Android Studio AVD `CampManagementEmulator`, `emulator-5554`)

- فتح «ملفي» وإغلاقه 5 مرات: `TRAINEE-E2E-1` ظهر في كل مرة و`crashLines=0`.
- تنقّل 3 أزرار: نص شريط التنقل السفلي بين `y=1773–1815` وأقصى محتوى `y=1857` على شاشة `1080x1920` — لا تغطية لشريط النظام.
- تنقّل بالإيماءات: أقصى محتوى `y=1857` — لا تغطية.
- الوضع الداكن بعد كتابة نص في حقلي الدخول: تباين `17.18:1` و`15.17:1`.
- Arabic round-trip على MySQL 8.4.3 / `dxn_training`: عنوان القسم وعنوان أول بطاقة في `/api/sections/tree` يطابقان `content.json` تماماً، مع `hasArabicCodepoints=true`.
- الرابط العميق: `am start -a VIEW -c BROWSABLE -p com.dxn.training -d https://dxn-train.dockhosting.cloud/join/LEADER123` فتح التسجيل، والحقل الخامس ظهر فيه `LEADER123` (`deepLinkIdVisible=True`).
- لقطات شاشة محلية (غير مودعة في git):
  - `C:\Users\kml\AppData\Local\Temp\opencode\plan6-profile.png`
  - `C:\Users\kml\AppData\Local\Temp\opencode\plan6-dark-login-typed.png`
  - `C:\Users\kml\AppData\Local\Temp\opencode\plan6-deeplink-register.png`

### فحص Playwright

- `GET /join/LEADER123`: العنوان `انضم للفريق — حكاية تميّز`، الرقم `LEADER123` ظاهر، رابط التحميل يشير إلى `hikaya-tamayoz-1.1.0.apk` (محلياً قبل بناء 1.2.0)، وصفر أخطاء console.
- `GET /join/bad_id!!`: `404 Not Found` كما مطلوب.
- ملاحظة: الرابط العام الحالي يعرض 404 في متصفح Chrome لأنه لا توجد صفحة نشر فعلية على النطاق؛ الصفحة تعمل محلياً عبر Laravel، والتطبيق يفتح الرابط مباشرة عند تمريره باسم الحزمة.

## 4. NOT RUN / نقاط مفتوحة

- ملف `correct.ogg` لم يُسلّم بعد؛ الصوت لا يعمل حتى يوضع في `DXNTrainingApp/app/src/main/res/raw/correct.ogg`، ثم يُعاد بناء APK.
- لم يُجرَّب يدوياً على المحاكي: حفظ/مشاركة PDF من واجهة الشهادة، وتدفّق logout بعد deep link؛ يغطيهما اختبار instrumented واختبار الوحدة على التوالي.
- لم تُجرَّب automations التفاعلية (القصاصات/الاهتزاز/تقليل الحركة) على جهاز حقيقي.
- لم يُثبَّت إصدار 1.2.0 على جهاز حقيقي؛ بناء release/R8 نجح فقط.
- لم يُنشر على Google Play، ولم يُرفع APK إلى الخادم؛ التغييرات المحلية في `dxn-api/public/downloads` جاهزة للنشر مع تحديث Plan 2.
- لم تُرفع صور/نصوص المحتوى التي حصرها `docs/superpowers/plans/2026-11-28-plan6-content-needs.md`.

## 5. أسئلة متبقية للمالك

1. هل تريد معالجة `mediaUrl` النسبي بجعل BASE_URL بادئة له، أم سيُرسل API روابط مطلقة دائماً؟
2. هل يبقى إرسال `share_achievement` بعد فتح sharing chooser بنجاح، أم يُرسل قبل الفتح/عند الضغط؟
3. نُفّذ D5 بالأوسمة التلقائية حسب المقترح؛ هل تؤكد الاكتفاء بها أم تريد منح الأوسمة يدوياً من القائد؟
4. إذا كان المقصود بـ "PLAY WRITE" هو Google Play publishing، فذلك لم يُنفّذ؛ أُنجز فحص Playwright لصفحة الدعوة بدلاً منه. الرجاء التأكيد إن كان مطلوباً.
