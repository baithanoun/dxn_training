<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\User;
use App\Models\UserLevelProgress;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class TeamsTest extends TestCase
{
    use RefreshDatabase;

    public function test_team_aggregates(): void
    {
        $this->seed(ContentSeeder::class);
        $admin = User::factory()->create(['role' => 'admin']);
        $leaderA = User::factory()->create(['role' => 'leader', 'name' => 'Leader A']);
        $leaderB = User::factory()->create(['role' => 'leader', 'name' => 'Leader B']);
        $a1 = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leaderA->id, 'last_activity_date' => now()]);
        $a2 = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leaderA->id, 'last_activity_date' => now()->subDays(10)]);
        $b1 = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leaderB->id, 'last_activity_date' => now()->subDays(10)]);

        $published = Level::where('status', 'published')->get();
        foreach ($published->take(2) as $level) {
            UserLevelProgress::create([
                'user_id' => $a1->id, 'level_id' => $level->id,
                'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
            ]);
        }
        foreach ($published as $level) {
            UserLevelProgress::create([
                'user_id' => $b1->id, 'level_id' => $level->id,
                'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
            ]);
        }

        $teams = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/teams')
            ->assertOk()->assertJsonCount(2)->json();
        $byLeader = collect($teams)->keyBy('leaderId');

        $this->assertSame(['leaderId', 'leader', 'leaderName', 'members', 'completion', 'activeToday'], array_keys($teams[0]));
        $this->assertIsString($teams[0]['leaderId']);
        $this->assertSame('Leader A', $byLeader[(string) $leaderA->id]['leaderName']);
        $this->assertSame('Leader B', $byLeader[(string) $leaderB->id]['leaderName']);
        $this->assertSame(2, $byLeader[(string) $leaderA->id]['members']);
        $this->assertSame((int) round(2 / (2 * $published->count()) * 100), $byLeader[(string) $leaderA->id]['completion']);
        $this->assertSame(1, $byLeader[(string) $leaderA->id]['activeToday']);
        $this->assertSame(1, $byLeader[(string) $leaderB->id]['members']);
        $this->assertSame(100, $byLeader[(string) $leaderB->id]['completion']);
        $this->assertSame(0, $byLeader[(string) $leaderB->id]['activeToday']);
    }

    public function test_team_detail_404_for_non_leader(): void
    {
        $admin = User::factory()->create(['role' => 'admin']);
        $trainee = User::factory()->create(['role' => 'trainee']);

        $this->actingAs($admin, 'sanctum')->getJson("/api/admin/teams/{$trainee->id}")->assertStatus(404);
    }
}
