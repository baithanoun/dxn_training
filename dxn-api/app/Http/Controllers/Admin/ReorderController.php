<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Requests\Admin\ReorderRequest;
use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class ReorderController extends Controller
{
    public function __invoke(ReorderRequest $request)
    {
        $type = $request->validated('type');
        $ids = $request->validated('ids');
        $parent = $request->validated('parentId');

        [$model, $col] = match ($type) {
            'sections' => [Section::class, null],
            'skills' => [Skill::class, 'section_id'],
            'levels' => [Level::class, 'skill_id'],
        };
        $siblings = $model::query()->when($col, fn ($q) => $q->where($col, $parent))->pluck('id')->map(fn ($i) => (string) $i)->all();

        if (array_diff($ids, $siblings) || array_diff($siblings, $ids)) {
            throw ValidationException::withMessages(['ids' => 'ids must be exactly the set of sibling ids.']);
        }

        DB::transaction(function () use ($model, $ids) {
            foreach ($ids as $pos => $id) {
                $model::whereKey($id)->update(['order_index' => $pos + 1]);
            }
        });

        return response()->noContent();
    }
}
