# Plan 4 — هوية فريق «حكاية تميّز»: الألوان، الاسم، الخط، نبذة الفريق، ومخاطبة المتدرب حسب الجنس

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans أو superpowers:subagent-driven-development. خطوات `- [ ]`. **commit واحد لكل مهمة**، لا push ولا merge. توقّف للمالك عند ⏸.

**Goal:** تحويل لوحة الإدارة (`dxn-admin-web`) وتطبيق Android (`DXNTrainingApp`) إلى هوية فريق «حكاية تميّز» (أحد فرق DXN):
1. ألوان مأخوذة من الشعار + لمسة خضراء.
2. الاسم بسطرين: **«حكاية تميّز»** وتحته **«DXN»**.
3. خط عربي موحّد: **IBM Plex Sans Arabic**.
4. اسم الفريق ونبذته **من قاعدة البيانات** قابلة للتعديل من لوحة الإدارة، وتظهر في Onboarding بدل النص الحالي.
5. نصوص التطبيق تخاطب المتدرب **حسب جنسه** (مذكر/مؤنث).
6. حذف أي ذكر لـ Duolingo من المنتج.

**Architecture:** مصدر واحد للألوان والخط في كل تطبيق (Web: `tailwind.config.js` + `src/index.css`؛ Android: `ui/theme/{Color,Type,Theme}.kt`)، مكوّن اسم واحد (`BrandMark`) في كل تطبيق، إعدادات الفريق في جدول `settings` الموجود عبر `App\Models\Setting`، و`gender` عمود جديد في `users`.

**Tech Stack:** Laravel 13 (موجود)، React 19 + Tailwind 3 + Recharts (موجود)، Kotlin + Compose Material3 (موجود). لا مكتبات جديدة؛ ملفات الخط تُضاف كأصول (ترخيص SIL OFL 1.1).

**Spec:** قرارات المالك 2026-11-23:
- الاسم: «حكاية تميّز» وتحته «DXN».
- نص Onboarding «تعلم بأسلوب Duolingo» يُستبدل باسم الفريق ونبذته، من قاعدة البيانات، قابلة للتغيير في أي وقت، ولا يُذكر Duolingo إطلاقاً.
- لا صيغة مؤنث ثابتة؛ الصيغة حسب جنس المتدرب.
- استخدام خط عربي.
- إضافة اللمسة الخضراء.
- الشعار الدقيق: `docs/brand/hikaya-tamayoz-logo.jpg` (أضافه المنفّذ السابق من صورة المالك).

## نبرة الهوية
دافئة، داعمة، مرتبطة بالصحة والطبيعة. نص النبذة الافتراضي (يُخزَّن في قاعدة البيانات ويُعدَّل من اللوحة):

> **teamName:** حكاية تميّز
> **teamTagline:** لأن كل طموح يستحق أن يُروى!
> **teamBio:** في حكاية تميّز، نجمع بين ثقافة الحياة الصحية مع DXN وقوة العمل الجماعي الذكي عبر الإنترنت. نوفّر بيئة تدريبية داعمة ترافق كل متدرب ومتدربة من البداية حتى الاحتراف، لبناء مشروع خاص بمرونة تامة، ولتصبح كل حكاية قادمة حكاية تميّز.
> **teamWelcome:** أهلاً بك في عائلتك الثانية.. رحلتك نحو التميّز تبدأ الآن!

(قرار المالك: صياغة **محايدة** للنصوص الافتراضية. `teamWelcome` تُكتب بلا تشكيل فتصلح للجنسين. هذه قيم افتراضية فقط، ويحرّرها المالك من اللوحة.)

## Global Constraints
- **الألوان مأخوذة بالعيّنة** من `docs/brand/hikaya-tamayoz-logo.jpg` (متوسط بكسلات JPEG): plum `#69163D`، rose `#C0648D`، خلفية `#F8F8F4`. إن وفّر المالك SVG لاحقاً وتغيّر لون بأكثر من ΔE≈3: أعد الاشتقاق وأعد قياس التباين.
- نص عادي ≥ **4.5:1**؛ نص كبير وأيقونات وحدود عناصر تفاعلية ≥ **3:1** (WCAG 2.1 AA).
- `rose-500` **ليس لنص عادي ولا لزر نصه أبيض** (3.85). الزر الثانوي = rose-600 (4.96)، وrose-600 كنص على العاجي = 4.66.
- ألوان `warning`, `xp`, `heart`, `leaf`, `locked` للأيقونات والتعبئات فقط؛ للنص نسخها `*-text`.
- لا تغيير لمعرّفات الكود: `com.dxn.training`، أسماء المجلدات، مسارات الـ API الحالية.
- RTL كما هو؛ «DXN» داخل السطر الثاني LTR.
- Focus مرئي بلون plum-500.
- العقد: JSON camelCase، ids نصية؛ endpoints جديدة تعيد JSON غير مغلّف؛ كل endpoint جديد له PHPUnit feature test.
- أي اختصار مقصود يحمل `shortcut: <limit>, <when to upgrade>`.
- لا يُذكر Duolingo في أي نص يظهر للمستخدم أو في كود التطبيقين.

## Review Focus
1. المالك يغيّر النبذة من اللوحة → التطبيق يعرض النص الجديد في Onboarding عند الفتح التالي بدون تحديث التطبيق. *(T-B2، M4)*
2. `GET /api/app/config` يعمل **بدون تسجيل دخول** ولا يكشف أي إعداد آخر (xp، rate limit…). *(T-B1 test)*
3. نبذة فيها `<script>` أو HTML → تُخزَّن وتُعرض نصاً خاماً، لا تُنفَّذ. *(T-B1 test)*
4. متدربة (`gender=female`) ترى «أحسنتِ»، ومتدرب يرى «أحسنتَ»؛ مستخدم بلا جنس (قديم) لا ينهار التطبيق ويعرض الصيغة الافتراضية. *(G2 manual)*
5. `grep -rni duolingo DXNTrainingApp/app/src dxn-admin-web/src dxn-api/app dxn-api/database` → لا نتائج. *(M4)*

