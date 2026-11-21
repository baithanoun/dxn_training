import { Page, Locator, expect } from '@playwright/test';

export class LessonPage {
  readonly page: Page;
  readonly cardTitle: Locator;
  readonly cardBody: Locator;
  readonly nextButton: Locator;
  readonly startQuizButton: Locator;
  readonly progressBar: Locator;
  readonly tipText: Locator;

  constructor(page: Page) {
    this.page = page;
    this.cardTitle = page.getByTestId('lesson-card-title');
    this.cardBody = page.getByTestId('lesson-card-body');
    this.nextButton = page.getByRole('button', { name: 'التالي' });
    this.startQuizButton = page.getByRole('button', { name: 'ابدأ الاختبار' });
    this.progressBar = page.getByRole('progressbar', { name: /بطاقة/ });
    this.tipText = page.getByTestId('lesson-tip');
  }

  async goto(levelId: string): Promise<void> {
    await this.page.goto(`/lesson/${levelId}`);
  }

  async clickNext(): Promise<void> {
    await this.nextButton.click();
  }

  async startQuiz(): Promise<void> {
    await this.startQuizButton.click();
  }

  async completeCards(): Promise<void> {
    while (await this.nextButton.isVisible().catch(() => false)) {
      await this.nextButton.click();
    }
  }

  async expectCardCount(current: number, total: number): Promise<void> {
    await expect(this.page.getByText(`بطاقة ${current} من ${total}`)).toBeVisible();
  }
}
