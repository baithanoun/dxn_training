تم إنجاز ما يلي بناءً على طلبك

## التحديث الأخير: دمج متطلبات DXN Training v3.0 + تحسينات QA/UI-UX + ربط Backend API

تم تحديث التطبيق ولوحة الإدارة ليتوافقا مع **SRS v3.0**، ثم تم تطبيق تحسينات ضمان الجودة وتجربة المستخدم بناءً على المراجعة النقدية.

### 1. تطبيق Android (DXNTrainingApp)

- تحديث هوية الألوان لتتوافق مع DXN.
- تحديث البيانات الوهمية إلى **5 أقسام تدريبية** مبنية على الحقائب الخمس.
- إصلاح منطق القفل التسلسلي: القسم التالي لا يُفتح إلا بعد **إكمال القسم السابق وإصدار شهادته**.
- إضافة **اختبار إتمام القسم** (final quiz) كشرط لإصدار الشهادة (`passedFinalQuiz`).
- تحديث `HomeScreen`:
  - بطاقات الأقسام مع حالة القفل والشهادة.
  - عرض Snackbar يوضح للمستخدم القسم المطلوب عند الضغط على قسم مقفل.
- تحديث `LearnScreen` لتصفية المستويات حسب الأقسام المفتوحة فقط.
- تحسين **Touch Targets** للمهارات والمستويات لتكون 48dp/56dp.
- تحديث `CompletionScreen`:
  - إظهار رسالة واضحة عند الرسوب.
  - عرض الشهادة عند إكمال القسم.
  - تعطيل زر "المستوى التالي" حتى النجاح.
- تحسين **Error Handling** في Lesson/Quiz/ChatBattle/MatchPairs/OrderSequence لعرض "المستوى غير متاح" بدلاً من crash.
- إصلاح `ChatBattleScreen`:
  - إعادة تعيين المحادثة عند تغيير `levelId`.
  - استدعاء `updateLevelProgress` لضمن منح الشهادة عندما يكون Chat Battle هو final quiz.
- تحديث `HomeScreen`:
  - فتح أول مستوى في القسم تلقائياً عند الضغط على قسم متاح لأول مرة.
- **ربط تطبيق Android بـ Backend API**:
  - إضافة Retrofit + Gson + OkHttp Logging Interceptor.
  - عنوان السيرفر قابل للتغيير من `app/build.gradle.kts` (`BASE_URL`).
  - `ApiService` مع endpoints: `/api/sections/tree`, `/api/users/{userId}/progress`, `/api/users/{userId}/sections-progress`, `/api/levels/{levelId}/complete`, `/api/users/{userId}/profile`.
  - `NetworkModels.kt` يحول استجابات JSON إلى نماذج التطبيق.
  - `ContentRepository` يحاول جلب البيانات من السيرفر أولاً، ثم يعود إلى `MockData` عند الفشل.
  - `HomeViewModel` يحمّل البيانات تلقائياً عند الإطلاق ويعرض حالات التحميل/الخطأ.
  - `ProfileViewModel` يحمّل بيانات المستخدم من API.
  - جميع شاشات الألعاب تستخدم نفس instance من `ContentRepository` عبر `AppNavigation`.
- **تم بناء APK debug بنجاح**.
- **إصلاحات مراجعة QA (Android)**:
  - معالجة `CancellationException` (structured concurrency).
  - إصلاح Mock fallback (تقرير حالة الشبكة الفعلية).
  - حماية من مسح التقدم (guard empty server responses).
  - دمج تقدم السيرفر (merge بدلاً من replace).
  - تفعيل `submitLevelCompletion` (مزامنة مع API).
  - استخدام `MutableStateFlow.update` (atomic).
  - Singleton `ContentRepository` (تجنب split-brain при config change).
  - توجيه `Level.type` (جعل 4 شاشات ألعاب قابلة للوصول).
  - إصلاح `OrderSequenceScreen` (إعادة ترتيب + تمرير `passThreshold`).
  - إصلاح `passThreshold` (ChatBattle، CompletionScreen).

### 2. لوحة إدارة الويب (dxn-admin-web)

- تحديث `tailwind.config.js` بألوان DXN الجديدة.
- تحديث `mockData.ts` إلى 5 أقسام + حقائب + شهادات + قواعد القفل.
- تحديث `Content.tsx`:
  - عرض الحقيبة والشهادة والشرط السابق للقفل.
  - إضافة **Empty State** عند عدم وجود أقسام.
