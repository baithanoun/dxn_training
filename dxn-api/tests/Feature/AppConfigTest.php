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
            'appDownloadUrl', 'appName', 'appVersion', 'appVersionCode', 'maintenanceMode', 'teamBio', 'teamName', 'teamTagline', 'teamWelcome',
        ], $keys);
    }

    public function test_download_link_and_version_are_editable_and_public(): void
    {
        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', [
                'app_download_url' => 'https://example.org/app.apk',
                'app_version' => '1.2.0',
            ])->assertOk();

        $config = $this->getJson('/api/app/config')->assertOk()->json();
        $this->assertSame('https://example.org/app.apk', $config['appDownloadUrl']);
        $this->assertSame('1.2.0', $config['appVersion']);
    }

    public function test_download_link_must_be_https(): void
    {
        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', ['app_download_url' => 'javascript:alert(1)'])
            ->assertStatus(422)
            ->assertJsonValidationErrors('app_download_url');

        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', ['app_download_url' => 'http://example.org/app.apk'])
            ->assertStatus(422);
    }

    public function test_download_link_defaults_to_null(): void
    {
        $this->assertNull($this->getJson('/api/app/config')->assertOk()->json('appDownloadUrl'));
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
