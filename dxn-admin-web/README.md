:# DXN Training — لوحة إدارة الويب

موقع إدارة تفاعلي (Admin Dashboard) لتطبيق DXN Training، مبني بالكامل باستخدام React + TypeScript + Vite + Tailwind CSS، مع دعم كامل للغة العربية واتجاه RTL.

## الميزات

- نظرة عامة على أداء التطبيق والمتدربين (KPIs، رسوم بيانية، متدربون متوقفون).
- إدارة المحتوى التدريبي بشكل هرمي (أقسام ← مهارات ← مستويات).
- منشئ سيناريوهات مرئي (Visual Scenario Builder) لمحاكي المحادثة الحية مع معاينة حية.
- إدارة المستخدمين والفرق.
- لوحة تحليلات متقدمة (Funnel، Retention، أكثر الأسئلة رسوباً، تصدير تقارير).
- صفحة الإعدادات العامة والأمان والتلعيب.

## التقنيات

- React 19
- TypeScript
- Vite 6
- Tailwind CSS 3
- React Router 7
- Recharts
- Lucide React

## التشغيل محلياً

```bash
cd dxn-admin-web
npm install
npm run dev
```

ثم افتح المتصفح على: `http://localhost:5173`

## البناء للإنتاج

```bash
npm run build
```

الملفات الناتجة توجد في مجلد `dist/`.

## هيكل المشروع

```
src/
├── components/ui/       # مكونات واجهة reusable
├── data/mockData.ts     # بيانات تجريبية للعرض
├── layouts/             # تخطيطات الصفحات
│   └── AdminLayout.tsx
├── lib/                 # أدوات مساعدة
│   └── utils.ts
└── pages/               # صفحات لوحة الإدارة
    ├── Overview.tsx
    ├── Content.tsx
    ├── ScenarioBuilder.tsx
    ├── Users.tsx
    ├── Teams.tsx
    ├── Analytics.tsx
    └── Settings.tsx
```

## ملاحظات

- البيانات الحالية هي بيانات وهمية (Mock Data) لأغراض العرض.
- يمكن ربط المشروع لاحقاً بـ Backend API (مثل NestJS) لجلب البيانات الحقيقية.
- التصميم يدعم الشاشات الصغيرة بشكل جزئي، لكنه محسّن بشكل أساسي لشاشات Desktop ≥ 1280px.

## لقطات شاشة

| الصفحة | المعاينة |
|---|---|
| نظرة عامة | `screenshots/dxn-admin-overview.png` |
| المحتوى التدريبي | `screenshots/dxn-admin-content.png` |
| منشئ السيناريوهات | `screenshots/dxn-admin-scenario.png` |
| المستخدمون | `screenshots/dxn-admin-users.png` |
| التحليلات | `screenshots/dxn-admin-analytics-fixed.png` |
