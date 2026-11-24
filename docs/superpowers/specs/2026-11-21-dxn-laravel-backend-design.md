# تصميم: خادم Laravel وربط لوحة React وتطبيق Android — DXN Training

التاريخ: 2026-11-21 · الحالة: بانتظار الموافقة النهائية

## 1. الهدف والسبب
النظام حالياً واجهات فقط: تطبيق Android (Kotlin/Compose) ولوحة إدارة React، وكلاهما على بيانات وهمية. لا خادم، ولا تسجيل دخول حقيقي، والنتائج و XP تُحسب في الجهاز (قابلة للتلاعب). المطلوب نظام متكامل: خادم **Laravel** هو المصدر الوحيد للبيانات، تتصل به **لوحة React الحالية** (تُكمَّل، لا تُستبدل) و**تطبيق Android**.

أول ما يجب أن يعمل فعلياً (قرار صاحب المشروع): **المدير يسجّل الدخول للوحة ويُدخل المحتوى التدريبي**. بعد ذلك تُبنى بقية الأجزاء كلها؛ الترتيب بعد ذلك غير مهم لصاحب المشروع.

## 2. الأهداف / غير الأهداف / ما يُعدّ فشلاً
**أهداف**
- Laravel API (JSON/REST) يغطي: الحسابات والأدوار، شجرة المحتوى، التقدّم، XP، الشهادات، الفرق، التحليلات الأساسية.
- لوحة React: تسجيل دخول + كل صفحة من الصفحات السبع تقرأ وتكتب من الخادم بدل `mockData.ts`.
- تطبيق Android: تسجيل/دخول حقيقي، تحميل الشجرة والتقدّم من الخادم، وإرسال إكمال المستوى.
- النشر على https://app.dockhosting.dev/ مع MySQL.

**غير أهداف (الآن)** — مذكورة في SRS لكنها مؤجلة صراحة:
2FA، وضع Offline الكامل مع طابور مزامنة، Leagues، الإشعارات (FCM/Socket)، iOS، Hints/Gems الشرائية، مشاركة الإنجازات كصور، إدارة إصدارات محتوى متقدمة (نكتفي بحقل `version`)، التحقق من رقم عضوية DXN مع نظام الشركة.

**فشل حتى لو عمل تقنياً**
- إذا بقي العميل قادراً على منح نفسه XP أو فتح قسم دون اجتياز فعلي.
- إذا استطاع مستخدم قراءة تقدّم/بيانات مستخدم آخر (IDOR).
- إذا كُسر تطبيق Android الحالي بلا داعٍ (العقد الحالي يجب أن يبقى صالحاً قدر الإمكان).

## 3. القيود
- بيئة التطوير: Windows، PHP 8.3، Composer 2.10، MySQL 8.4 موجود ضمن Laragon (`C:\laragon\bin\mysql\mysql-8.4.3-winx64`)، لا Docker.
- قاعدة البيانات: **MySQL** (قرار صاحب المشروع). PostgreSQL المذكور في SRS غير معتمد.
- الاستضافة: dockhosting.dev — تفاصيلها (Docker؟ إصدار PHP؟ MySQL مُدار؟) **لم تُفحص بعد**، تُحسم في مرحلة النشر.
- مستودع واحد: `baithanoun/dxn_training`؛ الخادم في مجلد جديد `dxn-api/`.
- للمهام الصغيرة المفوَّضة يُستخدم أخف نموذج (تفضيل صاحب المشروع).

## 4. البدائل التي نوقشت
| البديل | القرار |
|---|---|
| لوحة الإدارة داخل Laravel (Blade/Filament) بدل React | **مرفوض** — صاحب المشروع اختار إكمال React |
| Laravel API + React + Android | **مختار** |
| قاعدة البيانات: SQLite للتطوير / MySQL / PostgreSQL | **MySQL** (اختيار صاحب المشروع؛ توصيتي كانت SQLite مؤقتاً) |