---

## Brand tokens (مصدر الحقيقة)

### اشتقاق التدرّج
`50..400` = مزج الأساسي مع الأبيض بنسب `0.92, 0.84, 0.68, 0.50, 0.28`؛ `500` = الأساسي؛ `600..900` = مزج مع `#140512` بنسب `0.15, 0.30, 0.45, 0.60`.

### plum — الأساسي (CTA، العناوين، الشريط الجانبي، الروابط)
| step | HEX | HSL |
|---|---|---|
| 50 | `#F3ECEF` | 334 23% 94% |
| 100 | `#E7DAE0` | 332 21% 88% |
| 200 | `#CFB4C1` | 331 22% 76% |
| 300 | `#B48A9E` | 331 22% 62% |
| 400 | `#935773` | 332 26% 46% |
| **500** | **`#69163D`** | **332 65% 25%** |
| 600 | `#5C1336` | 331 66% 22% |
| 700 | `#50112E` | 332 65% 19% |
| 800 | `#430E27` | 332 65% 16% |
| 900 | `#360C20` | 331 64% 13% |

### rose — الثانوي (التقدّم، التمييز، الحلقات، الشارات)
| step | HEX | HSL |
|---|---|---|
| 50 | `#FAF3F6` | 334 41% 97% |
| 100 | `#F5E6ED` | 332 43% 93% |
| 200 | `#EBCDDB` | 332 43% 86% |
| 300 | `#E0B2C6` | 334 43% 79% |
| 400 | `#D28FAD` | 333 43% 69% |
| **500** | **`#C0648D`** | **333 42% 57%** |
| 600 | `#A6567A` | 333 32% 49% |
| 700 | `#8C4866` | 334 32% 42% |
| 800 | `#733953` | 333 34% 34% |
| 900 | `#592B40` | 333 35% 26% |

### محايدة، دلالية، تلعيب، واللمسة الخضراء
| token | HEX | HSL | الاستخدام |
|---|---|---|---|
| `ivory` | `#F8F8F4` | 60 22% 96% | خلفية الصفحات (عيّنة من الشعار) |
| `surface` | `#FFFFFF` | 0 0% 100% | البطاقات |
| `text` | `#2A1420` | 327 35% 12% | النص الأساسي |
| `muted` | `#6E5A64` | 330 10% 39% | نص ثانوي |
| `border` | plum-100 `#E7DAE0` | | الحدود والمدخلات |
| `success` | `#2F7D5B` | 154 45% 34% | رسائل النظام الناجحة |
| `error` | `#C0392B` | 6 63% 46% | خطأ (أحمر مائل للبرتقالي، مميّز عن البرقوقي) |
| `error-bg` / `error-border` | `#FDECEA` / `#F5C2BC` | | بانرات الخطأ |
| `warning` / `warning-text` | `#B7791F` / `#8A5A14` | | تحذير (أيقونة / نص) |
| `info` | `#2B6CB0` | 211 61% 43% | معلومات |
| `xp` / `xp-text` | `#B7791F` / `#8A5A14` | | XP والشهادات (أيقونة / نص) |
| `xp-container` | `#FBF1E2` | | خلفية بطاقات المكافآت |
| `heart` | `#D64545` | 0 64% 55% | القلوب (أيقونات) |
| `streak` | rose-500 | | شعلة Streak (أيقونة) |
| `locked` / `locked-text` | `#A8959E` / `#7A6870` | | عنصر مقفل (زخرفي / نص) |
| **`leaf`** | **`#5E8C61`** | 124 20% 46% | **اللمسة الخضراء** — أيقونات النمو والإنجاز |
| **`leaf-text`** | **`#466B49`** | | نص بنفس الدلالة |
| **`leaf-container`** | **`#EEF4EE`** | | خلفية شارة «منجز» / قسم مكتمل |

**أين تظهر اللمسة الخضراء (`leaf`)** — بالحد الأدنى ليبقى البرقوقي هو الهوية:
- علامة ✓ للمستوى المكتمل وعقدة المستوى المكتملة في شجرة المهارات.
- تعبئة شريط التقدّم عند 100% وشارة «منجز» (`leaf-container` + `leaf-text`).
- أيقونة ورقة صغيرة زخرفية في Onboarding وبجانب عنوان النبذة.
- في اللوحة: شارة الحالة «منشور» في صفحة المحتوى، ومؤشرات KPI الإيجابية (`change` > 0) بدل الأخضر الحالي.

### التباين المُقاس (WCAG)
| الزوج | النسبة | الحكم |
|---|---|---|
| plum-500 على white / ivory | 11.78 / 11.06 | ✓ |
| white على plum-500 | 11.78 | ✓ |
| plum-500 على plum-50 / rose-50 | 10.13 / 10.78 | ✓ |
| rose-500 على white / ivory | 3.85 / 3.62 | نص كبير/أيقونات فقط |
| white على rose-500 | 3.85 | **ممنوع لنص عادي** |
| white على rose-600 | 4.96 | ✓ (الزر الثانوي) |
| rose-600 على white / ivory | 4.96 / 4.66 | ✓ |
| white على rose-700 | 6.49 | ✓ |
| rose-700 على ivory | 6.10 | ✓ |
| rose-300 على plum-500 | 6.37 | ✓ (سطر «DXN» على الداكن) |
| rose-500 على plum-500 | 3.06 | أيقونات فقط |
| text على white / ivory | 17.22 / 16.18 | ✓ |
| muted على white / ivory | 6.35 / 5.96 | ✓ |
| success على white / ivory | 4.99 / 4.69 | ✓ |
| error على white / ivory | 5.44 / 5.11 | ✓ |
| info على white | 5.42 | ✓ |
| warning/xp على white | 3.64 | أيقونات |
| warning-text/xp-text على white | 5.91 | ✓ |
| heart على white | 4.38 | أيقونات |
| leaf على white | 3.89 | أيقونات |
| leaf-text على white | 6.07 | ✓ |
| locked على white | 2.82 | زخرفي فقط |
| locked-text على white | 5.20 | ✓ |

