import { Page, Locator, expect } from '@playwright/test';

export class TeamPage {
  readonly page: Page;
  readonly summaryTitle: Locator;
  readonly memberRows: Locator;
  readonly activeFilter: Locator;
  readonly inactiveFilter: Locator;
  readonly encourageButton: Locator;
  readonly completionRate: Locator;

  constructor(page: Page) {
    this.page = page;
    this.summaryTitle = page.getByText('ملخص الفريق');
    this.memberRows = page.getByTestId('team-member-row');
    this.activeFilter = page.getByRole('button', { name: 'نشط' });
    this.inactiveFilter = page.getByRole('button', { name: 'متوقف' });
    this.encourageButton = page.getByRole('button', { name: 'إرسال تشجيع' });
    this.completionRate = page.getByTestId('team-completion-rate');
  }

  async goto(): Promise<void> {
    await this.page.goto('/team');
  }

  async filterActive(): Promise<void> {
    await this.activeFilter.click();
  }

  async filterInactive(): Promise<void> {
    await this.inactiveFilter.click();
  }

  async sendEncouragement(): Promise<void> {
    await this.encourageButton.click();
  }

  async getMember(name: string): Promise<Locator> {
    return this.memberRows.filter({ hasText: name });
  }

  async expectCompletionRateVisible(): Promise<void> {
    await expect(this.completionRate).toBeVisible();
  }
}
