<?php

namespace App\Http\Controllers;

use App\Models\Setting;

class AppConfigController extends Controller
{
    /** Written by scripts/release-android.ps1 next to the published APK. */
    public const LATEST_APK_MANIFEST = 'downloads/latest.json';

    public function index()
    {
        $value = function (string $key): mixed {
            return Setting::get($key, Setting::DEFAULTS[$key]);
        };

        $latest = $this->latestApk();

        return response()->json([
            'appName' => $value('app_name'),
            'teamName' => $value('team_name'),
            'teamTagline' => $value('team_tagline'),
            'teamBio' => $value('team_bio'),
            'teamWelcome' => $value('team_welcome'),
            'maintenanceMode' => $value('maintenance_mode'),
            // A link set in the dashboard (e.g. Google Play) wins over the bundled APK.
            'appDownloadUrl' => $value('app_download_url')
                ?: ($latest ? asset('downloads/'.$latest['file']) : null),
            'appVersion' => $latest['versionName'] ?? $value('app_version'),
            'appVersionCode' => $latest['versionCode'] ?? null,
        ]);
    }

    /** @return array{versionCode:int, versionName:string, file:string}|null */
    private function latestApk(): ?array
    {
        $path = public_path(self::LATEST_APK_MANIFEST);
        if (! is_file($path)) {
            return null;
        }

        $data = json_decode((string) file_get_contents($path), true);
        $valid = is_array($data)
            && is_int($data['versionCode'] ?? null)
            && is_string($data['versionName'] ?? null)
            && is_string($data['file'] ?? null)
            && preg_match('/^[A-Za-z0-9._-]+\.apk$/', $data['file'])
            && is_file(public_path('downloads/'.$data['file']));

        return $valid ? $data : null;
    }
}
