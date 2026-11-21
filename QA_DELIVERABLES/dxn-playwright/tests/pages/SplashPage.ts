import { Page, Locator } from '@playwright/test';

export class SplashPage {
  readonly page: Page;
  readonly logo: Locator;
  readonly loadingText: Locator;

  constructor(page: Page) {
    this.page = page;
    this.logo = page.getByTestId('splash-logo');
    this.loadingText = page.getByText('جاري التحميل');
  }

  async goto(): Promise<void> {
    await this.page.goto('/splash');
  }

  async waitForNavigation(): Promise<void> {
    await this.page.waitForURL(/\/(onboarding\/1|login|)$/);
  }

  async assertLoaded(): Promise<void> {
    await this.logo.waitFor({ state: 'visible' });
  }
}
