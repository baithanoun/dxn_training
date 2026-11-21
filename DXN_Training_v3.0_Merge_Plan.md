# خطة دمج متطلبات DXN Training SRS v3.0

## ملخص التغييرات في v3.0

بعد قراءة وثيقة `DXN_Training_App_SRS-v3.0-Merged.docx` ومطابقتها مع ما تم إنجازه، تتركز التغييرات الجديدة في ثلاثة محاور رئيسية:

1. **دمج محتوى الحقائب الخمس (Bag Content)** مع شجرة المهارات، مما أنتج 5 أقسام تدريبية واضحة.
2. **تحديث الهوية البصرية** لتتوافق مع ألوان شعار DXN الرسمية (أحمر + أخضر + أزرق داكن + ذهبي).
3. **قفل المنهج بالترتيب + شهادة إتمام لكل قسم** قبل السماح بالانتقال للقسم التالي.

بقية المتطلبات (محرك الاختبارات، التلعيب، Analytics، Scenario Builder، لوحة الإدارة) تظل كما في v2.5 مع تعديلات طفيفة في الألوان والمحتوى.

---

## حالة التنفيذ الحالية (محدّث)

| المكون | الحالة |
|---|---|
| `designs/` — 16 wireframe | ✅ مكتمل |
| `DXNTrainingApp/` — Android (Kotlin + Compose) | ✅ مكتمل — v3.0 منفّذ + APK debug جاهز |
| `dxn-admin-web/` — لوحة إدارة React | ✅ مكتمل — 7 صفحات أساسية + v3.0 منفّذ |

### صفحات لوحة الإدارة المنجزة

- ✅ نظرة عامة (Overview) — مرتبطة بالحقائب والشهادات
- ✅ المحتوى التدريبي (Content Tree) — 5 حقائب + شهادات + قفل تسلسلي
- ✅ منشئ السيناريوهات (Scenario Builder) — إعدادات + Validation
- ✅ المستخدمون (Users) — تقدم الحقائب والشهادات
- ✅ الفرق (Teams)
- ✅ التحليلات (Analytics) — ألوان DXN الجديدة
- ✅ الإعدادات (Settings)

---

## الفجوات بين v3.0 والتنفيذ الحالي

> **ملاحظة:** جميع الفجوات الجوهرية المذكورة أدناه تم سدّها في التنفيذ الحالي. ما تبقى من مهام هي تحسينات إنتاجية (Backend حقيقي، قوالب PDF للشهادات، شعار DXN الرسمي كملف صورة).

### 1. هيكل المحتوى الجديد (5 أقسام + حقائب) ✅ تم

**ما في v3.0:**

| القسم | المصدر | المحتوى |
|---|---|---|
| القسم 1 | الحقيبة 1 | أساسيات النجاح مع DXN (الشركة، الألوان، الاستثمار، التسجيل، المنتجات الأساسية) |
| القسم 2 | الحقيبة 2 | استثمار الاستهلاك (تحويل المصروفات الشهرية إلى مشروع استثماري) |
| القسم 3 | الحقيبة 3 | نظرية خماسية الصحة وتطبيقاتها (الوجبة الواحدة، الحموضة، الخمائر…) |
| القسم 4 | الحقيبة 4 + الوثيقة | الخطة التسويقية والرتب (PV/SV، الرتب النجمية، محاكي الأرباح) |
| القسم 5 | الحقيبة 5 + الوثيقة | الطريق إلى النجم الماسي + مهارات الميدان (خطة شهرية، قائمة المعارف، الدعوة، الرد على الاعتراضات) |

**الحالي:** 5 أقسام كاملة في Android Mock Data و web mockData.ts.

### 2. نظام الشهادات ✅ تم

**ما في v3.0:**

- شهادة إتمام لكل قسم بعد اجتياز الاختبار.
- القسم التالي لا يُفتح إلا بعد الحصول على شهادة القسم السابق.
- شهادة تخرج نهائية بعد القسم 5.

