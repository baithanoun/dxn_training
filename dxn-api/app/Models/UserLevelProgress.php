<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserLevelProgress extends Model
{
    protected $table = 'user_level_progress';

    protected $fillable = [
        'user_id', 'level_id', 'status', 'score', 'best_score',
        'attempts', 'passed_final_quiz', 'completed_at',
    ];

    protected $casts = [
        'passed_final_quiz' => 'boolean',
        'completed_at' => 'datetime',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function level(): BelongsTo
    {
        return $this->belongsTo(Level::class);
    }
}