## 5. ما تقرر في طريقة البناء
### 5.1 الخادم
- Laravel أحدث إصدار متوافق مع PHP 8.3، في `dxn-api/`.
- التحقق من الهوية: **Laravel Sanctum** بتوكن Bearer لكل من اللوحة والتطبيق. *(قراري)*
- الأدوار: `admin`, `leader`, `trainee` في عمود `role` على `users`، مع `sponsor_id` (القائد). الصلاحيات عبر Policies/Middleware. *(قراري: لا حزمة صلاحيات خارجية)*
- كل الاستجابات JSON بأسماء حقول camelCase (يطابق Android وReact). المعرّفات تُرسَل كنصوص (`"12"`) لأن Android يعرّفها `String`. *(قراري)*
- Rate limiting على تسجيل الدخول وعلى إرسال نتائج المستويات.

### 5.2 نموذج البيانات (مبني على SRS §5.4 مع إضافات من الكود الحالي)
- `users` (full_name, email, phone, dxn_id **مشفّر** عبر cast، role, sponsor_id, total_xp, current_streak, last_activity_date, hearts, gems, is_active)
- `sections` (title, description, order_index, bag_id, certificate_title, required_section_id, status [draft/published], version)
- `skills` (section_id, title, icon, order_index, status)
- `levels` (skill_id, title, type, pass_threshold, xp_reward, order_index, is_final_quiz, status)
  - `type` ∈ `lesson | quiz | chat_battle | match_pairs | order_sequence` (أسماء Android هي المعتمدة؛ `simulation` في mockData الويب = `chat_battle`)
- `lesson_cards` (level_id, title, body, tip, media_url, order_index)
- `game_rounds` (level_id, prompt, initial_trust, order_index)
- `round_options` (round_id, text, is_correct, is_neutral, trust_impact, feedback, xp_bonus, order_index)
- `user_level_progress` (user_id, level_id, status, score, best_score, attempts, passed_final_quiz, completed_at) — فريد على (user_id, level_id)
- `user_section_progress` / `certificates` (user_id, section_id, certificate_code, issued_at)
- `analytics_events` (user_id, event_name, level_id, payload JSON, device_info, created_at)
- `settings` (key/value) لصفحة الإعدادات.

### 5.3 قواعد العمل على الخادم (هي الحقيقة، لا العميل)
- **القفل**: المستوى الأول في القسم الأول مفتوح؛ المستوى التالي يُفتح بعد إكمال السابق؛ القسم يُفتح بعد إكمال القسم `required_section_id` وإصدار شهادته.
- **حساب النتيجة**: طلب إكمال المستوى يرسل **الإجابات** (معرّفات الخيارات المختارة لكل جولة، أو الترتيب)، والخادم يحسب النتيجة والاجتياز والـ XP. حقلا `score`/`passed` القادمان من العميل يُتجاهلان.
- **XP**: `xp_reward` عند أول اجتياز، مضاعفة عند الإتقان من أول محاولة، وتقليل في المحاولات المتكررة *(النِّسب: قرار المنفّذ، توضع في `settings`)*.
- **الشهادة**: تُصدر تلقائياً عند اجتياز المستوى المعلَّم `is_final_quiz` في القسم.
- **Streak**: يُحدَّث عند أول إكمال في يوم جديد.

### 5.4 واجهة API
**عامة/موبايل** (متوافقة مع `ApiService.kt` الحالي):
| Method | Path | ملاحظة |
|---|---|---|
| POST | `/api/auth/register` | اسم، بريد أو هاتف، كلمة مرور، dxnId، كود القائد (اختياري) |
| POST | `/api/auth/login` · `/api/auth/logout` · GET `/api/auth/me` | يعيد token |
| GET | `/api/sections/tree` | المنشور فقط، بنفس شكل `SectionResponse` |
| GET | `/api/users/{userId}/progress` | `{userId}` يجب أن يساوي المستخدم الحالي، أو يكون قائده، أو مدير |
| GET | `/api/users/{userId}/sections-progress` | نفس القاعدة |
| GET | `/api/users/{userId}/profile` | نفس القاعدة |
| POST | `/api/levels/{levelId}/complete` | الجسم: `{ answers: [...] }`؛ يعيد `{success, xpAwarded, certificateIssued, certificateTitle, score, passed}` |
| POST | `/api/events` | دفعة أحداث تحليلية |
| GET | `/api/leader/team` | للقائد: أعضاؤه وتقدّمهم |

