<?php

namespace App\Http\Controllers;

use App\Models\Setting;

class AppConfigController extends Controller
{
    public function index()
    {
        $value = function (string $key): mixed {
            return Setting::get($key, Setting::DEFAULTS[$key]);
        };

        return response()->json([
            'appName' => $value('app_name'),
            'teamName' => $value('team_name'),
            'teamTagline' => $value('team_tagline'),
            'teamBio' => $value('team_bio'),
            'teamWelcome' => $value('team_welcome'),
            'maintenanceMode' => $value('maintenance_mode'),
            'appDownloadUrl' => $value('app_download_url'),
            'appVersion' => $value('app_version'),
        ]);
    }
}
