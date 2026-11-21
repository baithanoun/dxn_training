import { test, expect } from '../fixtures/base';

test.describe('Splash Screen @smoke', () => {
  test('should display logo and loading text', async ({ splashPage }) => {
    await splashPage.goto();
    await expect(splashPage.logo).toBeVisible();
    await expect(splashPage.loadingText).toBeVisible();
  });

  test('should navigate away from splash automatically @critical', async ({ splashPage, page }) => {
    await splashPage.goto();
    await splashPage.waitForNavigation();
    expect(page.url()).not.toContain('/splash');
  });

  test('should not exceed splash budget', async ({ splashPage, page }) => {
    await splashPage.goto();
    const start = Date.now();
    await splashPage.waitForNavigation();
    const elapsed = Date.now() - start;
    expect(elapsed).toBeLessThan(5000); // 5s max splash time
  });
});
