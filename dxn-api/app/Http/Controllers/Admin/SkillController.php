<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Requests\Admin\SkillRequest;
use App\Http\Resources\Admin\SkillResource;
use App\Models\Skill;

class SkillController extends Controller
{
    public function store(SkillRequest $request)
    {
        $d = $request->columns();
        $k = Skill::create($d + ['order_index' => (Skill::where('section_id', $d['section_id'])->max('order_index') ?? 0) + 1]);

        return (new SkillResource($k))->response()->setStatusCode(201);
    }

    public function update(SkillRequest $request, string $id)
    {
        $k = Skill::findOrFail($id);
        $k->update($request->columns());

        return new SkillResource($k);
    }

    public function destroy(string $id)
    {
        Skill::findOrFail($id)->delete();

        return response()->noContent();
    }
}
