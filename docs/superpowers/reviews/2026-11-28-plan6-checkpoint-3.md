# Plan 6 — تقرير نقطة التوقف 3

التاريخ: 2026-11-28
الفرع: `feat/plan6-trainer-notes`
الأساس: `origin/main` @ `aacbb35bb3dc9a671fc40a88a8fccdb9941e463d`
الحالة: أُنجزت المرحلة C وإصدار 1.1.0 محلياً، مع تأجيل كل الاستفسارات والقرارات إلى نهاية التشغيل حسب تعليمات المالك. لا push ولا merge ولا نشر.

## 1. الـ commits

| # | Hash | الرسالة |
|---|---|---|
| C1 | `68889661d1cd6809cae399f8f450224894ae594b` | `feat(android): show lesson images` |
| C2 | `531cbbc7006ada2f75e11c3b7a6f96a5fd4a02ee` | `docs: add plan6 content needs checklist` |
| C3 | `acc1996e7e70105502a758340bfc39797d0a8152` | `feat(android): celebrate correct answers` |
| C4 | `237986475f5161e7a59cf9b64c6e82146dd09a54` | `feat: leader member details and progress badges` |
| R3 | (هذا الـ commit) | `release(android): 1.1.0` |

المرحلتان A وB موثّقتان في `2026-11-28-plan6-checkpoint-1.md` و`2026-11-28-plan6-checkpoint-2.md`. كل commit يحمل السطر: `Co-Authored-By: Big Pickle <noreply@anthropic.com>`.

## 2. نتائج أوامر التحقق كما ظهرت

