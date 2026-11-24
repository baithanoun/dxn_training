<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Model;

#[Fillable(['level_id', 'title', 'body', 'tip', 'media_url', 'order_index'])]
class LessonCard extends Model
{
}
