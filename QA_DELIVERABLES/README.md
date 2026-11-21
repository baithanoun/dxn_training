: DXN Training App — QA Deliverables

This folder contains a complete QA audit and production-ready Playwright automation framework for the DXN Training App.

## Deliverables

| File / Folder | Description |
|---------------|-------------|
| `QA_AUDIT_REPORT.md` | Full screen-by-screen QA audit with findings, risk heatmap, and roadmap |
| `bugs.json` | Machine-readable bug list (JSON) |
| `bugs.csv` | Machine-readable bug list (CSV) for Jira/TestRail import |
| `TEST_PLAN.md` | QA strategy, test design techniques, entry/exit criteria, tooling |
| `dxn-playwright/` | Complete Playwright TypeScript automation framework |

## Quick Start — Playwright

```bash
cd dxn-playwright
cp .env.example .env
# Edit .env with real URLs and credentials
npm install
npx playwright install --with-deps
npm run test:smoke
npm run report
```

## Framework Highlights

- **15 Page Objects** — one per app screen
- **Custom fixtures** — trainee, leader, admin authentication
- **Tagged tests** — @smoke, @critical, @regression, @a11y, @perf
- **Multi-browser** — Chromium, Firefox, WebKit
- **Mobile viewports** — Pixel 7, iPhone 14
- **Accessibility** — axe-core WCAG 2.1 AA scans
- **Performance** — LCP/CLS budgets via CDP
- **CI/CD** — GitHub Actions with sharding and artifact upload
- **Code quality** — ESLint + Prettier + TypeScript strict mode

## Assumptions

This audit was performed against the **16 wireframes/screens generated earlier** for the DXN Training App, assuming a responsive web SPA target. Update the following in `.env` and POM selectors once the real application is available:

- Exact framework (React/Next.js/Vue)
- Production URLs
- Authentication flow details
- Real data-testid / ARIA labels
- Backend API endpoints

## Next Steps

1. Fill in `.env` with real environment details.
2. Align POM selectors with actual implementation attributes.
3. Replace mock data/API stubs with real backend calls.
4. Run `@smoke` and `@critical` tests in CI.
5. Triage bugs from `bugs.json` / `bugs.csv`.
