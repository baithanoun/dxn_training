<?php

namespace App\Http\Requests\Admin;

use App\Models\Section;
use Illuminate\Validation\Rule;

class SkillRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'sectionId' => [$this->isMethod('POST') ? 'required' : 'prohibited', Rule::exists('sections', 'id')->whereNull('deleted_at')],
            'title' => [$this->req(), 'string', 'max:255'],
            'icon' => ['nullable', 'string', 'max:255'],
            'status' => ['sometimes', Rule::in(Section::STATUSES)],
        ];
    }
}
