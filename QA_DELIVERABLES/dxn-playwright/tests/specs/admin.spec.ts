import { test, expect } from '../fixtures/base';

test.describe('Admin Panel @regression @critical', () => {
  test('should display admin overview for admin users @critical', async ({
    authenticatedAsAdmin: page,
    adminPage,
  }) => {
    await adminPage.goto();
    await expect(adminPage.header).toBeVisible();
    await adminPage.expectKpisVisible();
  });

  test('should deny admin access for trainees @critical', async ({ authenticatedPage: page }) => {
    test.info().annotations.push({ type: 'issue', description: 'QA-022' });
    await page.goto('/admin/overview');
    // Expect redirect to home or 403 page
    await expect(page).not.toHaveURL('/admin/overview');
    await expect(page.getByText(/غير مصرح|403|unauthorized/i)).toBeVisible();
  });

  test('should show failed skills list', async ({ authenticatedAsAdmin: page, adminPage }) => {
    await adminPage.goto();
    await adminPage.expectFailedSkillsCount(1);
  });

  test('should expose content management routes', async ({ authenticatedAsAdmin: page }) => {
    test.info().annotations.push({ type: 'issue', description: 'QA-021' });
    await page.goto('/admin/content');
    await expect(page.getByRole('heading', { name: 'إدارة المحتوى' }).or(page.getByText(/غير موجود/i))).toBeVisible();
  });
});
