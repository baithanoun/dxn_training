# سجل التحقق النهائي — النظام الكامل (Plan 2 F2 / Plan 3 T12)

> تاريخ التنفيذ: 2026-11-23 · الفرع: `feat/plan1-api-content` · وضع التحقق: **Evidence-led**.
> كل أمر أدناه نُفّذ فعلياً وسُجّل ناتجه، أو وُسم **`NOT RUN`** صراحةً. لا يُعدّ أي فحص يدوي ناجحاً لم يُنفَّذ.
> أُضيف أثناء التنفيذ إصلاح واحد للبيانات الأولية (`e595a30`) بعد اكتشاف أن مستويات `chat_battle` كانت غير قابلة للاجتياز؛ تفاصيله في القسم 3.

## 1. الفحوص الحتمية

| # | الأمر / الفحص | النتيجة | الدليل |
|---|---|---|---|
| 1 | `cd dxn-api && php artisan test` | **PASS** | `115 tests, 562 assertions`، exit code `0`. الخط الأساسي قبل الإصلاح كان `114 tests, 551 assertions`؛ أُضيف اختبار انحدار `ContentSeederTest::test_seeded_levels_are_passable_with_best_answers` مع إصلاح الـ seed. |
| 2 | `cd dxn-api && php artisan migrate:fresh --seed` على MySQL المحلي | **PASS** | شُغّل MySQL `8.4.3` محلياً، ثم نجح `migrate:fresh --seed` على قاعدة التطوير `dxn_training` بلا أخطاء (exit code `0`). `php artisan storage:link` أفاد بأن الرابط موجود بالفعل. |
| 3 | `cd dxn-admin-web && npm run build` | **PASS** | `tsc -b && vite build` نجح؛ `✓ 2485 modules transformed`، `✓ built in 1.37s`، exit code `0`. تحذير واحد فقط: حزمة JS `801.07 kB` (> 500 kB). |
| 4 | `cd dxn-admin-web && npm run lint` | **PASS** | `oxlint`، exit code `0`، 14 تحذيراً (`react/only-export-components` في `button.tsx`, `badge.tsx`, `auth.tsx`؛ `react/set-state-in-effect` في `Overview`, `Settings` ×2, `Teams` ×2, `Analytics`, `Users` ×3, `Content` ×2)، بلا أخطاء. |
| 5 | `cd DXNTrainingApp && $env:JAVA_HOME="D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"; .\gradlew.bat :app:assembleDebug --no-daemon` | **PASS** | `BUILD SUCCESSFUL in 16s` — `39 actionable tasks: 1 executed, 38 up-to-date`، exit code `0` (OpenJDK 17.0.12، Gradle wrapper 8.7). أُعيد تشغيله بعد إصلاح الـ seed؛ لم يتغيّر كود Android. |
| 6 | Arabic round-trip على MySQL | **PASS** | أنشئ قسم بالعنوان `اختبار الترميز — ١٢٣ ✓` (id=`6`) من لوحة الإدارة، ثم قرئ من MySQL بالبايتات: `D8A7D8AED8AAD8A8D8A7D8B120D8A7D984D8AAD8B1D985D98AD8B220E2809420D9A1D9A2D9A320E29C93` — مساوٍ تماماً للنص المطلوب، لا تقريباً ولا ترميزاً بديلاً. حُذف سجل الاختبار `soft-delete` من الواجهة ثم `DELETE` نهائي؛ أعاد الاستعلام صفر صفوف. لم يُعوَّض بـ SQLite. |
| 7 | السيناريو اليدوي الكامل (القسم 3 أدناه) | **PASS** | نُفّذت الخطوات 1–9 فعلياً على المتصفح والمحاكي مع تسجيل النتائج والقيم في القسم 3. ملاحظة واحدة موثّقة في الخطوة 8 بخصوص تحديث شاشة Profile. |

أدوات مُثبَّتة ومُتحقَّق منها: PHP `8.3.30`، Composer `2.10.2`، Node `v26.1.0`، npm `11.13.0`، JDK `17.0.12+7`، MySQL `8.4.3`، Android emulator `CampManagementEmulator` (Android 15 / API 35).

## 2. Arabic round-trip — خطوات التنفيذ (نُفّذت بالكامل)

1. شغّل MySQL المحلي ثم `cd dxn-api && php artisan migrate:fresh --seed` على قاعدة التطوير `dxn_training` وشغّل `php artisan serve --host=0.0.0.0 --port=8000` و`npm run dev` وافتح `/admin/`.
2. أنشئ قسماً من لوحة الإدارة بعنوان `اختبار الترميز — ١٢٣ ✓` (الشرطة الطويلة `—`، الأرقام العربية `١٢٣`، وعلامة `✓`).
3. اقرأ القيمة من MySQL بالأمر التالي (استُخدم المسار الكامل `C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe` مع `--default-character-set=utf8mb4` لتفادي تحذير cp720):

   ```sh
   mysql -u root dxn_training -e "SELECT title FROM sections ORDER BY id DESC LIMIT 1"
   ```

