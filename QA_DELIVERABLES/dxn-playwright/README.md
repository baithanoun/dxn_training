: DXN Training App — Playwright Automation Framework

## Overview

Production-ready end-to-end test automation for the DXN Training App. Built with TypeScript, Playwright, axe-core, and Page Object Model.

## Features

- **Page Object Model (POM):** Reusable, maintainable page classes.
- **Custom fixtures:** Pre-authenticated sessions, API mocking, test data.
- **Multi-browser:** Chromium, Firefox, WebKit.
- **Mobile viewports:** Pixel 7, iPhone 14.
- **Accessibility:** axe-core scans per screen.
- **Performance:** Web Vitals assertions.
- **Visual regression:** `toHaveScreenshot` examples.
- **CI-ready:** GitHub Actions workflow.
- **Tagging:** `@smoke`, `@regression`, `@critical`, `@a11y`, `@perf`.

## Quick Start

```bash
cd QA_DELIVERABLES/dxn-playwright

# 1. Install dependencies
npm install

# 2. Install browsers (only needed once)
npx playwright install --with-deps

# 3. Configure environment
cp .env.example .env
# Edit .env with real URLs and credentials

# 4. Run smoke tests
npm run test:smoke

# 5. Run all tests
npm test

# 6. Open HTML report
npm run report
```

## Scripts

| Script | Purpose |
|--------|---------|
| `npm test` | Run all tests across projects |
| `npm run test:smoke` | Smoke tests only |
| `npm run test:critical` | Critical priority tests |
| `npm run test:a11y` | Accessibility scans |
| `npm run test:perf` | Performance tests |
| `npm run test:ui` | Interactive UI mode |
| `npm run lint` | ESLint check |
| `npm run format` | Prettier format |
| `npm run typecheck` | TypeScript check |

## Project Structure

```
dxn-playwright/
├── playwright.config.ts       # Multi-browser / CI config
├── tests/
│   ├── fixtures/
│   │   ├── base.ts            # Custom test + page fixtures
│   │   └── auth.ts            # Authentication fixture
│   ├── pages/
│   │   ├── SplashPage.ts
│   │   ├── LoginPage.ts
│   │   ├── HomePage.ts
│   │   └── ...                # One POM per screen
│   ├── specs/
│   │   ├── splash.spec.ts
│   │   ├── auth.spec.ts
│   │   ├── home.spec.ts
│   │   ├── learning.spec.ts
│   │   ├── social.spec.ts
│   │   ├── admin.spec.ts
│   │   ├── accessibility.spec.ts
│   │   └── performance.spec.ts
│   └── utils/
│       ├── config.ts          # Environment config
│       ├── logger.ts          # Console / file logger
│       ├── test-data.ts       # Factories and constants
│       ├── global-setup.ts    # One-time setup
│       └── global-teardown.ts # One-time cleanup
└── .github/workflows/playwright.yml
```

## Environment Variables

See `.env.example`. Key variables:

- `BASE_URL` — application under test
- `API_BASE_URL` — backend API base
- `TEST_USER_EMAIL` / `TEST_USER_PASSWORD` — default trainee
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`
- `LEADER_EMAIL` / `LEADER_PASSWORD`
- `ENABLE_A11Y_TESTS`, `ENABLE_PERF_TESTS`

## Writing Tests

```typescript
import { test, expect } from '../fixtures/base';

test('user can complete a lesson quiz @critical', async ({ homePage, quizPage }) => {
  await homePage.goto();
  await homePage.startFirstUnlockedLevel();
  await quizPage.answerCorrectly();
  await expect(quizPage.completionTitle).toBeVisible();
});
```

## Selectors Strategy

- Prefer `getByRole`, `getByLabel`, `getByText`.
- Use `data-testid` as fallback for complex components.
- Avoid CSS/XPath and brittle selectors.
- No hard-coded `page.waitForTimeout`.

## CI / GitHub Actions

The workflow runs on every push/PR:

1. Install Node + dependencies.
2. Install Playwright browsers.
3. Run lint + typecheck.
4. Start dev server (if `BASE_URL` not set).
5. Run tests with sharding and upload reports.

## Tags

Use tags to slice execution:

- `@smoke` — fast, must-pass gate
- `@critical` — business-critical paths
- `@regression` — full coverage
- `@a11y` — accessibility checks
- `@perf` — performance budgets

## Maintenance Tips

1. Update POMs when UI changes; keep selectors centralized.
2. Add new specs for every new screen or flow.
3. Run `npm run test:smoke` before merging.
4. Review failed screenshots/videos in `playwright-report/`.
5. Keep test data deterministic; reset state in fixtures.

## Contact / Ownership

QA Automation Lead — maintain this framework alongside feature development.
