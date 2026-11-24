<?php

namespace Tests\Feature\Mobile;

use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class TreeTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        $this->seed(ContentSeeder::class);
        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum');
    }

    public function test_tree_returns_only_published_with_exact_keys(): void
    {
        $draftSection = Section::create(['title' => 'Draft section', 'order_index' => 99, 'status' => 'draft']);
        $draftSkill = Skill::create(['section_id' => $draftSection->id, 'title' => 'Draft skill', 'order_index' => 1, 'status' => 'draft']);
        Level::create(['skill_id' => $draftSkill->id, 'title' => 'Draft level', 'type' => 'quiz', 'order_index' => 1, 'status' => 'draft']);

        $tree = $this->getJson('/api/sections/tree')
            ->assertOk()
            ->assertJsonCount(5)
            ->json();

        $section = $tree[0];
        $this->assertSame(
            ['id', 'title', 'orderIndex', 'bagId', 'description', 'certificateTitle', 'requiredSectionId', 'skills'],
            array_keys($section)
        );
        $this->assertIsString($section['id']);
        $this->assertIsString($section['skills'][0]['id']);

        $skill = $section['skills'][0];
        $this->assertSame(['id', 'title', 'orderIndex', 'levels'], array_keys($skill));

        $level = $skill['levels'][0];
        $this->assertSame(
            ['id', 'title', 'orderIndex', 'xpReward', 'passThreshold', 'type', 'cards', 'rounds'],
            array_keys($level)
        );
        $this->assertIsString($level['id']);
        $this->assertIsString($level['cards'][0]['id']);
        $this->assertSame(['id', 'title', 'body', 'tip', 'mediaUrl'], array_keys($level['cards'][0]));

        $round = $level['rounds'][0];
        $this->assertSame(['id', 'prompt', 'initialTrust', 'options'], array_keys($round));
        $this->assertIsString($round['id']);
        $this->assertSame(
            ['id', 'text', 'isCorrect', 'isNeutral', 'trustImpact', 'feedback', 'xpBonus'],
            array_keys($round['options'][0])
        );
        $this->assertIsString($round['options'][0]['id']);
        $this->assertIsBool($round['options'][0]['isCorrect']);
        $this->assertIsBool($round['options'][0]['isNeutral']);

        $ids = collect($tree)->flatMap(fn ($s) => $s['skills'])->flatMap(fn ($k) => $k['levels'])->pluck('id')->all();
        $this->assertNotContains((string) Level::where('status', 'draft')->firstOrFail()->id, $ids);
    }

    public function test_tree_hides_trashed_level(): void
    {
        $level = Section::where('order_index', 1)->firstOrFail()
            ->skills()->where('order_index', 1)->firstOrFail()
            ->levels()->where('order_index', 1)->firstOrFail();
        $level->delete();

        $tree = $this->getJson('/api/sections/tree')->assertOk()->json();
        $ids = collect($tree)->flatMap(fn ($s) => $s['skills'])->flatMap(fn ($k) => $k['levels'])->pluck('id')->all();

        $this->assertCount(10, $ids);
        $this->assertNotContains((string) $level->id, $ids);
    }
}
