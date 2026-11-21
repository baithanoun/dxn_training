import { FullConfig } from '@playwright/test';
import { logger } from './logger';

/**
 * Global setup runs once before all test projects.
 * Use it to seed test data, verify environments, or warm caches.
 */
async function globalSetup(_config: FullConfig): Promise<void> {
  logger.info('Starting global setup...');

  // Example: ensure test users exist via API
  // const api = new ApiClient();
  // await api.ensureTestUser(config.users.trainee);
  // await api.ensureTestUser(config.users.leader);
  // await api.ensureTestUser(config.users.admin);

  logger.info('Global setup complete.');
}

export default globalSetup;