4. تحقّق من **مساواة السلسلة نصياً وبالبايتات** مع `اختبار الترميز — ١٢٣ ✓` (ليس تقريباً ولا ترميزاً بديلاً).
5. احذف قسم الاختبار من لوحة الإدارة وأعد تشغيل الأمر للتأكد من اختفائه.

| الخطوة | النتيجة |
|---|---|
| 1 — تجهيز MySQL/الخوادم/الواجهة | **PASS** — MySQL 8.4.3، `migrate:fresh --seed`، API على `8000`، Vite على `5173`، و`admin/login` مدخَل إليه. |
| 2 — إنشاء القسم بالعنوان العربي | **PASS** — السجل `id=6` بالعنوان `اختبار الترميز — ١٢٣ ✓`. |
| 3 — القراءة بأمر `mysql` أعلاه | **PASS** — القراءة تمت بترميز `utf8mb4` دون تحذير cp720. |
| 4 — التحقق من المساواة النصية والبايتية | **PASS** — البايتات: `D8A7D8AED8AAD8A8D8A7D8B120D8A7D984D8AAD8B1D985D98AD8B220E2809420D9A1D9A320E29C93`. |
| 5 — حذف سجل الاختبار | **PASS** — حذف من الواجهة ثم `DELETE` نهائي؛ الاستعلام النهائي أعاد `Empty set`. |

## 3. السيناريو اليدوي الكامل (E2E) — نُفّذ بالكامل

> شغّل الخوادم والمحاكي لأغراض هذا التحقق:
>
> ```powershell
> # 1) API
> cd dxn-api
> php artisan serve --host=0.0.0.0 --port=8000
>
> # 2) لوحة الإدارة
> cd dxn-admin-web
> npm run dev -- --host 127.0.0.1 --port 5173
>
> # 3) Android
> adb install DXNTrainingApp/app/build/outputs/apk/debug/app-debug.apk
> # المحاكي: CampManagementEmulator، Android 15، emulator-5554
> ```
>
> ملاحظات التنفيذ الفعلي:
>
> - فُتحت لوحة الإدارة من `http://localhost:5173/admin/...` **وليس** `http://127.0.0.1:5173/...`؛ سياسة CORS في Laravel تسمح بالأصل `http://localhost:5173`، بينما يفشل الأصل `127.0.0.1` في طلبات API.
> - أُضيف دائماً `Accept: application/json` إلى طلبات API اليدوية؛ بدونها قد يعطي مسار API غير المصادَّق خطأ 500 `Route [login] not defined`.
> - الحساب الافتراضي المستخدم: `admin@dxn.local` / `change-me-locally` من `dxn-api/.env` (التطوير فقط).
> - الدخول والتسجيل محدودان بـ `5` محاولات/دقيقة (`throttle:5,1`)، وإكمال المستوى يستخدم `throttle:completion` (الافتراضي 10/دقيقة)، والأحداث `60,1`. روعيت هذه الحدود أثناء المحاولات.
> - قبل السيناريو: `php artisan migrate:fresh --seed` و`php artisan storage:link`.

