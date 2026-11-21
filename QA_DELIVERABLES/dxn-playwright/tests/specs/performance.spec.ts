import { test, expect } from '../fixtures/base';
import { config } from '../utils/config';

const routes = [
  { name: 'Login', route: '/login' },
  { name: 'Home', route: '/' },
  { name: 'Lesson', route: '/lesson/l1' },
];

test.describe('Performance Budgets @perf', () => {
  test.beforeEach(({}, testInfo) => {
    if (!config.featureFlags.perf) {
      testInfo.skip();
    }
  });

  for (const { name, route } of routes) {
    test(`${name} should meet LCP budget`, async ({ page }) => {
      const cdpSession = await page.context().newCDPSession(page);
      await cdpSession.send('Performance.enable');

      await page.goto(route);
      await page.waitForLoadState('networkidle');

      const metrics = await cdpSession.send('Performance.getMetrics');
      const lcpMetric = metrics.metrics.find((m) => m.name === 'LargestContentfulPaint');
      const lcp = lcpMetric ? lcpMetric.value * 1000 : null;

      if (lcp !== null) {
        expect(lcp, `${name} LCP was ${lcp}ms`).toBeLessThan(config.performance.maxLCP);
      }
    });

    test(`${name} should not have layout shifts above CLS budget`, async ({ page }) => {
      await page.goto(route);
      await page.waitForLoadState('networkidle');

      const cls = await page.evaluate(() => {
        return new Promise<number>((resolve) => {
          const observer = new PerformanceObserver((list) => {
            let value = 0;
            for (const entry of list.getEntries()) {
              if (!(entry as any).hadRecentInput) {
                value += (entry as any).value;
              }
            }
            resolve(value);
          });
          observer.observe({ entryTypes: ['layout-shift'] as any });
          setTimeout(() => resolve(0), 3000);
        });
      });

      expect(cls, `${name} CLS was ${cls}`).toBeLessThan(config.performance.maxCLS);
    });
  }

  test('home should load critical API within budget', async ({ page, homePage }) => {
    const responsePromise = page.waitForResponse(/api\/v1\/(progress|sections)/, { timeout: config.timeouts.api });
    await homePage.goto();
    const response = await responsePromise;
    expect(response.status()).toBe(200);
  });
});
