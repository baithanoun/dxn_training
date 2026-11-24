# DXN Training App — مشروع Android Studio

تطبيق تدريب تفاعلي لنظام DXN بأسلوب التلعيب (Gamification) وشجرة المهارات، متصل بواجهة DXN Laravel API. الخادم هو مصدر الحقيقة للتقدّم والنقاط والشهادات.

## المتطلبات

- Android Studio Hedgehog (2023.1.1) أو أحدث، و Android SDK 34.
- JDK 17 (على هذا الجهاز: `D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7`).
- Kotlin 1.9+ و Gradle wrapper 8.7. ملف `local.properties` (يحتوي مسارات JDK/SDK) مستبعد من Git ويجب أن يشير إلى SDK المثبّت محلياً.
- API يعمل على `http://10.0.2.2:8000/` من المحاكي (راجع `../dxn-api/README.md`).

## فتح المشروع والتشغيل

1. افتح Android Studio واختر **Open** وحدد مجلد `DXNTrainingApp`.
2. انتظر مزامنة Gradle، ثم شغّل محاكياً (Emulator) أو جهازاً حقيقياً.
3. شغّل خادم API من طرفية أخرى: `cd dxn-api && php artisan serve --host=0.0.0.0 --port=8000`.
4. سجّل الدخول أو أنشئ حساباً (التسجيل يتطلب رقم عضوية DXN وكلمة مرور، ويمكن إدخال رقم عضوية القائد اختيارياً لربط المتدرب بفريقه).

## عنوان الـ API (BASE_URL)

في `app/build.gradle.kts` داخل `buildTypes`:

| النمط | القيمة | ملاحظات |
|---|---|---|
| `debug` | `"http://10.0.2.2:8000/"` | `10.0.2.2` هو عنوان الجهاز المضيف من محاكي Android. |
| `release` | `DXN_RELEASE_BASE_URL` أو `https://<domain>/` | يُقرأ من متغير البيئة `DXN_RELEASE_BASE_URL` أو `-PDXN_RELEASE_BASE_URL=...`. طالما بقي `<domain>` سيفشل بناء release عمداً. |

الاتصال بنصّ (cleartext) مسموح **في debug فقط** عبر `app/src/debug/AndroidManifest.xml`؛ لا يُسمح به في إصدار الإنتاج.

## توقيع إصدار النشر

1. أنشئ keystore مرة واحدة (لا تُلتزم في Git):
   ```powershell
   keytool -genkeypair -v -keystore dxn-release.jks -alias dxn -keyalg RSA -keysize 2048 -validity 10000
   ```
2. انسخ `keystore.properties.example` إلى `DXNTrainingApp/keystore.properties` واملأ القيم. الملف مستبعد من Git.
3. ابنِ الإصدار:
   ```powershell
   $env:DXN_RELEASE_BASE_URL = "https://your-real-domain.example/"
   .\gradlew.bat :app:bundleRelease
   ```
   R8/ProGuard مفعّل، وقواعد `app/proguard-rules.pro` تحافظ على نماذج Gson/Retrofit. إن لم يوجد `keystore.properties` يُوقَّع الإصدار بمفتاح debug مؤقتاً — **لا تنشر APK موقّعاً بمفتاح debug**.

## البناء من سطر الأوامر

```powershell
$env:JAVA_HOME = "D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"
cd DXNTrainingApp
.\gradlew.bat :app:assembleDebug --no-daemon
```

آخر تحقق (2026-11-23): `BUILD SUCCESSFUL in 15s` مع JDK 17.0.12. ملف APK الناتج في `app/build/outputs/apk/debug/` (مُستبعد من Git).

## المصادقة والجلسة

- `SessionStore` (DataStore Preferences) يحفظ الرمز ومعرّف المستخدم والدور؛ عند إعادة تشغيل التطبيق يُستأنف التقدم إلى الشاشة الرئيسية إن وُجدت جلسة.
- `ApiClient` يضيف `Authorization: Bearer <token>` و`X-Client: android` إلى الطلبات؛ عند 401 يمسح الجلسة ويعيد المستخدم إلى الدخول. توكن الموبايل بلا تاريخ انتهاء (انتهاء الجلسة يخص لوحة الإدارة فقط).
- رسائل الأخطاء من الخادم تظهر كما هي (مثل "لم يتم العثور على قائد بهذا الرقم" و"رقم العضوية مسجّل مسبقاً")؛ عند تعذّر الاتصال تظهر "تعذر الاتصال بالخادم".

