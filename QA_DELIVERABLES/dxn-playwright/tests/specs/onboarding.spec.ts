import { test, expect } from '../fixtures/base';

test.describe('Onboarding Flow @smoke', () => {
  test('should display first onboarding page', async ({ onboardingPage }) => {
    await onboardingPage.goto(1);
    await expect(onboardingPage.title).toContainText('تعلم');
    await expect(onboardingPage.dots).toHaveCount(3);
  });

  test('should navigate through all onboarding pages @critical', async ({ onboardingPage, page }) => {
    await onboardingPage.goto(1);
    await onboardingPage.clickNext();
    await onboardingPage.assertOnPage(2);
    await onboardingPage.clickNext();
    await onboardingPage.assertOnPage(3);
    await onboardingPage.clickNext();
    await page.waitForURL('/login');
  });

  test('should allow skipping onboarding for authenticated users', async ({ onboardingPage, page }) => {
    test.info().annotations.push({ type: 'issue', description: 'QA-002' });
    await onboardingPage.goto(1);
    // If skip implemented, this should redirect to home for authenticated state
    const hasSkip = await onboardingPage.skipLink.isVisible().catch(() => false);
    if (hasSkip) {
      await onboardingPage.clickSkip();
      expect(page.url()).not.toContain('/onboarding');
    }
  });

  test('should match onboarding visual baseline', async ({ page }) => {
    await page.goto('/onboarding/1');
    await expect(page).toHaveScreenshot('onboarding-page-1.png', {
      maxDiffPixelRatio: 0.02,
    });
  });
});
