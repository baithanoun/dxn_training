<?php

namespace App\Http\Requests;

use App\Models\Level;
use App\Services\ProgressService;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Http\Exceptions\HttpResponseException;
use Illuminate\Validation\Validator;

class CompletionRequest extends FormRequest
{
    public function authorize(): bool
    {
        $level = $this->level();
        if (! $level) {
            throw new HttpResponseException(response()->json(['message' => 'Not found'], 404));
        }
        if (! $this->user()) {
            return true; // auth middleware owns 401
        }

        // shortcut: early check keeps 409 ahead of body validation for locked levels;
        // the authoritative check is inside the transaction after lockForUpdate (M-3).
        if (! app(ProgressService::class)->isUnlocked($this->user(), $level)) {
            throw new HttpResponseException(response()->json(['message' => 'المستوى مقفل'], 409));
        }

        return true;
    }

    public function rules(): array
    {
        $type = $this->level()?->type;
        $rules = [
            'answers' => $type === 'lesson' ? ['nullable', 'array'] : ['required', 'array', 'min:1'],
            'answers.*' => ['array'],
            'answers.*.roundId' => ['required', 'string'],
        ];

        if (in_array($type, ['quiz', 'match_pairs', 'chat_battle'], true)) {
            $rules['answers.*.optionId'] = ['required', 'string'];
        } elseif ($type === 'order_sequence') {
            $rules['answers.*.orderedOptionIds'] = ['required', 'array', 'min:1'];
            $rules['answers.*.orderedOptionIds.*'] = ['required', 'string'];
        }

        return $rules;
    }

    public function after(): array
    {
        return [function (Validator $v) {
            $level = $this->level();
            if (! $level || $level->type === 'lesson' || $v->errors()->isNotEmpty()) {
                return;
            }

            $rounds = $level->rounds()->with('options')->orderBy('order_index')->get();
            $answers = $this->input('answers', []);
            if (! is_array($answers) || count($answers) !== $rounds->count()) {
                $v->errors()->add('answers', 'عدد الإجابات يجب أن يساوي عدد الجولات');

                return;
            }

            $roundsByKey = $rounds->keyBy(fn ($r) => (string) $r->id);
            $seen = [];
            foreach ($answers as $i => $answer) {
                $answer = is_array($answer) ? $answer : [];
                $roundId = (string) ($answer['roundId'] ?? '');
                if (! $roundsByKey->has($roundId)) {
                    $v->errors()->add("answers.$i.roundId", 'الجولة غير تابعة لهذا المستوى');
                    continue;
                }
                if (isset($seen[$roundId])) {
                    $v->errors()->add("answers.$i.roundId", 'جولة مكررة');
                    continue;
                }
                $seen[$roundId] = true;

                $optionIds = $roundsByKey->get($roundId)->options
                    ->pluck('id')->map(fn ($id) => (string) $id)->all();
                if (in_array($level->type, ['quiz', 'match_pairs', 'chat_battle'], true)) {
                    if (! in_array((string) ($answer['optionId'] ?? ''), $optionIds, true)) {
                        $v->errors()->add("answers.$i.optionId", 'الخيار غير تابع للجولة');
                    }
                } elseif ($level->type === 'order_sequence') {
                    $ordered = array_map('strval', (array) ($answer['orderedOptionIds'] ?? []));
                    $expected = $optionIds;
                    sort($ordered);
                    sort($expected);
                    if (count($ordered) !== count($expected) || $ordered !== $expected) {
                        $v->errors()->add("answers.$i.orderedOptionIds", 'الترتيب يجب أن يحتوي كل الخيارات مرة واحدة');
                    }
                }
            }
        }];
    }

    private function level(): ?Level
    {
        $levelId = $this->route('levelId');

        return $levelId ? Level::where('status', 'published')->find($levelId) : null;
    }
}