**إدارة** (`/api/admin/*`، دور admin):
- CRUD كامل: sections, skills, levels, lesson-cards, rounds + options (يُحفظ السيناريو كاملاً في طلب واحد من منشئ السيناريوهات)، إعادة الترتيب، نشر/إلغاء نشر.
- users: قائمة مع بحث/فلاتر (دور، نشاط، قائد) وترقيم صفحات، إنشاء، تعديل الدور/القائد، تعطيل.
- teams: القادة مع عدد الأعضاء ونسبة الإكمال؛ أعضاء قائد معيّن.
- dashboard/analytics: مؤشرات النظرة العامة، نشاط 30 يوماً، Funnel، أكثر الأسئلة رسوباً، Retention — محسوبة من الجداول.
- certificates: الشهادات لكل قسم وعدد الإصدارات.
- export: CSV للمستخدمين والتقارير.
- settings: قراءة/حفظ.

### 5.5 لوحة React
- صفحة `/login` + حماية المسارات؛ التوكن يُحفظ في `localStorage` *(قراري؛ مقبول مع CSP وعدم وجود HTML خام من المستخدم)*.
- طبقة `src/lib/api.ts` واحدة فوق `fetch` (لا axios، لا مكتبة جديدة) تضيف التوكن وتعالج 401.
- كل صفحة تستبدل استيراد `mockData` بطلب للخادم، مع حالات تحميل/خطأ/فارغ. الأزرار الميتة الحالية تُربط فعلياً.
- عنوان الخادم من `VITE_API_URL`.

### 5.6 Android
- `AuthViewModel` يستدعي login/register ويخزّن التوكن (DataStore)؛ OkHttp Interceptor يضيف `Authorization`.
- `submitLevelCompletion` يرسل الإجابات بدل `score/passed`.
- `BASE_URL` يُضبط على عنوان الخادم الفعلي.
- يبقى `MockData` احتياطاً كما هو.

### 5.7 الترتيب (مراحل، كل مرحلة قابلة للتشغيل والفحص)
1. هيكل Laravel + MySQL + Sanctum + حساب مدير أولي (seeder) + جداول المحتوى + CRUD المحتوى + بذر المحتوى من `mockData.ts`.
2. React: تسجيل الدخول + صفحة المحتوى + منشئ السيناريوهات على الخادم.
3. المستخدمون والفرق (API + صفحتا React).
4. API الموبايل: التسجيل، الشجرة، التقدّم، الإكمال المحسوب على الخادم، الشهادات، XP/Streak + ربط Android.
5. الأحداث والتحليلات والنظرة العامة والتصدير والإعدادات.
6. النشر على dockhosting.dev.

### 5.8 الاختبارات
- Laravel Feature tests (PHPUnit/Pest الافتراضي) لكل endpoint مهم، وإلزامية لـ: القفل، حساب النتيجة، XP، الشهادة، قواعد الوصول (IDOR).
- فحص يدوي للوحة عبر المتصفح بعد كل مرحلة.

### 5.9 قرارات بعد مراجعة المنفّذ *(كلها قراراتي ما لم يُذكر غير ذلك)*
- **تقييم الإجابات حسب النوع** (جسم `POST /levels/{id}/complete`):
  - `lesson`: بلا إجابات؛ الإكمال = اجتياز بنتيجة 100.
  - `quiz` و`chat_battle`: `answers: [{roundId, optionId}]`. أسئلة الاختبار = `game_rounds` وخياراتها `round_options` (`initial_trust` يُتجاهل في quiz). quiz: النتيجة = نسبة الإجابات الصحيحة. chat_battle: الثقة = `initial_trust` + مجموع `trust_impact`، والاجتياز = الثقة ≥ `pass_threshold`.
  - `order_sequence`: `answers: [{roundId, orderedOptionIds: [...]}]`؛ الترتيب الصحيح = `order_index` للخيارات.
  - `match_pairs`: `answers: [{roundId, pairs: [{leftId, rightId}]}]`؛ التمثيل الدقيق يُستخرج من `MatchPairsScreen.kt`.
  - النتيجة دائماً 0–100. Android يغيّر `LevelCompletionRequest` إلى هذا الشكل؛ الطلب القديم `{score, passed}` يُرفض بـ 422 (لا يوجد مستخدمون حقيقيون بعد).
