<?php

namespace Tests\Feature\Mobile;

use App\Models\Certificate;
use App\Models\GameRound;
use App\Models\Level;
use App\Models\RoundOption;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use App\Models\UserLevelProgress;
use App\Services\ProgressService;
use Carbon\Carbon;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class CompletionTest extends TestCase
{
    use RefreshDatabase;

    protected function tearDown(): void
    {
        Carbon::setTestNow();
        parent::tearDown();
    }

    private function level(int $sectionOrder, int $skillOrder, int $levelOrder): Level
    {
        return Section::where('order_index', $sectionOrder)->firstOrFail()
            ->skills()->where('order_index', $skillOrder)->firstOrFail()
            ->levels()->where('order_index', $levelOrder)->firstOrFail();
    }

    private function answers(Level $level, bool $correct = true): array
    {
        $answers = [];
        foreach ($level->rounds()->orderBy('order_index')->get() as $round) {
            if ($level->type === 'order_sequence') {
                $ids = $round->options()->orderBy('order_index')->pluck('id')->map(fn ($id) => (string) $id)->all();
                $answers[] = [
                    'roundId' => (string) $round->id,
                    'orderedOptionIds' => $correct ? $ids : array_reverse($ids),
                ];
                continue;
            }
            $option = $round->options()->where('is_correct', $correct)->firstOrFail();
            $answers[] = ['roundId' => (string) $round->id, 'optionId' => (string) $option->id];
        }

        return $answers;
    }

    private function quizWithTwoRounds(): Level
    {
        $section = Section::create(['title' => 'Custom', 'order_index' => 99, 'status' => 'published']);
        $skill = Skill::create(['section_id' => $section->id, 'title' => 'Custom', 'order_index' => 1, 'status' => 'published']);
        $level = Level::create([
            'skill_id' => $skill->id, 'title' => 'Two rounds', 'type' => 'quiz',
            'order_index' => 1, 'status' => 'published',
        ]);
        for ($i = 1; $i <= 2; $i++) {
            $round = GameRound::create([
                'level_id' => $level->id, 'prompt' => 'Q'.$i, 'initial_trust' => 50, 'order_index' => $i,
            ]);
            RoundOption::create(['round_id' => $round->id, 'text' => 'yes', 'is_correct' => true, 'order_index' => 1]);
            RoundOption::create(['round_id' => $round->id, 'text' => 'no', 'is_correct' => false, 'order_index' => 2]);
        }

        return $level;
    }

    private function trainee(array $attributes = []): User
    {
        return User::factory()->create(array_merge(['role' => 'trainee'], $attributes));
    }

    private function complete(User $user, Level $level, array $body)
    {
        return $this->actingAs($user, 'sanctum')->postJson("/api/levels/{$level->id}/complete", $body);
    }

    public function test_first_try_pass_awards_multiplied_xp(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()
            ->assertJsonPath('score', 100)
            ->assertJsonPath('passed', true)
            ->assertJsonPath('xpAwarded', 60)
            ->assertJsonPath('totalXp', 60);

        $this->assertSame(60, (int) $user->fresh()->total_xp);
    }

    public function test_repeat_same_score_awards_zero(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);
        $body = ['answers' => $this->answers($level)];

        $this->complete($user, $level, $body)->assertOk()->assertJsonPath('xpAwarded', 60);
        $this->complete($user, $level, $body)->assertOk()->assertJsonPath('xpAwarded', 0)->assertJsonPath('totalXp', 60);

        $row = UserLevelProgress::where('user_id', $user->id)->where('level_id', $level->id)->firstOrFail();
        $this->assertSame(2, $row->attempts);
        $this->assertSame(100, $row->best_score);
        $this->assertSame('completed', $row->status);
    }

    public function test_improved_replay_awards_quarter(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);
        UserLevelProgress::create([
            'user_id' => $user->id, 'level_id' => $level->id, 'status' => 'completed',
            'score' => 50, 'best_score' => 50, 'attempts' => 1,
        ]);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()
            ->assertJsonPath('score', 100)
            ->assertJsonPath('xpAwarded', 10);

        $this->assertSame(10, (int) $user->fresh()->total_xp);
    }

    public function test_first_pass_after_fail_awards_full_xp(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level, false)])
            ->assertOk()->assertJsonPath('passed', false)->assertJsonPath('xpAwarded', 0);
        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()->assertJsonPath('passed', true)->assertJsonPath('xpAwarded', 40);
    }

    public function test_fail_awards_zero_status_attempted(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level, false)])
            ->assertOk()
            ->assertJsonPath('passed', false)
            ->assertJsonPath('score', 0)
            ->assertJsonPath('xpAwarded', 0);

        $row = UserLevelProgress::where('user_id', $user->id)->where('level_id', $level->id)->firstOrFail();
        $this->assertSame('attempted', $row->status);
        $this->assertSame(0, $row->score);
        $this->assertSame(1, $row->attempts);
        $this->assertSame(0, (int) $user->fresh()->total_xp);
    }

    public function test_locked_level_409_no_row(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 2, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertStatus(409)
            ->assertJsonPath('message', 'المستوى مقفل');

        $this->assertSame(0, UserLevelProgress::where('user_id', $user->id)->where('level_id', $level->id)->count());
    }

    public function test_old_body_shape_422(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, [
            'userId' => (string) $user->id, 'score' => 100, 'passed' => true,
        ])->assertStatus(422)->assertJsonValidationErrors('answers');
    }

    public function test_missing_option_id_422(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);
        $round = $level->rounds()->firstOrFail();

        $this->complete($user, $level, [
            'answers' => [['roundId' => (string) $round->id]],
        ])->assertStatus(422);
    }

    public function test_foreign_round_id_422(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);
        $foreignRound = $this->level(1, 2, 1)->rounds()->firstOrFail();
        $foreignOption = $foreignRound->options()->firstOrFail();

        $this->complete($user, $level, [
            'answers' => [['roundId' => (string) $foreignRound->id, 'optionId' => (string) $foreignOption->id]],
        ])->assertStatus(422);
    }

    public function test_duplicate_round_422(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);
        $answer = $this->answers($level)[0];

        $this->complete($user, $level, ['answers' => [$answer, $answer]])
            ->assertStatus(422);
    }

    public function test_unanswered_round_422(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $user = $this->trainee();
        $level = $this->quizWithTwoRounds();
        $round = $level->rounds()->orderBy('order_index')->firstOrFail();
        $option = $round->options()->where('is_correct', true)->firstOrFail();

        $this->complete($user, $level, [
            'answers' => [['roundId' => (string) $round->id, 'optionId' => (string) $option->id]],
        ])->assertStatus(422);
    }

    public function test_final_level_issues_certificate_once(): void
    {
        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $section = Section::where('order_index', 1)->firstOrFail();
        $ordered = app(ProgressService::class)->orderedLevels($section);
        $final = $ordered->last();
        $this->assertTrue($final->is_final_quiz);
        foreach ($ordered->take($ordered->count() - 1) as $previous) {
            UserLevelProgress::create([
                'user_id' => $user->id, 'level_id' => $previous->id,
                'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
            ]);
        }

        $this->complete($user, $final, ['answers' => $this->answers($final)])
            ->assertOk()
            ->assertJsonPath('certificateIssued', true);
        $this->assertStringStartsWith('DXN-', Certificate::where('user_id', $user->id)->firstOrFail()->code);

        $this->complete($user, $final, ['answers' => $this->answers($final)])
            ->assertOk()
            ->assertJsonPath('certificateIssued', false);
        $this->assertSame(1, Certificate::where('user_id', $user->id)->where('section_id', $section->id)->count());
    }

    public function test_streak_yesterday_increments(): void
    {
        Carbon::setTestNow(Carbon::parse('2026-11-22 12:00:00'));
        $this->seed(ContentSeeder::class);
        $user = $this->trainee(['current_streak' => 2, 'last_activity_date' => '2026-11-21']);
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()->assertJsonPath('currentStreak', 3);
        $this->assertSame(3, $user->fresh()->current_streak);
    }

    public function test_streak_today_unchanged(): void
    {
        Carbon::setTestNow(Carbon::parse('2026-11-22 12:00:00'));
        $this->seed(ContentSeeder::class);
        $user = $this->trainee(['current_streak' => 3, 'last_activity_date' => '2026-11-22']);
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()->assertJsonPath('currentStreak', 3);
        $this->assertSame(3, $user->fresh()->current_streak);
    }

    public function test_streak_older_resets(): void
    {
        Carbon::setTestNow(Carbon::parse('2026-11-22 12:00:00'));
        $this->seed(ContentSeeder::class);
        $user = $this->trainee(['current_streak' => 7, 'last_activity_date' => '2026-11-19']);
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])
            ->assertOk()->assertJsonPath('currentStreak', 1);
        $this->assertSame(1, $user->fresh()->current_streak);
    }

    public function test_completion_writes_level_event(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->seed(ContentSeeder::class);
        $user = $this->trainee();
        $level = $this->level(1, 1, 1);

        $this->complete($user, $level, ['answers' => $this->answers($level)])->assertOk();

        $this->assertSame(1, DB::table('analytics_events')
            ->where('user_id', $user->id)
            ->where('level_id', $level->id)
            ->where('event_name', 'level_completed')
            ->count());
    }
}
