<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

#[Fillable(['section_id', 'title', 'icon', 'order_index', 'status'])]
class Skill extends Model
{
    use SoftDeletes;

    public function section(): BelongsTo
    {
        return $this->belongsTo(Section::class);
    }

    public function levels(): HasMany
    {
        return $this->hasMany(Level::class)->orderBy('order_index');
    }
}
