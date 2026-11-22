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

    protected function prepareForValidation(): void
    {
        if (is_string($this->input('dxnId'))) {
            $this->merge(['dxnId' => trim($this->input('dxnId'))]);
        }
        if (is_string($this->input('leaderDxnId'))) {
            $this->merge(['leaderDxnId' => trim($this->input('leaderDxnId'))]);
        }
    }

    public function messages(): array
    {
        return [
            'fullName.required' => 'الاسم مطلوب',
            'email.required_without' => 'البريد الإلكتروني أو الهاتف مطلوب',
            'phone.required_without' => 'البريد الإلكتروني أو الهاتف مطلوب',
            'password.required' => 'كلمة المرور مطلوبة',
            'dxnId.required' => 'رقم العضوية مطلوب',
        ];
    }
}
