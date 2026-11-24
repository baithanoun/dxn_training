<?php

namespace App\Http\Requests\Admin;

use Illuminate\Validation\Rule;

class ReorderRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'type' => ['required', Rule::in(['sections', 'skills', 'levels'])],
            'parentId' => ['required_unless:type,sections', 'nullable', 'string'],
            'ids' => ['required', 'array', 'min:1'],
            'ids.*' => ['required', 'string', 'distinct'],
        ];
    }
}
