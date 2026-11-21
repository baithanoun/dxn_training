import { Page, Locator, expect } from '@playwright/test';

export class RegisterPage {
  readonly page: Page;
  readonly nameInput: Locator;
  readonly emailInput: Locator;
  readonly dxnIdInput: Locator;
  readonly sponsorCodeInput: Locator;
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly consentCheckbox: Locator;
  readonly submitButton: Locator;
  readonly backButton: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nameInput = page.getByLabel('الاسم الكامل');
    this.emailInput = page.getByLabel(/البريد|الهاتف/);
    this.dxnIdInput = page.getByLabel('رقم عضوية DXN');
    this.sponsorCodeInput = page.getByLabel('كود القائد');
    this.passwordInput = page.getByLabel('كلمة المرور', { exact: false });
    this.confirmPasswordInput = page.getByLabel('تأكيد كلمة المرور');
    this.consentCheckbox = page.getByRole('checkbox', { name: /أوافق/ });
    this.submitButton = page.getByRole('button', { name: 'إنشاء الحساب' });
    this.backButton = page.getByRole('button', { name: 'رجوع' });
    this.errorMessage = page.getByTestId('register-error');
  }

  async goto(): Promise<void> {
    await this.page.goto('/register');
  }

  async fillForm(data: {
    name: string;
    email: string;
    dxnId: string;
    sponsorCode?: string;
    password: string;
  }): Promise<void> {
    await this.nameInput.fill(data.name);
    await this.emailInput.fill(data.email);
    await this.dxnIdInput.fill(data.dxnId);
    if (data.sponsorCode) await this.sponsorCodeInput.fill(data.sponsorCode);
    await this.passwordInput.fill(data.password);
    await this.confirmPasswordInput.fill(data.password);
  }

  async submit(): Promise<void> {
    await this.submitButton.click();
  }

  async expectFieldError(field: string, message: string | RegExp): Promise<void> {
    const locator = this.page.getByTestId(`error-${field}`);
    await expect(locator).toContainText(message);
  }
}
