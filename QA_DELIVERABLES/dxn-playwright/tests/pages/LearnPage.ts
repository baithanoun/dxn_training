import { Page, Locator, expect } from '@playwright/test';

export class LearnPage {
  readonly page: Page;
  readonly continueButton: Locator;
  readonly levelRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.continueButton = page.getByRole('button', { name: 'تابع' });
    this.levelRows = page.getByTestId('level-row');
  }

  async goto(): Promise<void> {
    await this.page.goto('/learn');
  }

  async getLevelRow(title: string): Promise<Locator> {
    return this.levelRows.filter({ hasText: title });
  }

  async startLevel(title: string): Promise<void> {
    const row = await this.getLevelRow(title);
    await row.getByRole('button', { name: 'ابدأ' }).click();
  }

  async expectLevelLocked(title: string): Promise<void> {
    const row = await this.getLevelRow(title);
    await expect(row.getByTestId('level-status')).toHaveText('مقفل');
  }
}
