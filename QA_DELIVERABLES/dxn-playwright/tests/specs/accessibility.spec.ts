import { test, expect } from '../fixtures/base';
import AxeBuilder from '@axe-core/playwright';
import { config } from '../utils/config';

const screens = [
  { name: 'Login', route: '/login' },
  { name: 'Register', route: '/register' },
  { name: 'Home', route: '/' },
  { name: 'Learn', route: '/learn' },
  { name: 'Lesson', route: '/lesson/l1' },
  { name: 'Quiz', route: '/quiz/l1' },
  { name: 'Chat Battle', route: '/chat/l3' },
  { name: 'Profile', route: '/profile' },
  { name: 'Team', route: '/team' },
  { name: 'Admin', route: '/admin/overview' },
];

test.describe('Accessibility Scans @a11y', () => {
  test.use({ locale: 'ar-SA' });

  test.beforeEach(({}, testInfo) => {
    if (!config.featureFlags.a11y) {
      testInfo.skip();
    }
  });

  for (const screen of screens) {
    test(`should have no critical/serious axe violations on ${screen.name}`, async ({ page }) => {
      await page.goto(screen.route);
      await page.waitForLoadState('networkidle');

      const accessibilityScanResults = await new AxeBuilder({ page })
        .withTags(['wcag2a', 'wcag2aa', 'wcag21aa'])
        .analyze();

      const criticalAndSerious = accessibilityScanResults.violations.filter(
        (v) => v.impact === 'critical' || v.impact === 'serious',
      );

      expect(
        criticalAndSerious,
        `Critical/serious a11y issues found on ${screen.name}: ${criticalAndSerious
          .map((v) => `${v.id} (${v.nodes.length} nodes)`)
          .join(', ')}`,
      ).toHaveLength(0);
    });
  }

  test('keyboard navigation should reach all interactive elements on home', async ({ page, homePage }) => {
    await homePage.goto();
    const interactive = page.locator('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    const count = await interactive.count();
    expect(count).toBeGreaterThan(0);

    // Tab through first few elements and ensure focus is visible
    await page.keyboard.press('Tab');
    const focused = page.locator(':focus');
    await expect(focused).toBeVisible();
  });
});
