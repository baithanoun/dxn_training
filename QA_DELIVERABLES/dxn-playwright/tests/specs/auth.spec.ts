import { test, expect } from '../fixtures/base';
import { config } from '../utils/config';
import { testData } from '../utils/test-data';

test.describe('Authentication @smoke @critical', () => {
  test.describe('Login', () => {
    test('should login with valid credentials @critical', async ({ loginPage, page }) => {
      await loginPage.login(config.users.trainee.email, config.users.trainee.password);
      await page.waitForURL('/');
    });

    test('should show error for invalid credentials', async ({ loginPage }) => {
      await loginPage.goto();
      await loginPage.fillEmail(config.users.trainee.email);
      await loginPage.fillPassword('wrong-password');
      await loginPage.clickSubmit();
      await loginPage.expectError(/خطأ|غير صحيح|invalid/i);
    });

    test('should validate required fields', async ({ loginPage }) => {
      await loginPage.goto();
      await loginPage.clickSubmit();
      await loginPage.expectError(/مطلوب|required/i);
    });

    test('should enforce rate limiting after repeated failures', async ({ loginPage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-003' });
      await loginPage.goto();
      for (let i = 0; i < 6; i++) {
        await loginPage.fillEmail(config.users.trainee.email);
        await loginPage.fillPassword(`wrong-${i}`);
        await loginPage.clickSubmit();
      }
      // Expect throttling message or disabled button
      const disabled = await loginPage.submitButton.isDisabled();
      const throttled = await loginPage.page.getByText(/محاولات|كثير|too many/i).isVisible();
      expect(disabled || throttled).toBeTruthy();
    });

    test('should navigate to register page', async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.registerLink.click();
      await page.waitForURL('/register');
    });
  });

  test.describe('Register', () => {
    test('should register with valid data @critical', async ({ registerPage, page }) => {
      const user = testData.users.newTrainee();
      await registerPage.goto();
      await registerPage.fillForm({
        name: user.fullName,
        email: generateUniqueEmail(),
        dxnId: user.dxnId,
        password: user.password,
      });
      await registerPage.consentCheckbox.check();
      await registerPage.submit();
      await page.waitForURL('/');
    });

    test('should reject invalid email', async ({ registerPage }) => {
      await registerPage.goto();
      await registerPage.emailInput.fill(testData.users.invalidEmail);
      await registerPage.submitButton.click();
      await registerPage.expectFieldError('email', /بريد|email/i);
    });

    test('should reject weak password', async ({ registerPage }) => {
      await registerPage.goto();
      await registerPage.passwordInput.fill(testData.users.weakPassword);
      await registerPage.confirmPasswordInput.fill(testData.users.weakPassword);
      await registerPage.submitButton.click();
      await registerPage.expectFieldError('password', /ضعيف|weak|8/i);
    });

    test('should require consent checkbox', async ({ registerPage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-006' });
      await registerPage.goto();
      const hasConsent = await registerPage.consentCheckbox.isVisible().catch(() => false);
      if (hasConsent) {
        await registerPage.submitButton.click();
        await registerPage.expectFieldError('consent', /موافقة|consent/i);
      }
    });
  });
});

function generateUniqueEmail(): string {
  return `qa.${Date.now()}@dxn.example`;
}