**الحالي:**
- Android: `SectionProgress` مع `certificateIssued` + `certificateId` يُصدر عند إكمال القسم.
- Web: قائمة `certificates` في `mockData.ts` وعرضها في Overview و Users و Content.

### 3. الهوية البصرية لـ DXN ✅ تم

**ما في v3.0:**

| اللون | الكود | المعنى | الاستخدام |
|---|---|---|---|
| أحمر | `#DC3545` / `#E52F12` | النار والشمس — الإصرار والطموح | الأزرار الرئيسية، Streak، القلوب، التنبيهات المهمة |
| أخضر | `#0A7C4E` / `#2E7D32` | الأشجار والجانوديرما — النمو | شجرة المهارات، النجاح، التقدم، الأيقونات الإيجابية |
| أزرق داكن | `#10284E` / `#1A365D` | الماء والأساس | العناوين، الشريط الجانبي، النصوص الأساسية |
| ذهبي | `#D4A017` | المكافآت والـ XP والأوسمة والشهادات | عناصر التلعيب |

**الحالي:**
- Android: `DXNRed` primary، `DXNGreen` success/progress، `DXNBlue` headers، `DXNGold` XP/certificates.
- Web: `tailwind.config.js` تم تحديثه بالألوان الجديدة، والأزرار والعناوين تستخدمها.

### 4. شعار DXN ✅ تم (نسخة نصية مؤقتة)

**ما في v3.0:** استخدام الشعار الرسمي في شاشة البداية (Splash)، Header التطبيق، ولوحة الإدارة.

**الحالي:**
- Android: شاشة Splash تستخدم نص "DXN" على خلفية حمراء/زرقاء.
- Web: `AdminLayout.tsx` يعرض شعار نصي "DXN" بدلاً من Shield.
- **تحسين مقترح:** استبدال النص بملف SVG/PNG رسمي عند توفره.

### 5. قواعد القفل والترتيب ✅ تم

**ما في v3.0:**

- القسم N+1 يُقفل حتى إتمام القسم N والحصول على شهادته.
- داخل القسم: المستوى N+1 يُقفل حتى إتمام N.

**الحالي:**
- Android: `ContentRepository.computeEffectiveSectionStatus()` + `requiredSectionId`.
- Web: `requiredSectionId` معروض في Content ويُستخدم لإظهار حالة القفل.

---

## خطة العمل المقترحة لدمج v3.0

### المرحلة 1: تحديث الهوية البصرية (أسبوع 1)

**الهدف:** جعل المظهر مطابقاً لهوية DXN الرسمية.

#### مهام الويب (dxn-admin-web)

1. **تحديث ملف الألوان** (`tailwind.config.js`):
   - إضافة الأحمر الرسمي: `dxn-red: "#DC3545"` و `dxn-red-deep: "#E52F12"`.
   - استخدام الأحمر للأزرار الرئيسية (CTA) بدلاً من الأخضر.
   - استخدام الأخضر لشجرة المهارات والتقدم.
   - استخدام الأزرق الداكن للعناوين والشريط الجانبي.
   - الاحتفاظ بالذهبي للـ XP والمكافآت.

2. **إضافة شعار DXN**:
   - إنشاء مجلد `public/logo.svg` أو `public/logo.png`.
   - استبدال أيقونة Shield في `AdminLayout.tsx` بالشعار.
   - إضافة الشعار إلى Header الصفحات الرئيسية.

3. **تحديث الأيقونات والشارات**:
   - القلوب بالأحمر.
   - Streak بالأحمر/البرتقالي.
   - XP بالذهبي.
   - شارات النجاح بالأخضر.

#### مهام Android (DXNTrainingApp)

1. تحديث `Color.kt` أو `Theme.kt` بألوان DXN الرسمية.
2. إضافة شعار DXN إلى `SplashScreen` و`TopAppBar`.
3. تعديل الأزرار الرئيسية لتستخدم الأحمر.
4. تعديل شجرة المهارات لتستخدم الأخضر.

### المرحلة 2: إعادة بناء شجرة المحتوى (أسبوع 2)

**الهدف:** تحويل المحتوى من 3 أقسام إلى 5 أقسام بناءً على الحقائب.

