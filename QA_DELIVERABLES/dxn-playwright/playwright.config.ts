import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file in project root
dotenv.config({ path: path.resolve(__dirname, '.env') });

const baseURL = process.env.BASE_URL || 'http://localhost:3000';
const isCI = !!process.env.CI;

export default defineConfig({
  testDir: './tests/specs',
  outputDir: './test-results',
  snapshotDir: './tests/snapshots',

  /* Run tests in files in parallel */
  fullyParallel: true,

  /* Fail the build on CI if you accidentally left test.only */
  forbidOnly: isCI,

  /* Retry on CI only */
  retries: isCI ? 2 : 0,

  /* Opt out of parallel tests on CI for stability, or use env worker count */
  workers: isCI ? parseInt(process.env.CI_WORKERS || '4', 10) : undefined,

  /* Reporter configuration: HTML, JUnit for CI, Allure compatible JSON */
  reporter: [
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'playwright-report/results.xml' }],
    ['list'],
  ],

  /* Shared settings for all projects */
  use: {
    baseURL,
    trace: process.env.TRACE_ON_FAILURE as 'on' | 'off' | 'retain-on-failure' || 'retain-on-failure',
    video: process.env.VIDEO_ON_FAILURE as 'on' | 'off' | 'retain-on-failure' || 'retain-on-failure',
    screenshot: 'only-on-failure',

    /* Browser context defaults */
    viewport: { width: 1280, height: 720 },
    actionTimeout: 10000,
    navigationTimeout: 15000,

    /* Collect console logs */
    launchOptions: {
      slowMo: 0,
    },

    /* Locale and timezone to match target users */
    locale: 'ar-SA',
    timezoneId: 'Asia/Riyadh',
    colorScheme: 'light',
  },

  /* Configure projects for major browsers + mobile */
  projects: [
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts$/,
      teardown: 'teardown',
    },
    {
      name: 'teardown',
      testMatch: /.*\.teardown\.ts$/,
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        channel: 'chromium',
        storageState: 'playwright/.auth/trainee.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        storageState: 'playwright/.auth/trainee.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'webkit',
      use: {
        ...devices['Desktop Safari'],
        storageState: 'playwright/.auth/trainee.json',
      },
      dependencies: ['setup'],
    },
    /* Mobile viewports */
    {
      name: 'mobile-chrome',
      use: {
        ...devices['Pixel 7'],
        storageState: 'playwright/.auth/trainee.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'mobile-safari',
      use: {
        ...devices['iPhone 14'],
        storageState: 'playwright/.auth/trainee.json',
      },
      dependencies: ['setup'],
    },
    /* Role-specific projects for admin/leader tests */
    {
      name: 'chromium-admin',
      testMatch: /admin|social/,
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'chromium-leader',
      testMatch: /social/,
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/leader.json',
      },
      dependencies: ['setup'],
    },
  ],

  /* Run local dev server before starting the tests if no BASE_URL is set */
  webServer: !process.env.BASE_URL?.startsWith('http')
    ? {
        command: 'npm run dev',
        url: 'http://localhost:3000',
        reuseExistingServer: !isCI,
        timeout: 120000,
      }
    : undefined,

  /* Global setup / teardown hooks */
  globalSetup: require.resolve('./tests/utils/global-setup'),
  globalTeardown: require.resolve('./tests/utils/global-teardown'),
});
