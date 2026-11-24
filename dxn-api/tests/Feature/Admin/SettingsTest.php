<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\Section;
use App\Models\Setting;
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

        $this->assertSame('حكاية تميّز', $settings['app_name']);
        $this->assertSame('ar', $settings['default_language']);
        $this->assertFalse($settings['maintenance_mode']);
        $this->assertSame(20, $settings['xp_lesson_default']);
        $this->assertSame(3, $settings['hearts_default']);
        $this->assertSame(1.5, $settings['xp_first_try_multiplier']);
        $this->assertSame(0.25, $settings['xp_replay_ratio']);
        $this->assertSame(60, $settings['session_minutes']);
        $this->assertSame(10, $settings['quiz_rate_limit_per_minute']);
        $this->assertCount(19, $settings);
    }

    public function test_defaults_include_team_keys(): void
    {
        $this->assertSame('حكاية تميّز', Setting::DEFAULTS['team_name']);
        $this->assertSame('لأن كل طموح يستحق أن يُروى!', Setting::DEFAULTS['team_tagline']);
        $this->assertSame(
            'في حكاية تميّز، نجمع بين ثقافة الحياة الصحية مع DXN وقوة العمل الجماعي الذكي عبر الإنترنت. نوفّر بيئة تدريبية داعمة ترافق كل متدرب ومتدربة من البداية حتى الاحتراف، لبناء مشروع خاص بمرونة تامة، ولتصبح كل حكاية قادمة حكاية تميّز.',
            Setting::DEFAULTS['team_bio'],
        );
        $this->assertSame('أهلاً بك في عائلتك الثانية.. رحلتك نحو التميّز تبدأ الآن!', Setting::DEFAULTS['team_welcome']);
    }

    public function test_team_bio_max_1000(): void
    {
        $admin = $this->admin();
        $response = $this->actingAs($admin, 'sanctum')
            ->putJson('/api/admin/settings', ['team_bio' => str_repeat('a', 1001)])
            ->assertStatus(422)->assertJsonValidationErrors('team_bio');

        $this->assertNotSame('مفتاح غير معروف', $response->json('errors.team_bio.0'));

        $this->actingAs($admin, 'sanctum')
            ->putJson('/api/admin/settings', ['team_bio' => str_repeat('a', 1000)])
            ->assertOk();
    }

    public function test_invalid_value_422(): void
    {
        $this->actingAs($this->admin(), 'sanctum')
            ->putJson('/api/admin/settings', ['hearts_default' => 0])
            ->assertStatus(422)->assertJsonValidationErrors('hearts_default');
    }

    public function test_unknown_key_422(): void
    {
        $this->actingAs($this->admin(), 'sanctum')
            ->putJson('/api/admin/settings', ['foo' => 1])
            ->assertStatus(422);
    }

    public function test_maintenance_blocks_mobile_not_admin(): void
    {
        $admin = $this->admin();
        $this->actingAs($admin, 'sanctum')->putJson('/api/admin/settings', ['maintenance_mode' => true])
            ->assertOk();

        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->getJson('/api/sections/tree')->assertStatus(503);

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/settings')->assertOk();
    }

    public function test_completion_limiter_honors_setting(): void
    {
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
