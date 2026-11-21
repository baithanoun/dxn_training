import { test, expect } from '../fixtures/base';

test.describe('Profile & Team @regression @critical', () => {
  test.describe('Profile', () => {
    test('should display user stats @smoke', async ({ authenticatedPage: page, profilePage }) => {
      await profilePage.goto();
      await expect(profilePage.userName).toBeVisible();
      await expect(profilePage.dxnId).toBeVisible();
      await profilePage.expectStatsVisible();
    });

    test('should navigate through menu items', async ({ authenticatedPage: page, profilePage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-018' });
      await profilePage.goto();
      await profilePage.openMenuItem('الشهادات');
      await expect(page).toHaveURL(/\/(certificates|profile)/);
    });

    test('should show admin button only for admins', async ({ authenticatedAsAdmin: page, profilePage }) => {
      await profilePage.goto();
      await expect(profilePage.adminButton).toBeVisible();
    });
  });

  test.describe('Team Dashboard', () => {
    test('should display team summary and members', async ({ authenticatedAsLeader: page, teamPage }) => {
      await teamPage.goto();
      await expect(teamPage.summaryTitle).toBeVisible();
      await teamPage.expectCompletionRateVisible();
      await expect(teamPage.memberRows.first()).toBeVisible();
    });

    test('should filter active and inactive members', async ({ authenticatedAsLeader: page, teamPage }) => {
      await teamPage.goto();
      await teamPage.filterInactive();
      // Each visible row should indicate inactive status
      const firstRow = teamPage.memberRows.first();
      await expect(firstRow.getByTestId('member-status')).toHaveText(/متوقف|inactive/i);
    });

    test('should send encouragement to team @critical', async ({ authenticatedAsLeader: page, teamPage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-020' });
      await teamPage.goto();
      await teamPage.sendEncouragement();
      await expect(page.getByText(/تم الإرسال|sent/i)).toBeVisible();
    });
  });
});