> المنفّذ يقيس `leaf-text` على `leaf-container` و`xp-text` على `xp-container` في P0 ويسجّلهما (متوقع ≥ 5).

### ألوان BrandMark
| الخلفية | «حكاية تميّز» | «DXN» |
|---|---|---|
| فاتحة | plum-500 | rose-600، ≈60% من حجم السطر الأول، `tracking-widest` |
| داكنة (plum-500) | white | rose-300 |

### ألوان الرسوم (Recharts)
`primary` plum-500، `secondary` rose-500، `tertiary` xp `#B7791F`، `growth` leaf `#5E8C61`، `info` `#2B6CB0`، `error` `#C0392B`.

### الخط
**IBM Plex Sans Arabic** (SIL OFL 1.1) — الأوزان 400، 500، 600، 700. المصدر: إصدارات GitHub الرسمية `IBM/plex` (حزمة `ibm-plex-sans-arabic`). يُضمَّن مع ملف `OFL.txt`. سبب الاختيار: وضوح عالٍ على الشاشات الصغيرة، أوزان كاملة، ويتوافق مع إعداد Tailwind الحالي (`fontFamily.sans` يذكره أصلاً لكنه غير محمّل).

---

## P0 — معاينة الهوية

**Files:** Create `docs/brand/palette-preview.html`. Existing: `docs/brand/hikaya-tamayoz-logo.jpg`.
- [x] صفحة ثابتة (`<html lang="ar" dir="rtl">`، بلا مكتبات خارجية، الخط من `../../dxn-admin-web/public/fonts/` بعد W0 أو `system-ui` مؤقتاً) تعرض: الشعار، التدرّجين، الجدول الدلالي، BrandMark بحجمين على فاتح وداكن، أزرار (أساسي/ثانوي/outline/disabled)، بطاقة، شارة «منجز» خضراء، بانر خطأ، شريط تقدّم 40% و100%، 3 قلوب، XP، عنصر مقفل، نموذج Onboarding بالنبذة الافتراضية، وجدول التباين مع قياس `leaf-text/leaf-container` و`xp-text/xp-container`.
- [ ] Commit `docs(brand): hikaya tamayoz palette, logo and preview`

### ⏸ نقطة توقف المالك — موافقة على الألوان والـ wordmark واللمسة الخضراء

---

## L0 — تجهيز ملفات الشعار من JPEG (لا يوجد PNG/SVG — قرار المالك)

**Files:** Create `docs/brand/build_logo_assets.py` (سكربت، لا يُشغَّل في الإنتاج)، المخرجات: `docs/brand/logo-512.png`، `docs/brand/logo-mark-512.png`، `docs/brand/logo-1024.png`.
**المصدر الوحيد:** `docs/brand/hikaya-tamayoz-logo.jpg` (الحجم الفعلي 770×772؛ خلفية عاجية، ظل رمادي خفيف حول الحلقة).

- [x] السكربت بـ Pillow من `.venv` الموجود (`.venv\Scripts\python.exe -I docs\brand\build_logo_assets.py`):
  1. قصّ مربّع حول الدائرة (احسب حدودها من بكسلات الحلقة الوردية: R>170 وG<130 وB>100)، مع هامش 2%.
  2. قناع دائري: كل ما خارج الدائرة شفاف (يزيل الظل الرمادي والزوايا).
  3. داخل الدائرة: الخلفية العاجية تُترك كما هي (لا إزالة بالعتبة — تفادياً لهالات JPEG حول الحروف) → الناتج شعار دائري بخلفية عاجية داخلية وحواف شفافة.
  4. `logo-512.png` و`logo-1024.png` (resample LANCZOS).
  5. `logo-mark-512.png` لأيقونة Android: نفس الدائرة مكبّرة لتملأ 66/108 من اللوحة (منطقة الأمان) على خلفية شفافة.
- [x] افحص بصرياً عند 48px و192px: النص «حكاية تميّز» داخل الشعار يصبح غير مقروء عند 48px — هذا مقبول للأيقونة (الاسم مكتوب بجانبها في BrandMark). تحقّق كمياً: تباين الحواف 94.65 عند 48px مقابل 38.16 عند 192px، أي أن الحروف تتداخل عند الحجم الصغير.
- [x] `// shortcut: logo assets upscaled from a 770px JPEG, replace with vector when the owner has one` في رأس السكربت (كتعليق Python يبدأ بـ`# //` لتفادي خطأ الصياغة).
- [x] Commit `docs(brand): logo PNG assets derived from owner JPEG`

W3 تستخدم `logo-512.png` (→ `public/brand/logo.png`) و`logo-1024.png` لصفحة الدخول، وتولّد favicon 64px منه. M3 تستخدم `logo-512.png` (→ `res/drawable-nodpi/brand_logo.png`) و`logo-mark-512.png` كـ foreground للأيقونة.

## المرحلة B — بيانات الفريق والجنس (API)

### B1 — إعدادات الفريق + endpoint عام

