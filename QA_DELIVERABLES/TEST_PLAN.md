: DXN Training App — Test Plan & Strategy

## 1. Purpose

This document defines the QA strategy, scope, and execution plan for the DXN Training App. It aligns with ISO 25010 quality characteristics and ISTQB test design principles.

## 2. Scope

### In Scope
- Web SPA responsive experience (mobile, tablet, desktop)
- 16 identified screens from wireframes
- Authentication, onboarding, learning flows, gamification, team, admin
- Functional, UI/UX, accessibility, security, performance, API, and cross-browser testing
- Playwright E2E automation suite

### Out of Scope (Phase 1)
- Native Android/iOS app testing (separate mobile test strategy)
- Backend load testing beyond representative k6 plan
- Production chaos engineering

## 3. Quality Objectives

| Quality Characteristic | Target | Measurement |
|--------------------------|--------|-------------|
| Functional Suitability | >95% critical path coverage | E2E pass rate |
| Reliability | 99.5% uptime staging | CI results + monitoring |
| Usability | 0 critical/serious WCAG violations | axe-core scans |
| Performance | LCP < 2.5s, CLS < 0.1 | Lighthouse / Playwright perf |
| Security | 0 high/critical vulnerabilities | Snyk + OWASP ZAP |
| Maintainability | <10% flaky tests | CI retry analytics |

## 4. Test Levels

### 4.1 Unit Tests (Bottom)
- Business logic: XP calculation, trust scoring, unlock rules, mastery algorithm.
- Utilities: date helpers, validators, i18n.
- Target: 80%+ coverage for pure functions.

### 4.2 Integration Tests (Middle)
- API contracts (Pact)
- Service worker + IndexedDB offline behavior
- State management (Redux/Zustand/TanStack Query)

### 4.3 E2E Tests (Top)
- Playwright covering critical user journeys
- Tagged: @smoke, @critical, @regression, @a11y, @perf

## 5. Test Design Techniques

- **Equivalence Partitioning:** Group inputs into valid/invalid classes (email, DXN ID, phone).
- **Boundary Value Analysis:** Test min/max lengths, empty inputs, max XP values.
- **Decision Tables:** Map role × action × expected access for RBAC.
- **State Transition:** Locked → Unlocked → Completed for levels.
- **Pairwise Testing:** Browser × viewport × language × role combinations.
- **Risk-Based Testing:** Prioritize auth, payments (future), progress persistence, admin RBAC.

## 6. Roles & Responsibilities

| Role | Responsibility |
|------|----------------|
| QA Lead | Strategy, test plan, risk assessment, CI gates |
| Automation Engineer | POM maintenance, framework stability, flake reduction |
| Manual/Exploratory QA | Complex UX, edge cases, device lab testing |
| Developers | Unit tests, component tests, fixing defects |
| Product Owner | Acceptance criteria, prioritization |

## 7. Test Environments

| Environment | URL | Purpose |
|-------------|-----|---------|
| Local | http://localhost:3000 | Developer + automation dev |
| Staging | https://staging.dxn-training.example | QA + CI nightly |
| Production | https://app.dxn-training.example | Smoke + monitoring |

## 8. Tools

| Category | Tool |
|----------|------|
| E2E Automation | Playwright |
| Accessibility | axe-core / Pa11y |
| Performance | Lighthouse CI, Web Vitals |
| Security | OWASP ZAP, Snyk, Semgrep |
| API Contract | Pact |
| Load | k6 |
| Monitoring | Sentry, PostHog, Datadog |
| CI/CD | GitHub Actions |
| Test Management | Jira / Xray / TestRail |

## 9. Entry / Exit Criteria

### Entry Criteria
- Feature code complete and merged to staging
- Acceptance criteria defined and reviewed
- Test data prepared
- Environment stable

### Exit Criteria
- 100% @critical tests passing
- 0 P0/P1 open defects
- Accessibility: 0 critical/serious violations
- Performance budgets met on staging
- Security scan: no new high/critical issues

## 10. Defect Management

| Severity | SLA | Example |
|----------|-----|---------|
| P0 Critical | 4 hours | Data loss, auth bypass, payments broken |
| P1 High | 24 hours | RBAC missing, core feature non-functional |
| P2 Medium | 1 week | UI inconsistency, minor accessibility issue |
| P3 Low | Backlog | Copy tweak, cosmetic issue |

## 11. Reporting

- Daily CI summary to Slack/Teams
- Weekly QA status report with burn-down
- Release readiness report with quality gates
- Post-release monitoring review (48h)

## 12. Continuous Improvement

- Retrospectives per release
- Flaky test review and remediation
- Test case refinement based on production incidents
- Quarterly accessibility and security audits
