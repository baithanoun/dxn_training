<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Requests\Admin\SectionRequest;
use App\Http\Resources\Admin\SectionResource;
use App\Models\Section;

class SectionController extends Controller
{
    public function store(SectionRequest $request)
    {
        $s = Section::create($request->columns() + ['order_index' => (Section::max('order_index') ?? 0) + 1]);

        return (new SectionResource($s))->response()->setStatusCode(201);
    }

    public function update(SectionRequest $request, string $id)
    {
        $s = Section::findOrFail($id);
        $s->update($request->columns());

        return new SectionResource($s);
    }

    public function destroy(string $id)
    {
        $s = Section::findOrFail($id);
        $dependents = Section::where('required_section_id', $s->id)->pluck('title');
        if ($dependents->isNotEmpty()) {
            return response()->json([
                'message' => 'Cannot delete: required by section(s) '.$dependents->implode(', '),
            ], 422);
        }
        $s->delete();

        return response()->noContent();
    }
}
