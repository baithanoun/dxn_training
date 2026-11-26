<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Setting;
use Illuminate\Http\Request;

class SettingsController extends Controller
{
    private const RULES = [
        'app_name' => 'string|max:100',
        'team_name' => 'string|max:100',
        'team_tagline' => 'nullable|string|max:150',
        'team_bio' => 'nullable|string|max:1000',
        'team_welcome' => 'nullable|string|max:200',
        'app_download_url' => 'nullable|url:https|max:500',
        'app_version' => ['nullable', 'string', 'max:20', 'regex:/^[0-9A-Za-z.-]+$/'],
        'default_language' => 'in:ar',
        'maintenance_mode' => 'boolean',
        'xp_lesson_default' => 'integer|min:0|max:1000',
        'hearts_default' => 'integer|min:1|max:10',
        'heart_refill_hours' => 'integer|min:1|max:48',
        'streak_reminder_days' => 'integer|min:1|max:30',
        'xp_first_try_multiplier' => 'numeric|min:1|max:3',
        'xp_replay_ratio' => 'numeric|min:0|max:1',
        'notify_streak' => 'boolean',
        'notify_reengage' => 'boolean',
        'notify_leagues' => 'boolean',
        'notify_leaders' => 'boolean',
        'session_minutes' => 'integer|min:15|max:1440',
        'quiz_rate_limit_per_minute' => 'integer|min:1|max:120',
    ];

    public function index()
    {
        $out = [];
        foreach (Setting::DEFAULTS as $key => $default) {
            $out[$key] = Setting::get($key, $default);
        }
        return response()->json($out);
    }

    public function update(Request $request)
    {
        $unknown = array_diff(array_keys($request->all()), array_keys(self::RULES));
        if ($unknown !== []) {
            $errors = [];
            foreach ($unknown as $key) {
                $errors[$key] = ['مفتاح غير معروف'];
            }

            return response()->json(['message' => 'مفتاح غير معروف', 'errors' => $errors], 422);
        }

        $data = $request->validate(self::RULES);
        foreach ($data as $key => $value) {
            Setting::updateOrCreate(['key' => $key], ['value' => $value]);
        }
        return response()->json(['message' => 'تم حفظ الإعدادات']);
    }
}