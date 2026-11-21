<?php

namespace App\Http\Requests\Admin;

class CardsRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'cards' => ['present', 'array'],
            'cards.*.title' => ['required', 'string', 'max:255'],
            'cards.*.body' => ['required', 'string', 'max:5000'],
            'cards.*.tip' => ['nullable', 'string'],
            'cards.*.mediaUrl' => ['nullable', 'string', 'max:255'],
        ];
    }
}
