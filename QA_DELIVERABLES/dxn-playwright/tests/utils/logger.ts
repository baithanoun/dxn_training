import { ConsoleMessage } from '@playwright/test';

/**
 * Helper to capture browser console messages during tests.
 */
export function attachConsoleLogger(message: ConsoleMessage): void {
  const type = message.type();
  const text = message.text();

  // Ignore common noise
  if (text.includes('[HMR]') || text.includes('React DevTools')) return;

  if (type === 'error' || type === 'warning') {
    // In CI these can be attached to test report via testInfo.attach
    // eslint-disable-next-line no-console
    console.log(`[browser:${type}] ${text}`);
  }
}

/**
 * Simple file-like logger abstraction.
 */
export const logger = {
  info: (msg: string) => {
    // eslint-disable-next-line no-console
    console.log(`[INFO] ${msg}`);
  },
  warn: (msg: string) => {
    // eslint-disable-next-line no-console
    console.warn(`[WARN] ${msg}`);
  },
  error: (msg: string) => {
    // eslint-disable-next-line no-console
    console.error(`[ERROR] ${msg}`);
  },
};
