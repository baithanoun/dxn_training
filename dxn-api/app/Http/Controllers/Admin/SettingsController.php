<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Setting;
use Illuminate\Http\Request;

class SettingsController extends Controller
{
    private const DEFAULTS = [
        'app_name' => 'DXN Training',
        'default_language' => 'ar',
        'maintenance_mode' => false,
        'xp_lesson_default' => 20,
        'hearts_default' => 3,
        'heart_refill_hours' => 4,
        'streak_reminder_days' => 2,
        'xp_first_try_multiplier' => 1.5,
        'xp_replay_ratio' => 0.25,
        'notify_streak' => true,
        'notify_reengage' => true,
        'notify_leagues' => true,
        'notify_leaders' => true,
        'session_minutes' => 60,
        'quiz_rate_limit_per_minute' => 10,
    ];

    private const RULES = [
        'app_name' => 'string|max:100',
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
        $stored = Setting::all()->pluck('value', 'key')->toArray();
        $out = [];
        foreach (self::DEFAULTS as $key => $default) {
            $out[$key] = array_key_exists($key, $stored) ? $stored[$key] : $default;
        }
        return response()->json($out);
    }

    public function update(Request $request)
    {
        $data = $request->validate(self::RULES);
        foreach ($data as $key => $value) {
            Setting::updateOrCreate(['key' => $key], ['value' => $value]);
        }
        return response()->json(['message' => 'تم حفظ الإعدادات']);
    }
}