<?php

namespace App\Http\Requests\Admin;

use App\Models\Level;
use Illuminate\Validation\Validator;

class RoundsRequest extends AdminRequest
{
    public function rules(): array
    {
        return [
            'rounds' => ['required', 'array', 'min:1'],
            'rounds.*.title' => ['nullable', 'string', 'max:255'],
            'rounds.*.prompt' => ['required', 'string', 'max:2000'],
            'rounds.*.initialTrust' => ['required', 'integer', 'between:0,100'],
            'rounds.*.options' => ['required', 'array', 'min:2'],
            'rounds.*.options.*.text' => ['required', 'string', 'max:1000'],
            'rounds.*.options.*.isCorrect' => ['required', 'boolean'],
            'rounds.*.options.*.isNeutral' => ['required', 'boolean'],
            'rounds.*.options.*.trustImpact' => ['required', 'integer', 'between:-100,100'],
            'rounds.*.options.*.feedback' => ['nullable', 'string'],
            'rounds.*.options.*.xpBonus' => ['nullable', 'integer', 'between:0,100'],
        ];
    }

    public function after(): array
    {
        return [function (Validator $v) {
            $type = Level::findOrFail($this->route('id'))->type;
            if ($type === 'lesson') {
                $v->errors()->add('rounds', 'Lesson levels have no rounds.');

                return;
            }
            foreach ($this->input('rounds', []) as $i => $r) {
                foreach (is_array($r['options'] ?? null) ? $r['options'] : [] as $j => $o) {
                    if (($o['isCorrect'] ?? false) === true && ($o['isNeutral'] ?? false) === true) {
                        $v->errors()->add("rounds.$i.options.$j.isNeutral", 'An option cannot be both correct and neutral.');
                    }
                }
            }
            if (! in_array($type, ['quiz', 'chat_battle', 'match_pairs'])) {
                return;
            }
            foreach ($this->input('rounds', []) as $i => $r) {
                $opts = is_array($r['options'] ?? null) ? $r['options'] : [];
                if (! collect($opts)->contains(fn ($o) => ($o['isCorrect'] ?? false) === true)) {
                    $v->errors()->add("rounds.$i.options", 'Each round needs at least one correct option.');
                }
            }
        }];
    }
}
