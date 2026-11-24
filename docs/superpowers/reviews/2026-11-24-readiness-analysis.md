# تحليل الجاهزية للنشر — «حكاية تميّز» — 2026-11-24

الفرع: `feat/plan1-api-content` (62 commit فوق `main`، **غير مدفوع وغير مدموج**). شجرة العمل نظيفة (`git status` فارغ).
الطريقة: أوامر تحقق فعلية (القسم 3) + قراءة موجّهة للملفات. ما لم يُفحص مُعلَّم.

## 1. حالة المكوّنات

| المكوّن | الحالة | الدليل | الفجوات |
|---|---|---|---|
| **API (Laravel 13)** | مكتمل وظيفياً ومُختبر | 135 اختباراً ناجحاً؛ 40 route تحت `/api`؛ Plans 2–4 منفّذة (`314fece`..`c91cea4`)؛ health route `/up` (`bootstrap/app.php:13`) | لا security headers؛ `sanctum.expiration = null` (`config/sanctum.php:53`)؛ وسائط على قرص محلي |
| **لوحة الإدارة (React)** | مكتملة، كل الصفحات على الـ API | `npm run build` نجح؛ لا `mockData`؛ هوية «حكاية تميّز» (`7b9987c`..`828d65d`) | تحذيرا lint (`Content.tsx:126,808` set-state-in-effect)؛ chunk > 500kB |
| **Android** | مكتمل وظيفياً، **غير جاهز للتوزيع** | `assembleDebug` نجح؛ auth حقيقي، تقدّم من الخادم، مخاطبة حسب الجنس، الهوية (`7a22220`..`bc0bf7c`)؛ R8 + توقيع قابل للضبط (`d7e454f`) | release يوقّع بمفتاح debug عند غياب keystore؛ `targetSdk = 34`؛ `versionCode = 1`؛ خلل شاشة Profile |
| **الهوية البصرية** | مكتملة | Plan 4 منفّذة؛ `docs/brand/*`؛ سجل التحقق | الشعار مشتق من JPEG 770px (حدّة محدودة) |
| **النشر (DockHosting)** | مجهّز، **لم يُختبر** | `deploy/Dockerfile`، `entrypoint.sh`، `apache-vhost.conf`، `.env.production.example`، `deploy/README.md` | بناء الصورة **NOT RUN**؛ لا CI؛ لا نسخ احتياطي موثّق؛ وسائط تُفقد بدون Persistent storage |
| **السياسات والامتثال** | **مفقودة** | لا سياسة خصوصية/شروط/حذف حساب (grep على routes وviews وكود Android = لا نتائج) | مطلوبة قبل Google Play وقبل جمع بيانات شخصية |
| **التوثيق** | جيد | `AGENTS.md`، READMEs، `plan2-verification.md` | — |

**تقدير الجاهزية (تقدير وليس قياساً):** API ≈ 90%، اللوحة ≈ 95%، Android ≈ 75% (وظيفياً جاهز؛ التوزيع ناقص)، النشر ≈ 50%، الامتثال ≈ 0%. **الإجمالي للإطلاق العام ≈ 70%؛ لإطلاق تجريبي داخلي محدود ≈ 85%.**

## 2. بنود الجاهزية

### Critical (تمنع الإطلاق)
- **K-1 لا سياسة خصوصية ولا شروط استخدام** — مؤكد. التطبيق يجمع الاسم والبريد والهاتف ورقم عضوية DXN (مشفّر) والجنس. Google Play يشترط رابط سياسة خصوصية، وأي تطبيق يتيح إنشاء حساب يجب أن يتيح طلب حذفه (verify with current Play policy).
- **K-2 لا حذف حساب** — مؤكد (لا route حذف مستخدم؛ `UserController` بلا `destroy`، والتعطيل فقط).
- **K-3 النشر لم يُجرَّب** — مؤكد. `docker build` لم يُشغَّل قط؛ أي خطأ في `deploy/Dockerfile` يظهر أول مرة على المضيف.
- **K-4 فقدان الصور المرفوعة** — مؤكد حسب `deploy/README.md:14`: الملفات تُفقد في كل نشر بدون Persistent storage (خطط مدفوعة). `FILESYSTEM_DISK=public` في `.env.production.example:31`.
- **K-5 لا نسخ احتياطي** — مؤكد (لا ذكر في `deploy/README.md`). قاعدة الإنتاج تحوي حسابات وتقدّماً وشهادات.

