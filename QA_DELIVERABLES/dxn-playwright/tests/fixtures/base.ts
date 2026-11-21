import { test as base, expect } from '@playwright/test';
import { SplashPage } from '../pages/SplashPage';
import { OnboardingPage } from '../pages/OnboardingPage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { HomePage } from '../pages/HomePage';
import { LearnPage } from '../pages/LearnPage';
import { LessonPage } from '../pages/LessonPage';
import { QuizPage } from '../pages/QuizPage';
import { ChatBattlePage } from '../pages/ChatBattlePage';
import { MatchPairsPage } from '../pages/MatchPairsPage';
import { OrderSequencePage } from '../pages/OrderSequencePage';
import { CompletionPage } from '../pages/CompletionPage';
import { ProfilePage } from '../pages/ProfilePage';
import { TeamPage } from '../pages/TeamPage';
import { AdminPage } from '../pages/AdminPage';
import { authFixture, AuthFixtures } from './auth';
import { attachConsoleLogger } from '../utils/logger';

type POMFixtures = {
  splashPage: SplashPage;
  onboardingPage: OnboardingPage;
  loginPage: LoginPage;
  registerPage: RegisterPage;
  homePage: HomePage;
  learnPage: LearnPage;
  lessonPage: LessonPage;
  quizPage: QuizPage;
  chatBattlePage: ChatBattlePage;
  matchPairsPage: MatchPairsPage;
  orderSequencePage: OrderSequencePage;
  completionPage: CompletionPage;
  profilePage: ProfilePage;
  teamPage: TeamPage;
  adminPage: AdminPage;
};

export type TestFixtures = POMFixtures & AuthFixtures;

export const test = base.extend<TestFixtures>({
  ...authFixture,

  splashPage: async ({ page }, use) => await use(new SplashPage(page)),
  onboardingPage: async ({ page }, use) => await use(new OnboardingPage(page)),
  loginPage: async ({ page }, use) => await use(new LoginPage(page)),
  registerPage: async ({ page }, use) => await use(new RegisterPage(page)),
  homePage: async ({ page }, use) => await use(new HomePage(page)),
  learnPage: async ({ page }, use) => await use(new LearnPage(page)),
  lessonPage: async ({ page }, use) => await use(new LessonPage(page)),
  quizPage: async ({ page }, use) => await use(new QuizPage(page)),
  chatBattlePage: async ({ page }, use) => await use(new ChatBattlePage(page)),
  matchPairsPage: async ({ page }, use) => await use(new MatchPairsPage(page)),
  orderSequencePage: async ({ page }, use) => await use(new OrderSequencePage(page)),
  completionPage: async ({ page }, use) => await use(new CompletionPage(page)),
  profilePage: async ({ page }, use) => await use(new ProfilePage(page)),
  teamPage: async ({ page }, use) => await use(new TeamPage(page)),
  adminPage: async ({ page }, use) => await use(new AdminPage(page)),

  page: async ({ page }, use) => {
    page.on('console', attachConsoleLogger);
    await use(page);
  },
});

export { expect };
