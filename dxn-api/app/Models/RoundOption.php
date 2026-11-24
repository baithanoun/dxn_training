<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;

#[Fillable(['round_id', 'text', 'is_correct', 'is_neutral', 'trust_impact', 'feedback', 'xp_bonus', 'order_index'])]
class RoundOption extends Model
{
    protected function casts(): array
    {
        return [
            'is_correct' => 'boolean',
            'is_neutral' => 'boolean',
        ];
    }
}