#### مهام الويب

1. **تحديث `src/data/mockData.ts`**:
   - استبدال مصفوفة `sections` الحالية بالهيكل الجديد (5 أقسام).
   - إضافة حقول `bag_id` و `certificate_enabled` لكل قسم.
   - إضافة حقول `required_section_id` و `required_certificate` للقفل التسلسلي.

2. **تحديث صفحة Content Management**:
   - إضافة عرض الحقيبة المصدر (Bag 1-5) لكل قسم.
   - إضافة إعدادات القفل التسلسلي ("يفتح بعد إتمام القسم السابق").
   - إضافة خيار "اختبار إتمام القسم + شهادة".

3. **إضافة نموذج بيانات الشهادة**:
   - `Certificates(id, section_id, title_ar, title_en, template_url, is_active)`.
   - إضافة قسم الشهادات إلى إعدادات القسم.

#### مهام Android

1. تحديث البيانات الوهمية (Mock Data) في ملفات `data/` لتتطابق مع 5 أقسام.
2. تحديث `SkillTreeScreen` لعرض 5 أقسام.
3. إضافة منطق القفل على مستوى القسم (Section Lock).

### المرحلة 3: إدارة الشهادات (أسبوع 3)

**الهدف:** بناء نظام شهادات داخل لوحة الإدارة والتطبيق.

#### مهام الويب

1. **صفحة/قسم جديد: إدارة الشهادات**:
   - جدول الشهادات مع: القسم، عنوان الشهادة، القالب، عدد الصادرة.
   - معاينة الشهادة.
   - رفع/تعديل قالب الشهادة (SVG/HTML).

2. **تحديث صفحة Users**:
   - إضافة تبويب "الشهادات الحاصل عليها" في تفاصيل المستخدم.
   - عرض تاريخ الحصول على كل شهادة.

3. **تحديث Analytics**:
   - إضافة مؤشر "الشهادات الصادرة حسب القسم".
   - إضافة مؤشر "نسبة إكمال القسم + الحصول على الشهادة".

#### مهام Android

1. إضافة شاشة `CertificateScreen`.
2. إضافة زر "تحميل الشهادة" بعد اجتياز اختبار القسم.
3. إضافة `CertificatesRepository` محلي لعرض الشهادات في الملف الشخصي.

### المرحلة 4: تحسين منشئ السيناريوهات (أسبوع 4)

**الهدف:** جعل Scenario Builder يدعم الشخصيات المخصصة والإعدادات العامة.

#### مهام الويب

1. **إضافة Settings Drawer**:
   - اسم الشخصية الافتراضية ووصفها.
   - رفع/اختيار صورة رمزية للشخصية.
   - نسبة الثقة الابتدائية (افتراضي 50%).
   - عدد القلوب الابتدائي (افتراضي 3).
   - شرط الاجتياز (100% ثقة أو عدم فقدان القلوب).
   - مكافأة XP الأساسية + مضاعف الإتقان.
   - ربط السيناريو بمستوى معين.

2. **إضافة Validation**:
   - تنبيه إذا كان هناك خيار بدون نص.
   - تنبيه إذا لم يكن هناك خيار صحيح واحد على الأقل.

3. **الحفظ التلقائي**:
   - حفظ تلقائي كل 30-60 ثانية.
   - اختصارات Ctrl+S / Ctrl+Z.

### المرحلة 5: Backend + API (أسبوع 5-6)

**الهدف:** تحويل البيانات الوهمية إلى API حقيقي.

#### المهام المقترحة

1. تصميم قاعدة البيانات:
   - تحديث جدول `Sections` ليدعم 5 أقسام + `bag_id` + `required_section_id`.
   - إضافة جدول `Certificates`.
   - إضافة `User_Certificates`.
   - إضافة `Content_Versions`.

2. إنشاء Endpoints:
   - `GET /api/sections/tree` — شجرة المحتوى مع حالة القفل.
   - `POST /api/sections/{id}/complete` — إكمال قسم + إصدار شهادة.
   - `GET /api/certificates` — إدارة الشهادات.
   - `GET /api/certificates/{id}/download` — تحميل شهادة PDF.

