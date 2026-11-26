<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\File;
use Tests\TestCase;

class AppLatestApkTest extends TestCase
{
    use RefreshDatabase;

    private string $publicDir;

    protected function setUp(): void
    {
        parent::setUp();
        // Point public_path() at a scratch dir so the real published APK is never touched.
        $this->publicDir = sys_get_temp_dir().'/dxn-public-'.uniqid();
        File::ensureDirectoryExists($this->publicDir.'/downloads');
        $this->app->usePublicPath($this->publicDir);
    }

    protected function tearDown(): void
    {
        File::deleteDirectory($this->publicDir);
        parent::tearDown();
    }

    private function publish(array $manifest, bool $withApk = true): void
    {
        if ($withApk) {
            File::put($this->publicDir.'/downloads/'.$manifest['file'], 'apk');
        }
        File::put($this->publicDir.'/downloads/latest.json', json_encode($manifest));
    }

    public function test_no_published_apk_means_no_link_or_code(): void
    {
        $config = $this->getJson('/api/app/config')->assertOk()->json();

        $this->assertNull($config['appDownloadUrl']);
        $this->assertNull($config['appVersionCode']);
        $this->assertSame('1.0.0', $config['appVersion']);
    }

    public function test_published_apk_provides_link_version_and_code(): void
    {
        $this->publish(['versionCode' => 3, 'versionName' => '1.1.0', 'file' => 'hikaya-tamayoz.apk']);

        $config = $this->getJson('/api/app/config')->assertOk()->json();

        $this->assertStringEndsWith('/downloads/hikaya-tamayoz.apk', $config['appDownloadUrl']);
        $this->assertSame('1.1.0', $config['appVersion']);
        $this->assertSame(3, $config['appVersionCode']);
    }

    public function test_dashboard_link_overrides_published_apk_link(): void
    {
        $this->publish(['versionCode' => 3, 'versionName' => '1.1.0', 'file' => 'hikaya-tamayoz.apk']);
        $this->actingAs(User::factory()->create(['role' => 'admin']), 'sanctum')
            ->putJson('/api/admin/settings', ['app_download_url' => 'https://play.google.com/x'])
            ->assertOk();

        $config = $this->getJson('/api/app/config')->assertOk()->json();

        $this->assertSame('https://play.google.com/x', $config['appDownloadUrl']);
        $this->assertSame(3, $config['appVersionCode']);
    }

    public function test_manifest_pointing_to_missing_or_unsafe_file_is_ignored(): void
    {
        $this->publish(['versionCode' => 3, 'versionName' => '1.1.0', 'file' => 'gone.apk'], withApk: false);
        $this->assertNull($this->getJson('/api/app/config')->json('appVersionCode'));

        File::put($this->publicDir.'/secret.apk', 'x');
        $this->publish(['versionCode' => 3, 'versionName' => '1.1.0', 'file' => '../secret.apk'], withApk: false);
        $this->assertNull($this->getJson('/api/app/config')->json('appDownloadUrl'));
    }
}
