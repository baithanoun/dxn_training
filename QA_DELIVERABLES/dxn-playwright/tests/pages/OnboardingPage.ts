import { Page, Locator } from '@playwright/test';

export class OnboardingPage {
  readonly page: Page;
  readonly title: Locator;
  readonly nextButton: Locator;
  readonly skipLink: Locator;
  readonly dots: Locator;

  constructor(page: Page) {
    this.page = page;
    this.title = page.getByRole('heading');
    this.nextButton = page.getByRole('button', { name: /التالي|ابدأ|تسجيل الدخول/ });
    this.skipLink = page.getByRole('link', { name: 'تخطي' });
    this.dots = page.getByTestId('onboarding-dots').locator('span');
  }

  async goto(pageNumber = 1): Promise<void> {
    await this.page.goto(`/onboarding/${pageNumber}`);
  }

  async clickNext(): Promise<void> {
    await this.nextButton.click();
  }

  async clickSkip(): Promise<void> {
    await this.skipLink.click();
  }

  async completeOnboarding(): Promise<void> {
    await this.goto(1);
    await this.clickNext();
    await this.clickNext();
    await this.clickNext();
  }

  async assertOnPage(pageNumber: number): Promise<void> {
    await this.page.waitForURL(`/onboarding/${pageNumber}`);
  }
}
