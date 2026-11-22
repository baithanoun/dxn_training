<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class SettingsTest extends TestCase
{
    use RefreshDatabase;

    private function admin(): User
    {
        return User::factory()->create(['role' => 'admin']);
    }

    public function test_defaults_returned(): void
    {
        $settings = $this->actingAs($this->admin(), 'sanctum')
            ->getJson('/api/admin/settings')
            ->assertOk()->json();

        $this->assertSame('DXN Training', $settings['app_name']);
        $this->assertSame('ar', $settings['default_language']);
        $this->assertFalse($settings['maintenance_mode']);
        $this->assertSame(20, $settings['xp_lesson_default']);
        $this->assertSame(3, $settings['hearts_default']);
        $this->assertSame(1.5, $settings['xp_first_try_multiplier']);
        $this->assertSame(0.25, $settings['xp_replay_ratio']);
        $this->assertSame(60, $settings['session_minutes']);
        $this->assertSame(10, $settings['quiz_rate_limit_per_minute']);
        $this->assertCount(15, $settings);
    }

    public function test_invalid_value_422(): void
    {
        $this->actingAs($this->admin(), 'sanctum')
            ->putJson('/api/admin/settings', ['hearts_default' => 0])
            ->assertStatus(422)->assertJsonValidationErrors('hearts_default');
    }

    public function test_unknown_key_422(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $this->actingAs($this->admin(), 'sanctum')
            ->putJson('/api/admin/settings', ['foo' => 1])
            ->assertStatus(422);
    }

    public function test_maintenance_blocks_mobile_not_admin(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $admin = $this->admin();
        $this->actingAs($admin, 'sanctum')->putJson('/api/admin/settings', ['maintenance_mode' => true])
            ->assertOk();

        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->getJson('/api/sections/tree')->assertStatus(503);

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/settings')->assertOk();
    }

    public function test_completion_limiter_honors_setting(): void
    {
        $this->markTestIncomplete('fixed in T2/T3');

        $section = Section::create(['title' => 'S', 'order_index' => 1, 'status' => 'published']);
        $skill = Skill::create(['section_id' => $section->id, 'title' => 'K', 'order_index' => 1, 'status' => 'published']);
        $level = Level::create([
            'skill_id' => $skill->id, 'title' => 'L', 'type' => 'lesson',
            'order_index' => 1, 'status' => 'published',
        ]);
        $user = User::factory()->create(['role' => 'trainee']);

        $this->actingAs($this->admin(), 'sanctum')
            ->putJson('/api/admin/settings', ['quiz_rate_limit_per_minute' => 1])
            ->assertOk();

        $this->actingAs($user, 'sanctum')->postJson("/api/levels/{$level->id}/complete", [])
            ->assertOk();
        $this->postJson("/api/levels/{$level->id}/complete", [])
            ->assertStatus(429);
    }
}