**Files:** Modify `dxn-api/app/Models/Setting.php` (`DEFAULTS`)، `dxn-api/app/Http/Controllers/Admin/SettingsController.php` (`RULES`)، `dxn-api/routes/api.php`. Create `dxn-api/app/Http/Controllers/AppConfigController.php`، `dxn-api/tests/Feature/AppConfigTest.php`. Modify `dxn-api/tests/Feature/Admin/SettingsTest.php`.

**Interfaces:**
- مفاتيح جديدة في `Setting::DEFAULTS`: `team_name` = «حكاية تميّز»، `team_tagline` = «لأن كل طموح يستحق أن يُروى!»، `team_bio` = النبذة المحايدة أعلاه حرفياً، `team_welcome` = «أهلاً بك في عائلتك الثانية.. رحلتك نحو التميّز تبدأ الآن!»؛ وغيّر `app_name` الافتراضي إلى «حكاية تميّز».
- `RULES`: `team_name` `string|max:100`، `team_tagline` `nullable|string|max:150`، `team_bio` `nullable|string|max:1000`، `team_welcome` `nullable|string|max:200`. تُخزَّن كنص خام (بدون strip)؛ العرض هو المسؤول عن عدم تفسير HTML.
- `GET /api/app/config` — **بدون auth**، `throttle:60,1`، **خارج** middleware الصيانة → `{appName, teamName, teamTagline, teamBio, teamWelcome, maintenanceMode}` فقط.
- لا تعدّل صفوف `settings` الموجودة في قاعدة البيانات؛ القيم الافتراضية تُطبَّق عبر `Setting::get` حين لا يوجد صف. إن كان صف `app_name` محفوظاً بقيمة «DXN Training» يحدّثه المالك من اللوحة (سجّل ذلك في رسالة الـ commit).

- [x] Tests (`AppConfigTest`): `test_config_is_public` (بدون توكن → 200)، `test_config_returns_only_whitelisted_keys` (مفاتيح الاستجابة = الستة بالضبط)، `test_config_reflects_updated_settings` (PUT كمدير ثم GET → القيم الجديدة)، `test_html_bio_returned_raw` (`<script>x</script>` يعود كما هو كنص)، `test_config_available_during_maintenance`.
- [x] `SettingsTest`: `test_team_bio_max_1000` (1001 حرف → 422)، `test_defaults_include_team_keys`.
- [x] `php artisan test` → PASS (`122 tests, 588 assertions`). Commit `feat(api): team profile settings and public app config endpoint` — ملاحظة: صف `app_name` المحفوظ مسبقاً في قاعدة التطوير قد لا يزال يحمل `DXN Training` ويُحدَّث من اللوحة بواسطة المالك.

### B2 — جنس المستخدم

**Files:** Create migration `dxn-api/database/migrations/2026_11_23_000000_add_gender_to_users_table.php`. Modify `app/Models/User.php` (`#[Fillable]` + cast)، `app/Http/Requests/RegisterRequest.php`، `app/Http/Controllers/AuthController.php` (register)، `app/Http/Controllers/Admin/UserController.php` (store/update/index filter اختياري)، `app/Http/Resources/UserResource.php`، `app/Http/Resources/Admin/AdminUserResource.php`. Tests: `tests/Feature/RegisterTest.php`، `tests/Feature/Admin/UsersTest.php`.

**Interfaces:**
- عمود `gender` `string(6)->nullable()->after('name')` بقيم `male|female`. **nullable** لأن المستخدمين الحاليين بلا قيمة. `down()` يحذف العمود.
- مُسلسَل `gender`: `'MALE' | 'FEMALE' | null` في `UserResource` و`AdminUserResource`.
- `POST /api/auth/register`: `gender` **مطلوب** `in:male,female` (الطلب يرسل lowercase أو uppercase — طبّعه بـ `strtolower` في `prepareForValidation`)؛ رسالة الخطأ: «اختر الجنس».
- Admin `POST/PUT users`: `gender` `nullable|in:male,female`.

- [x] Tests: `test_register_requires_gender` (422 `errors.gender`)، `test_register_stores_gender` (`user.gender==='FEMALE'`)، `test_admin_can_set_gender`، `test_existing_user_without_gender_serializes_null`.
- [x] `php artisan migrate` على MySQL المحلي (dev فقط) + `php artisan test` → PASS (`129 tests, 620 assertions`)؛ عمود `gender varchar(6) nullable` موجود بعد `name`.
- [x] Commit `feat(api): user gender for gendered addressing`

---

## المرحلة W — لوحة الإدارة

### W0 — الخط

**Files:** Create `dxn-admin-web/public/fonts/ibm-plex-sans-arabic/{IBMPlexSansArabic-Regular,-Medium,-SemiBold,-Bold}.woff2` + `OFL.txt`. Modify `dxn-admin-web/src/index.css`، `tailwind.config.js` (`fontFamily.sans`).
- [x] نزّل الملفات من إصدار `IBM/plex` الرسمي (`@ibm/plex-sans-arabic@1.1.0`، رخصة OFL 1.1 محفوظة كـ `OFL.txt`). لا Google Fonts عن بُعد.
- [x] `@font-face` لكل وزن: `font-family: "IBM Plex Sans Arabic"; font-weight: 400|500|600|700; font-display: swap;` مع المسارات النسبية تحت `src/assets/fonts/` (اخترناها لتعمل مع `base: '/admin/'` في الإنتاج و`/` في dev). تحقق بـ `npm run build`: الـ CSS الناتج يشير إلى `/admin/assets/IBMPlexSansArabic-…-hash.woff2` (لا `/fonts/...` ثابت).
- [x] `fontFamily.sans`: `["IBM Plex Sans Arabic", "Noto Sans Arabic", "system-ui", "sans-serif"]` (حُذف Cairo).
- [x] Commit `feat(admin-web): self-hosted IBM Plex Sans Arabic`. تحقق DevTools (Network/Computed font) **لم يُشغّل** في هذه البيئة؛ الدليل المتاح: نجاح `npm run build` + `npm run lint` وفحص الـ CSS الناتج.

