# مراجعة تقدّم Plan 2 — 2026-11-22

الفرع: `feat/plan1-api-content` · المرجع: `docs/superpowers/plans/2026-11-21-plan2-complete-system.md` + `docs/superpowers/specs/2026-11-21-dxn-laravel-backend-design.md`.
طريقة المراجعة: قراءة الكود + أوامر التحقق في آخر الملف. لم يُشغَّل بناء Android.

## 1. حالة المهام

| مهمة | الحالة | Commit | الفجوات |
|---|---|---|---|
| A1 Content page | مكتمل (غير مُراجَع يدوياً) | 93e8d01 | لا تحقق متصفح مسجَّل |
| A2 Scenario Builder | **مفقود** | — | `ScenarioBuilder.tsx` ما زال يستورد `@/data/mockData` |
| B1 Admin users API | جزئي | 06ebb27 | لا اختبارات؛ CSV غير مُهرَّب (انظر I-4)؛ `sectionsProgress`/`certificates` في `show` — verify in project |
| B2 Admin teams API | جزئي | af6c756 | لا اختبارات؛ `leader` يُعاد كائناً لا اسماً (I-6) |
| B3 Users/Teams pages | **مفقود** | — | `Users.tsx`, `Teams.tsx` على mockData |
| C1 Register | جزئي | 759e67c | لا اختبارات؛ رسائل الخطأ إنجليزية لا العربية المحددة؛ تطبيع `dxn_id` الفارغ إلى null غير مُنفَّذ في `User::booted` |
| C2 Tree/Progress | جزئي | 759e67c | لا اختبارات؛ أداء (M-1)؛ `sectionCompleted` دائماً false |
| C3 Completion | جزئي — **أخطاء منطقية** | 759e67c | I-1, I-2, I-3؛ لا اختبارات |
| C4 Leader team + events | جزئي | 04c02af | I-5؛ لا اختبارات |
| D1 Android auth | جزئي | a8fd4e0, 82f7b69 | SessionStore/Interceptor/endpoints فقط؛ `AuthViewModel`, `LoginScreen`, `RegisterScreen` لم تُربط؛ `DEFAULT_USER_ID = "u1"` ما زال في `ContentRepository.kt:15`؛ `BASE_URL` في `defaultConfig` (release أيضاً) |
| D2 Android completion | **جارٍ، غير مُلتزَم** | — | تعديلات على `ApiService.kt`, `ContentRepository.kt`, `QuizScreen.kt` (+51/−22) |
| D3 Profile/Team/Events | **مفقود** | — | |
| E1 Dashboard/Analytics API | جزئي | f6125df | `dashboard/overview` فقط؛ `admin/analytics`, `admin/analytics/export`, `admin/certificates` **غير موجودة**؛ لا اختبارات |
| E2 Overview/Analytics pages | **مفقود** | — | على mockData |
| E3 Settings | جزئي | d7a6d6b | API فقط؛ مفاتيح مجهولة لا تُرفض (I-7)؛ لا ربط لـ session_minutes/quiz_rate_limit/maintenance_mode (I-8)؛ لا اختبارات؛ الصفحة — verify in project |
| F1 Deploy kit | **مفقود** | — | |
| F2 Verification/docs | **مفقود** | — | |

ملاحظات عملية: C1+C2+C3 في commit واحد (759e67c) خلافاً لقاعدة commit لكل مهمة — يُسجَّل فقط، لا إعادة كتابة للتاريخ. نقاط توقف المالك A2 وC3 تم تجاوزها.

## 2. الملاحظات

### Critical
- **C-1 لا اختبارات لأي endpoint جديد.** `php artisan test` = 35 اختباراً (نفس العدد قبل 759e67c)؛ لا ملفات تحت `tests/Feature/Mobile`, ولا `RegisterTest`, `UsersTest`, `TeamsTest`, `SettingsTest`, `DashboardTest`, `tests/Unit/ScoringServiceTest`. الخطة تُلزم بها، ومنطق النقاط/الصلاحيات غير محمي.

