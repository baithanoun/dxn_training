import dotenv from 'dotenv';
import path from 'path';

// Ensure env loaded even when imported outside playwright config
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

export const config = {
  baseURL: process.env.BASE_URL || 'http://localhost:3000',
  apiBaseURL: process.env.API_BASE_URL || process.env.BASE_URL || 'http://localhost:3000',

  users: {
    trainee: {
      email: process.env.TEST_USER_EMAIL || 'test.distributor@example.com',
      password: process.env.TEST_USER_PASSWORD || 'ChangeMe123!',
      phone: process.env.TEST_USER_PHONE || '+966500000001',
      dxnId: process.env.TEST_DXN_ID || 'DXN-TEST-001',
    },
    leader: {
      email: process.env.LEADER_EMAIL || 'test.leader@example.com',
      password: process.env.LEADER_PASSWORD || 'ChangeMeLeader123!',
    },
    admin: {
      email: process.env.ADMIN_EMAIL || 'test.admin@example.com',
      password: process.env.ADMIN_PASSWORD || 'ChangeMeAdmin123!',
    },
  },

  featureFlags: {
    a11y: process.env.ENABLE_A11Y_TESTS !== 'false',
    perf: process.env.ENABLE_PERF_TESTS !== 'false',
    visual: process.env.ENABLE_VISUAL_TESTS === 'true',
  },

  timeouts: {
    action: 10000,
    navigation: 15000,
    api: 10000,
  },

  performance: {
    maxLCP: 2500,
    maxFCP: 1800,
    maxTTI: 3500,
    maxCLS: 0.1,
  },
} as const;

export type UserRole = 'trainee' | 'leader' | 'admin';
