# دليل النشر على DockHosting

> هذا الدليل للمالك فقط: إنشاء التطبيق على المضيف، إدخال المتغيرات، ربط قاعدة البيانات، وإعداد DNS
> تتم يدوياً من لوحة المضيف. لا توجد بيانات دخول أو أسرار في هذا المستودع.

## 1. تأكيد قدرات المضيف (تم في 2026-11-23)

راجعت الوثائق الرسمية، والنتيجة أن المضيف **يقبل Docker**، لذلك استمررنا في تنفيذ حزمة النشر:

- <https://app.dockhosting.dev/docs> — يدعم «تطبيقات الويب وواجهات البرمجة في Node.js وNext.js وNestJS وLaravel وPHP وPython وGo وRuby والمواقع الثابتة وأي تطبيق له Dockerfile»، ويوفّر قواعد بيانات مُدارة (PostgreSQL/MySQL/MariaDB/MongoDB/Redis/Valkey/ClickHouse).
- <https://app.dockhosting.dev/docs/stacks/docker> — إذا كان مجلد المشروع يحتوي على `Dockerfile` فالمضيف يبني به «بغض النظر عن بقية المستودع»، ويوجّه الزيارات إلى المنفذ الذي يعلنه `EXPOSE` (أو إعداد `PORT`)؛ يجب أن يستمع التطبيق على `0.0.0.0`. **يُمنع تثبيت خوادم قواعد بيانات داخل الصورة** (MySQL مثلًا). يُنصح بملف `.dockerignore`. الملفات المكتوبة داخل الحاوية تُفقد عند كل إعادة نشر؛ يلزم تخزين دائم (خطط مدفوعة) للرفوعات.
- <https://app.dockhosting.dev/docs/stacks/laravel> — يكتشف `composer.json` + `artisan`، ويشغّل `composer install --no-dev` و`npm run build` عند وجود واجهة، ويخدم PHP-FPM خلف nginx على المنفذ 8080. يضيف `APP_ENV=production` و`APP_DEBUG=false` و`SESSION_DRIVER=file` و`CACHE_STORE=file` و`QUEUE_CONNECTION=sync` إن لم تكن محددة. **يجب ضبط `APP_KEY` بنفسك في Variables** وإلا خرج المستخدمون عند كل إعادة نشر. عند ربط قاعدة بيانات مُدارة تُحقن متغيرات `DB_*` تلقائياً. ينفّذ `php artisan migrate --force` بعد كل نشر. يضبط `TRUSTED_PROXIES=*` و`HTTPS=on`. العاملات والجدولة (cron) تحتاج خطة مدفوعة دائمة التشغيل.
- <https://app.dockhosting.dev/docs/databases> — MySQL 8.0 و5.7، قاعدة البيانات المُدارة على شبكة خاصة، الخطة المجانية 500 ميغابايت، والنسخ الاحتياطي اليومي في الخطط المدفوعة.
- <https://app.dockhosting.dev/docs/stacks/php> — «الملفات التي يكتبها تطبيقك تُفقد في النشر التالي ما لم تكن في تخزين دائم (خطط مدفوعة)». فعّل Persistent storage في Settings وأعطِ المجلد الذي يكتب إليه التطبيق، مثال `/app/uploads`.

**الخلاصة العملية:** نبني صورة Docker خاصة بنا (`php:8.3-apache`)، لأننا نتحكم بإصدار PHP وملحقاته، ونستقبل `EXPOSE 80`. لا نثبّت MySQL داخل الصورة، بل نربط MySQL 8.0 مُداراً من المضيف. نضبط المتغيرات من قسم **Variables** في اللوحة. الرفوعات تحت `storage/app/public` **تُفقد عند إعادة النشر ما لم يُوصل مجلد تخزين دائم (خطة مدفوعة) بمسار `/var/www/html/storage/app/public`**؛ هذا شرط أساسي إذا أردنا رفع صور المحتوى. cron/الطوابير غير متاحة إلا بخطة always-on مدفوعة؛ الافتراضي `QUEUE_CONNECTION=sync`.

## 2. شكل النشر

- جذر المستند في الصورة: `/var/www/html/public` (عبر `deploy/apache-vhost.conf` مع `AllowOverride All` و`a2enmod rewrite`).
- واجهة React تُبنى أثناء بناء الصورة إلى `/var/www/html/public/admin/` مع `VITE_API_URL=""`، أي أن كل طلبات `fetch` تذهب إلى `/api/*` على **نفس النطاق**؛ لذلك `FRONTEND_URL` = `APP_URL` ولا نحتاج CORS متعدد المصادر.
- `routes/web.php` يعيد `public/admin/index.html` لكل مسار يبدأ بـ `/admin/...`، و`BrowserRouter basename="/admin"` يتولّى التوجيه داخل React.
- أبقينا على مسار `/` الترحيبي الأصلي كما هو (لا يؤثر على الواجهة) لتفادي كسر أي رابط موجود.
- استخدمنا متحكّماً `AdminSpaController` بدلاً من closure حرفي كما في الخطة، لأن `php artisan route:cache` لا يستطيع تسلسل closures؛ المتحكّم يؤدي الوظيفة نفسها ويسمح بت caches المسارات في الصورة.
- أضفنا `dxn-admin-web/.env.production` بقيمة `VITE_API_URL=` فارغة: ملفات الوضع (`production`) تتقدّم على `.env.local` أثناء البناء، فلا تتسرّب URL التطوير إلى الإنتاج. ملف `.env.local` مستبعد من صورة Docker عبر `.dockerignore`.
- الملفات: `deploy/Dockerfile`، `deploy/apache-vhost.conf`، `deploy/entrypoint.sh`، `deploy/.env.production.example`، `.dockerignore` في جذر المستودع.

