# Plan 6 — تقرير نقطة التوقف 2

التاريخ: 2026-11-28  
الفرع: `feat/plan6-trainer-notes`  
الأساس: `origin/main` @ `aacbb35bb3dc9a671fc40a88a8fccdb9941e463d`  
الحالة: أُنجزت المرحلة B وإصدار 1.0.2 محلياً، مع متابعة المرحلة C بناءً على تعليمات المالك بتأجيل الاستفسارات للنهاية. لا push ولا merge ولا نشر.

## 1. الـ commits

| # | Hash | الرسالة |
|---|---|---|
| R1 | `7e71013458672785eddf320202814695b78dbc65` | `release(android): 1.0.1` |
| B1 | `e9fee37872f833515d5d4e3b7670af5befc2a684` | `feat(android): show the final score as n/100 and percentage` |
| B2 | `fdcb366b09ab548d264231079b637619051b5d3c` | `feat(android): certificate preview, save and share` |
| B3 | `3e73cd5341f7f08a1abc71d58fc26e0a3326042e` | `feat(android): share achievement via the system share sheet` |
| B4 | `e086e7081eacaa8375f8fc192e13769ad4313a94` | `feat: leader invite link and prefilled registration` |
| R2 | (هذا الـ commit) | `release(android): 1.0.2` |

المرحلة A موثّقة في `2026-11-28-plan6-checkpoint-1.md`. كل commit يحمل السطر: `Co-Authored-By: Big Pickle <noreply@anthropic.com>`.

## 2. نتائج أوامر التحقق كما ظهرت

- `DXNTrainingApp`: `$env:JAVA_HOME = "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"` ثم `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL** بعد كل مهمة Android في هذه المرحلة. أُضيف اختبار وحدة لـ B3، واختبار instrumented لـ B2 جُمّع بنجاح عبر `:app:compileDebugAndroidTestKotlin` لكنه لم يُشغّل على جهاز.
- `dxn-api`: `php artisan test` مع الإزالة المؤقتة لـ `public/downloads/latest.json` وإعادته فوراً → **PASS: 161 اختباراً / 725 تأكيداً** بعد B4. طُبّق `vendor/bin/pint --dirty --format agent` بعد تعديل PHP.
- B4 RED/GREEN: اختبارا `JoinPageTest` فشلا قبل إضافة المسار/العرض ثم نجحا بعد التنفيذ.
- `dxn-admin-web`: `npm run build` و`npm run lint` → **PASS** مع تحذيرات oxlint سابقة غير متعلقة بالخطة (لا ملفات واجهة تغيّرت في B).
- R2: `.\scripts\release-android.ps1 -JavaHome "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"` → **BUILD SUCCESSFUL in 1m 50s** (52 مهمة: 22 executed، 30 up-to-date؛ R8/lintVital ناجحان). الناتج المحلي: `hikaya-tamayoz-1.0.2.apk` (2.4 MB) و`latest.json` بـ `versionCode: 4` وSHA-256 `d3d78860f9910bb69529f77a870a34124b264eedcd99574921858d0165c4b6d5`. حُذف APK 1.0.1 من مجلد النشر، وحُذف المفتاحان المؤقتان بعد البناء.
- لا يوجد push أو merge أو نشر إلى الخادم.

## 3. تفاصيل المهام

### B1 — عرض الدرجة بوضوح
- نُفّذ: نص كبير `score / 100` ونسبة `score%` فوق شريط التقدم، بالألوان `LeafText`/`Error`، ونص حالة مجنس «اجتزتَ/اجتزتِ المستوى» أو «تحتاج/تحتاجين <threshold>% للنجاح» من `ContentRepository`.
- التحقق: بناء واختبارات Android ناجحة؛ فحص الكود يؤكد بقاء إعادة المحاولة المفعّلة من A5.
- انحراف: لا يوجد.
- NOT RUN: المظهر البصري على جهاز/محاكي — لا يوجد adb/emulator.

### B2 — شاشة الشهادة + حفظ + مشاركة
- نُفّذ: `CertificateRenderer` يرسم PNG بأبعاد 1600×1131 بالهوية البصرية والخط PlexArabic؛ `CertificateScreen` للمعاينة والحفظ/المشاركة عبر MediaStore على API 29+؛ FileProvider و`res/xml/file_paths.xml`؛ مسار `certificate/{sectionId}`؛ أزرار من شاشة النتيجة ومن كل شهادة في «ملفي».
- التحقق: `:app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL؛ اختبار الأبعاد instrumented جُمّع فقط. بناء release لاحقاً نجح أيضاً (R2).
- انحراف/افتراض: D3 = PNG محلي؛ على API 24–28 تُحفظ الصورة في مجلد التطبيق وتُشارك عبر FileProvider بدل طلب `WRITE_EXTERNAL_STORAGE` (`shortcut` موثّق في الكود ورسالة الـ commit). لم تُضف مكتبة.
- NOT RUN: running instrumented test، المعاينة، الحفظ في المعرض، ومشاركة الصورة على جهاز — لا يوجد جهاز.

