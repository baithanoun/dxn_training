<?php

namespace Tests\Feature\Admin;

use App\Models\AnalyticsEvent;
use App\Models\Certificate;
use App\Models\GameRound;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use Carbon\Carbon;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class DashboardTest extends TestCase
{
    use RefreshDatabase;

    public function test_overview_shape(): void
    {
        $this->seed(ContentSeeder::class);
        $overview = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json();

        $this->assertSame([
            'stats', 'activity', 'failedSkills', 'inactiveUsers', 'certificatesBySection', 'bagsProgress',
        ], array_keys($overview));
        $this->assertSame(['title', 'value', 'change', 'changeType', 'icon'], array_keys($overview['stats'][0]));
        $this->assertContains($overview['stats'][0]['changeType'], ['up', 'down']);
        $this->assertSame(['date', 'sessions', 'activeUsers'], array_keys($overview['activity'][0]));
        $this->assertSame(['sectionId', 'title', 'issuedCount'], array_keys($overview['certificatesBySection'][0]));
        $this->assertSame(['sectionId', 'title', 'percent'], array_keys($overview['bagsProgress'][0]));
        $this->assertIsString($overview['certificatesBySection'][0]['sectionId']);
        $this->assertIsString($overview['bagsProgress'][0]['sectionId']);
    }

    public function test_overview_empty_db_no_division_by_zero(): void
    {
        $overview = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json();

        $this->assertSame([], $overview['failedSkills']);
        $this->assertSame([], $overview['inactiveUsers']);
        $this->assertSame([], $overview['certificatesBySection']);
        $this->assertSame([], $overview['bagsProgress']);
        $this->assertSame('0%', $overview['stats'][1]['value']);
        $this->assertNotEmpty($overview['activity']);
    }

    private function orderedLevels(): array
    {
        $levels = [];
        foreach (Section::orderBy('order_index')->get() as $section) {
            foreach ($section->skills()->orderBy('order_index')->get() as $skill) {
                foreach ($skill->levels()->orderBy('order_index')->get() as $level) {
                    $levels[] = $level;
                }
            }
        }

        return $levels;
    }

    private function event(User $user, string $name, ?int $levelId = null, array $payload = [], ?Carbon $at = null): void
    {
        AnalyticsEvent::create([
            'user_id' => $user->id,
            'event_name' => $name,
            'level_id' => $levelId,
            'payload' => $payload,
            'created_at' => $at ?? now(),
        ]);
    }

    protected function tearDown(): void
    {
        Carbon::setTestNow();
        parent::tearDown();
    }

    public function test_analytics_funnel_counts(): void
    {
        $this->seed(ContentSeeder::class);
        $levels = $this->orderedLevels();
        [$first, $second] = [$levels[0], $levels[1]];

        $admin = User::factory()->create(['role' => 'admin']);
        $u1 = User::factory()->create(['role' => 'trainee']);
        $u2 = User::factory()->create(['role' => 'trainee']);

        $this->event($u1, 'level_started', $first->id);
        $this->event($u1, 'level_started', $first->id);
        $this->event($u2, 'level_started', $first->id);
        $this->event($u1, 'level_completed', $first->id, ['score' => 100, 'attempt' => 1]);
        $this->event($u2, 'level_failed', $first->id, ['score' => 0, 'attempt' => 1]);
        $this->event($u1, 'level_started', $second->id);

        $analytics = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/analytics')
            ->assertOk()->json();

        $this->assertSame([
            ['title' => 'Completion Rate', 'value' => '50%', 'change' => 0],
            ['title' => 'Average Time', 'value' => '—', 'change' => 0],
            ['title' => 'First-Attempt Success', 'value' => '50%', 'change' => 0],
            ['title' => 'Drop-off Rate', 'value' => '50%', 'change' => 0],
        ], $analytics['summary']);
        $this->assertSame([
            ['name' => 'فتح المستوى', 'value' => 4],
            ['name' => 'إكمال الدرس', 'value' => 2],
            ['name' => 'اجتياز الاختبار', 'value' => 1],
            ['name' => 'المستوى التالي', 'value' => 1],
        ], $analytics['funnel']);

        $sectionId = $first->skill->section_id;
        $filtered = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/analytics?sectionId=' . $sectionId)
            ->assertOk()->json();
        $this->assertSame($analytics['funnel'], $filtered['funnel']);

        $leaderFiltered = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/analytics?leaderId=' . $u2->id)
            ->assertOk()->json();
        $this->assertSame(0, $leaderFiltered['funnel'][0]['value']);
    }

    public function test_failed_questions_error_rate(): void
    {
        $this->seed(ContentSeeder::class);
        $rounds = GameRound::orderBy('id')->limit(2)->get();
        [$r1, $r2] = [$rounds[0], $rounds[1]];
        $admin = User::factory()->create(['role' => 'admin']);
        $u = User::factory()->create(['role' => 'trainee']);

        foreach ([true, true, false, false] as $correct) {
            $this->event($u, 'question_answered', $r1->level_id, [
                'roundId' => $r1->id, 'optionId' => 1, 'correct' => $correct,
                'timeMs' => $correct ? 10000 : 20000,
            ]);
        }
        foreach ([6000, 6000] as $timeMs) {
            $this->event($u, 'question_answered', $r2->level_id, [
                'roundId' => $r2->id, 'optionId' => 2, 'correct' => false, 'timeMs' => $timeMs,
            ]);
        }
        $this->event($u, 'app_opened');

        $analytics = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/analytics')
            ->assertOk()->json();

        $this->assertSame([
            ['id' => (string) $r2->id, 'question' => $r2->prompt, 'attempts' => 2, 'errorRate' => 100, 'avgTime' => '6 ث'],
            ['id' => (string) $r1->id, 'question' => $r1->prompt, 'attempts' => 4, 'errorRate' => 50, 'avgTime' => '15 ث'],
        ], $analytics['failedQuestions']);
    }

    public function test_retention_d1(): void
    {
        Carbon::setTestNow('2026-11-22 12:00:00');
        $admin = User::factory()->create(['role' => 'admin']);

        $a = User::factory()->create(['role' => 'trainee', 'created_at' => '2026-11-20 09:00:00']);
        $b = User::factory()->create(['role' => 'trainee', 'created_at' => '2026-11-20 09:00:00']);
        $c = User::factory()->create(['role' => 'trainee', 'created_at' => '2026-11-21 09:00:00']);
        $d = User::factory()->create(['role' => 'trainee', 'created_at' => '2026-11-21 09:00:00']);

        $this->event($a, 'app_opened', null, [], Carbon::parse('2026-11-21 10:00:00'));
        $this->event($b, 'app_opened', null, [], Carbon::parse('2026-11-22 10:00:00'));
        $this->event($c, 'app_opened', null, [], Carbon::parse('2026-11-22 10:00:00'));

        $analytics = $this->actingAs($admin, 'sanctum')
            ->getJson('/api/admin/analytics?from=2026-11-01&to=2026-11-22')
            ->assertOk()->json();

        $this->assertSame([
            ['day' => 'اليوم 1', 'rate' => 50],
            ['day' => 'اليوم 7', 'rate' => 0],
            ['day' => 'اليوم 30', 'rate' => 0],
        ], $analytics['retention']);
    }

    public function test_analytics_empty_db_zeros(): void
    {
        $analytics = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/analytics')->assertOk()->json();

        $this->assertSame([
            ['title' => 'Completion Rate', 'value' => '0%', 'change' => 0],
            ['title' => 'Average Time', 'value' => '—', 'change' => 0],
            ['title' => 'First-Attempt Success', 'value' => '0%', 'change' => 0],
            ['title' => 'Drop-off Rate', 'value' => '0%', 'change' => 0],
        ], $analytics['summary']);
        $this->assertSame([
            ['name' => 'فتح المستوى', 'value' => 0],
            ['name' => 'إكمال الدرس', 'value' => 0],
            ['name' => 'اجتياز الاختبار', 'value' => 0],
            ['name' => 'المستوى التالي', 'value' => 0],
        ], $analytics['funnel']);
        $this->assertSame([], $analytics['failedQuestions']);
        $this->assertSame([
            ['day' => 'اليوم 1', 'rate' => 0],
            ['day' => 'اليوم 7', 'rate' => 0],
            ['day' => 'اليوم 30', 'rate' => 0],
        ], $analytics['retention']);
    }

    public function test_analytics_export_csv(): void
    {
        $this->seed(ContentSeeder::class);
        $level = Level::firstOrFail();
        $round = GameRound::create([
            'level_id' => $level->id,
            'prompt' => '=SUM(1+2)',
            'initial_trust' => 0,
            'order_index' => 99,
        ]);
        $admin = User::factory()->create(['role' => 'admin']);
        $u = User::factory()->create(['role' => 'trainee']);

        $this->event($u, 'level_started', $level->id);
        $this->event($u, 'question_answered', $level->id, [
            'roundId' => $round->id, 'optionId' => 1, 'correct' => false, 'timeMs' => 2000,
        ]);

        $response = $this->actingAs($admin, 'sanctum')->get('/api/admin/analytics/export')->assertOk();
        $csv = $response->getContent();

        $this->assertStringStartsWith("\xEF\xBB\xBF", $csv);
        $this->assertStringContainsString('text/csv', $response->headers->get('Content-Type'));
        $this->assertStringContainsString("'".'=SUM(1+2)', $csv);
        $this->assertStringContainsString('فتح المستوى', $csv);
        $this->assertStringContainsString('نسبة الخطأ', $csv);
    }

    public function test_certificates_issued_count(): void
    {
        $this->seed(ContentSeeder::class);
        $section = Section::orderBy('order_index')->firstOrFail();
        $draft = Section::create([
            'title' => 'مسودة', 'order_index' => 99, 'status' => 'draft',
        ]);
        $admin = User::factory()->create(['role' => 'admin']);
        foreach ([1, 2] as $i) {
            $user = User::factory()->create(['role' => 'trainee']);
            Certificate::create([
                'user_id' => $user->id,
                'section_id' => $section->id,
                'code' => 'CERT-'.$i,
                'issued_at' => now(),
            ]);
        }

        $certificates = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/certificates')
            ->assertOk()->json();

        $this->assertSame(['sectionId', 'title', 'issuedCount', 'isActive'], array_keys($certificates[0]));
        $this->assertIsString($certificates[0]['sectionId']);
        $byId = collect($certificates)->keyBy(fn ($c) => 's' . $c['sectionId']);
        $this->assertSame(2, $byId['s' . $section->id]['issuedCount']);
        $this->assertTrue($byId['s' . $section->id]['isActive']);
        $this->assertSame(0, $byId['s' . $draft->id]['issuedCount']);
        $this->assertFalse($byId['s' . $draft->id]['isActive']);
    }

    private function certificate(User $user, Section $section, string $code, string $at): void
    {
        Certificate::create([
            'user_id' => $user->id,
            'section_id' => $section->id,
            'code' => $code,
            'issued_at' => $at,
            'created_at' => $at,
        ]);
    }

    public function test_overview_stats_exact_titles_and_streak_format(): void
    {
        Carbon::setTestNow('2026-11-22 12:00:00');
        $this->seed(ContentSeeder::class);
        User::factory()->create(['role' => 'trainee', 'current_streak' => 5]);
        User::factory()->create(['role' => 'trainee', 'current_streak' => 6]);

        $overview = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json();

        $this->assertSame([
            ['المستخدمون النشطون اليوم', 'Users'],
            ['معدل الإكمال العام', 'BarChart3'],
            ['متوسط طول الـ Streak', 'Flame'],
            ['الشهادات الصادرة اليوم', 'Award'],
        ], collect($overview['stats'])->map(fn ($s) => [$s['title'], $s['icon']])->all());
        $this->assertSame('5.5 يوم', $overview['stats'][2]['value']);
        $this->assertStringEndsWith(' يوم', $overview['stats'][2]['value']);
        $this->assertStringNotContainsString('أيام', $overview['stats'][2]['value']);
    }

    public function test_overview_counts_certificates_issued_today_only(): void
    {
        Carbon::setTestNow('2026-11-22 12:00:00');
        $this->seed(ContentSeeder::class);
        $section = Section::orderBy('order_index')->firstOrFail();

        foreach ([1, 2] as $i) {
            $this->certificate(
                User::factory()->create(['role' => 'trainee']),
                $section,
                'CERT-TODAY-'.$i,
                '2026-11-22 0'.$i.':00:00'
            );
        }
        $this->certificate(
            User::factory()->create(['role' => 'trainee']),
            $section,
            'CERT-OLD',
            '2026-11-20 10:00:00'
        );

        $overview = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json();

        $this->assertSame(2, $overview['stats'][3]['value']);
    }

    public function test_overview_change_compares_previous_period(): void
    {
        Carbon::setTestNow('2026-11-22 12:00:00');
        $this->seed(ContentSeeder::class);
        $section = Section::orderBy('order_index')->firstOrFail();
        [$a, $b, $c] = [
            User::factory()->create(['role' => 'trainee']),
            User::factory()->create(['role' => 'trainee']),
            User::factory()->create(['role' => 'trainee']),
        ];

        // Current window: one active user and one certificate today.
        $this->event($a, 'app_opened');
        $this->certificate($a, $section, 'CERT-TODAY', '2026-11-22 09:00:00');
        // Previous window (2026-09-22..2026-10-22): three active users and two certificates.
        foreach ([$a, $b, $c] as $user) {
            $this->event($user, 'app_opened', null, [], Carbon::parse('2026-10-10 09:00:00'));
        }
        $this->certificate($b, $section, 'CERT-PREV-1', '2026-10-05 09:00:00');
        $this->certificate($c, $section, 'CERT-PREV-2', '2026-10-06 09:00:00');

        $stats = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json()['stats'];

        $this->assertSame(1, $stats[0]['value']);
        $this->assertSame(-67, $stats[0]['change']);
        $this->assertSame('down', $stats[0]['changeType']);
        $this->assertSame(1, $stats[3]['value']);
        $this->assertSame(-50, $stats[3]['change']);
        $this->assertSame('down', $stats[3]['changeType']);
    }

    public function test_overview_change_zero_when_no_prior_data(): void
    {
        Carbon::setTestNow('2026-11-22 12:00:00');
        $this->seed(ContentSeeder::class);
        $section = Section::orderBy('order_index')->firstOrFail();
        [$a, $b] = [
            User::factory()->create(['role' => 'trainee']),
            User::factory()->create(['role' => 'trainee']),
        ];

        $this->event($a, 'app_opened');
        $this->event($b, 'app_opened');
        $this->certificate($a, $section, 'CERT-TODAY-1', '2026-11-22 09:00:00');
        $this->certificate($b, $section, 'CERT-TODAY-2', '2026-11-22 10:00:00');

        $stats = $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->getJson('/api/admin/dashboard/overview')
            ->assertOk()->json()['stats'];

        $this->assertSame(2, $stats[0]['value']);
        $this->assertSame(2, $stats[3]['value']);
        foreach ($stats as $stat) {
            $this->assertSame(0, $stat['change']);
            $this->assertSame('up', $stat['changeType']);
        }
    }
}