### W1 — tokens

**Files:** Modify `dxn-admin-web/tailwind.config.js`، `src/index.css`.
- [x] تحت `theme.extend.colors`: `brand: { plum: {50..900}, rose: {50..900}, ivory, surface, text, muted }`، `semantic`، `game` بالقيم من الجداول.
- [x] أبقِ مفتاح `dxn` مؤقتاً موجّهاً للقيم الجديدة؛ مع تعليق `// shortcut: legacy dxn alias pointing to brand tokens, remove in W3`.
- [x] `src/index.css` `:root` بالقيم HSL المطلوبة؛ `--radius` بلا تغيير. تحقّق: لا يوجد قسم `.dark`.
- [x] `npm run build && npm run lint` PASS؛ الـ CSS المُنتَج يحوي `#69163D` والقيم الجديدة. الفحص اليدوي لـ `/admin/login` و`/admin/` **لم يُشغّل** في هذه البيئة. Commit `feat(admin-web): hikaya tamayoz color tokens`

### W2 — إزالة الألوان المباشرة

**Files:** Create `dxn-admin-web/src/lib/chartColors.ts`. Modify `src/pages/{Analytics,Content,Overview,Settings,Teams,Users}.tsx` (الأسطر في الملحق؛ أعد grep لأن الأسطر قد تتغيّر).
- [x] `chartColors.ts`: `export const chartColors = { primary: "#69163D", secondary: "#C0648D", tertiary: "#B7791F", growth: "#5E8C61", info: "#2B6CB0", error: "#C0392B" } as const`.
- [x] Analytics/Overview: `#E52F12` → `chartColors.primary`؛ `#D4A017` → `chartColors.secondary`؛ `#DC3545` → `chartColors.error`.
- [x] بانرات `border-red-200 bg-red-50 text-red-*` → `border-semantic-error-border bg-semantic-error-bg text-semantic-error`.
- [x] `text-red-600` → `text-semantic-error`؛ `text-green-600` (Settings) → `text-game-leaf-text`.
- [x] مؤشرات KPI الإيجابية وشارة «منشور» → `game-leaf-*`؛ السلبية → `semantic-error`.
- [x] كل `dxn-*` في `src` → المكافئ في `brand-*` / `semantic-*` / `game-*`.
- [x] تحقق: hex فقط في `src/lib/chartColors.ts`؛ لا ألوان red/green/... مباشرة؛ لا `dxn-`؛ build + lint PASS. حُذفت أصول القالب غير المستخدمة `react.svg`/`vite.svg` هنا مبكراً (كانت مطلوبة في W3) كي يمر فحص الـ hex. الفحص البصري اليدوي للصفحات السبع والبانر والرسوم **لم يُشغّل**.
- [x] Commit `refactor(admin-web): replace hard-coded colors with brand tokens`

### W3 — الاسم، الشعار، وإعدادات الفريق

**Files:** Create `dxn-admin-web/public/brand/logo.png` (نسخة من `docs/brand/logo-512.png` — L0)، `public/favicon.png` (64px)، `src/components/BrandMark.tsx`. Modify `src/layouts/AdminLayout.tsx`، `src/pages/Login.tsx`، `src/pages/Settings.tsx`، `src/lib/types.ts`، `index.html`، `tailwind.config.js` (حذف `dxn`). Delete `src/assets/react.svg`, `src/assets/vite.svg` إن لم تُستورد (grep).

**Interface:** `export function BrandMark({ size = "sm", tone = "light" }: { size?: "sm" | "lg"; tone?: "light" | "dark" })` — `<img src={`${import.meta.env.BASE_URL}brand/logo.png`} alt="شعار حكاية تميّز">` (sm 40px، lg 96px) + `<span>حكاية تميّز</span>` (sm `text-lg font-bold`، lg `text-3xl font-bold`) + `<span dir="ltr">DXN</span>` (sm `text-xs`، lg `text-sm`، `tracking-widest font-semibold`)؛ الألوان من جدول BrandMark.
> قرار المالك: BrandMark يعرض «حكاية تميّز» ثابتاً؛ `team_name` يُستخدم في Onboarding فقط.

- [x] `AdminLayout` → `<BrandMark size="sm" tone="light"/>` بدل «DXN» و«DXN Training» (الشريط الجانبي أبيض).
- [x] `Login.tsx` → `<BrandMark size="lg" tone="light"/>` أعلى البطاقة.
- [x] `index.html`: `<title>حكاية تميّز | DXN</title>`، `<html lang="ar" dir="rtl">`، `<link rel="icon" type="image/png" href="%BASE_URL%favicon.png">`؛ تحقّق أن dist/index.html يشير إلى `/admin/favicon.png`. وُجدت 64×64 favicon.png من الشعار.
- [x] **Settings.tsx — قسم «هوية الفريق»** أول بطاقة في «إعدادات عامة»: اسم الفريق، الشعار النصي، نبذة الفريق (textarea بعدّاد /1000)، رسالة الترحيب؛ GET/PUT عبر `/api/admin/settings`؛ معاينة نص عادي (لا `dangerouslySetInnerHTML`)؛ `app_name` الافتراضي «حكاية تميّز»؛ النوع نُقل إلى `src/lib/types.ts`.
- [x] Users page: عمود «الجنس» + اختيار في الإضافة والتعديل + مرشح `?gender=` (ذكر/أنثى/غير محدد).
- [x] حُذف `dxn` من tailwind؛ `grep dxn-` = لا نتائج.
- [x] تحقق: build + lint PASS؛ لا «DXN Training» في `dxn-admin-web`. الفحص اليدوي (الشريط، الدخول، العنوان، الحفظ بعد إعادة التحميل، تكبير 200%) **لم يُشغّل**.
- [x] Commit `feat(admin-web): hikaya tamayoz wordmark, team profile settings and gender field`