| # | خطوة السيناريو | النتيجة |
|---|---|---|
| 1 | سجّل الدخول كمدير في `/admin/login` بحساب `.env`، وتأكد من ظهور لوحة Overview. | **PASS** — لوحة Overview ظهرت بعد الدخول بـ `admin@dxn.local` / `change-me-locally` على `http://localhost:5173/admin/login`. |
| 2 | من Users أنشئ قائداً (`role=LEADER`) ببريد/هاتف ورقم عضوية DXN، وسجّل رقمه. | **PASS** — `قائد اختبار`، DXN ID `LEADER-E2E-1`، `leader.e2e@example.com`، كلمة المرور `Passw0rd!`، الدور `LEADER`، `id=2`. |
| 3 | في تطبيق Android سجّل متدرباً جديداً وأدخل رقم عضوية القائد من الخطوة 2 في حقل "رقم عضوية القائد (اختياري)"؛ تحقّق من ربطه بالقائد. | **PASS** — `Trainee E2E`، DXN ID `TRAINEE-E2E-1`، `trainee.e2e@example.com`، `Passw0rd!`؛ قاعدة البيانات: `users.id=3` و`sponsor_id=2`. |
| 4 | أكمل المستوى الأول من القسم 1؛ تحقّق أن نقاط XP جاءت من استجابة السيرفر (لا من الحساب المحلي) وأن المستوى التالي فُتح بعد التحديث. | **PASS** — المستوى 1: نتيجة `100` و`+60 XP` من الاستجابة؛ `user_level_progress` = `completed`، `best_score=100`، و`users.total_xp=60`؛ المستوى 2 أصبح متاحاً. |
| 5 | افتح شاشة الفريق بحساب القائد على Android؛ تحقّق من ظهور المتدرب ونسبة تقدّمه. | **PASS** — شاشة الفريق عرضت `Trainee E2E` و`إنجاز 9%` و`آخر نشاط قبل 0 يوم`. |
| 6 | في لوحة الإدارة Users تحقّق من ظهور XP وآخر نشاط للمتدرب. | **PASS** — `Trainee E2E`، XP `٦٠`، آخر نشاط `2026-11-23`، الحالة `نشط`. |
| 7 | افتح Overview وAnalytics؛ تحقّق من انعكاس النشاط/الإكمال/المتوسطات بعد إعادة التحميل. | **PASS** — Overview: مستخدم نشط واحد، إكمال `9%`، سلسلة `1` يوم، الحقيبة 1 بنسبة `33%`. Analytics: `Completion Rate 100%`، `Average Time 18 ث`، `First-Attempt Success 100%`. |
| 8 | أكمل اختبار القسم الأخير (`is_final_quiz`)؛ تحقّق من إصدار الشهادة وظهورها في Profile. | **PASS** (بعد إعادة تشغيل التطبيق؛ انظر الملاحظة أدناه) — المستوى 3 (`is_final_quiz`) نجح بـ`100` و`+90 XP`؛ المجموع `190 XP`. الشهادة أُنشئت: `id=1`, `user_id=3`, `section_id=1`, `code=DXN-OW1GTNXZG8`, `issued_at=2026-11-23 10:45:00`. بعد `am force-stop` وإعادة الفتح ظهر في Profile: `1 شهادة` و`🎓 شهادة أساسيات DXN — 2026-11-23`. |
| 9 | تحقّق من أن القسم 2 أصبح غير مقفل في شجرة التطبيق. | **PASS** — الشجرة بعد الإكمال: القسم 1 `مكتمل`، القسم 2 `استثمار الاستهلاك / الحقيبة 2 — متاح`، القسم 3 `مقفل`. |

### ملاحظات موثّقة أثناء E2E

- **عائق حقيقي في بيانات الـ seed:** جميع مستويات `chat_battle` كانت تبدأ بـ`initialTrust=50` و`pass_threshold=80`، لكن أفضل إجابة كانت تمنح `trustImpact=25` فقط، فيكون أعلى نتيجة ممكنة `75 < 80`. لذلك تعذّر اجتياز المستوى 2 في التطبيق رغم صحة الإجابة. أُصلح ذلك في `dxn-api/database/seeders/data/content.json` بجعل الخيار الصحيح يمنح `30` للمستويات 2/5/8، مع اختبار انحدار في `ContentSeederTest`، والالتزام `e595a30 fix(api): make seeded chat_battle levels passable` (مع trailer المطلوب). بعد الإصلاح نجح `php artisan test` بـ`115 tests, 562 assertions`.
- **تحديث قاعدة التطوير دون مسح التقدّم:** نُفّذ `UPDATE round_options o JOIN game_rounds g ON g.id=o.round_id JOIN levels l ON l.id=g.level_id SET o.trust_impact=30 WHERE l.type='chat_battle' AND o.is_correct=1;` فأصبحت قيم المستويات 2/5/8 هي `50 + 30 = 80`، دون إعادة `migrate:fresh` حتى لا تُفقد بيانات E2E. بعدها اجتاز المتدرب المستوى 2 بنتيجة `80` و`+40 XP` (المحاولة الثالثة)، ثم المستوى 3.
- **تحديث شاشة Profile:** مباشرة بعد منح الشهادة عرض تبويب Profile العدد `0` ورسالة `لا توجد شهادات بعد`، رغم وجود الصف في قاعدة البيانات وظهور الشهادة في شجرة الرئيسية. بعد إعادة تشغيل التطبيق (`am force-stop` ثم إعادة الفتح) ظهرت الشهادة بشكل صحيح. سُجّل هذا كسلوك مُلاحظ؛ لم يُجرَّ تعديل على كود Android ضمن نطاق هذا التحقق.
- حالة progress النهائية قبل إغلاق التحقق: المستويات 1/2/3 `completed` بنتائج `100 / 80 / 100`، `total_xp=190`، `current_streak=1`.

## 4. مراجع