3. ربط لوحة الإدارة بالـ API:
   - استبدال `mockData.ts` تدريجياً بطبقة API.
   - إضافة React Query أو SWR لإدارة البيانات.

### المرحلة 6: اختبارات ومراجعة (أسبوع 7)

1. اختبار RTL والتصميم العربي.
2. اختبار قواعد القفل التسلسلي.
3. اختبار إصدار وتحميل الشهادات.
4. اختبار Scenario Builder مع الشخصيات المخصصة.
5. مراجعة الألوان مقابل هوية DXN.

---

## أولويات التنفيذ الفورية

إذا كان الهدف هو دمج التغييرات بسرعة مع الحفاظ على الجودة، نوصي بالترتيب التالي:

| الأولوية | المهمة | السبب |
|---|---|---|
| 1 | تحديث الألوان وإضافة شعار DXN | سريع التأثير، ويظهر تطابقاً فورياً مع الهوية |
| 2 | إعادة بناء شجرة المحتوى (5 أقسام) | جوهري لتلبية v3.0 |
| 3 | إضافة قسم إدارة الشهادات | ميزة جديدة واضحة في v3.0 |
| 4 | قواعد القفل التسلسلي | ضروري لتجربة المتدرب الصحيحة |
| 5 | تحسين Scenario Builder | يكتمل فيه الواجهة الإدارية |
| 6 | Backend API | للإنتاج الحقيقي |

---

## الملفات التي تم تعديلها

### dxn-admin-web

```
tailwind.config.js             ✅ ألوان DXN
src/data/mockData.ts           ✅ 5 أقسام + حقائب + شهادات + قفل
src/layouts/AdminLayout.tsx    ✅ شعار DXN نصي + ألوان
src/pages/Content.tsx          ✅ قفل الأقسام + إعدادات الشهادة
src/pages/Overview.tsx         ✅ KPIs مرتبطة بالحقائب والشهادات
src/pages/Analytics.tsx        ✅ ألوان DXN في المخططات
src/pages/Users.tsx            ✅ تقدم الحقائب والشهادات
src/pages/ScenarioBuilder.tsx  ✅ Settings Drawer + Validation
```

### DXNTrainingApp

```
app/src/main/java/.../ui/theme/Color.kt     ✅ ألوان DXN
app/src/main/java/.../ui/theme/Theme.kt     ✅ تحديث الثيم
app/src/main/java/.../data/model/Content.kt ✅ حقول bagId + certificateTitle + requiredSectionId
app/src/main/java/.../data/model/Progress.kt ✅ SectionProgress
app/src/main/java/.../data/repository/MockData.kt      ✅ 5 أقسام
app/src/main/java/.../data/repository/ContentRepository.kt ✅ قفل الأقسام + ربط API + Mock fallback
app/src/main/java/.../data/repository/UserRepository.kt    ✅ تحميل ملف المستخدم من API
app/src/main/java/.../data/remote/ApiClient.kt          ✅ Retrofit Client
app/src/main/java/.../data/remote/ApiService.kt         ✅ Endpoints
app/src/main/java/.../data/remote/NetworkModels.kt      ✅ DTOs + mapping
app/src/main/java/.../ui/viewmodel/HomeViewModel.kt     ✅ تحميل البيانات من Repository
app/src/main/java/.../ui/viewmodel/ProfileViewModel.kt  ✅ تحميل الملف الشخصي من API
app/src/main/java/.../ui/screens/HomeScreen.kt         ✅ بطاقات الأقسام والقفل + loading/error
app/src/main/java/.../ui/screens/LearnScreen.kt        ✅ تصفية حسب القسم المفتوح
app/src/main/java/.../ui/screens/*.kt                  ✅ تحديث الألوان + shared ContentRepository
app/src/main/AndroidManifest.xml                       ✅ صلاحيات الإنترنت
app/src/main/java/.../ui/navigation/AppNavigation.kt   ✅ مشاركة instance واحد للـ Repository
app/build.gradle.kts      ✅ Java 17 + Retrofit + OkHttp Logging + BASE_URL buildConfigField
DXNTrainingApp/local.properties          ✅ JDK 17 محلي
```

