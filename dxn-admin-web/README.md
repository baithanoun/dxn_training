# DXN Training — لوحة إدارة الويب

موقع إدارة تفاعلي (Admin Dashboard) لتطبيق DXN Training، مبني باستخدام React + TypeScript + Vite + Tailwind CSS، مع دعم كامل للغة العربية واتجاه RTL. **كل البيانات تأتي من واجهة Laravel API** عبر `src/lib/api.ts`؛ لا توجد بيانات وهمية ولا ربط بـ NestJS.

## الميزات

- نظرة عامة على أداء التطبيق والمتدربين (KPIs، رسوم بيانية، متدربون متوقفون) من `dashboard/overview`.
- إدارة المحتوى التدريبي بشكل هرمي (أقسام ← مهارات ← مستويات) مع رفع الوسائط.
- منشئ سيناريوهات مرئي (Visual Scenario Builder) لمحاكي المحادثة مع معاينة حية.
- إدارة المستخدمين والفرق (بحث/تصفية/صفحات/تصدير CSV).
- لوحة تحليلات متقدمة (Funnel، Retention، أكثر الأسئلة رسوباً، تصدير تقارير).
- صفحة الإعدادات العامة/الأمان/التلعيب (من `/api/admin/settings`) مع "(قريباً)" للميزات غير المفعّلة.

## التقنيات والمتطلبات

- Node.js و npm، React 19، TypeScript، Vite 8، Tailwind CSS 3، React Router 7، Recharts، Lucide React.
- واجهة DXN API تعمل على `http://127.0.0.1:8000` للتطوير (راجع `../dxn-api/README.md`).

## التشغيل محلياً

```bash
cd dxn-admin-web
npm install
cp .env.example .env.local     # VITE_API_URL=http://127.0.0.1:8000
npm run dev
```

افتح `http://localhost:5173/admin/` (سجّل الدخول عبر `/admin/login`). يجب تشغيل API أولاً. **حساب المدير الافتراضي** يأتي من `dxn-api/.env` (`ADMIN_EMAIL=admin@dxn.local` / `ADMIN_PASSWORD=change-me-locally` للتطوير). الدخول محدود بـ 5 محاولات/دقيقة، فانتظر دقيقة بين المحاولات الفاشلة.

## متغيرات البيئة

| الملف | القيمة | الغرض |
|---|---|---|
| `.env.example` (مُلتزم) | `VITE_API_URL=http://127.0.0.1:8000` | قالب التطوير. |
| `.env.local` (غير مُلتزم) | نفس القيمة | يُنشئه المالك محلياً ويُستخدم أثناء `npm run dev`. |
| `.env.production` (مُلتزم) | `VITE_API_URL=` (فارغة) | أثناء `npm run build` تتقدّم على `.env.local`، فتذهب كل الطلبات إلى `/api/*` على نفس النطاق مع Laravel. |

## البناء والفحص

```bash
npm run build    # tsc -b && vite build → dist/
npm run lint     # oxlint (لا ESLint ولا test script في المشروع)
```

نتيجة التحقق الأخير: `npm run build` و`npm run lint` كلاهما exit code `0` (تحذير oxlint غير حرج وتحذير حجم الحزمة فقط). لبناء الواجهة داخل صورة الإنتاج: `npm run build -- --outDir ../dxn-api/public/admin --emptyOutDir`.

## المصادقة والتوجيه

- الرمز (token) يُحفظ في `localStorage['dxn_admin_token']` بواسطة `src/lib/api.ts`، ويُرسل كـ `Authorization: Bearer …` مع `Accept: application/json`.
- أي استجابة 401 (عدا فشل الدخول نفسه) تمسح الرمز وتعيد التوجيه إلى `/admin/login`.
- المسارات: `/admin/login`، `/admin/` (نظرة عامة)، `/admin/content`، `/admin/scenario?levelId=…`، `/admin/users`، `/admin/teams`، `/admin/analytics`، `/admin/settings`. الراوتر يستخدم `basename="/admin"` و Vite يستخدم `base: '/admin/'`.
- في الإنتاج تخدم Laravel ملفات البناء من `dxn-api/public/admin/` (مجلد مُولَّد/gitignored) عبر SPA fallback؛ راجع [`../deploy/README.md`](../deploy/README.md).

## هيكل المشروع

```
src/
├── components/ui/       # مكونات واجهة reusable
├── layouts/             # AdminLayout.tsx
├── lib/                 # api.ts (كل الطلبات)، auth.tsx، types.ts، utils.ts، sanitize.ts
└── pages/               # Login، Overview، Content، ScenarioBuilder،
                         # Users، Teams، Analytics، Settings
```

## ملاحظات

- التصميم يدعم الشاشات الصغيرة بشكل جزئي، لكنه محسّن بشكل أساسي لشاشات Desktop ≥ 1280px.
- لا تُلتزم ملفات `.env.local` ولا مجلدات `node_modules/` و`dist/`.
- سجل التحقق النهائي للنظام كامل: `docs/superpowers/plans/2026-11-21-plan2-verification.md`.

## لقطات شاشة

| الصفحة | المعاينة |
|---|---|
| نظرة عامة | `screenshots/dxn-admin-overview.png` |
| المحتوى التدريبي | `screenshots/dxn-admin-content.png` |
| منشئ السيناريوهات | `screenshots/dxn-admin-scenario.png` |
| المستخدمون | `screenshots/dxn-admin-users.png` |
| التحليلات | `screenshots/dxn-admin-analytics-fixed.png` |
