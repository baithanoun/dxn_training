<?php

namespace Tests\Feature\Mobile;

use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
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

        $this->assertSame(['id', 'name', 'progressPercent', 'lastActiveDays', 'status', 'badges', 'sections'], array_keys($team[0]));
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

    public function test_team_members_include_badges_and_section_summaries(): void
    {
        $leader = User::factory()->create(['role' => 'leader']);
        $member = User::factory()->create([
            'role' => 'trainee', 'sponsor_id' => $leader->id, 'current_streak' => 7,
        ]);
        [$firstSection, $firstLevel] = $this->publishedContent(1);
        [$secondSection, $secondLevel] = $this->publishedContent(2);
        $this->completeLevel($member, $firstLevel);
        $this->issueCertificate($member, $firstSection, 'DXN-CERT-1');

        $team = $this->actingAs($leader, 'sanctum')->getJson('/api/leader/team')->assertOk()->json();
        $entry = collect($team)->firstWhere('id', (string) $member->id);

        $this->assertSame(['first_section', 'streak_7'], $entry['badges']);
        $this->assertSame([
            'title' => 'قسم 1', 'completedLevels' => 1, 'totalLevels' => 1, 'certificate' => true,
        ], $entry['sections'][0]);
        $this->assertSame([
            'title' => 'قسم 2', 'completedLevels' => 0, 'totalLevels' => 1, 'certificate' => false,
        ], $entry['sections'][1]);
    }

    public function test_all_complete_badge_requires_every_level_and_certificate(): void
    {
        $leader = User::factory()->create(['role' => 'leader']);
        $member = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leader->id]);
        [$firstSection, $firstLevel] = $this->publishedContent(1);
        [$secondSection, $secondLevel] = $this->publishedContent(2);
        $this->completeLevel($member, $firstLevel);
        $this->completeLevel($member, $secondLevel);
        $this->issueCertificate($member, $firstSection, 'DXN-CERT-1');
        $this->issueCertificate($member, $secondSection, 'DXN-CERT-2');

        $team = $this->actingAs($leader, 'sanctum')->getJson('/api/leader/team')->assertOk()->json();
        $entry = collect($team)->firstWhere('id', (string) $member->id);

        $this->assertSame(['first_section', 'all_complete'], $entry['badges']);
        $this->assertSame(100, $entry['progressPercent']);
    }

    public function test_all_complete_badge_withheld_when_a_certificate_is_missing(): void
    {
        $leader = User::factory()->create(['role' => 'leader']);
        $member = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leader->id]);
        [$firstSection, $firstLevel] = $this->publishedContent(1);
        [$secondSection, $secondLevel] = $this->publishedContent(2);
        $this->completeLevel($member, $firstLevel);
        $this->completeLevel($member, $secondLevel);
        $this->issueCertificate($member, $firstSection, 'DXN-CERT-1');

        $team = $this->actingAs($leader, 'sanctum')->getJson('/api/leader/team')->assertOk()->json();
        $entry = collect($team)->firstWhere('id', (string) $member->id);

        $this->assertSame(['first_section'], $entry['badges']);
    }

    /** @return array{Section, Level} */
    private function publishedContent(int $order): array
    {
        $section = Section::create([
            'title' => 'قسم '.$order, 'description' => '', 'order_index' => $order,
            'bag_id' => $order, 'certificate_title' => 'شهادة '.$order,
            'required_section_id' => null, 'status' => 'published', 'version' => 1,
        ]);
        $skill = Skill::create([
            'section_id' => $section->id, 'title' => 'مهارة '.$order,
            'order_index' => 1, 'status' => 'published',
        ]);
        $level = Level::create([
            'skill_id' => $skill->id, 'title' => 'مستوى '.$order, 'type' => 'quiz',
            'pass_threshold' => 80, 'xp_reward' => 10, 'order_index' => 1, 'status' => 'published',
        ]);

        return [$section, $level];
    }

    private function completeLevel(User $user, Level $level): void
    {
        UserLevelProgress::create([
            'user_id' => $user->id, 'level_id' => $level->id,
            'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
        ]);
    }

    private function issueCertificate(User $user, Section $section, string $code): void
    {
        Certificate::create([
            'user_id' => $user->id, 'section_id' => $section->id,
            'code' => $code, 'issued_at' => now(),
        ]);
    }
}
