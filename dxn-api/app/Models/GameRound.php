<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

#[Fillable(['level_id', 'title', 'prompt', 'initial_trust', 'order_index'])]
class GameRound extends Model
{
    public function options(): HasMany
    {
        return $this->hasMany(RoundOption::class, 'round_id')->orderBy('order_index');
    }
}
