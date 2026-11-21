<?php

namespace App\Http\Requests\Admin;

use App\Models\Section;
use Illuminate\Validation\Rule;

class SectionRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'title' => [$this->req(), 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'bagId' => ['nullable', 'integer', 'between:1,99'],
            'certificateTitle' => ['nullable', 'string', 'max:255'],
            'requiredSectionId' => ['nullable', Rule::exists('sections', 'id')->whereNull('deleted_at'), Rule::notIn([(string) $this->route('id')])],
            'status' => ['sometimes', Rule::in(Section::STATUSES)],
        ];
    }
}
