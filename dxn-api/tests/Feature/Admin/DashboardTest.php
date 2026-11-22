<?php

namespace Tests\Feature\Admin;

use App\Models\User;
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
}
