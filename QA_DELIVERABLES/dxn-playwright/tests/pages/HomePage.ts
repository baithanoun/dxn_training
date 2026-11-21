import { Page, Locator, expect } from '@playwright/test';

export class HomePage {
  readonly page: Page;
  readonly greeting: Locator;
  readonly streakBadge: Locator;
  readonly xpText: Locator;
  readonly dailyProgressBar: Locator;
  readonly skillTreeTitle: Locator;
  readonly skillNodes: Locator;
  readonly continueButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.greeting = page.getByTestId('home-greeting');
    this.streakBadge = page.getByTestId('streak-badge');
    this.xpText = page.getByTestId('total-xp');
    this.dailyProgressBar = page.getByRole('progressbar', { name: /تقدمك اليومي/ });
    this.skillTreeTitle = page.getByRole('heading', { name: 'شجرة المهارات' });
    this.skillNodes = page.getByTestId('skill-node');
    this.continueButton = page.getByRole('button', { name: 'تابع' });
  }

  async goto(): Promise<void> {
    await this.page.goto('/');
  }

  async getNodeByTitle(title: string): Promise<Locator> {
    return this.skillNodes.filter({ hasText: title });
  }

  async startNode(title: string): Promise<void> {
    const node = await this.getNodeByTitle(title);
    await node.click();
  }

  async startFirstUnlockedLevel(): Promise<void> {
    const unlocked = this.skillNodes.filter({ has: this.page.getByTestId('node-unlocked') });
    await unlocked.first().click();
  }

  async assertProgressDisplayed(): Promise<void> {
    await expect(this.dailyProgressBar).toBeVisible();
    await expect(this.xpText).toBeVisible();
  }

  async expectNodeLocked(title: string): Promise<void> {
    const node = await this.getNodeByTitle(title);
    await expect(node).toHaveAttribute('aria-disabled', 'true');
  }
}