## 3. خطوات المالك

1. انسخ `deploy/.env.production.example` إلى **Variables** في لوحة DockHosting واستبدل `<domain>` و`<db-*>` بقيمك. لا ترفع ملف `.env` إلى المستودع.
2. ولّد `APP_KEY` مرة واحدة على جهازك: `php artisan key:generate --show`، ثم الصق القيمة (`base64:...`) في Variables. **لا تغيّرها بعد النشر**، وإلا سُجّل المستخدمون خارج حساباتهم.
3. اربط قاعدة MySQL 8.0 مُدارة من المضيف؛ تُحقن `DB_HOST/DB_DATABASE/DB_USERNAME/DB_PASSWORD` تلقائياً. جدول `sessions` موجود في الـ migrations الأصلية، و`SESSION_DRIVER=database` يعمل بدون عمل إضافي.
4. إن أردت رفع الوسائط: فعّل **Persistent storage** في Settings واجعل نقطة التركيب `/var/www/html/storage/app/public`. رابط `public/storage` ينشئه `entrypoint.sh` تلقائياً عند كل بدء.
5. انشر التطبيق من جذر المستودع. المضيف سيبني الصورة من `deploy/Dockerfile` ويوجّه المنفذ 80 إلى النطاق العام.
6. **بعد أول نشر فقط**، من قشرة التطبيق/SSH على الحاوية نفّذ يدوياً:
   - `php artisan db:seed --class=AdminSeeder`
   - `php artisan db:seed --class=ContentSeeder`
  ثم احذف `ADMIN_PASSWORD` (وإن أردت `ADMIN_EMAIL`) من Variables. لا تُشغّل `db:seed` تلقائياً في الصورة مطلقاً. `ContentSeeder` آمن لإعادة تشغيله، لكن `AdminSeeder` يعتمد على كلمة المرور المؤقتة.
7. حدّث `DXNTrainingApp/app/build.gradle.kts`: استبدل `https://<domain>/` في `release` بعنوان HTTPS الحقيقي قبل نشر التطبيق على المتاجر. مسار debug يبقى `http://10.0.2.2:8000/` وcleartext مسموح في debug فقط.
8. افتح `https://<domain>/admin/` وسجّل الدخول بحساب المدير الذي أنشأته. التأكد من أن `/admin/login` و`/admin/content` تعيدان صفحة React (SPA fallback).

`entrypoint.sh` ينفّذ `package:discover` ثم `config:cache` و`route:cache` و`migrate --force`، ثم `storage:link || true` (يتجاهل وجود الرابط سابقاً)، ثم `exec apache2-foreground`. أي فشل في migrate يُوقف بدء الحاوية حتى تظهر المشكلة في السجلات بدل تشغيل تطبيق بمخطط قديم. السجلات تُكتب إلى stdout/stderr ليجمعها المضيف.

## 4. أوامر البناء المحلية

```powershell
# واجهة React — البناء العادي
cd dxn-admin-web; npm run build

# النسخة نفسها للمصدر الواحد داخل Laravel
npm run build -- --outDir ../dxn-api/public/admin --emptyOutDir

# اختبارات الـ API
cd ../dxn-api; php artisan test

# صورة Docker (إن كان Docker مثبتاً؛ من جذر المستودع)
docker build -f deploy/Dockerfile .
```

`dxn-api/public/admin/` مستبعد من Git (`built in the image`)؛ لا تلتزم ملفات البناء.

## 5. سجل التحقق (2026-11-23)

| التحقق | النتيجة |
|---|---|
| `docker --version` | **غير متاح** على هذا الجهاز (`command not found`) ⟹ `docker build` **NOT RUN** |
| `npm run build` في `dxn-admin-web` | PASS (2485 وحدة، تحذير حجم الحزمة فقط) |
| `npm run build -- --outDir ../dxn-api/public/admin --emptyOutDir` | PASS؛ `index.html` يشير إلى `/admin/assets/...`؛ لا يوجد `127.0.0.1:8000` في الحزمة؛ `fetch` يستخدم البادئة الفارغة |
| `npm run lint` | PASS مع تحذيرات oxlint سابقة (Fast refresh / setState in effect) لم تُحدث في هذه المهمة |
| `php artisan route:cache` ثم `route:clear` | PASS؛ مسار `admin/{any?}` قابل للتخزين المؤقت |
| `php artisan serve --host=127.0.0.1 --port=8000` + `GET /admin/` | PASS (200، HTML يحتوي `id="root"` ومسارات `/admin/assets/`) |
| Routes عميقة `/admin/login` و`/admin/anything/deep` | PASS (200 + نفس HTML) عبر router محلي يحاكي إعادة توجيه Apache؛ ملاحظة: `php artisan serve` نفسه يعاني من خاصة PATH_INFO عند وجود مجلد `public/admin` فيعيد 404 لهذه المسارات، ولا تظهر المشكلة على Apache في الصورة |
| `php artisan test` | PASS — 129 اختباراً / 620 تأكيداً (تحقق 2026-11-24 بعد Laravel Boost) |
| `:app:assembleDebug` مع JDK 17 | PASS — `BUILD SUCCESSFUL in 42s` |
| بناء الصورة الفعلي | **NOT RUN** (Docker غير مثبت) |
| النشر الفعلي / DNS / الأسرار على المضيف | **NOT RUN** — من مسؤولية المالك |

