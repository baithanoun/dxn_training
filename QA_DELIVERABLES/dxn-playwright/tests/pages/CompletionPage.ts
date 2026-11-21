import { Page, Locator, expect } from '@playwright/test';

export class CompletionPage {
  readonly page: Page;
  readonly title: Locator;
  readonly xpEarned: Locator;
  readonly nextLevelButton: Locator;
  readonly reviewButton: Locator;
  readonly shareButton: Locator;
  readonly skillBar: Locator;

  constructor(page: Page) {
    this.page = page;
    this.title = page.getByRole('heading', { name: /ممتاز|حاول مرة أخرى/ });
    this.xpEarned = page.getByTestId('xp-earned');
    this.nextLevelButton = page.getByRole('button', { name: 'المستوى التالي' });
    this.reviewButton = page.getByRole('button', { name: 'مراجعة البطاقات' });
    this.shareButton = page.getByRole('button', { name: 'مشاركة الإنجاز' });
    this.skillBar = page.getByRole('progressbar', { name: /إنجاز/ });
  }

  async waitForLoaded(): Promise<void> {
    await this.title.waitFor({ state: 'visible' });
  }

  async clickNextLevel(): Promise<void> {
    await this.nextLevelButton.click();
  }

  async clickReview(): Promise<void> {
    await this.reviewButton.click();
  }

  async clickShare(): Promise<void> {
    await this.shareButton.click();
  }

  async expectXp(xp: number): Promise<void> {
    await expect(this.xpEarned).toContainText(`+${xp}`);
  }
}