---

## ملاحظات تقنية

- **لا نحتاج لإعادة بناء المشروع من الصفر** — كل التغييرات قابلة للدمج تدريجياً.
- **Scenario Builder** يحتاج تحسينات أكثر من إعادة بناء.
- **الشهادات** تحتاج تصميم قالب (SVG/HTML to PDF) للمرحلة الإنتاجية.
- **القفل التسلسلي** يجب أن يُطبّق في الـ Backend أولاً، والـ Frontend يعرض الحالة فقط.
- **الألوان** تُعدّل في CSS Variables لضمان الاتساق.

---

## نتائج الفحص والبناء (بعد التحسينات)

| المشروع | الأمر | النتيجة |
|---|---|---|
| Android | `./gradlew :app:compileDebugKotlin` | ✅ ناجح |
| Android | `./gradlew :app:assembleDebug` | ✅ ناجح — APK في `app/build/outputs/apk/debug/` |
| Web | `npm run build` | ✅ ناجح — `dist/` جاهز للنشر |
| Web | `npm run dev` | ✅ ناجح — يعمل على `http://127.0.0.1:5173` |

## ربط تطبيق Android بـ Backend API

تم ربط تطبيق Android بـ Backend API باستخدام **Retrofit + Kotlin Coroutines** مع الاحتفاظ ببيانات `MockData` كاحتياط عند عدم توفر السيرفر.

### عنوان السيرفر

يُعرّف العنوان في `app/build.gradle.kts` كـ `buildConfigField`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://dxn-training-api.example.com/\"")
```

> **ملاحظة:** يُغيَّر العنوان إلى عنوان السيرفر الحقيقي بعد نشر الموقع، بدون تعديل الكود.

### المكتبات المضافة

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.11.0")
```

### الـ Endpoints المتوقعة من السيرفر

| Method | Endpoint | الوصف | الاستجابة المتوقعة |
|---|---|---|---|
| GET | `/api/sections/tree` | شجرة المحتوى الكاملة | `List<SectionResponse>` |
| GET | `/api/users/{userId}/progress` | تقدم المستخدم في المستويات | `Map<levelId, UserProgressResponse>` |
| GET | `/api/users/{userId}/sections-progress` | تقدم المستخدم في الأقسام | `Map<sectionId, SectionProgressResponse>` |
| POST | `/api/levels/{levelId}/complete` | تسجيل إكمال مستوى | `LevelCompletionResponse` |
| GET | `/api/users/{userId}/profile` | بيانات المستخدم | `UserProfileResponse` |

### أشكال JSON المتوقعة

#### 1. شجرة المحتوى

```json
[
  {
    "id": "s1",
    "title": "أساسيات النجاح مع DXN",
    "orderIndex": 1,
    "bagId": 1,
    "description": "...",
    "certificateTitle": "شهادة أساسيات DXN",
    "requiredSectionId": null,
    "skills": [
      {
        "id": "s1k1",
        "title": "الشركة وهويتها",
        "orderIndex": 1,
        "levels": [
          {
            "id": "l1",
            "title": "شعار DXN ومعنى الألوان",
            "orderIndex": 1,
            "xpReward": 40,
            "passThreshold": 80,
            "type": "lesson",
            "cards": [...],
            "rounds": [...]
          }
        ]
      }
    ]
  }
]
```

#### 2. تقدم المستويات

```json
{
  "l1": { "status": "COMPLETED", "score": 100, "attempts": 1, "bestScore": 100, "passedFinalQuiz": true },
  "l2": { "status": "UNLOCKED", "score": 0, "attempts": 0, "bestScore": 0 }
}
```

#### 3. تقدم الأقسام والشهادات

```json
{
  "s1": { "status": "COMPLETED", "certificateIssued": true, "certificateId": "cert-s1", "completedAt": "2026-11-17" }
}
```

#### 4. إكمال مستوى (طلب/استجابة)

