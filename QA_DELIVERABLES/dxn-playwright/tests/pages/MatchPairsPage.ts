import { Page, Locator, expect } from '@playwright/test';

export class MatchPairsPage {
  readonly page: Page;
  readonly prompt: Locator;
  readonly leftItems: Locator;
  readonly rightItems: Locator;
  readonly checkButton: Locator;
  readonly timer: Locator;

  constructor(page: Page) {
    this.page = page;
    this.prompt = page.getByTestId('match-prompt');
    this.leftItems = page.getByTestId('match-left-item');
    this.rightItems = page.getByTestId('match-right-item');
    this.checkButton = page.getByRole('button', { name: 'تحقق' });
    this.timer = page.getByTestId('match-timer');
  }

  async goto(levelId: string): Promise<void> {
    await this.page.goto(`/match/${levelId}`);
  }

  async pair(leftText: string, rightText: string): Promise<void> {
    await this.leftItems.filter({ hasText: leftText }).click();
    await this.rightItems.filter({ hasText: rightText }).click();
  }

  async submit(): Promise<void> {
    await this.checkButton.click();
  }

  async expectTimerVisible(): Promise<void> {
    await expect(this.timer).toBeVisible();
  }
}
