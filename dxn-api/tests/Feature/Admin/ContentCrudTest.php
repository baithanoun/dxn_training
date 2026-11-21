<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ContentCrudTest extends TestCase
{
    use RefreshDatabase;

    private function admin(): static
    {
        return $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum');
    }

    private function section(array $a = []): Section
    {
        return Section::create($a + ['title' => 'S', 'order_index' => 1, 'status' => 'draft']);
    }

    private function skill(Section $s, array $a = []): Skill
    {
        return Skill::create($a + ['section_id' => $s->id, 'title' => 'K', 'order_index' => 1, 'status' => 'draft']);
    }

    private function level(Skill $k, array $a = []): Level
    {
        return Level::create($a + ['skill_id' => $k->id, 'title' => 'L', 'type' => 'lesson', 'order_index' => 1, 'status' => 'draft']);
    }

    public function test_tree_requires_admin(): void
    {
        $this->getJson('/api/admin/content/tree')->assertStatus(401);
        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->getJson('/api/admin/content/tree')->assertStatus(403);
        $this->admin()->getJson('/api/admin/content/tree')->assertOk();
    }

    public function test_tree_includes_drafts_and_excludes_deleted(): void
    {
        $s = $this->section(['title' => 'Draft sec']);
        $k = $this->skill($s);
        $l = $this->level($k);
        $this->level($k, ['title' => 'gone', 'order_index' => 2])->delete();
        $this->section(['title' => 'Trashed', 'order_index' => 2])->delete();

        $this->admin()->getJson('/api/admin/content/tree')
            ->assertOk()
            ->assertJsonCount(1)
            ->assertJsonPath('0.id', (string) $s->id)
            ->assertJsonPath('0.status', 'draft')
            ->assertJsonPath('0.skills.0.sectionId', (string) $s->id)
            ->assertJsonCount(1, '0.skills.0.levels')
            ->assertJsonPath('0.skills.0.levels.0.id', (string) $l->id)
            ->assertJsonPath('0.skills.0.levels.0.cardsCount', 0)
            ->assertJsonPath('0.skills.0.levels.0.roundsCount', 0);
    }

    public function test_create_section_appends_order_index(): void
    {
        $this->section(['order_index' => 4]);
        $this->admin()->postJson('/api/admin/sections', ['title' => 'New', 'bagId' => 3])
            ->assertCreated()
            ->assertJsonPath('orderIndex', 5)
            ->assertJsonPath('bagId', 3);
    }

    public function test_update_level_validates_type_and_threshold(): void
    {
        $l = $this->level($this->skill($this->section()));
        $this->admin()->putJson("/api/admin/levels/{$l->id}", ['type' => 'video'])
            ->assertStatus(422)->assertJsonValidationErrors('type');
        $this->admin()->putJson("/api/admin/levels/{$l->id}", ['passThreshold' => 150])
            ->assertStatus(422)->assertJsonValidationErrors('passThreshold');
        $this->admin()->putJson("/api/admin/levels/{$l->id}", ['passThreshold' => 70, 'type' => 'quiz'])
            ->assertOk()->assertJsonPath('passThreshold', 70);
    }

    public function test_setting_final_quiz_clears_other_final_in_section(): void
    {
        $s = $this->section();
        $a = $this->level($this->skill($s), ['is_final_quiz' => true]);
        $b = $this->level($this->skill($s, ['order_index' => 2]));
        $other = $this->level($this->skill($this->section(['order_index' => 2])), ['is_final_quiz' => true]);

        $this->admin()->putJson("/api/admin/levels/{$b->id}", ['isFinalQuiz' => true])->assertOk();
        $this->assertFalse($a->fresh()->is_final_quiz);
        $this->assertTrue($b->fresh()->is_final_quiz);
        $this->assertTrue($other->fresh()->is_final_quiz);
    }

    public function test_delete_section_required_by_another_returns_422(): void
    {
        $s1 = $this->section(['title' => 'First']);
        $s2 = $this->section(['title' => 'Second needs first', 'order_index' => 2, 'required_section_id' => $s1->id]);

        $this->admin()->deleteJson("/api/admin/sections/{$s1->id}")
            ->assertStatus(422)
            ->assertJsonPath('message', fn ($m) => str_contains($m, 'Second needs first'));
        $this->assertNull($s1->fresh()->deleted_at);

        $s2->delete();
        $this->admin()->deleteJson("/api/admin/sections/{$s1->id}")->assertNoContent();
    }

    public function test_delete_level_soft_deletes(): void
    {
        $l = $this->level($this->skill($this->section()));
        $this->admin()->deleteJson("/api/admin/levels/{$l->id}")->assertNoContent();
        $this->assertSoftDeleted('levels', ['id' => $l->id]);
    }

    public function test_put_cards_replaces_in_order(): void
    {
        $l = $this->level($this->skill($this->section()));
        $l->cards()->create(['title' => 'old', 'body' => 'x', 'order_index' => 1]);

        $this->admin()->putJson("/api/admin/levels/{$l->id}/cards", ['cards' => [
            ['title' => 'A', 'body' => 'aa', 'tip' => 't'],
            ['title' => 'B', 'body' => 'bb', 'mediaUrl' => '/storage/x.png'],
        ]])->assertOk()->assertJsonCount(2, 'cards');

        $this->admin()->getJson("/api/admin/levels/{$l->id}")
            ->assertOk()
            ->assertJsonCount(2, 'cards')
            ->assertJsonPath('cards.0.title', 'A')
            ->assertJsonPath('cards.0.orderIndex', 1)
            ->assertJsonPath('cards.1.title', 'B')
            ->assertJsonPath('cards.1.orderIndex', 2)
            ->assertJsonPath('cards.1.mediaUrl', '/storage/x.png')
            ->assertJsonPath('rounds', []);

        $this->admin()->putJson("/api/admin/levels/{$l->id}/cards", ['cards' => [['title' => 'A']]])
            ->assertStatus(422);
    }

    public function test_reorder_rejects_foreign_ids(): void
    {
        $a = $this->section();
        $b = $this->section(['order_index' => 2]);
        $a1 = $this->skill($a);
        $a2 = $this->skill($a, ['order_index' => 2]);
        $b1 = $this->skill($b);

        $this->admin()->postJson('/api/admin/reorder', [
            'type' => 'skills', 'parentId' => (string) $a->id,
            'ids' => [(string) $a2->id, (string) $b1->id],
        ])->assertStatus(422);
        $this->assertSame(1, $a1->fresh()->order_index);
        $this->assertSame(2, $a2->fresh()->order_index);
        $this->assertSame(1, $b1->fresh()->order_index);
    }

    public function test_reorder_rejects_partial_list(): void
    {
        $a = $this->section();
        $a1 = $this->skill($a);
        $this->skill($a, ['order_index' => 2]);
        $this->admin()->postJson('/api/admin/reorder', [
            'type' => 'skills', 'parentId' => (string) $a->id, 'ids' => [(string) $a1->id],
        ])->assertStatus(422);
    }

    public function test_reorder_applies_positions(): void
    {
        $a = $this->section();
        $a1 = $this->skill($a);
        $a2 = $this->skill($a, ['order_index' => 2]);
        $this->admin()->postJson('/api/admin/reorder', [
            'type' => 'skills', 'parentId' => (string) $a->id, 'ids' => [(string) $a2->id, (string) $a1->id],
        ])->assertNoContent();
        $this->assertSame(1, $a2->fresh()->order_index);
        $this->assertSame(2, $a1->fresh()->order_index);
    }
}