### Important
- **I-1 توقيع release بمفتاح debug** — `build.gradle.kts:65-69` يعود إلى `signingConfigs.debug` عند غياب keystore (مع تعليق تحذيري). خطر نشر APK غير قابل للتحديث لاحقاً. الأفضل: فشل البناء عند غياب الـ keystore في مهمة `bundleRelease`.
- **I-2 `targetSdk = 34`** — Google Play يفرض حداً أدنى يرتفع سنوياً؛ في 2026 غالباً أعلى من 34 لتطبيق جديد (محتمل؛ verify in Play Console).
- **I-3 خلل شاشة Profile** — مؤكد في سجل التحقق (`plan2-verification.md:84`): الشهادة الجديدة لا تظهر حتى إعادة تشغيل التطبيق. `ProfileViewModel` يحمّل الشهادات من `sections-progress` مرة واحدة (`ProfileViewModel.kt:32-55`). لم يُصلَح.
- **I-4 لا security headers** — محتمل الأثر: لا HSTS / X-Frame-Options / X-Content-Type-Options / CSP للوحة (grep = لا نتائج). التوكن مخزّن في `localStorage` فأي XSS يسرقه؛ CSP تخفف ذلك.
- **I-5 توكنات الموبايل بلا انتهاء** — `sanctum.expiration = null`؛ توكن اللوحة له `session_minutes` (Plan 3 T3)، وتوكن الموبايل دائم بالتصميم. مقبول بشرط توفر خروج/إلغاء؛ يُسجَّل كقرار.
- **I-6 كلمة مرور المدير الأولية** — `ADMIN_PASSWORD` في متغيرات المضيف (`.env.production.example:38`). يجب تغييرها بعد أول دخول وحذف المتغير.
- **I-7 `migrate --force` في كل إقلاع** (`entrypoint.sh:10`) — آمن لـ instance واحد؛ مع عدة instances قد تتسابق الـ migrations (محتمل). لا نسخ احتياطي قبل الـ migration.
- **I-8 محتوى تدريبي مبذور من بيانات تجريبية** — `ContentSeeder` من `MockData.kt`؛ فيه معلومات صحية وأرقام عمولات. يحتاج مراجعة المالك قبل الإطلاق.
- **I-9 لا CI** — لا `.github/workflows` فعّالة للبناء والاختبار؛ كل التحقق يدوي محلي.

### Minor
- M-1 تحذيرا oxlint في `Content.tsx:126,808` (set-state-in-effect).
- M-2 حجم bundle اللوحة > 500 kB (تحذير Vite).
- M-3 `versionCode = 1` / `versionName = "1.0"` — يجب رفعه لكل إصدار.
- M-4 أول تشغيل لـ `gradlew :app:assembleDebug` فشل ثم نجح فوراً دون تغيير (غالباً قفل ملفات/daemon على Windows) — لم يُحقَّق في السبب.
- M-5 الشعار مشتق من JPEG؛ نسخة 1024px مكبّرة.

## 3. نتائج التحقق (2026-11-24)

| الأمر | النتيجة |
|---|---|
| `git status --short` | فارغ |
| `git log --oneline main..HEAD \| wc -l` | 62 |
| `cd dxn-api && php artisan test` | **135 passed, 640 assertions** |
| `php artisan route:list --path=api` | 40 route |
| `cd dxn-admin-web && npm run build` | **PASS** (تحذير chunk size) |
| `npm run lint` | **PASS مع تحذيرين** (`Content.tsx:126`, `:808`) |
| `.\gradlew.bat :app:assembleDebug` | **PASS** (المحاولة الأولى فشلت بلا رسالة واضحة، الثانية نجحت دون تغيير) |
| `grep DXN_RELEASE_BASE_URL\|signingConfig build.gradle.kts` | موجود؛ placeholder مرفوض لنسخ release (`:131-139`)؛ fallback توقيع debug (`:65-69`) |
| `grep APP_DEBUG\|ADMIN_PASSWORD deploy/` | المفاتيح موجودة في `.env.production.example` و`README.md` (لم تُعرض القيم) |
| `docker build -f deploy/Dockerfile .` | **NOT RUN** — Docker غير مثبت |
| نشر فعلي / smoke test إنتاج | **NOT RUN** |
| فحص يدوي متصفح/محاكي اليوم | **NOT RUN** (آخر فحص يدوي مسجّل في `plan2-verification.md` بتاريخ 2026-11-23) |
