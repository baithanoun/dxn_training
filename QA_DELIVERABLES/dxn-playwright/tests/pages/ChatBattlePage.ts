import { Page, Locator, expect } from '@playwright/test';

export class ChatBattlePage {
  readonly page: Page;
  readonly characterName: Locator;
  readonly trustBar: Locator;
  readonly trustValue: Locator;
  readonly hearts: Locator;
  readonly optionButtons: Locator;
  readonly sendButton: Locator;
  readonly continueButton: Locator;
  readonly feedback: Locator;

  constructor(page: Page) {
    this.page = page;
    this.characterName = page.getByTestId('chat-character-name');
    this.trustBar = page.getByRole('progressbar', { name: 'الثقة' });
    this.trustValue = page.getByTestId('trust-value');
    this.hearts = page.getByTestId('heart-icon');
    this.optionButtons = page.getByTestId('chat-option');
    this.sendButton = page.getByRole('button', { name: 'إرسال' });
    this.continueButton = page.getByRole('button', { name: 'متابعة' });
    this.feedback = page.getByTestId('chat-feedback');
  }

  async goto(levelId: string): Promise<void> {
    await this.page.goto(`/chat/${levelId}`);
  }

  async selectOption(text: string): Promise<void> {
    await this.optionButtons.filter({ hasText: text }).click();
  }

  async sendResponse(): Promise<void> {
    await this.sendButton.click();
  }

  async expectTrust(value: number): Promise<void> {
    await expect(this.trustValue).toHaveText(`${value}%`);
  }

  async expectFeedback(contains: string): Promise<void> {
    await expect(this.feedback).toContainText(contains);
  }
}