## التقدّم والإكمال من الخادم

- شجرة المحتوى وتقدّم المستويات والأقسام تُقرأ من `/api/sections/tree` و`/api/users/{id}/progress|sections-progress`؛ الفتح والشهادات لا تُحسب محلياً.
- إرسال الإكمال إلى `/api/levels/{id}/complete` يتضمن `answers` لكل جولة (`optionId` أو `orderedOptionIds`)؛ النتيجة والنجاح وXP والـ Streak والشهادة تأتي في الاستجابة. القالب القديم `{userId, score, passed}` مرفوض (422).
- عند فشل الإرسال تظهر شاشة النتيجة "لم تُحفظ النتيجة — تحقق من الاتصال" مع "إعادة المحاولة"، ولا يُضاف XP ولا يُفتح مستوى محلياً.
- `MockData.kt` **للعرض فقط عند تعذّر تحميل الشجرة**، وتظهر لافتة "وضع غير متصل — المحتوى للعرض فقط"؛ ليست مصدراً للحقيقة.
- القلوب (Hearts) محلية لكل محاولة داخل الجهاز؛ الخادم لا يفرضها.

## الأحداث التحليلية

`EventSender` يرسل دفعات إلى `POST /api/events` (معدل `60,1`) للأسماء المسموحة فقط: `app_opened`، `level_started`، `question_answered`، `share_achievement`، `certificate_downloaded`. الإرسال fire-and-forget وفشله يُتجاهل (لا طابور محلي). أحداث `level_completed`/`level_failed` يكتبها الخادم وحده عند إكمال المستوى.

## بنية المشروع

```
DXNTrainingApp/
├── app/
│   ├── src/main/java/com/dxn/training/
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── local/          # SessionStore (DataStore)
│   │   │   ├── model/          # نماذج البيانات (User, Content, Progress, Team)
│   │   │   ├── remote/         # ApiClient, ApiService, NetworkModels, EventSender
│   │   │   └── repository/     # Content/User/Team repositories + MockData (للعرض فقط)
│   │   ├── ui/
│   │   │   ├── components/     # مكونات مشتركة (Buttons, Cards, Progress)
│   │   │   ├── navigation/     # AppNavigation + Screen routes
│   │   │   ├── screens/        # جميع الشاشات
│   │   │   ├── theme/          # Colors, Type, Theme
│   │   │   └── viewmodel/      # ViewModels
│   │   └── res/                # الموارد (الأيقونات، الألوان، الخطوط)
│   ├── src/debug/AndroidManifest.xml   # cleartext للمحاكي فقط
│   └── build.gradle.kts                # BASE_URL في buildTypes
├── designs/                    # رسمات الشاشات الأولية (Wireframes)
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## الشاشات المنجزة

1. Splash
2. Onboarding (3 صفحات)
3. Login / Register
4. Main Screen مع Bottom Navigation
5. Home / Skill Tree
6. Learn (متابعة التعلم)
7. Lesson Cards
8. Quiz (MCQ)
9. Chat Battle Simulator
10. Match Pairs (مطابقة البطاقات)
11. Order Sequence (ترتيب الخطوات)
12. Completion / Result
13. Profile
14. Team Dashboard (للقادة فقط، من `/api/leader/team`)
15. Admin Overview

## نظام التلعيب

- XP والـ Streak والشهادات تُحسب وتُخزَّن في الخادم؛ التطبيق يعرض قيم الاستجابة.
- فتح المستويات/Aقسام يأتي من حالة التقدّم على الخادم.
- شريط الثقة (Trust Bar) في محاكي المحادثة، ومكافآت عند اجتياز المستوى.

## ملاحظات

- يدعم RTL بشكل كامل، ويستخدم Jetpack Compose + Material3.
- السيناريو اليدوي الكامل (تسجيل متدرب، إكمال مستوى، ظهوره لدى القائد، الشهادة وفتح القسم التالي) نُفّذ و**PASS** محلياً؛ التفاصيل في `docs/superpowers/plans/2026-11-21-plan2-verification.md`. ما زال `NOT RUN`: النشر الفعلي وبناء صورة Docker (مسؤولية المالك).
- تعليمات النشر واستبدال رابط الإنتاج في `deploy/README.md`.
