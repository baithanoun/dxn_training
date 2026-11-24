<?php

namespace Tests\Feature;

use App\Models\GameRound;
use App\Models\Level;
use App\Models\LessonCard;
use App\Models\RoundOption;
use App\Models\Section;
use App\Services\ScoringService;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ContentSeederTest extends TestCase
{
    use RefreshDatabase;

    public function test_seeds_five_published_sections_with_levels_cards_and_rounds(): void
    {
        $this->seed(ContentSeeder::class);

        $this->assertSame(5, Section::count());
        $this->assertSame(5, Section::where('status', 'published')->count());

        // Counts taken from MockData.sections: 11 levels, 16 cards (c1-c16), 11 rounds (r1-r11), 25 options (MockData has no o4).
        $this->assertSame(11, Level::count());
        $this->assertSame(16, LessonCard::count());
        $this->assertSame(11, GameRound::count());
        $this->assertSame(25, RoundOption::count());

        foreach (Level::all() as $level) {
            $this->assertGreaterThanOrEqual(1, $level->cards()->count(), "level {$level->id} has no cards");
            $this->assertNotSame('lesson', $level->type);
            $this->assertGreaterThanOrEqual(1, $level->rounds()->count(), "level {$level->id} has no rounds");
            foreach ($level->rounds as $round) {
                $this->assertTrue($round->options()->where('is_correct', true)->exists(), "round {$round->id} has no correct option");
            }
        }

        $byOrder = Section::orderBy('order_index')->get();
        $this->assertSame($byOrder[0]->id, $byOrder[1]->required_section_id);
        $this->assertSame('شهادة أساسيات DXN', $byOrder[0]->certificate_title);
        $this->assertTrue(RoundOption::where('text', '1993 — د. ليم سيو سين')->where('is_correct', true)->exists());
    }

    public function test_seeded_levels_are_passable_with_best_answers(): void
    {
        $this->seed(ContentSeeder::class);
        $scoring = app(ScoringService::class);

        foreach (Level::with('rounds.options')->get() as $level) {
            $answers = $level->rounds->map(function ($round) use ($level) {
                if ($level->type === 'order_sequence') {
                    return [
                        'roundId' => (string) $round->id,
                        'orderedOptionIds' => $round->options->sortBy('order_index')
                            ->pluck('id')->map(fn ($id) => (string) $id)->all(),
                    ];
                }

                $best = $level->type === 'chat_battle'
                    ? $round->options->sortByDesc('trust_impact')->first()
                    : $round->options->firstWhere('is_correct', true);

                return ['roundId' => (string) $round->id, 'optionId' => (string) $best->id];
            })->all();

            $score = $scoring->score($level, $answers);
            $this->assertTrue(
                $scoring->passed($level, $score),
                "seeded level {$level->id} ({$level->type}) cannot be passed: score {$score} < threshold {$level->pass_threshold}"
            );
        }
    }

    public function test_seeder_is_idempotent(): void
    {
        $this->seed(ContentSeeder::class);
        $this->seed(ContentSeeder::class);

        $this->assertSame(5, Section::count());
        $this->assertSame(11, Level::count());
    }

    public function test_last_level_of_each_section_is_final_quiz(): void
    {
        $this->seed(ContentSeeder::class);

        $this->assertSame(5, Level::where('is_final_quiz', true)->count());

        foreach (Section::with('skills.levels')->get() as $section) {
            $levels = $section->skills->flatMap->levels;
            $this->assertTrue((bool) $levels->last()->is_final_quiz, "section {$section->id} last level not final");
            $this->assertSame(1, $levels->where('is_final_quiz', true)->count());
        }
    }
}
