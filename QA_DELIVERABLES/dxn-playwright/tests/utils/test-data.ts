import { faker } from '@faker-js/faker/locale/ar';

export const testData = {
  users: {
    newTrainee: () => ({
      fullName: faker.person.fullName(),
      email: faker.internet.email({ provider: 'qa.dxn' }),
      phone: `+9665${faker.string.numeric(8)}`,
      dxnId: `DXN-${faker.string.numeric(6)}`,
      password: 'TestPass123!',
    }),
    weakPassword: '123',
    invalidEmail: 'not-an-email',
  },

  content: {
    sampleLessonId: 'l1',
    sampleSkillId: 's1k1',
    sampleLevelId: 'l2',
  },

  team: {
    encouragementMessage: 'أنت قادر على تحقيق الأهداف! استمر في التعلم اليومي.',
  },

  validation: {
    dxnIdPattern: /^DXN-\d{6}$/,
    saudiPhonePattern: /^\+9665\d{8}$/,
  },
} as const;

export function generateUniqueEmail(prefix = 'qa'): string {
  return `${prefix}.${faker.string.alphanumeric(8)}@qa.dxn.example`;
}
