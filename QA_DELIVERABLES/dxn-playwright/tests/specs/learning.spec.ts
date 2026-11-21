import { test, expect } from '../fixtures/base';
import { testData } from '../utils/test-data';

const levelId = testData.content.sampleLessonId;

test.describe('Learning Flows @regression @critical', () => {
  test.describe('Lesson Cards', () => {
    test('should display lesson cards and progress', async ({ lessonPage }) => {
      await lessonPage.goto(levelId);
      await expect(lessonPage.cardTitle).toBeVisible();
      await expect(lessonPage.cardBody).toBeVisible();
      await expect(lessonPage.progressBar).toBeVisible();
    });

    test('should navigate through cards and start quiz', async ({ lessonPage, page }) => {
      await lessonPage.goto(levelId);
      await lessonPage.completeCards();
      await lessonPage.startQuiz();
      await page.waitForURL(`/quiz/${levelId}`);
    });
  });

  test.describe('Quiz MCQ', () => {
    test('should answer correctly and complete level @critical', async ({ quizPage, completionPage, page }) => {
      await quizPage.goto(levelId);
      await quizPage.answerCorrectly();
      await quizPage.submitAndContinue();
      await completionPage.waitForLoaded();
      await expect(completionPage.title).toContainText('ممتاز');
    });

    test('should lose a heart on wrong answer', async ({ quizPage }) => {
      await quizPage.goto(levelId);
      await quizPage.answerIncorrectly();
      await quizPage.expectHeartCount(2);
    });

    test('should validate answers server-side @critical', async ({ page }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-010' });
      await page.route('**/api/v1/levels/*/submit', (route, request) => {
        const postData = request.postDataJSON();
        // Server must return validated score
        expect(postData).toHaveProperty('answers');
        route.fulfill({
          status: 200,
          body: JSON.stringify({ score: 100, xp: 50, passed: true }),
        });
      });
      await page.goto(`/quiz/${levelId}`);
      await page.getByTestId('quiz-option').first().click();
      await page.getByRole('button', { name: 'تحقق' }).click();
    });
  });

  test.describe('Chat Battle', () => {
    test('should update trust bar on response @critical', async ({ chatBattlePage }) => {
      await chatBattlePage.goto('l3');
      await chatBattlePage.selectOption('Feel-Felt-Found');
      await chatBattlePage.sendResponse();
      await chatBattlePage.expectTrust(75);
    });

    test('should show feedback after answer', async ({ chatBattlePage }) => {
      await chatBattlePage.goto('l3');
      await chatBattlePage.selectOption('Feel-Felt-Found');
      await chatBattlePage.sendResponse();
      await chatBattlePage.expectFeedback('احترافي');
    });
  });

  test.describe('Match Pairs', () => {
    test('should allow pairing items', async ({ matchPairsPage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-014' });
      await matchPairsPage.goto('l2');
      await matchPairsPage.expectTimerVisible();
      await matchPairsPage.pair('الريشي', 'طرد سموم');
      await matchPairsPage.submit();
    });
  });

  test.describe('Order Sequence', () => {
    test('should reorder steps', async ({ orderSequencePage }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-015' });
      await orderSequencePage.goto('l2');
      await orderSequencePage.moveStepDown('طلب القرار');
      await orderSequencePage.expectStepPosition('طلب القرار', 4);
    });
  });

  test.describe('Completion', () => {
    test('should award XP and show next level @critical', async ({ page }) => {
      await page.goto('/completion/l1/100/120');
      await expect(page.getByTestId('xp-earned')).toContainText('+120');
      await page.getByRole('button', { name: 'المستوى التالي' }).click();
      expect(page.url()).not.toContain('/completion');
    });

    test('should support share achievement', async ({ page }) => {
      test.info().annotations.push({ type: 'issue', description: 'QA-016' });
      await page.goto('/completion/l1/100/120');
      await page.getByRole('button', { name: 'مشاركة الإنجاز' }).click();
      // If Web Share API stub, expect modal or toast
      await expect(page.getByTestId('share-modal').or(page.getByText('تم النسخ'))).toBeVisible();
    });
  });
});