> تنبيه: لم نفتح متصفحاً، ولم نُجرّب login فعلياً ضد MySQL على المضيف (قاعدة MySQL المحلية غير مشغّلة أثناء الفحص). اختبارات PHPUnit على SQLite تغطي منطق API، لكن اختبار الدخول End-to-end بعد النشر مطلوب من المالك.


## ������� ����

- ������ �������� (Sanctum) ��� ������ (sanctum.expiration = null) �����: ������ �������� (deactivate) �������Ǻ ����� ���� ������ �� ���� ������� ����� ������ �������� �����.
- ��� ��� ����: ���� ���� ���� ������ �� ���� ������� �� ���� `ADMIN_PASSWORD` �� Variables (���� ������ 6 �����).
- ���� ������ (X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Permissions-Policy, HSTS �� ������̡ CSP �����) ����� �������� ��� `App\Http\Middleware\SecurityHeaders`.

## Smoke tests (ط¨ط¹ط¯ ط§ظ„ظ†ط´ط±)

```powershell
$BASE = "https://<domain>"
curl -s -o NUL -w "%{http_code}" "$BASE/up"
curl -s -H 'Accept: application/json' "$BASE/api/app/config"
curl -s -o NUL -w "%{http_code}" -H 'Accept: application/json' -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' -d '{"login":"x","password":"y"}'
curl -sI "$BASE/admin/" | Select-String 'content-security-policy|x-content-type-options'
```

ظ…طھظˆظ‚ط¹: /up â†’ 200ط› /api/app/config â†’ 200 ظˆط§ظ„ط§ط³ظ… آ«ط­ظƒط§ظٹط© طھظ…ظٹظ‘ط²آ»ط› login ط®ط§ط·ط¦ â†’ 401ط› ط±ط¤ظˆط³ R2 ظ…ظˆط¬ظˆط¯ط©.
```
## ط§ظ„ظ†ط³ط® ط§ظ„ط§ط­طھظٹط§ط·ظٹ ظˆط§ظ„ظ…ط±ط§ظ‚ط¨ط©

- طھظپط¹ظٹظ„ ط§ظ„ظ†ط³ط® ط§ظ„ط§ط­طھظٹط§ط·ظٹ ط§ظ„ظٹظˆظ…ظٹ ظ„ظ€ MySQL ط§ظ„ظ…ظڈط¯ط§ط± ط¥ظ† ظˆظپظ‘ط±ظ‡ ط§ظ„ظ…ط¶ظٹظپ (verify with host docs). ظˆط¥ظ„ط§ ط§ط³طھط®ط¯ظ… `deploy/backup.sh` (ظٹظ‚ط±ط£ ط¨ظٹط§ظ†ط§طھ ط§ظ„ط§طھطµط§ظ„ ظ…ظ† envطŒ ط¨ظ„ط§ ظƒظ„ظ…ط§طھ ظ…ط±ظˆط± ط¯ط§ط®ظ„ ط§ظ„ظ…ظ„ظپ).
- ط§ط®طھط¨ط§ط± ط§ط³طھط¹ط§ط¯ط© ظˆط§ط­ط¯ ط¹ظ„ظ‰ ظ‚ط§ط¹ط¯ط© ظ…ط­ظ„ظٹط© (`dxn_restore_test`) ظˆطھط³ط¬ظٹظ„ ط§ظ„ظ†طھظٹط¬ط©.
- ظ…ط±ط§ظ‚ط¨ط© uptime ط®ط§ط±ط¬ظٹط© ط¹ظ„ظ‰ `/up` (ط®ط¯ظ…ط© ظٹط®طھط§ط±ظ‡ط§ ط§ظ„ظ…ط§ظ„ظƒ)ط› ط§ظ„ط³ط¬ظ„ط§طھ ط¹ط¨ط± `LOG_CHANNEL=stderr` ظپظٹ ظ„ظˆط­ط© ط§ظ„ظ…ط¶ظٹظپ.
- Rollback = ط¥ط¹ط§ط¯ط© ظ†ط´ط± commit ط³ط§ط¨ظ‚ط› طھط­ط°ظٹط±: migrations ظ„ط§ طھظڈط¹ظƒط³ طھظ„ظ‚ط§ط¦ظٹط§ظ‹.
```
