import { Page, Locator, expect } from '@playwright/test';

export class AdminPage {
  readonly page: Page;
  readonly header: Locator;
  readonly activeUsersCard: Locator;
  readonly completionRateCard: Locator;
  readonly avgStreakCard: Locator;
  readonly failedSkillsList: Locator;

  constructor(page: Page) {
    this.page = page;
    this.header = page.getByRole('heading', { name: 'لوحة الإدارة' });
    this.activeUsersCard = page.getByTestId('kpi-active-users');
    this.completionRateCard = page.getByTestId('kpi-completion-rate');
    this.avgStreakCard = page.getByTestId('kpi-avg-streak');
    this.failedSkillsList = page.getByTestId('failed-skill-row');
  }

  async goto(): Promise<void> {
    await this.page.goto('/admin/overview');
  }

  async expectKpisVisible(): Promise<void> {
    await expect(this.activeUsersCard).toBeVisible();
    await expect(this.completionRateCard).toBeVisible();
    await expect(this.avgStreakCard).toBeVisible();
  }

  async expectFailedSkillsCount(min: number): Promise<void> {
    await expect(this.failedSkillsList).toHaveCount(min);
  }
}
