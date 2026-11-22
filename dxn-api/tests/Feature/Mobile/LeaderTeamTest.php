<?php

namespace Tests\Feature\Mobile;

use App\Models\Level;
use App\Models\User;
use App\Models\UserLevelProgress;
use Carbon\Carbon;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class LeaderTeamTest extends TestCase
{
    use RefreshDatabase;

    protected function tearDown(): void
    {
        Carbon::setTestNow();
        parent::tearDown();
    }

    public function test_trainee_forbidden(): void
    {
        $trainee = User::factory()->create(['role' => 'trainee']);

        $this->actingAs($trainee, 'sanctum')->getJson('/api/leader/team')->assertStatus(403);
    }

    public function test_last_active_days_and_status(): void
    {
        Carbon::setTestNow(Carbon::parse('2026-11-22 12:00:00'));
        $leader = User::factory()->create(['role' => 'leader']);
        $yesterday = User::factory()->create(['name' => 'Am yesterday', 'sponsor_id' => $leader->id, 'last_activity_date' => '2026-11-21']);
        $old = User::factory()->create(['name' => 'Am old', 'sponsor_id' => $leader->id, 'last_activity_date' => '2026-11-12']);
        $never = User::factory()->create(['name' => 'Never active', 'sponsor_id' => $leader->id, 'last_activity_date' => null]);

        $team = $this->actingAs($leader, 'sanctum')->getJson('/api/leader/team')->assertOk()->json();
        $byId = collect($team)->keyBy('id');

        $this->assertSame(['id', 'name', 'progressPercent', 'lastActiveDays', 'status'], array_keys($team[0]));
        $this->assertIsString($team[0]['id']);
        $this->assertSame(1, $byId[(string) $yesterday->id]['lastActiveDays']);
        $this->assertSame('ACTIVE', $byId[(string) $yesterday->id]['status']);
        $this->assertSame(10, $byId[(string) $old->id]['lastActiveDays']);
        $this->assertSame('INACTIVE', $byId[(string) $old->id]['status']);
        $this->assertSame(999, $byId[(string) $never->id]['lastActiveDays']);
        $this->assertSame('INACTIVE', $byId[(string) $never->id]['status']);
    }

    public function test_progress_percent(): void
    {
        $this->seed(ContentSeeder::class);
        $leader = User::factory()->create(['role' => 'leader']);
        $member = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leader->id]);
        $level = Level::where('status', 'published')->firstOrFail();
        UserLevelProgress::create([
            'user_id' => $member->id, 'level_id' => $level->id,
            'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
        ]);
        $expected = (int) round(1 / Level::where('status', 'published')->count() * 100);

        $team = $this->actingAs($leader, 'sanctum')->getJson('/api/leader/team')->assertOk()->json();

        $this->assertCount(1, $team);
        $this->assertSame((string) $member->id, $team[0]['id']);
        $this->assertSame($expected, $team[0]['progressPercent']);
    }
}
