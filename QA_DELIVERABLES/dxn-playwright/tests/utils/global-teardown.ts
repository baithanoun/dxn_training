import { FullConfig } from '@playwright/test';
import { logger } from './logger';

/**
 * Global teardown runs once after all test projects.
 */
async function globalTeardown(_config: FullConfig): Promise<void> {
  logger.info('Starting global teardown...');

  // Example: clean up seeded test data
  // const api = new ApiClient();
  // await api.cleanupTestUsers();

  logger.info('Global teardown complete.');
}

export default globalTeardown;