- الخطة الملزمة: `docs/superpowers/plans/2026-11-21-plan2-complete-system.md` (F2) و`docs/superpowers/plans/2026-11-22-plan3-remaining.md` (T12).
- النشر وتهيئة الإنتاج: `deploy/README.md` — النشر الفعلي/DNS/أسرار المضيف **`NOT RUN`** ومسؤولية المالك؛ لم يُسجَّل دخول إلى المضيف.
- بناء صورة Docker **`NOT RUN`** (Docker غير مثبّت على هذه البيئة كما سُجّل في `deploy/README.md`).
- فحوص المتصفح والمحاكي اليدوية للواجهات: **PASS** — الخطوات 1–9 في القسم 3 وخطوات Arabic round-trip في القسم 2 نُفّذت فعلياً؛ لم تعد `NOT RUN`.
- إصلاح بيانات الـ seed: الالتزام `e595a30` على الفرع `feat/plan1-api-content`؛ لم يُدفع أو يُدمج.

## 5. سجل تحقق Plan 4 — هوية «حكاية تميّز»

> تاريخ التنفيذ: 2026-11-23 · الفرع: `feat/plan1-api-content`. الأقسام 1–3 أعلاه سُجّلت **قبل** تغييرات Plan 4؛ نتائجها اليدوية لا تُحتسب كتحقق من الهوية الجديدة. كل فحص أدناه نُفّذ فعلياً أو وُسم **`NOT RUN`** صراحةً.

### 5.1 الفحوص الحتمية (نُفّذت بعد اكتمال Plan 4)

| # | الأمر / الفحص | النتيجة | الدليل |
|---|---|---|---|
| 1 | `cd dxn-api && php artisan test` | **PASS** | `129 tests, 620 assertions`، exit code `0`. شمل اختبارات `/api/app/config` وجنس المستخدم. |
| 2 | `cd dxn-admin-web && npm run build` | **PASS** | `tsc -b && vite build` نجح (`✓ built in 1.23s`). تحذير حجم الحزمة `806.57 kB` (> 500 kB) فقط. |
| 3 | `cd dxn-admin-web && npm run lint` | **PASS** | `oxlint`، exit code `0`؛ تحذيرات `react/set-state-in-effect` سابقة في `Settings/Analytics/Users/Content`، بلا أخطاء. |
| 4 | `cd DXNTrainingApp && $env:JAVA_HOME="D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"; .\gradlew.bat :app:assembleDebug --no-daemon` | **PASS** | `BUILD SUCCESSFUL` بعد G1 وبعد G2، exit code `0`. |
| 5 | `git diff --check` | **PASS** | بلا أخطاء (تحذيرات CRLF فقط). |
| 6 | grep `duolingo` في `DXNTrainingApp/app/src`, `dxn-admin-web/src`, `dxn-api/app`, `dxn-api/database` | **PASS** | لا نتائج. |
| 7 | grep `DXN Training` في `DXNTrainingApp/app/src`, `dxn-admin-web/src`, `dxn-admin-web/index.html`, `dxn-api/app` | **PASS** | لا نتائج. |
| 8 | grep `Color(0x` في `DXNTrainingApp/app/src` | **PASS** | النتائج كلها في `ui/theme/Color.kt` فقط. |
| 9 | grep hex في `dxn-admin-web/src` | **PASS** | النتائج كلها في `lib/chartColors.ts` فقط. |
| 10 | فحص خانات Plan 4 | **PASS** | لا توجد خانات `- [ ]` متبقية في `docs/superpowers/plans/2026-11-23-plan4-hikaya-tamayoz-branding.md`. |

### 5.2 فحوص يدوية / بيئية — **NOT RUN**

- المعاينة البصرية للوحة الإدارة بالهوية الجديدة، وحفظ/إعادة تحميل نبذة الفريق، وعرض 200%، وفحص DevTools لخط IBM Plex Sans Arabic وشبكة التحميل (W0): **NOT RUN**.
- رحلة المحاكي بعد الهوية: تسجيل متدربة ← «أحسنتِ»، متدرب ← «أحسنتَ»، حساب قديم بلا جنس ← صيغة مذكّرة بلا انهيار، وكذلك تدفق الـ offline cached-config: **NOT RUN** (لا محاكي في بيئة التنفيذ هذه).
- Arabic round-trip على MySQL بعد تغييرات الهوية: **PASS سابقاً** في القسم 2 قبل Plan 4؛ **NOT RUN** بعد Plan 4. أي تحقق جديد مسؤولية المالك.
- نقطة توقف الموافقة على الألوان/الـ wordmark في P0: تجاوزها المالك صراحةً باختيار «أكمل تلقائياً»؛ لم يُجرَ عرض موافقة منفصل.
- الالتزامات Plan 4 كلها محلية على الفرع `feat/plan1-api-content`؛ **لم يُدفع أو يُدمج** أي منها.
