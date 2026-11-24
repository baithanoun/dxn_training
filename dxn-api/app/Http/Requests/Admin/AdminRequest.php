<?php

namespace App\Http\Requests\Admin;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Support\Str;

abstract class AdminRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // route middleware enforces role:admin
    }

    /** Required on create, optional on update. */
    protected function req(): string
    {
        return $this->isMethod('POST') ? 'required' : 'sometimes';
    }

    /** Validated input with camelCase keys converted to snake_case columns. */
    public function columns(): array
    {
        return collect($this->validated())->mapWithKeys(fn ($v, $k) => [Str::snake($k) => $v])->all();
    }
}
