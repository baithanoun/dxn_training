import { test as base, Page, TestInfo, Fixtures } from '@playwright/test';
import { existsSync } from 'fs';
import { LoginPage } from '../pages/LoginPage';
import { config } from '../utils/config';

export type AuthFixtures = {
  authenticatedPage: Page;
  authenticatedAsLeader: Page;
  authenticatedAsAdmin: Page;
};

const storageState = {
  trainee: 'playwright/.auth/trainee.json',
  leader: 'playwright/.auth/leader.json',
  admin: 'playwright/.auth/admin.json',
};

async function ensureAuthenticated(
  page: Page,
  baseURL: string | undefined,
  role: 'trainee' | 'leader' | 'admin',
): Promise<void> {
  await page.goto(baseURL ?? '/');

  // If storageState worked, user lands on home. Otherwise fall back to UI login.
  const onHome = await page.locator('[data-testid="home-greeting"]').isVisible().catch(() => false);
  if (onHome) return;

  const login = new LoginPage(page);
  const credentials = config.users[role];
  await login.login(credentials.email, credentials.password);
}

type AuthFixtureContext = {
  page: Page;
  baseURL: string | undefined;
};

export const authFixture = {
  authenticatedPage: async (
    { page, baseURL }: AuthFixtureContext,
    use: (page: Page) => Promise<void>,
    testInfo: TestInfo,
  ) => {
    if (!existsSync(storageState.trainee)) {
      testInfo.skip(true, 'Trainee storage state not found. Run auth.setup.ts first.');
      return;
    }
    await ensureAuthenticated(page, baseURL, 'trainee');
    await use(page);
  },

  authenticatedAsLeader: async (
    { page, baseURL }: AuthFixtureContext,
    use: (page: Page) => Promise<void>,
    testInfo: TestInfo,
  ) => {
    if (!existsSync(storageState.leader)) {
      testInfo.skip(true, 'Leader storage state not found. Run auth.setup.ts first.');
      return;
    }
    await ensureAuthenticated(page, baseURL, 'leader');
    await use(page);
  },

  authenticatedAsAdmin: async (
    { page, baseURL }: AuthFixtureContext,
    use: (page: Page) => Promise<void>,
    testInfo: TestInfo,
  ) => {
    if (!existsSync(storageState.admin)) {
      testInfo.skip(true, 'Admin storage state not found. Run auth.setup.ts first.');
      return;
    }
    await ensureAuthenticated(page, baseURL, 'admin');
    await use(page);
  },
};


// Convenience base test with only auth fixtures
export const authTest = base.extend<AuthFixtures>(authFixture as unknown as Fixtures<AuthFixtures, object>);