- تحديث `Overview.tsx`:
  - تقدم الحقائب الخمس محسوب من بيانات الشهادات (بدلاً من `Math.random()`).
  - الشهادات الصادرة حسب القسم.
- تحديث `Users.tsx`:
  - تقدم الحقائب الخمس محسوب بشكل ثابت من XP.
  - الشهادات تظهر "منجزة / قيد التقدم / مقفل" بدلاً من قيمة عشوائية.
- تحديث `ScenarioBuilder.tsx`:
  - درج إعدادات (linked bag/section/skill + حد الاجتياز).
  - Validation تلقائي + **حماية من XSS** (`hasXss`).
  - **تأكيد قبل النشر/إلغاء النشر وحذف الخيار** (`window.confirm`).
- تحديث `AdminLayout.tsx` بشعار DXN النصي والألوان الجديدة.
- تحديث `Teams.tsx` بألوان التقدم المحسّنة.
- **إصلاحات مراجعة QA (Web)**:
  - إصلاح `selectedLevel` stale (إعادة تعيين при تغيير القسم/المهارة).
  - إضافة fallback عند عدم العثور على المستوى.
  - إضافة `key={level.id}` (منع stale form values).
  - إصلاح `iconMap[stat.icon]` (fallback إلى `Users`).
  - تغيير `key={idx}` إلى `key={stat.title}`.
  - تغيير `lang="en"` إلى `lang="ar" dir="rtl"` في index.html.
- **تم بناء الموقع بنجاح**.
- **تم التقاط لقطات شاشة** لصفحات الويب الرئيسية.

## مجلدات المشروع

- `designs/` — رسمات الشاشات الأولية (16 صورة PNG).
- `DXNTrainingApp/` — مشروع Android Studio الكامل.
- `dxn-admin-web/` — موقع إدارة الويب الكامل.
- `srs_v3_clean.md` — نص SRS v3.0 المستخرج.
- `DXN_Training_v3.0_Merge_Plan.md` — خطة الدمج والفجوات.
- `web_*.png` — لقطات شاشة من الويب.

## الشاشات المنجزة في موقع الإدارة

- نظرة عامة (Overview Dashboard) مع KPIs ورسوم بيانية وشهادات.
- إدارة المحتوى التدريبي (شجرة الأقسام والحقائب والشهادات والقفل).
- منشئ السيناريوهات المرئي (Visual Scenario Builder) مع معاينة حية وإعدادات وتحقق.
- المستخدمون (إدارة الحسابات وتفاصيل التقدم والشهادات).
- الفرق (متابعة أداء القادة والأعضاء).
- التحليلات (Funnel، Retention، أكثر الأسئلة رسوباً، تصدير).
- الإعدادات (عامة، تلعيب، إشعارات، أمان).

## الشاشات المنجزة في التطبيق

- شاشة البداية (Splash) بهوية DXN.
- Onboarding (3 صفحات) بألوان DXN.
- تسجيل الدخول / إنشاء حساب.
- الرئيسية (مسار التدريب — 5 أقسام + قفل + شهادات).
- التعلم (متابعة المستويات حسب الأقسام المفتوحة).
- الدرس (بطاقات Micro-Learning).
- اختبار الاختيار من متعدد (MCQ).
- محاكي المحادثة الحي (Chat Battle) مع Trust Bar.
- مطابقة البطاقات (Match Pairs).
- ترتيب الخطوات (Order Sequence).
- شاشة النتيجة والمكافآت.
- الملف الشخصي.
- لوحة متابعة الفريق.
- لوحة الإدارة — نظرة عامة.

## كيفية التشغيل

### Android

1. افتح Android Studio.
2. اختر **Open** وحدد مجلد `DXNTrainingApp`.
3. تأكد من استخدام JDK 17+ (تم إعداد `local.properties` لاستخدام JDK محلي في `D:\webserver\appp\jdk17`).
4. شغّل `./gradlew :app:assembleDebug` أو استخدم زر Run.

### الويب

1. انتقل إلى `dxn-admin-web/`.
2. شغّل `npm install` ثم `npm run dev`.
3. افتح `http://127.0.0.1:5173/`.

## التقنيات المستخدمة

- Kotlin 1.9.24
- Jetpack Compose (Material3)
- Navigation Compose
- StateFlow / ViewModel
- Retrofit 2 + Gson + OkHttp Logging Interceptor
- React + TypeScript + Vite + Tailwind CSS
- Recharts
- RTL بالكامل
- بيانات Mock محلية مع Backend API fallback
