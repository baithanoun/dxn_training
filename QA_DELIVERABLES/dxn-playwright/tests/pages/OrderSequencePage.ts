import { Page, Locator, expect } from '@playwright/test';

export class OrderSequencePage {
  readonly page: Page;
  readonly prompt: Locator;
  readonly stepItems: Locator;
  readonly checkButton: Locator;
  readonly continueButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.prompt = page.getByTestId('order-prompt');
    this.stepItems = page.getByTestId('order-step');
    this.checkButton = page.getByRole('button', { name: 'تحقق' });
    this.continueButton = page.getByRole('button', { name: 'متابعة' });
  }

  async goto(levelId: string): Promise<void> {
    await this.page.goto(`/order/${levelId}`);
  }

  async moveStepDown(text: string): Promise<void> {
    const item = this.stepItems.filter({ hasText: text });
    await item.getByRole('button', { name: 'للأسفل' }).click();
  }

  async submit(): Promise<void> {
    await this.checkButton.click();
  }

  async expectStepPosition(text: string, position: number): Promise<void> {
    const item = this.stepItems.nth(position - 1);
    await expect(item).toContainText(text);
  }
}
