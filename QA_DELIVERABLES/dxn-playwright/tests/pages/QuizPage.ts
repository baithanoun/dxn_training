import { Page, Locator, expect } from '@playwright/test';

export class QuizPage {
  readonly page: Page;
  readonly question: Locator;
  readonly options: Locator;
  readonly checkButton: Locator;
  readonly continueButton: Locator;
  readonly hearts: Locator;
  readonly feedback: Locator;

  constructor(page: Page) {
    this.page = page;
    this.question = page.getByTestId('quiz-question');
    this.options = page.getByTestId('quiz-option');
    this.checkButton = page.getByRole('button', { name: 'تحقق' });
    this.continueButton = page.getByRole('button', { name: 'متابعة' });
    this.hearts = page.getByTestId('heart-icon');
    this.feedback = page.getByTestId('quiz-feedback');
  }

  async goto(levelId: string): Promise<void> {
    await this.page.goto(`/quiz/${levelId}`);
  }

  async selectOption(text: string): Promise<void> {
    await this.options.filter({ hasText: text }).click();
  }

  async answerCorrectly(): Promise<void> {
    // In real suite, derive correct answer from data or API mock
    await this.options.first().click();
    await this.checkButton.click();
  }

  async answerIncorrectly(): Promise<void> {
    await this.options.last().click();
    await this.checkButton.click();
  }

  async submitAndContinue(): Promise<void> {
    await this.checkButton.click();
    await this.continueButton.click();
  }

  async expectFeedback(contains: string): Promise<void> {
    await expect(this.feedback).toContainText(contains);
  }

  async expectHeartCount(count: number): Promise<void> {
    await expect(this.hearts).toHaveCount(count);
  }
}
