<?php

namespace Tests\Feature;

use App\Models\Setting;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class AppConfigTest extends TestCase
{
    use RefreshDatabase;

    public function test_config_is_public(): void
    {
        $this->getJson('/api/app/config')->assertOk();
    }

    public function test_config_returns_only_whitelisted_keys(): void
    {
        $keys = array_keys($this->getJson('/api/app/config')->assertOk()->json());
        sort($keys);

        $this->assertSame([
            'appName', 'maintenanceMode', 'teamBio', 'teamName', 'teamTagline', 'teamWelcome',
        ], $keys);
    }

    public function test_config_reflects_updated_settings(): void
    {
        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', [
                'app_name' => 'My App',
                'team_name' => 'My Team',
                'team_tagline' => 'My Tagline',
                'team_bio' => 'My Bio',
                'team_welcome' => 'My Welcome',
            ])->assertOk();

        $config = $this->getJson('/api/app/config')->assertOk()->json();

        $this->assertSame('My App', $config['appName']);
        $this->assertSame('My Team', $config['teamName']);
        $this->assertSame('My Tagline', $config['teamTagline']);
        $this->assertSame('My Bio', $config['teamBio']);
        $this->assertSame('My Welcome', $config['teamWelcome']);
        $this->assertFalse($config['maintenanceMode']);
    }

    public function test_html_bio_returned_raw(): void
    {
        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', ['team_bio' => '<script>x</script>'])
            ->assertOk();

        $this->assertSame(
            '<script>x</script>',
            $this->getJson('/api/app/config')->assertOk()->json('teamBio'),
        );
    }

    public function test_config_available_during_maintenance(): void
    {
        Setting::create(['key' => 'maintenance_mode', 'value' => true]);

        $config = $this->getJson('/api/app/config')->assertOk()->json();
        $this->assertTrue($config['maintenanceMode']);

        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->getJson('/api/sections/tree')->assertStatus(503);
    }
}
