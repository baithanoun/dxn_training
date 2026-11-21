<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class RegisterRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'fullName' => ['required', 'string', 'max:255'],
            'email' => ['required_without:phone', 'nullable', 'email', 'max:255', 'unique:users,email'],
            'phone' => ['required_without:email', 'nullable', 'string', 'max:32', 'unique:users,phone'],
            'password' => ['required', 'string', 'min:8'],
            'dxnId' => ['required', 'string', 'max:64'],
            'leaderDxnId' => ['nullable', 'string', 'max:64'],
        ];
    }
}
