<?php

namespace App\Http\Requests\Admin;

use App\Models\Level;
use App\Models\Section;
use Illuminate\Validation\Rule;

class LevelRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'skillId' => [$this->isMethod('POST') ? 'required' : 'prohibited', Rule::exists('skills', 'id')->whereNull('deleted_at')],
            'title' => [$this->req(), 'string', 'max:255'],
            'type' => [$this->req(), Rule::in(Level::TYPES)],
            'passThreshold' => ['sometimes', 'integer', 'between:0,100'],
            'xpReward' => ['sometimes', 'integer', 'between:0,1000'],
            'isFinalQuiz' => ['sometimes', 'boolean'],
            'status' => ['sometimes', Rule::in(Section::STATUSES)],
        ];
    }
}
