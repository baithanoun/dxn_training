import { test as setup } from '@playwright/test';
import { mkdirSync } from 'fs';
import { LoginPage } from '../pages/LoginPage';
import { config } from '../utils/config';
import { logger } from '../utils/logger';

const authDir = 'playwright/.auth';

async function isAppReachable(page: any): Promise<boolean> {
  try {
    await page.goto(config.baseURL, { timeout: 5000, waitUntil: 'commit' });
    return true;
  } catch {
    logger.warn(`App at ${config.baseURL} is not reachable. Skipping auth setup.`);
    return false;
  }
}

setup('authenticate trainee', async ({ page }) => {
  if (!(await isAppReachable(page))) {
    setup.skip();
    return;
  }
  mkdirSync(authDir, { recursive: true });
  const login = new LoginPage(page);
  await login.login(config.users.trainee.email, config.users.trainee.password);
  await page.context().storageState({ path: `${authDir}/trainee.json` });
});

setup('authenticate leader', async ({ page }) => {
  if (!(await isAppReachable(page))) {
    setup.skip();
    return;
  }
  mkdirSync(authDir, { recursive: true });
  const login = new LoginPage(page);
  await login.login(config.users.leader.email, config.users.leader.password);
  await page.context().storageState({ path: `${authDir}/leader.json` });
});

setup('authenticate admin', async ({ page }) => {
  if (!(await isAppReachable(page))) {
    setup.skip();
    return;
  }
  mkdirSync(authDir, { recursive: true });
  const login = new LoginPage(page);
  await login.login(config.users.admin.email, config.users.admin.password);
  await page.context().storageState({ path: `${authDir}/admin.json` });
});