**طلب:**
```json
{ "userId": "u1", "score": 100, "passed": true }
```

**استجابة:**
```json
{ "success": true, "xpAwarded": 40, "certificateIssued": false }
```

#### 5. بيانات المستخدم

```json
{
  "id": "u1",
  "fullName": "أحمد محمد",
  "email": "ahmed@dxn.com",
  "dxnId": "DXN-123456",
  "role": "TRAINEE",
  "totalXp": 1240,
  "currentStreak": 5,
  "hearts": 3,
  "gems": 120
}
```

### منطق الـ Fallback

- `ContentRepository.loadContent(userId)` تحاول جلب الأقسام والتقدم من السيرفر.
- إذا فشل الاتصال (لا إنترنت، عنوان خاطئ، خطأ HTTP)، تُحفظ رسالة الخطأ وتُعرض للمستخدم، ويُستخدم `MockData` كاحتياط.
- جميع شاشات الألعاب تستخدم نفس instance من `ContentRepository` (عبر `AppNavigation`) لضمان تناسق الحالة.

### الملفات المرتبطة بالربط

```
DXNTrainingApp/app/build.gradle.kts
DXNTrainingApp/app/src/main/AndroidManifest.xml
DXNTrainingApp/app/src/main/java/com/dxn/training/data/remote/ApiClient.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/data/remote/ApiService.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/data/remote/NetworkModels.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/data/repository/ContentRepository.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/ui/viewmodel/HomeViewModel.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/ui/screens/HomeScreen.kt
DXNTrainingApp/app/src/main/java/com/dxn/training/ui/navigation/AppNavigation.kt
```

## التحسينات المُطبَّقة بناءً على مراجعة QA/UI-UX

### Android
- ✅ إصلاح منطق القفل التسلسلي: القسم التالي يفتح فقط بعد إكمال السابق + إصدار شهادته.
- ✅ إضافة اختبار إتمام القسم (`passedFinalQuiz`) كشرط لمنح الشهادة.
- ✅ Snackbar يوضح للمستخدم القسم المطلوب عند الضغط على قسم مقفل.
- ✅ زيادة أحجام مناطق اللمس (Touch targets) للمهارات والمستويات.
- ✅ تحسين CompletionScreen: رسالة رسوب واضحة + عرض الشهادة + تعطيل التنقل عند الرسوب.
- ✅ Error Handling في جميع شاشات الألعاب لمنع crash مع levelId غير صالح.
- ✅ إعادة تعيين حالة ChatBattle عند تغيير المستوى.
- ✅ ربط `ChatBattleScreen` بـ `updateLevelProgress` لضمن منح الشهادة.
- ✅ فتح أول مستوى في القسم تلقائياً عند الضغط على قسم متاح لأول مرة.

### الويب
- ✅ إزالة `Math.random()` من Overview و Users — التقدم محسوب من البيانات.
- ✅ إضافة Empty State في Content.
- ✅ تأكيد قبل النشر/إلغاء النشر وحذف الخيار في Scenario Builder.
- ✅ حماية من XSS في Scenario Builder (`hasXss`).
- ✅ ألوان محسّنة في Teams.
- ✅ إصلاح حساب التقدم في Overview.tsx لتجنب `as number` و crash محتمل.

## لقطات الشاشة

- `web_overview.png`
- `web_content.png`
- `web_users.png`
- `web_analytics.png`
- `web_teams.png`
- `web_scenario_builder.png`
- `web_scenario_settings.png`

## الخلاصة

تم دمج متطلبات v3.0 بالكامل وتطبيق التحسينات العاجلة من مراجعة QA/UI-UX، **بالإضافة إلى ربط تطبيق Android بـ Backend API** مع Mock fallback. كل من Android والويب يبنيان بنجاح.

- عنوان السيرفر قابل للتغيير من `app/build.gradle.kts`.
- جميع الـ Endpoints المتوقعة موثّقة أعلاه.
- التطبيق يعمل Offline باستخدام `MockData` عند فشل الاتصال.
- APK debug جاهز في `app/build/outputs/apk/debug/`.
