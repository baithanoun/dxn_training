<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Setting extends Model
{
    public const DEFAULTS = [
        'app_name' => 'حكاية تميّز',
        'team_name' => 'حكاية تميّز',
        'team_tagline' => 'لأن كل طموح يستحق أن يُروى!',
        'team_bio' => 'في حكاية تميّز، نجمع بين ثقافة الحياة الصحية مع DXN وقوة العمل الجماعي الذكي عبر الإنترنت. نوفّر بيئة تدريبية داعمة ترافق كل متدرب ومتدربة من البداية حتى الاحتراف، لبناء مشروع خاص بمرونة تامة، ولتصبح كل حكاية قادمة حكاية تميّز.',
        'team_welcome' => 'أهلاً بك في عائلتك الثانية.. رحلتك نحو التميّز تبدأ الآن!',
        'app_download_url' => null,
        'app_version' => '1.0.0',
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

    protected $primaryKey = 'key';
    public $incrementing = false;
    protected $keyType = 'string';
    protected $fillable = ['key', 'value'];

    protected $casts = [
        'value' => 'json',
    ];

    public static function get(string $key, mixed $default = null): mixed
    {
        $row = static::find($key);
        return $row ? $row->value : $default;
    }
}