### B3 — مشاركة الإنجاز
- نُفّذ: `ShareHelper.shareText` مع `ACTION_SEND` و`createChooser`؛ نص الإنجاز يتضمن الجنس، اسم المستوى، الدرجة، «مع فريق حكاية تميّز» ورابط التحميل من `appDownloadUrl`؛ عند وجود شهادة تُشارك صورتها عبر مسار B2؛ `EventSender.send("share_achievement")` بعد فتح الـ chooser.
- التحقق: اختبار وحدة مركّز لتكوين النص + بناء واختبارات Android ناجحة.
- انحراف/ملاحظة: الحدث يُرسل بعد فتح الـ chooser، وليس بعد إتمام المشاركة فعلياً (لا يمكن رصد نتيجة chooser عبر Android). سجّل للمراجعة النهائية.
- NOT RUN: فتح الـ chooser ومشاركة الصورة على جهاز.

### B4 — رابط الدعوة
- نُفّذ: `GET /join/{dxnId}` مع تحقق `^[A-Za-z0-9-]{1,32}$` و404 وإخراج مُهربّ؛ صفحة تعرض رقم القائد دون اسمه ورابط التحميل؛ intent-filter للرابط، حفظ مؤقت للرقم وملء حقل رقم القائد عند التسجيل؛ بطاقة «رابط دعوتي» مع نسخ/مشاركة و«المسجّلون عبر رابطك» من `/api/leader/team`.
- التحقق: RED/GREEN لاختبارَي `JoinPageTest`؛ API كامل PASS؛ Pint؛ بناء Android ناجح.
- انحراف/افتراض: D6 = `https://dxn-train.dockhosting.cloud/join/<leaderDxnId>` بدون `autoVerify` في البداية، حسب توصية الخطة. لم يُنشئ اسم قائد عام.
- NOT RUN: فتح الرابط من متصفح/واتساب على جهاز، نسخ/مشاركة الرابط، وround-trip عربي على MySQL — لا يوجد جهاز ولا فحص يدوي على الخادم.

### R2 — إصدار 1.0.2
- نُفّذ: `versionCode = 4`، `versionName = "1.0.2"`، بناء Signed APK وتحديث `public/downloads` محلياً.
- التحقق: فُحص `latest.json` وSHA-256 أعلاه؛ لا رفع.
- NOT RUN: تثبيت التحديث على جهاز، وReview Focus 5، وأي فحص نشر.

## 4. لقطات شاشة المحاكي

**NOT RUN** — لا توجد لقطات لأنه لا يوجد adb/emulator متصل. المسارات: لا شيء.

## 5. مراجعة المالك المطلوبة

افحص الفرق والـ commits أعلاه، ثم أكّد القرارات المؤجلة D1–D6 في نهاية التشغيل الكامل. لا يعني بناء الإصدار محلياً أنه نُشر؛ الاستفسارات والقرارات كلها مؤجلة إلى النهاية حسب تعليمات المالك.
