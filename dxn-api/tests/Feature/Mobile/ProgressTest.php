<?php

namespace Tests\Feature\Mobile;

use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use App\Models\UserLevelProgress;
use App\Services\ProgressService;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ProgressTest extends TestCase
{
    use RefreshDatabase;

    private User $user;

    protected function setUp(): void
    {
        parent::setUp();
        $this->seed(ContentSeeder::class);
        $this->user = User::factory()->create(['role' => 'trainee']);
    }

    private function level(int $sectionOrder, int $skillOrder, int $levelOrder): Level
    {
        return Section::where('order_index', $sectionOrder)->firstOrFail()
            ->skills()->where('order_index', $skillOrder)->firstOrFail()
            ->levels()->where('order_index', $levelOrder)->firstOrFail();
    }

    public function test_new_user_first_level_unlocked_rest_locked(): void
    {
        $progress = $this->actingAs($this->user, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk()->json();

        $this->assertSame('UNLOCKED', $progress[(string) $this->level(1, 1, 1)->id]['status']);
        $this->assertSame('LOCKED', $progress[(string) $this->level(1, 2, 1)->id]['status']);
        $this->assertSame('LOCKED', $progress[(string) $this->level(2, 1, 1)->id]['status']);

        $sections = $this->getJson("/api/users/{$this->user->id}/sections-progress")->assertOk()->json();
        $this->assertSame('UNLOCKED', $sections[(string) Section::where('order_index', 1)->firstOrFail()->id]['status']);
        $this->assertSame('LOCKED', $sections[(string) Section::where('order_index', 2)->firstOrFail()->id]['status']);
    }

    public function test_completed_level_unlocks_next(): void
    {
        UserLevelProgress::create([
            'user_id' => $this->user->id,
            'level_id' => $this->level(1, 1, 1)->id,
            'status' => 'completed',
            'score' => 100,
            'best_score' => 100,
            'attempts' => 1,
        ]);

        $progress = $this->actingAs($this->user, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk()->json();

        $this->assertSame('COMPLETED', $progress[(string) $this->level(1, 1, 1)->id]['status']);
        $this->assertSame('UNLOCKED', $progress[(string) $this->level(1, 2, 1)->id]['status']);
        $this->assertSame('LOCKED', $progress[(string) $this->level(1, 3, 1)->id]['status']);
    }

    public function test_certificate_unlocks_next_section(): void
    {
        $first = Section::where('order_index', 1)->firstOrFail();
        $second = Section::where('order_index', 2)->firstOrFail();
        Certificate::create([
            'user_id' => $this->user->id,
            'section_id' => $first->id,
            'code' => 'DXN-CERT-1',
            'issued_at' => now(),
        ]);

        $sections = $this->actingAs($this->user, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/sections-progress")
            ->assertOk()->json();

        $this->assertSame('COMPLETED', $sections[(string) $first->id]['status']);
        $this->assertTrue($sections[(string) $first->id]['certificateIssued']);
        $this->assertSame('DXN-CERT-1', $sections[(string) $first->id]['certificateId']);
        $this->assertSame('UNLOCKED', $sections[(string) $second->id]['status']);

        $progress = $this->getJson("/api/users/{$this->user->id}/progress")->assertOk()->json();
        $this->assertSame('UNLOCKED', $progress[(string) $this->level(2, 1, 1)->id]['status']);
    }

    public function test_self_200(): void
    {
        $this->actingAs($this->user, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk();
    }

    public function test_own_leader_200(): void
    {
        $leader = User::factory()->create(['role' => 'leader']);
        $this->user->sponsor_id = $leader->id;
        $this->user->save();

        $this->actingAs($leader, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk();
    }

    public function test_admin_200(): void
    {
        $admin = User::factory()->create(['role' => 'admin']);

        $this->actingAs($admin, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk();
    }

    public function test_other_trainee_403(): void
    {
        $other = User::factory()->create(['role' => 'trainee']);

        $this->actingAs($other, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertStatus(403);
    }

    public function test_progress_contains_every_published_level(): void
    {
        $skill = Section::where('order_index', 1)->firstOrFail()->skills()->where('order_index', 1)->firstOrFail();
        Level::create(['skill_id' => $skill->id, 'title' => 'Draft', 'type' => 'quiz', 'order_index' => 90, 'status' => 'draft']);
        $trashed = Level::create(['skill_id' => $skill->id, 'title' => 'Trashed', 'type' => 'quiz', 'order_index' => 91, 'status' => 'published']);
        $trashed->delete();

        $progress = $this->actingAs($this->user, 'sanctum')
            ->getJson("/api/users/{$this->user->id}/progress")
            ->assertOk()->json();
        $expected = Level::where('status', 'published')->pluck('id')->map(fn ($id) => (string) $id)->all();

        $this->assertCount(count($expected), $progress);
        $this->assertEqualsCanonicalizing($expected, array_keys($progress));
        $this->assertSame((string) $this->level(1, 1, 1)->id, (string) array_key_first($progress));
        $this->assertSame(
            ['status', 'score', 'attempts', 'bestScore', 'sectionCompleted', 'passedFinalQuiz'],
            array_keys($progress[(string) $this->level(1, 1, 1)->id])
        );
        $this->assertTrue(app(ProgressService::class)->isUnlocked($this->user, $this->level(1, 1, 1)));
    }
}