### Important
- **I-1 XP لأول نجاح بعد محاولة فاشلة خاطئ.** `CompletionController.php:53-63`: `$isFirstAttempt = ! $row`. مستخدم فشل ثم نجح → ليس first attempt → يدخل فرع `$improved` فيأخذ `floor(xp×0.25)` بدل `xp_reward` الكامل. المواصفة: أول نجاح = `xp_reward`، وأول نجاح من المحاولة الأولى = `round(xp×1.5)`. الإصلاح: `$firstPass = ! $wasCompleted && $passed`؛ multiplier فقط إذا `! $row`.
- **I-2 لا تحقق من شكل الإجابات.** `CompletionController.php:31-39` + `ScoringService.php:24,35,50`: لا تحقق أن `roundId` يتبع المستوى، ولا أن `optionId` يتبع الجولة، ولا منع التكرار، ولا إلزام الإجابة عن كل الجولات (المطلوب 422). وغياب مفتاح `optionId` في عنصر → `Undefined array key` → **500** (محتمل؛ verify with test).
- **I-3 أحداث `level_completed`/`level_failed` لا تُكتب.** المواصفة (C3 خطوة 6) تطلب كتابتها من الخادم؛ `CompletionController` لا يكتبها، و`EventController::ALLOWED` يمنعها من العميل → `failedSkills` في `DashboardController.php:52-53` ستبقى صفراً دائماً.
- **I-4 CSV المستخدمين غير مُهرَّب.** `UserController.php:183-198`: `implode(',')` بلا اقتباس → اسم يحوي فاصلة يكسر الأعمدة؛ وقيم تبدأ بـ `= + - @` قابلة لحقن صيغ Excel. الإصلاح: `fputcsv` على `php://temp` + بادئة `'` للقيم الخطرة.
- **I-5 `lastActiveDays` على الأرجح سالب.** `LeaderTeamController.php:25`: في Carbon 3 (Laravel 13) `now()->diffInDays($past)` يعيد قيمة سالبة → `<= 3` دائماً → كل الأعضاء `ACTIVE`. (محتمل؛ verify with test). الإصلاح: `(int) $m->last_activity_date->diffInDays(now())` أو `abs`.
- **I-6 عقد `GET admin/teams` يختلف عن الخطة.** `TeamController.php:33`: `leader` = كائن `AdminUserResource`، الخطة: `leader` = اسم نصي. قرّر وثبّت في الخطة قبل B3 (المقترح: إبقاء الكائن وإضافة `leaderName` نصي — أقل كسر).
- **I-7 الإعدادات تقبل مفاتيح مجهولة.** `SettingsController.php:59`: `validate` يتجاهل المفاتيح الإضافية بصمت؛ الخطة: 422.
- **I-8 الإعدادات غير مربوطة بالسلوك.** لا استخدام لـ `session_minutes`, `quiz_rate_limit_per_minute`, `maintenance_mode` في أي مكان (grep). الـ completion throttle ثابت `30,1` في `routes/api.php:34`.

### Minor
- M-1 `ProgressService::sectionStatuses` يستدعي `levelStatuses` داخل حلقة الأقسام (`ProgressService.php:299`) + استعلام شهادة لكل قسم → O(أقسام²). `isUnlocked` يبني الخريطة كاملة لكل طلب إكمال.
- M-2 `sectionStatuses` يعيد `COMPLETED` بلا شهادة عند اكتمال كل المستويات (`ProgressService.php:302`)؛ المواصفة: COMPLETED ⇔ شهادة.
- M-3 فحص القفل خارج الـ transaction (`CompletionController.php:27`) — سباق نظري.
- M-4 `EventController.php:33` يقبل `occurredAt` من العميل كـ `created_at` → يمكن تزوير توزيع التحليلات زمنياً.
- M-5 `RegisterRequest` لا يطبّع `dxnId` الفارغ/المسافات؛ رسائل الأخطاء بالإنجليزية.
- M-6 `npm run build` يحذّر من chunk > 500 kB.

### ما هو سليم (تم التحقق بالقراءة)
- IDOR: `ProgressController::resolveUser` (`ProgressController.php:50-68`) يسمح للنفس/المدير/القائد عبر `sponsor_id` فقط، وإلا 403.
- مستوى مقفل → 409 `المستوى مقفل` قبل أي كتابة.
- الجسم القديم بلا `answers` لمستوى غير lesson → 422.
- تقييم chat_battle: `initial_trust` للجولة الأولى + Σ `trust_impact`، مقيد 0–100.
- streak (اليوم/أمس/أقدم) مطابق.
- شهادة واحدة: فحص + `unique(user_id, section_id)` في migration.
- Register: `leaderDxnId` يُبحث عنه بين `role=leader` فقط؛ `role` لا يُمرَّر من الطلب.
- Users: تعطيل يحذف التوكنات؛ منع تغيير الدور/التعطيل للنفس؛ BOM موجود.

## 3. نتائج التحقق الفعلية
- `cd dxn-api && php artisan test` → **35 passed, 208 assertions**.
- `php artisan route:list --path=api` → 36 route.
- `cd dxn-admin-web && npm run build` → نجح (تحذير chunk size).
- `grep -rl mockData dxn-admin-web/src` → Analytics.tsx, Overview.tsx, ScenarioBuilder.tsx, Teams.tsx, Users.tsx.
- `gradlew :app:assembleDebug` → **لم يُشغَّل**.
