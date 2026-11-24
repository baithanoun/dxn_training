<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class RoundsTest extends TestCase
{
    use RefreshDatabase;

    private function level(string $type = 'quiz'): Level
    {
        $s = Section::create(['title' => 'S', 'order_index' => 1, 'status' => 'draft']);
        $k = Skill::create(['section_id' => $s->id, 'title' => 'K', 'order_index' => 1, 'status' => 'draft']);

        return Level::create(['skill_id' => $k->id, 'title' => 'L', 'type' => $type, 'order_index' => 1, 'status' => 'draft']);
    }

    private function putRounds(Level $l, array $rounds)
    {
        return $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson("/api/admin/levels/{$l->id}/rounds", ['rounds' => $rounds]);
    }

    private function round(array $opts = null, array $a = []): array
    {
        return $a + ['prompt' => 'P', 'initialTrust' => 50, 'options' => $opts ?? [
            ['text' => 'A', 'isCorrect' => true, 'isNeutral' => false, 'trustImpact' => 10],
            ['text' => 'B', 'isCorrect' => false, 'isNeutral' => false, 'trustImpact' => -10, 'feedback' => 'no', 'xpBonus' => 5],
        ]];
    }

    public function test_put_rounds_replaces_and_orders(): void
    {
        $l = $this->level();
        $l->rounds()->create(['prompt' => 'old', 'initial_trust' => 1, 'order_index' => 1]);

        $this->putRounds($l, [$this->round(null, ['title' => 'R1']), $this->round(null, ['prompt' => 'Second'])])
            ->assertOk()
            ->assertJsonCount(2, 'rounds')
            ->assertJsonPath('rounds.0.title', 'R1')
            ->assertJsonPath('rounds.0.orderIndex', 1)
            ->assertJsonPath('rounds.1.prompt', 'Second')
            ->assertJsonPath('rounds.1.orderIndex', 2)
            ->assertJsonPath('rounds.0.options.1.text', 'B')
            ->assertJsonPath('rounds.0.options.1.orderIndex', 2)
            ->assertJsonPath('rounds.0.options.1.xpBonus', 5);
        $this->assertSame(2, $l->rounds()->count());
    }

    public function test_round_without_correct_option_returns_422(): void
    {
        $this->putRounds($this->level(), [$this->round([
            ['text' => 'A', 'isCorrect' => false, 'isNeutral' => false, 'trustImpact' => 0],
            ['text' => 'B', 'isCorrect' => false, 'isNeutral' => true, 'trustImpact' => 0],
        ])])->assertStatus(422)->assertJsonValidationErrors('rounds.0.options');
    }

    public function test_empty_option_text_returns_422(): void
    {
        $this->putRounds($this->level(), [$this->round([
            ['text' => 'A', 'isCorrect' => true, 'isNeutral' => false, 'trustImpact' => 0],
            ['text' => '', 'isCorrect' => false, 'isNeutral' => false, 'trustImpact' => 0],
        ])])->assertStatus(422)->assertJsonValidationErrors('rounds.0.options.1.text');
    }

    public function test_failed_save_keeps_previous_rounds(): void
    {
        $l = $this->level();
        $this->putRounds($l, [$this->round(), $this->round()])->assertOk();
        $this->putRounds($l, [$this->round(null, ['initialTrust' => 500])])->assertStatus(422);
        $this->assertSame(2, $l->rounds()->count());
    }

    public function test_lesson_level_rejects_rounds(): void
    {
        $this->putRounds($this->level('lesson'), [$this->round()])->assertStatus(422);
    }

    public function test_correct_and_neutral_together_returns_422(): void
    {
        $this->putRounds($this->level(), [$this->round([
            ['text' => 'A', 'isCorrect' => true, 'isNeutral' => true, 'trustImpact' => 0],
            ['text' => 'B', 'isCorrect' => false, 'isNeutral' => false, 'trustImpact' => 0],
        ])])->assertStatus(422)->assertJsonValidationErrors('rounds.0.options.0.isNeutral');
    }
}
