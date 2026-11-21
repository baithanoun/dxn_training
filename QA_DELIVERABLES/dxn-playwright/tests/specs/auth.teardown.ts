import { test as teardown } from '@playwright/test';
import { rmSync } from 'fs';

teardown('cleanup auth states', async () => {
  const authDir = 'playwright/.auth';
  try {
    rmSync(authDir, { recursive: true, force: true });
  } catch {
    // ignore
  }
});
