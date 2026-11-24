<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Resources\Admin\SectionResource;
use App\Models\Section;

class ContentTreeController extends Controller
{
    public function __invoke()
    {
        return response()->json(SectionResource::collection(
            Section::orderBy('order_index')->with(['skills.levels' => fn ($q) => $q->withCount(['cards', 'rounds'])])->get()
        )->resolve());
    }
}