---

## المرحلة M — تطبيق Android

### M0 — الخط

**Files:** Create `DXNTrainingApp/app/src/main/res/font/ibm_plex_sans_arabic_regular.ttf`, `_medium.ttf`, `_semibold.ttf`, `_bold.ttf` (أسماء lowercase بـ underscore فقط)، `res/font/ibm_plex_sans_arabic.xml` (font-family)، `app/src/main/assets/licenses/OFL-IBM-Plex.txt`. Modify `ui/theme/Type.kt`.
- [x] `Type.kt`: `val PlexArabic = FontFamily(Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal), Font(…_medium, FontWeight.Medium), Font(…_semibold, FontWeight.SemiBold), Font(…_bold, FontWeight.Bold))`؛ استُبدلت كل `FontFamily.Default` (7 مواضع) بـ `PlexArabic`. أُضيف `res/font/ibm_plex_sans_arabic.xml` وترخيص `assets/licenses/OFL-IBM-Plex.txt`.
- [x] `assembleDebug` نجاح. Commit `feat(android): IBM Plex Sans Arabic typography`

### M1 — tokens والثيم

**Files:** Modify `ui/theme/Color.kt`، `ui/theme/Theme.kt`، وكل مستخدمي الأسماء القديمة.
- [x] `Color.kt` باللوحة الجديدة كاملةً، مع ثلاثة رموز إضافية للوضع الداكن (`DarkBackground`, `DarkSurface`, `DarkLeaf`) كي لا تبقى ألوان خام خارج الملف.
- [x] حُذفت الأسماء القديمة وحدّثت كل الاستخدامات حسب الدلالة (CTA → BrandPlum؛ عناوين → BrandPlum700؛ أخطاء → Error؛ قلب → Heart؛ XP → Xp؛ مكتمل → Leaf*؛ خلفية → BrandIvory؛ بطاقة → Surface).
- [x] `Theme.kt` light بالقيم المطلوبة و`typography` من M0.
- [x] Dark: `primary BrandRose300`، `onPrimary BrandPlum900`، `background DarkBackground`، `surface DarkSurface`، `onSurface BrandPlum50`، `primaryContainer BrandPlum700`، `onPrimaryContainer BrandRose50`، `tertiary DarkLeaf` (قرار المالك: تحويل الوضع الداكن). جعلنا `dynamicColor = false` افتراضياً كي تظهر هوية الفريق فعلياً على Android 12+ (الفرع القديم متاح لمن يطلبه صراحةً).
- [x] `assembleDebug` PASS؛grep الأسماء القديمة = 0 و`Color(0x` في `Color.kt` فقط. أُزيل ذكر Duolingo من نص Onboarding مبكراً أثناء تعديل الألوان (يُسجَّل أيضاً في M4). Commit `feat(android): hikaya tamayoz color tokens and theme`

### M2 — إزالة الألوان المباشرة
**Files:** `ui/components/Common.kt`، `ui/screens/{CompletionScreen,LessonScreen,OnboardingScreen}.kt`، `ui/theme/Theme.kt` (الأسطر في الملحق — أعد grep).
- [x] كل الألوان الخام استُبدلت في M1 بالرموز الدلالية؛ أُتمّ M2 بجعل شارة النجاح والشهادة `LeafContainer`/`LeafText` (وفق الحالة: فشل → `ErrorContainer`)، وخلفية وسط الدرس `BrandRose50`، والصفحة الثالثة في Onboarding `LeafContainer` مع أيقونات `LeafText` (تفادياً لتباين الأبيض على `Leaf` = 3.89). ذكر Duolingo أُزيل في M1.
- [x] `grep -rn "Color(0x" app/src/main/java` → `ui/theme/Color.kt` فقط؛ `assembleDebug` PASS. Commit `refactor(android): replace hard-coded colors with brand tokens`

### M3 — الاسم، الشعار، الأيقونة

**Files:** Create `ui/components/BrandMark.kt`، `res/drawable-nodpi/brand_logo.png` (نسخة من `docs/brand/logo-512.png` — L0). Modify `res/values/strings.xml:2`، `res/values/colors.xml`، `res/values/themes.xml`، `res/drawable/ic_launcher_background.xml`، `res/drawable/ic_launcher_foreground.xml` (أو `mipmap-*`)، `ui/screens/{SplashScreen,LessonScreen,LoginScreen}.kt`.

**Interface:** `@Composable fun BrandMark(size: BrandMarkSize = BrandMarkSize.Small, onDark: Boolean = false, modifier: Modifier = Modifier)`؛ `enum class BrandMarkSize { Small, Large }` — `Image(painterResource(R.drawable.brand_logo), contentDescription = "شعار حكاية تميّز")` (40dp/96dp) + «حكاية تميّز» (`titleMedium`/`headlineMedium` Bold) + «DXN» داخل `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` (`labelSmall`/`labelLarge`، `letterSpacing = 2.sp`).

