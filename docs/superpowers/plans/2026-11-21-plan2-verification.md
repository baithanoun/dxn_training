# سجل التحقق النهائي — النظام الكامل (Plan 2 F2 / Plan 3 T12)

> تاريخ التنفيذ: 2026-11-23 · الفرع: `feat/plan1-api-content` · وضع التحقق: **Evidence-led**.
> كل أمر أدناه نُفّذ فعلياً وسُجّل ناتجه، أو وُسم **`NOT RUN`** صراحةً. لا يُعدّ أي فحص يدوي ناجحاً لم يُنفَّذ.

## 1. الفحوص الحتمية

| # | الأمر / الفحص | النتيجة | الدليل |
|---|---|---|---|
| 1 | `cd dxn-api && php artisan test` | **PASS** | `114 tests, 551 assertions`، exit code `0` (مطابق للخط الأساسي). |
| 2 | `cd dxn-api && php artisan migrate:fresh --seed` على MySQL المحلي | **`NOT RUN`** | خادم MySQL غير مشغّل: لا توجد عملية `mysqld`، والمنفذ `3306` مغلق. أُجريت محاولة تنفيذ للأمر لتسجيل الخطأ (لم تُحذف أي بيانات لأن الاتصال لم يُنشأ): `SQLSTATE[HY000] [2002] No connection could be made because the target machine actively refused it (Connection: mysql, Host: 127.0.0.1, Port: 3306, Database: dxn_training, …)`، exit code `1`. لم يُشغَّل الخادم أو يُوقفه هذا التحقق. |
| 3 | `cd dxn-admin-web && npm run build` | **PASS** | `tsc -b && vite build` نجح؛ `✓ 2485 modules transformed`، `✓ built in 1.37s`، exit code `0`. تحذير واحد فقط: حزمة JS `801.07 kB` (> 500 kB). |
| 4 | `cd dxn-admin-web && npm run lint` | **PASS** | `oxlint`، exit code `0`، 14 تحذيراً (`react/only-export-components` في `button.tsx`, `badge.tsx`, `auth.tsx`؛ `react/set-state-in-effect` في `Overview`, `Settings` ×2, `Teams` ×2, `Analytics`, `Users` ×3, `Content` ×2)، بلا أخطاء. |
| 5 | `cd DXNTrainingApp && $env:JAVA_HOME="D:\webserver\appp\jdk17\extracted\jdk-17.0.12+7"; .\gradlew.bat :app:assembleDebug --no-daemon` | **PASS** | `BUILD SUCCESSFUL in 15s` — `39 actionable tasks: 1 executed, 38 up-to-date`، exit code `0` (OpenJDK 17.0.12، Gradle wrapper 8.7). |
| 6 | Arabic round-trip على MySQL | **`NOT RUN`** | MySQL غير قابل للوصول (انظر #2). مُحاولة القراءة بأداة MySQL (المسار الكامل لأن `mysql` ليس على `PATH`): `mysql: Unknown OS character set 'cp720'` ثم `mysql: Switching to the default character set 'utf8mb4'.` ثم `ERROR 2003 (HY000): Can't connect to MySQL server on 'localhost:3306' (10061)`، exit code `1`. لم يُنشأ أي سجل اختباري، لذلك لا يوجد تنظيف مطلوب. **لم يُعوَّض هذا بـ SQLite.** |
| 7 | السيناريو اليدوي الكامل (القسم 3 أدناه) | **`NOT RUN`** | لم يُشغَّل محاكي Android ولا متصفح لهذا التحقق؛ الخطوات موثّقة أدناه ليُنفّذها المالك. |

أدوات مُثبَّتة ومُتحقَّق منها: PHP `8.3.30`، Composer `2.10.2`، Node `v26.1.0`، npm `11.13.0`، JDK `17.0.12+7`.

## 2. Arabic round-trip — خطوات التنفيذ اليدوي (كلها `NOT RUN`)

1. شغّل MySQL المحلي ثم `cd dxn-api && php artisan migrate:fresh --seed` (**فقط** على قاعدة التطوير `dxn_training`) وشغّل `php artisan serve --host=0.0.0.0 --port=8000` و`npm run dev` وافتح `/admin/`.
2. أنشئ قسماً من لوحة الإدارة بعنوان `اختبار الترميز — ١٢٣ ✓` (الشرطة الطويلة `—`، الأرقام العربية `١٢٣`، وعلامة `✓`).
3. اقرأ القيمة من MySQL بالأمر التالي (في PowerShell يمكن استخدام المسار الكامل `C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe` وإضافة `--default-character-set=utf8mb4` لتفادي تحذير cp720):

   ```sh
   mysql -u root dxn_training -e "SELECT title FROM sections ORDER BY id DESC LIMIT 1"
   ```

4. تحقّق من **مساواة السلسلة نصياً وبالبايتات** مع `اختبار الترميز — ١٢٣ ✓` (ليس تقريباً ولا ترميزاً بديلاً).
5. احذف قسم الاختبار من لوحة الإدارة وأعد تشغيل الأمر للتأكد من اختفائه.

| الخطوة | النتيجة |
|---|---|
| 1 — تجهيز MySQL/الخوادم/الواجهة | **`NOT RUN`** |
| 2 — إنشاء القسم بالعنوان العربي | **`NOT RUN`** |
| 3 — القراءة بأمر `mysql` أعلاه | **`NOT RUN`** |
| 4 — التحقق من المساواة النصية والبايتية | **`NOT RUN`** |
| 5 — حذف سجل الاختبار | **`NOT RUN`** |

## 3. السيناريو اليدوي الكامل (E2E) — كل الخطوات `NOT RUN`

> هذا السيناريو مطلوب من المالك. شغّل الخوادم أولاً:
>
> ```powershell
> # 1) API (طرفية أولى)
> cd dxn-api
> php artisan serve --host=0.0.0.0 --port=8000
>
> # 2) لوحة الإدارة (طرفية ثانية)
> cd dxn-admin-web
> cp .env.example .env.local     # VITE_API_URL=http://127.0.0.1:8000
> npm install
> npm run dev                    # افتح http://localhost:5173/admin/
>
> # 3) Android Studio: شغّل محاكياً وافتح DXNTrainingApp ثم Run
> ```
>
> ملاحظات للتنفيذ اليدوي: أضف دائماً ترويسة `Accept: application/json` إلى طلبات API اليدوية (مثلاً `curl -H "Accept: application/json" -H "Authorization: Bearer <token>" http://127.0.0.1:8000/api/auth/me`)؛ بدونها قد يعطي مسار API غير المصادَّق خطأ 500 `Route [login] not defined`. الحساب الافتراضي يُقرأ من `dxn-api/.env` (`ADMIN_EMAIL=admin@dxn.local` / `ADMIN_PASSWORD=change-me-locally` في التطوير؛ غيّره في الإنتاج). الدخول والتسجيل محدودان بـ `5` محاولات/دقيقة لكل عنوان (`throttle:5,1`) فانتظر دقيقة بين المحاولات الفاشلة؛ إكمال المستوى يستخدم المُحدِّد المُسمّى `completion` بمعدل من الإعداد `quiz_rate_limit_per_minute` (الافتراضي 10/دقيقة)، والأحداث `60,1`. قبل بدء السيناريو من قاعدة نظيفة: `php artisan migrate:fresh --seed` و`php artisan storage:link`.

| # | خطوة السيناريو | النتيجة |
|---|---|---|
| 1 | سجّل الدخول كمدير في `/admin/login` بحساب `.env`، وتأكد من ظهور لوحة Overview. | **`NOT RUN`** |
| 2 | من Users أنشئ قائداً (`role=LEADER`) ببريد/هاتف ورقم عضوية DXN، وسجّل رقمه. | **`NOT RUN`** |
| 3 | في تطبيق Android سجّل متدرباً جديداً وأدخل رقم عضوية القائد من الخطوة 2 في حقل "رقم عضوية القائد (اختياري)"؛ تحقّق من ربطه بالقائد. | **`NOT RUN`** |
| 4 | أكمل المستوى الأول من القسم 1؛ تحقّق أن نقاط XP جاءت من استجابة السيرفر (لا من الحساب المحلي) وأن المستوى التالي فُتح بعد التحديث. | **`NOT RUN`** |
| 5 | افتح شاشة الفريق بحساب القائد على Android؛ تحقّق من ظهور المتدرب ونسبة تقدّمه. | **`NOT RUN`** |
| 6 | في لوحة الإدارة Users تحقّق من ظهور XP وآخر نشاط للمتدرب. | **`NOT RUN`** |
| 7 | افتح Overview وAnalytics؛ تحقّق من انعكاس النشاط/الإكمال/المتوسطات بعد إعادة التحميل. | **`NOT RUN`** |
| 8 | أكمل اختبار القسم الأخير (`is_final_quiz`)؛ تحقّق من إصدار الشهادة وظهورها في Profile. | **`NOT RUN`** |
| 9 | تحقّق من أن القسم 2 أصبح غير مقفل في شجرة التطبيق. | **`NOT RUN`** |

صنّف أي فشل في هذا السيناريو كتراجع حقيقي (regression) وأرفقه بلقطة شاشة/سجلات؛ لا تُحدَّث حالة الجدول إلى PASS إلا بعد تنفيذ الخطوة فعلياً.

## 4. مراجع

- الخطة الملزمة: `docs/superpowers/plans/2026-11-21-plan2-complete-system.md` (F2) و`docs/superpowers/plans/2026-11-22-plan3-remaining.md` (T12).
- النشر وتهيئة الإنتاج: `deploy/README.md` — النشر الفعلي/DNS/أسرار المضيف **`NOT RUN`** ومسؤولية المالك؛ لم يُسجَّل دخول إلى المضيف.
- بناء صورة Docker **`NOT RUN`** (Docker غير مثبّت على هذه البيئة كما سُجّل في `deploy/README.md`).
- فحوص المتصفح والمحاكي اليدوية للواجهات **`NOT RUN`** في هذه المهمة؛ فحصا البناء والتجميع فقط نُفّذت.
