# DXN Training — واجهة Laravel API

مصدر الحقيقة للنظام: Laravel 13 + Sanctum، قاعدة MySQL للتطوير (SQLite `:memory:` للاختبارات). تستخدمه لوحة الإدارة React وتطبيق Android؛ القواعد التجارية (الفتح، النقاط، XP، الـ Streak، الشهادات) تُحسب في الخادم.

## المتطلبات

- PHP 8.3 (مع ملحقات `pdo_mysql`, `mbstring`, `intl`, `gd`, `zip`) و Composer 2.
- MySQL 8.4 محلياً (Laragon) بقاعدة بيانات باسم `dxn_training`، المستخدم `root` وكلمة مرور فارغة في التطوير.
- لا حاجة إلى Node لهذا التطبيق وحده؛ بناء لوحة الإدارة يتم من `dxn-admin-web/`.

## التشغيل محلياً

```bash
cd dxn-api
composer install
cp .env.example .env
php artisan key:generate
# أنشئ قاعدة dxn_training في MySQL ثم:
php artisan migrate --seed
php artisan storage:link          # مرة واحدة لرفع الوسائط
php artisan serve --host=0.0.0.0 --port=8000
```

`migrate --seed` يشغّل `AdminSeeder` و`ContentSeeder` و`SettingsSeeder`. `ContentSeeder` آمن لإعادة تشغيله. `php artisan migrate:fresh --seed` يحذف كل الجداول ويُستخدم **فقط** على قاعدة تطوير معروف أنها `dxn_training` المحلية. لتبديل قاعدة فارغة بعد تعديل الروابط: `php artisan config:clear && php artisan route:clear`.

**حساب المدير الافتراضي** يأتي من `.env`: `ADMIN_EMAIL=admin@dxn.local` و`ADMIN_PASSWORD=change-me-locally` (قيم التطوير في `.env.example`). غيّر كلمة المرور قبل أي نشر ولا تُبقِها في متغيرات الإنتاج بعد تشغيل `AdminSeeder`.

## الاختبارات

```bash
php artisan test        # أو: composer test (يمسح config أولاً)
```

> نتيجة التحقق الأخير (2026-11-23): **114 اختباراً / 551 تأكيداً، كلها ناجحة**. الاختبارات تعمل على SQLite عبر `phpunit.xml`، لذلك لا تكشف مشاكل ترميز MySQL؛ فحص Arabic round-trip يدوي موثّق في `docs/superpowers/plans/2026-11-21-plan2-verification.md`. كل نقطة API جديدة تحتاج اختبار Feature.

عند استدعاء API يدوياً أرسل دائماً `Accept: application/json`؛ بدونه قد يعطي مسار غير مصادَّق 500 برسالة `Route [login] not defined`. مثال:

```bash
curl -H 'Accept: application/json' -H 'Authorization: Bearer <token>' http://127.0.0.1:8000/api/auth/me
```

## متغيرات البيئة المهمة

| المتغير | الاستخدام |
|---|---|
| `APP_KEY` | إلزامي لتشفير الحقول (مثل رقم العضوية) والجلسات؛ ولّده بـ `php artisan key:generate --show` وثبّته في الإنتاج. |
| `APP_ENV` / `APP_DEBUG` / `APP_URL` | بيئة التطبيق وعنوانه؛ `false` و `production` في النشر. |
| `APP_LOCALE=ar`, `APP_FALLBACK_LOCALE=en` | لغة الواجهة/الرسائل. |
| `DB_CONNECTION=mysql`, `DB_HOST`, `DB_PORT=3306`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` | اتصال MySQL؛ تُحقن `DB_*` تلقائياً عند ربط قاعدة مُدارة في dockhosting. |
| `SESSION_DRIVER`, `CACHE_STORE`, `QUEUE_CONNECTION` | `database`/`file`/`sync` حسب البيئة (انظر `deploy/.env.production.example`). |
| `FILESYSTEM_DISK=public` | مطلوب مع `storage:link` لرفع صور المحتوى. |
| `FRONTEND_URL` | مصدر لوحة الإدارة في التطوير (`http://localhost:5173`)؛ يساوي `APP_URL` في الإنتاج لأن الواجهة والـ API على نفس النطاق. |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | يُستخدمان مرة واحدة بواسطة `AdminSeeder` ثم يُحذفان من متغيرات الإنتاج. |

القيم الكاملة في `.env.example`، قالب الإنتاج في `deploy/.env.production.example`. لا تلتزم ملفات `.env`.

## الإعدادات المؤثرة على السلوك

تُدار من `GET/PUT /api/admin/settings` (جدول `settings`)، والقيم الافتراضية في `App\Models\Setting::DEFAULTS`:

- `maintenance_mode` → مسارات الموبايل تُعيد 503 `{message:"التطبيق تحت الصيانة"}`؛ مسارات `/api/admin/*` و`auth/login|logout|me` تعمل.
- `quiz_rate_limit_per_minute` (الافتراضي 10) → يُحدِّد معدل المُحدِّد المُسمّى `completion` على `POST /api/levels/{id}/complete` (`throttle:completion`)، وليس قيمة ثابتة في المسار.
- `session_minutes` (الافتراضي 60) → مدة صلاحية توكن لوحة الإدارة (`dashboard`)؛ توكن الموبايل بلا انتهاء.
- `xp_first_try_multiplier` (1.5) و`xp_replay_ratio` (0.25) → حساب XP عند الإكمال.
- مفاتيح الإشعارات (`notify_*`) و`hearts_default`/`heart_refill_hours`/`notify_leagues` تُخزَّن فقط (ميزات «قريباً» وفق المواصفات).

## نقاط API

- `POST /api/auth/register|login|logout` و`GET /api/auth/me` — الدخول والتسجيل (رقم عضوية القائد يربط المتدرب بقائده). `me` وحده يُغلَّف في `{data: …}`؛ بقية النقاط تُعيد JSON غير مغلَّف.
- موبايل (بعد مصادقة): `GET /api/sections/tree`، `GET /api/users/{id}/progress|sections-progress|profile`، `POST /api/levels/{id}/complete`، `GET /api/leader/team`، `POST /api/events`.
- لوحة الإدارة (`auth:sanctum` + `role:admin`، البادئة `/api/admin`): `content/tree`، CRUD الأقسام/المهارات/المستويات، `levels/{id}/cards|rounds`، `reorder`، `media`، `users` (+ `export`)، `teams`، `settings`، `dashboard/overview`، `analytics` (+ `export`)، `certificates`.

المفاتيح في JSON بنمط camelCase وكل المعرّفات/Mعرّفات الربط نصوص (`string`)؛ الأدوار `ADMIN|LEADER|TRAINEE`. النتيجة والنجاح والـ XP والشهادات تُنتجها الواجهة الخلفية؛ جسم `{userId, score, passed}` القديم بدون `answers` يُرفض بـ 422. معدلات التحديد: register/login `5,1`، events `60,1`، completion عبر الإعداد أعلاه.

## واجهة الإدارة والنشر

لوحة React تُبنى إلى `dxn-api/public/admin/` (مجلد مُولَّد ومستبعد من Git) وتُقدَّم من Laravel تحت `/admin/`. خطوات النشر وتهيئة DockHosting والنسخ الاحتياطي والتخزين الدائم موضّحة في [`deploy/README.md`](../deploy/README.md)؛ النشر الفعلي مسؤولية المالك.

سجل التحقق النهائي: `docs/superpowers/plans/2026-11-21-plan2-verification.md`.