- [x] `strings.xml`: `app_name` = «حكاية تميّز».
- [x] Splash → `BrandMark(Large, onDark = true)`؛ Login أعلى النموذج → `BrandMark(Large)`؛ LessonScreen اكتفى بنص «DXN» بلون `BrandRose600` داخل البطاقة (قرار المنفّذ: يبقي مكان الصورة غير مزدحم).
- [x] `colors.xml`: حُذفت `purple_*`/`teal_*`، أُضيف `brand_plum`/`brand_ivory`؛ `themes.xml`: `statusBarColor` و`windowBackground` عاجيان.
- [x] أيقونة adaptive: خلفية `@color/brand_ivory`؛ foreground عبر layer-list يعرض `brand_logo_mark.png` (الدائرة بمقياس 66/108 على لوحة شفافة) — لا حاجة لـ Android Studio. أُضيفت نسخة `res/drawable-nodpi/brand_logo.png` و`brand_logo_mark.png` من L0.
- [x] `assembleDebug` PASS؛ لا «DXN Training» في `app/src`؛ فحص المحاكي بصرياً **لم يُشغّل**. Commit `feat(android): hikaya tamayoz wordmark, logo and app icon`

### M4 — Onboarding من بيانات الفريق (بدون Duolingo)

**Files:** Modify `data/remote/ApiService.kt`، `data/remote/NetworkModels.kt`، `ui/screens/OnboardingScreen.kt`، شاشة/ViewModel تملك Onboarding (verify: `AppNavigation.kt`). Create `data/repository/AppConfigRepository.kt`.

**Interfaces:**
- `@GET("api/app/config") suspend fun getAppConfig(): Response<AppConfigResponse>`؛ `data class AppConfigResponse(val appName: String, val teamName: String, val teamTagline: String?, val teamBio: String?, val teamWelcome: String?, val maintenanceMode: Boolean)`.
- `AppConfigRepository(api, context)`: `suspend fun load(): AppConfigResponse` — يحاول الشبكة، ويحفظ آخر نسخة ناجحة في DataStore (المكتبة موجودة) ويعيدها عند الفشل؛ وإن لم توجد نسخة محفوظة يعيد ثوابت مطابقة لـ `Setting::DEFAULTS` (B1) — `// shortcut: hard-coded first-run fallback mirrors server defaults, update both together`.
- Onboarding:
  - **الصفحة 1:** الشعار (`BrandMark(Large)`) + `teamName` عنواناً + `teamTagline` + `teamBio` (نص قابل للتمرير، `Text` عادي — لا HTML) + أيقونة ورقة `Leaf`.
  - **الصفحة 2:** «اكسب XP وأوسمة» → صيغة حسب الجنس غير متاحة قبل التسجيل؛ استخدم صيغة محايدة/جمع (انظر G1: «اكسبوا…» أو «اجمع XP وأوسمة» — قرار نصي يُسجَّل).
  - **الصفحة 3:** «ابنِ فريقك» → نفس قاعدة الصفحة 2.
  - **زر الختام:** `teamWelcome`.
- **احذف** النص «تعلم بأسلوب Duolingo» نهائياً.
- [ ] `grep -rni duolingo DXNTrainingApp/app/src dxn-admin-web/src dxn-api/app dxn-api/database` → لا نتائج.
- [ ] يدوياً: غيّر النبذة من اللوحة → أعد فتح التطبيق بعد مسح بيانات الـ Onboarding (أو تثبيت نظيف) → النص الجديد؛ أوقف الـ API → يظهر آخر نص محفوظ.
- [ ] Commit `feat(android): onboarding shows team name and bio from server`

---

## المرحلة G — المخاطبة حسب جنس المتدرب

### G1 — آلية الصيغ

**Files:** Create `DXNTrainingApp/app/src/main/java/com/dxn/training/ui/text/Gendered.kt`. Modify `data/model/User.kt` (حقل `gender`)، `data/remote/NetworkModels.kt`/`ApiService.kt` (`UserProfileResponse.gender: String?`)، `data/local/SessionStore.kt` (حفظ `gender` مع الجلسة)، `MainActivity.kt` أو جذر التنقل (توفير القيمة).

**Interfaces:**
- `enum class Gender { MALE, FEMALE, UNKNOWN }`؛ `fun String?.toGender(): Gender`.
- `val LocalGender = staticCompositionLocalOf { Gender.UNKNOWN }` يُوفَّر من الجلسة/الملف الشخصي في جذر الـ UI.
- `@Composable fun g(male: String, female: String): String` → حسب `LocalGender.current`؛ `UNKNOWN` → `male` (**قرار المالك**: الحسابات القديمة بلا جنس تُخاطَب بالمذكر).
- للنصوص خارج Composable: `fun Gender.pick(male: String, female: String): String`.

### G2 — تطبيق الصيغ على النصوص

**Files:** كل `ui/screens/*.kt` و`ui/components/Common.kt` التي تخاطب المستخدم (18 موضعاً تقريباً لكلمات مثل «أحسنت، ابدأ، تابع، حاول، استمر، أكمل» — اعمل grep أوسع لأفعال الأمر وضمائر المخاطب: `ك\b`، `أنت`).
- [ ] اجمع كل النصوص المخاطِبة في جدول داخل رسالة الـ commit (قبل/بعد مذكر/مؤنث). أمثلة إلزامية: «أحسنت!» → `g("أحسنتَ!", "أحسنتِ!")`؛ «حاول مرة أخرى» → `g("حاول مرة أخرى", "حاولي مرة أخرى")`؛ «ابدأ» → `g("ابدأ", "ابدئي")`؛ «تابع» → `g("تابع", "تابعي")`؛ «تقدمك» يبقى (لا يتغيّر)؛ «أنت» → `g("أنتَ", "أنتِ")`.
- [ ] `RegisterScreen`: حقل جنس إلزامي (زرّان «ذكر» / «أنثى»، `selectable` + `contentDescription`)، يُرسل `gender` إلى `/api/auth/register` (B2). رسالة خطأ الخادم تُعرض.
- [ ] بعد login/register: احفظ `gender` في `SessionStore` ووفّره عبر `LocalGender`.
- [ ] نصوص Onboarding قبل التسجيل لا تعرف الجنس → صيغة محايدة (M4).
- [ ] النصوص القادمة من قاعدة البيانات (محتوى الدروس، النبذة) **لا تُعدَّل** — مسؤولية المحرر.
- [ ] تحقق: `assembleDebug`؛ يدوياً: سجّل متدربة → «أحسنتِ» في شاشة النتيجة؛ متدرب → «أحسنتَ»؛ مستخدم قديم بلا جنس → الصيغة الافتراضية بلا انهيار.
- [ ] Commit `feat(android): gendered addressing based on trainee gender`