- **الشجرة لا تُخفي الإجابات**: `isCorrect` و`trustImpact` يبقيان في `/sections/tree` لأن التطبيق يعرض التغذية الراجعة فوراً. الحماية هي أن الخادم يعيد الحساب من الإجابات؛ معرفة الإجابات ≈ حفظها، وهذا مقبول في تطبيق تدريب.
- **القفل**: الترتيب داخل القسم متسلسل عبر المهارات (`skill.order_index` ثم `level.order_index`). "القسم الأول" = القسم المنشور الذي ليس له `required_section_id`. إن لم يكن في القسم مستوى `is_final_quiz` فآخر مستوى فيه يُعامل كاختبار نهائي.
- **تعديل المحتوى بعد وجود تقدّم**: حذف ناعم (soft delete) للأقسام/المهارات/المستويات؛ الشهادات الصادرة والتقدّم المسجّل لا يُلغى؛ إدراج مستوى جديد لا يُعيد قفل ما فُتح.
- **لوحة الإدارة للمدير فقط** *(قرار صاحب المشروع)*: القادة يتابعون فرقهم من تطبيق الموبايل عبر `/api/leader/team`، ولا يدخلون اللوحة.
- **الربط بالقائد** *(قرار صاحب المشروع)*: المتدرب يُدخل عند التسجيل **رقم عضوية DXN لقائده** (`leaderDxnId`، اختياري). يُبحث عنه بين المستخدمين ذوي الدور `leader`؛ إن لم يوجد يُرفض التسجيل بـ 422 ورسالة واضحة. بما أن `dxn_id` مشفّر، يُخزَّن معه `dxn_id_hash` (HMAC) فريد للبحث.
- **التسجيل**: ينشئ `trainee` فقط؛ الأدوار الأعلى يمنحها المدير. البريد فريد والهاتف فريد، ويُشترط أحدهما على الأقل؛ الدخول بأيّهما.
- **القلوب والإعادة**: القلوب منطق لعب داخل المحاولة على الجهاز (3 لكل محاولة)؛ الخادم لا يفرضها. `hearts/gems` في الملف الشخصي للعرض فقط. الإعادة مسموحة؛ `best_score` يُحدَّث؛ XP كامل عند أول اجتياز فقط ومخفّض بعده.
- **التوكنات**: نوع واحد (Sanctum)؛ تعطيل مستخدم يحذف توكناته؛ `/api/admin/*` يتطلب `role=admin`.
- **Android بلا اتصال**: يعرض المحتوى المخزّن/الاحتياطي للقراءة، لكن لا يسجّل تقدّماً محلياً كأنه رسمي؛ يُظهر أن النتيجة لم تُحفظ. التقدّم المحلي القديم يُهمل عند أول دخول.
- **بذر المحتوى**: `MockData.kt` (Android) هو المرجع لأنه يحوي البطاقات والجولات؛ الـ seeder قابل لإعادة التشغيل (idempotent).
- **الوسائط**: رفع صور من اللوحة إلى قرص `public` في Laravel (حد 5MB، صور فقط)؛ المحتوى عربي فقط.
- **مكان اللوحة عند النشر**: تُبنى React وتُخدم من نفس نطاق Laravel تحت `/admin`؛ أثناء التطوير CORS مفتوح لـ `localhost:5173`. لا queues: التحليلات تُحسب باستعلامات عند الطلب.
- **التحليلات**: أشكال البيانات الحالية في `mockData.ts` هي العقد المستهدف؛ Retention = D1/D7/D30 من `analytics_events`؛ Funnel = level_started → level_completed → section certificate.

## 6. متروك للمنفّذ
- إصدار Laravel الدقيق، Pest أو PHPUnit، تنظيم Controllers/Resources/FormRequests.
- تمثيل `match_pairs` و`order_sequence` داخل `game_rounds/round_options` — يُستخرج من `MatchPairsScreen.kt` و`OrderSequenceScreen.kt` و`MockData.kt`، مع الحفاظ على شكل JSON الذي يقرؤه Android.
- نسب مضاعفة/تقليل XP وقيمها الافتراضية.
- شكل ترقيم الصفحات في endpoints الإدارة.
