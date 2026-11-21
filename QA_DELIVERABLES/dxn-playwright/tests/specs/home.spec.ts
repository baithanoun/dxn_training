import { test, expect } from '../fixtures/base';
import { config } from '../utils/config';

test.describe('Home / Skill Tree @smoke @critical', () => {
  test.use({ locale: 'ar-SA' });

  test('should display daily progress, XP and streak for authenticated user @critical', async ({
    authenticatedPage: page,
    homePage,
  }) => {
    await homePage.goto();
    await expect(homePage.greeting).toBeVisible();
    await expect(homePage.streakBadge).toBeVisible();
    await homePage.assertProgressDisplayed();
  });

  test('should render skill tree nodes', async ({ authenticatedPage: page, homePage }) => {
    await homePage.goto();
    await expect(homePage.skillTreeTitle).toBeVisible();
    await expect(homePage.skillNodes.first()).toBeVisible();
  });

  test('should open unlocked skill node', async ({ authenticatedPage: page, homePage }) => {
    await homePage.goto();
    await homePage.startFirstUnlockedLevel();
    await page.waitForURL(/\/lesson\//);
  });

  test('should not allow interaction with locked nodes', async ({ authenticatedPage: page, homePage }) => {
    test.info().annotations.push({ type: 'issue', description: 'QA-009' });
    await homePage.goto();
    const locked = homePage.skillNodes.filter({ has: page.getByTestId('node-locked') }).first();
    await expect(locked).toHaveAttribute('aria-disabled', 'true');
  });

  test('should sync progress from server @critical', async ({ page }) => {
    test.info().annotations.push({ type: 'issue', description: 'QA-007' });
    await page.route('**/api/v1/progress', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          userId: 'test-user',
          completedLevels: ['l1'],
          currentStreak: 5,
          totalXp: 1240,
        }),
      });
    });
    await page.goto('/');
    await expect(page.getByTestId('total-xp')).toContainText('1,240');
  });

  test('should handle offline state gracefully', async ({ page, homePage }) => {
    await homePage.goto();
    await page.context().setOffline(true);
    await page.reload();
    await expect(page.getByTestId('offline-message').or(page.getByText(/لا يوجد اتصال/i))).toBeVisible();
    await page.context().setOffline(false);
  });
});
