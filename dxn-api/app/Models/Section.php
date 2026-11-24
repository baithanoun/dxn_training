<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

#[Fillable(['title', 'description', 'order_index', 'bag_id', 'certificate_title', 'required_section_id', 'status', 'version'])]
class Section extends Model
{
    use SoftDeletes;

    public const STATUSES = ['draft', 'published'];

    public function skills(): HasMany
    {
        return $this->hasMany(Skill::class)->orderBy('order_index');
    }

    public function requiredSection(): BelongsTo
    {
        return $this->belongsTo(Section::class, 'required_section_id')->orderBy('order_index');
    }
}
