import { Page, Locator, expect } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly registerLink: Locator;
  readonly forgotPasswordLink: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.getByLabel(/البريد|رقم الهاتف/);
    this.passwordInput = page.getByLabel('كلمة المرور');
    this.submitButton = page.getByRole('button', { name: 'دخول' });
    this.registerLink = page.getByRole('link', { name: 'سجّل' });
    this.forgotPasswordLink = page.getByRole('link', { name: 'نسيت كلمة المرور' });
    this.errorMessage = page.getByTestId('login-error');
  }

  async goto(): Promise<void> {
    await this.page.goto('/login');
  }

  async fillEmail(email: string): Promise<void> {
    await this.emailInput.fill(email);
  }

  async fillPassword(password: string): Promise<void> {
    await this.passwordInput.fill(password);
  }

  async clickSubmit(): Promise<void> {
    await this.submitButton.click();
  }

  async login(email: string, password: string): Promise<void> {
    await this.goto();
    await this.fillEmail(email);
    await this.fillPassword(password);
    await this.clickSubmit();
    await this.page.waitForURL('/');
  }

  async expectError(message: string | RegExp): Promise<void> {
    await expect(this.errorMessage).toContainText(message);
  }
}
