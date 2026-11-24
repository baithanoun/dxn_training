<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

#[Fillable(['skill_id', 'title', 'type', 'pass_threshold', 'xp_reward', 'order_index', 'is_final_quiz', 'status'])]
class Level extends Model
{
    use SoftDeletes;

    public const TYPES = ['lesson', 'quiz', 'chat_battle', 'match_pairs', 'order_sequence'];

    public function skill(): BelongsTo
    {
        return $this->belongsTo(Skill::class);
    }

    public function cards(): HasMany
    {
        return $this->hasMany(LessonCard::class)->orderBy('order_index');
    }

    public function rounds(): HasMany
    {
        return $this->hasMany(GameRound::class)->orderBy('order_index');
    }

    protected function casts(): array
    {
        return [
            'is_final_quiz' => 'boolean',
        ];
    }
}
