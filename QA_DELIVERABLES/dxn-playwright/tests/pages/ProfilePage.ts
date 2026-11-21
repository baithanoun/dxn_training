import { Page, Locator, expect } from '@playwright/test';

export class ProfilePage {
  readonly page: Page;
  readonly userName: Locator;
  readonly dxnId: Locator;
  readonly xpStat: Locator;
  readonly streakStat: Locator;
  readonly certificatesStat: Locator;
  readonly menuItems: Locator;
  readonly adminButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.userName = page.getByTestId('profile-name');
    this.dxnId = page.getByTestId('profile-dxn-id');
    this.xpStat = page.getByTestId('stat-xp');
    this.streakStat = page.getByTestId('stat-streak');
    this.certificatesStat = page.getByTestId('stat-certificates');
    this.menuItems = page.getByTestId('profile-menu-item');
    this.adminButton = page.getByRole('button', { name: 'لوحة الإدارة' });
  }

  async goto(): Promise<void> {
    await this.page.goto('/profile');
  }

  async openMenuItem(name: string): Promise<void> {
    await this.menuItems.filter({ hasText: name }).click();
  }

  async openAdmin(): Promise<void> {
    await this.adminButton.click();
  }

  async expectStatsVisible(): Promise<void> {
    await expect(this.xpStat).toBeVisible();
    await expect(this.streakStat).toBeVisible();
    await expect(this.certificatesStat).toBeVisible();
  }
}