---

## قرارات المالك
محسومة (2026-11-23): النبذة الافتراضية محايدة؛ الحسابات بلا جنس تُخاطَب بالمذكر؛ لا يوجد PNG/SVG للشعار ← L0.
- BrandMark يعرض «حكاية تميّز» **ثابتاً** في اللوحة والتطبيق؛ `team_name` القابل للتعديل يُستخدم في Onboarding فقط.- لا يُعرض شعار DXN الرسمي؛ تكفي كلمة «DXN» النصية في السطر الثاني.- الوضع الداكن في Android **يُحوَّل** إلى الألوان الجديدة كما في M1 (يُحذف تعليق `shortcut` الخاص بمراجعة المالك).لا توجد أسئلة مفتوحة.
- لا يُعرض شعار DXN الرسمي؛ تكفي كلمة «DXN» النصية في السطر الثاني.
- الوضع الداكن في Android **يُحوَّل** إلى الألوان الجديدة كما في M1.

لا توجد أسئلة مفتوحة.

## ترتيب التنفيذ
P0 → ⏸ → L0 → B1 → B2 → W0 → W1 → W2 → W3 → M0 → M1 → M2 → M3 → M4 → G1 → G2

## ملحق — مخرجات grep (2026-11-23)

### ألوان مباشرة — Web
```
Analytics.tsx:191,199  border-red-200 bg-red-50 (بانر خطأ)
Analytics.tsx:307      fill="#E52F12"
Analytics.tsx:332,334  stroke/fill "#D4A017"
Analytics.tsx:358      fill="#DC3545"
Content.tsx:203        border-red-200 bg-red-50
Content.tsx:215,424,814,890,900,923,935  text-red-600
Overview.tsx:117       border-red-200 bg-red-50
Overview.tsx:253,255   stroke/fill "#E52F12"
Overview.tsx:261,263   stroke/fill "#D4A017"
Overview.tsx:288       fill="#DC3545"
Settings.tsx:158,238   border-red-200 bg-red-50
Settings.tsx:442       text-green-600
Settings.tsx:443,503   text-red-600
Teams.tsx:104          border-red-200 bg-red-50
Teams.tsx:232          text-red-600
Users.tsx:194          border-red-200 bg-red-50
Users.tsx:403,531,550  text-red-600
src/assets/react.svg, vite.svg — أصول القالب (احذفها في W3 إن لم تُستورد)
```

### ألوان مباشرة — Android
```
ui/components/Common.kt:208          0xFFFEF3C7
ui/screens/CompletionScreen.kt:94    containerColor (اقرأ السطر)
ui/screens/CompletionScreen.kt:172   0xFFE3EDF3
ui/screens/CompletionScreen.kt:184   0xFFFFF8E1
ui/screens/LessonScreen.kt:79        0xFFFFE4E1
ui/screens/OnboardingScreen.kt:22-24 ألوان صفحات Onboarding
ui/theme/Color.kt:6-22               لوحة DXN الحالية
ui/theme/Theme.kt:22,26,27,30,44,45,48,49,52,53,56,60  containers (light + dark)
ui/theme/Type.kt                     FontFamily.Default (7 مواضع)
```

### الاسم وDuolingo
```
dxn-admin-web/index.html:7                    <title>dxn-admin-web</title>
dxn-admin-web/src/layouts/AdminLayout.tsx:38-41  "DXN" + "DXN Training"
dxn-admin-web/src/pages/Settings.tsx:37       app_name: "DXN Training"
dxn-api/app/Models/Setting.php:10             'app_name' => 'DXN Training'
DXNTrainingApp/.../SplashScreen.kt:42         text = "DXN"
DXNTrainingApp/.../OnboardingScreen.kt:53     text = "DXN"
DXNTrainingApp/.../LessonScreen.kt:83         text = "DXN"
DXNTrainingApp/.../OnboardingScreen.kt:22     "تعلم بأسلوب Duolingo"   ← يُحذف (M4)
DXNTrainingApp/app/src/main/res/values/strings.xml:2  app_name = "DXN Training"
```
ذكر Duolingo في وثائق داخلية (خارج المنتج، خارج نطاق هذه الخطة؛ للعلم فقط): `srs_clean.md`، `srs_v3_clean.md`، `التطبيق.md`، `deepseek_markdown_*.md`، `designs/README.md:10`، `generate_wireframes.py:177`.

### أوامر التحقق الختامية
- API: `cd dxn-api && php artisan test`.
- Web: `cd dxn-admin-web && npm run build && npm run lint`؛ `grep -rnE "#[0-9A-Fa-f]{6}" src` → `src/lib/chartColors.ts` فقط.
- Android: `.\gradlew.bat :app:assembleDebug`؛ `grep -rn "Color(0x" app/src/main/java` → `ui/theme/Color.kt` فقط.
- `grep -rni duolingo DXNTrainingApp/app/src dxn-admin-web/src dxn-api/app dxn-api/database` → لا نتائج.
- `grep -rn "DXN Training" DXNTrainingApp/app/src dxn-admin-web/src dxn-admin-web/index.html dxn-api/app` → لا نتائج.
