<?php

namespace Tests\Unit;

use App\Models\GameRound;
use App\Models\Level;
use App\Models\RoundOption;
use App\Models\Section;
use App\Models\Skill;
use App\Services\ScoringService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ScoringServiceTest extends TestCase
{
    use RefreshDatabase;

    private ScoringService $scoring;

    protected function setUp(): void
    {
        parent::setUp();
        $this->scoring = app(ScoringService::class);
    }

    private function level(string $type, int $passThreshold = 80): Level
    {
        $section = Section::create(['title' => 'S', 'order_index' => 1, 'status' => 'published']);
        $skill = Skill::create(['section_id' => $section->id, 'title' => 'K', 'order_index' => 1, 'status' => 'published']);

        return Level::create([
            'skill_id' => $skill->id, 'title' => 'L', 'type' => $type,
            'order_index' => 1, 'status' => 'published', 'pass_threshold' => $passThreshold,
        ]);
    }

    private function round(Level $level, int $order, int $initialTrust = 50): GameRound
    {
        return GameRound::create([
            'level_id' => $level->id, 'prompt' => 'Q', 'initial_trust' => $initialTrust, 'order_index' => $order,
        ]);
    }

    private function option(GameRound $round, bool $correct, int $order, int $trustImpact = 0): RoundOption
    {
        return RoundOption::create([
            'round_id' => $round->id, 'text' => 'Option '.$order, 'is_correct' => $correct,
            'trust_impact' => $trustImpact, 'order_index' => $order,
        ]);
    }

    public function test_quiz_scores_two_of_three_correct_as_67(): void
    {
        $level = $this->level('quiz', 60);
        $answers = [];
        for ($i = 1; $i <= 3; $i++) {
            $round = $this->round($level, $i);
            $correct = $this->option($round, true, 1);
            $wrong = $this->option($round, false, 2);
            $answers[] = ['roundId' => (string) $round->id, 'optionId' => (string) ($i <= 2 ? $correct->id : $wrong->id)];
        }

        $score = $this->scoring->score($level, $answers);

        $this->assertSame(67, $score);
        $this->assertTrue($this->scoring->passed($level, $score));
    }

    public function test_chat_battle_sums_trust_and_clamps_to_0_100(): void
    {
        $level = $this->level('chat_battle');
        $answers = [];
        foreach ([25, -20, 25] as $i => $impact) {
            $round = $this->round($level, $i + 1, $i === 0 ? 50 : 50);
            $option = $this->option($round, true, 1, $impact);
            $answers[] = ['roundId' => (string) $round->id, 'optionId' => (string) $option->id];
        }
        $this->assertSame(80, $this->scoring->score($level, $answers));

        $high = $this->level('chat_battle');
        $highRound = $this->round($high, 1, 90);
        $highOption = $this->option($highRound, true, 1, 25);
        $this->assertSame(100, $this->scoring->score($high, [
            ['roundId' => (string) $highRound->id, 'optionId' => (string) $highOption->id],
        ]));

        $low = $this->level('chat_battle');
        $lowRound = $this->round($low, 1, 10);
        $lowOption = $this->option($lowRound, true, 1, -40);
        $this->assertSame(0, $this->scoring->score($low, [
            ['roundId' => (string) $lowRound->id, 'optionId' => (string) $lowOption->id],
        ]));
    }

    public function test_order_sequence_scores_exact_and_reversed_orders(): void
    {
        $level = $this->level('order_sequence');
        $round = $this->round($level, 1);
        $first = $this->option($round, true, 1);
        $second = $this->option($round, false, 2);
        $third = $this->option($round, false, 3);
        $correct = [(string) $first->id, (string) $second->id, (string) $third->id];

        $this->assertSame(100, $this->scoring->score($level, [
            ['roundId' => (string) $round->id, 'orderedOptionIds' => $correct],
        ]));
        $this->assertSame(0, $this->scoring->score($level, [
            ['roundId' => (string) $round->id, 'orderedOptionIds' => array_reverse($correct)],
        ]));
    }

    public function test_match_pairs_scores_like_quiz(): void
    {
        $level = $this->level('match_pairs');
        $answers = [];
        for ($i = 1; $i <= 2; $i++) {
            $round = $this->round($level, $i);
            $correct = $this->option($round, true, 1);
            $wrong = $this->option($round, false, 2);
            $answers[] = ['roundId' => (string) $round->id, 'optionId' => (string) ($i === 1 ? $correct->id : $wrong->id)];
        }

        $this->assertSame(50, $this->scoring->score($level, $answers));
    }

    public function test_lesson_without_rounds_scores_100(): void
    {
        $level = $this->level('lesson');

        $this->assertSame(100, $this->scoring->score($level, []));
        $this->assertTrue($this->scoring->passed($level, 100));
    }
}