- C1 RED/GREEN: اختبار `LessonCardMediaUrlTest` فشل أولاً لغياب `mediaUrl` ثم نجح بعد إضافة الحقل والربط.
- C3 RED/GREEN: اختبار مساعد `confettiEnabled` فشل أولاً ثم نجح بعد إضافة المساعد.
- C4 RED/GREEN: اختبارات عقد `/api/leader/team` و`/api/users/{id}/profile` فشلت قبل التغيير ثم نجحت بعده.
- `dxn-api`: `php artisan test` مع الإزالة المؤقتة لـ `public/downloads/latest.json` وإعادته فوراً → **PASS: 161 اختباراً / 725 تأكيداً**. طُبّق `vendor/bin/pint --dirty --format agent` بعد تعديل PHP.
- `DXNTrainingApp`: `$env:JAVA_HOME = "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"` ثم `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL** بعد كل مهمة C.
- R3: `.\scripts\release-android.ps1 -JavaHome "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"` → **BUILD SUCCESSFUL in 1m 50s** (52 مهمة: 22 executed، 30 up-to-date؛ R8/lintVital ناجحان مع Coil دون قواعد ProGuard إضافية). الناتج المحلي: `hikaya-tamayoz-1.1.0.apk` (2.4 MB) و`latest.json` بـ `versionCode: 5` وSHA-256 `699df3c7833fb8e95ad15300b7c756e113caffbf71b13a68c07105f70aea55e3`. حُذف APK 1.0.2 من مجلد النشر، وحُذف المفتاحان المؤقتان بعد البناء.
- `dxn-admin-web`: لم تتغير ملفات الواجهة في المرحلة C؛ نتائج `npm run build` و`npm run lint` من نقطة التوقف 2 ما زالت **PASS** مع تحذيرات سابقة غير متعلقة بالخطة.
- لا يوجد push أو merge أو نشر إلى الخادم.

## 3. تفاصيل المهام

### C1 — صور الدروس في التطبيق
- نُفّذ: `LessonCardResponse.mediaUrl` و`LessonCard.mediaUrl`، عرض صورة بارتفاع 220 dp فوق نص البطاقة عبر Coil، مع `contentDescription = card.title` وخلفية عاجية أثناء التحميل وعند الفشل بصمت. لم تُضف قواعد ProGuard؛ بناء release ناجح.
- التحقق: RED/GREEN للربط، بناء واختبارات Android ناجحة، بناء release/R8 ناجح في R3.
- انحراف/افتراض: D1 = `io.coil-kt:coil-compose:2.6.0`، D2 = الصور داخل `dxn-api/public/content-images/`، ولم تُرفع صور فعلية في هذا الـ commit. سجّل للمراجعة النهائية.
- ملاحظة مفتوحة: إذا كانت `mediaUrl` القادمة من API نسبية، فلن تحمّلها Coil دون عنوان أساسي؛ القرار مطلوب في النهاية.

### C2 — إثراء المحتوى
- نُفّذ: `docs/superpowers/plans/2026-11-28-plan6-content-needs.md` يحصر البطاقات الـ16 مع أولويات الصور والمراجعة العلمية وروابط الملفات المصدرية.
- التحقق: جرد من `content.json` و`ContentSeeder.php`؛ لا كود ولا اختبارات آلية.
- انحراف: لا يوجد؛ المهمة محتوى بحت كما في الخطة. لم ترفع أصول أو نصوص معتمدة لأنها غير متوفرة في المستودع.
- NOT RUN: رفع الصور/النصوص المعتمدة من المالك أو المدرّبة.

### C3 — تشجيع فوري عند الإجابة الصحيحة
- نُفّذ: `ui/components/Celebration.kt` بقصاصات Canvas وAnimatable لمدة 1.2 ثانية بألوان الهوية، ورسالة مجنسة `أحسنتَ!/أحسنتِ!`، واهتزاز `HapticFeedbackType.LongPress`، مع احترام `ANIMATOR_DURATION_SCALE == 0`. رُبطت في شاشات Quiz وMatchPairs وChatBattle وOrderSequence عند الإجابة الصحيحة فقط.
- التحقق: RED/GREEN للمساعد، بناء واختبارات Android ناجحة، بناء release ناجح.
- انحراف/افتراض: D4 = اهتزاز فقط، بلا `correct.ogg` أو SoundPool، لعدم توفّر ملف صوت مرخّص.
- NOT RUN: الحركة والاهتزاز والرسالة على جهاز حقيقي، ووضع تقليل الحركة في النظام.

### C4 — لوحة القائد + الأوسمة
- نُفّذ: `/api/leader/team` يضيف `badges` و`sections` لكل عضو؛ `/api/users/{id}/profile` يضيف `badges`؛ تفاصيل العضو في `TeamScreen`؛ أوسمة في `ProfileScreen`. القواعد الحاسوبية: `first_section` عند وجود شهادة واحدة، `streak_7` عند current streak >= 7، `all_complete` عند إكمال كل مستوى منشور وحصول الشهادة لكل قسم منشور.
- التحقق: RED/GREEN، API كامل PASS، Pint، بناء Android ناجح.
- انحراف/افتراض: D5 = الخيار (أ) أوسمة تلقائية بلا جدول `badges` ولا endpoint منح يدوي.
- NOT RUN: واجهات التفاصيل والأوسمة على جهاز، وround-trip عربي على MySQL.

### R3 — إصدار 1.1.0
- نُفّذ: `versionCode = 5`، `versionName = "1.1.0"`، بناء Signed APK وتحديث `public/downloads` محلياً.
- التحقق: `latest.json` وSHA-256 أعلاه؛ لا رفع.
- NOT RUN: تثبيت الإصدار على جهاز، وفحص التحديث من 1.0.1/1.0.2، وأي نشر.

## 4. لقطات شاشة المحاكي

**NOT RUN** — لا توجد لقطات لأنه لا يوجد adb/emulator متصل. المسارات: لا شيء.

## 5. مراجعة المالك المطلوبة

افحص الفرق والـ commits أعلاه وأدلة البناء. الاستفسارات والقرارات المؤجلة (D1–D6، مسائل C1/B3/A1، والفحوص اليدوية) مُجمّعة في الرد النهائي، ولم يُتخذ أي إجراء خارج النطاق المحلي.
