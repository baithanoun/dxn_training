<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Requests\Admin\CardsRequest;
use App\Http\Requests\Admin\LevelRequest;
use App\Http\Resources\Admin\LevelDetailResource;
use App\Http\Resources\Admin\LevelResource;
use App\Models\Level;
use App\Models\Skill;
use Illuminate\Support\Facades\DB;

class LevelController extends Controller
{
    public function show(string $id)
    {
        return $this->detail(Level::findOrFail($id));
    }

    public function store(LevelRequest $request)
    {
        $d = $request->columns();
        $l = DB::transaction(function () use ($d) {
            $l = Level::create($d + ['order_index' => (Level::where('skill_id', $d['skill_id'])->max('order_index') ?? 0) + 1]);
            $this->clearOtherFinals($l);

            return $l;
        });

        return (new LevelResource($l))->response()->setStatusCode(201);
    }

    public function update(LevelRequest $request, string $id)
    {
        $l = Level::findOrFail($id);
        DB::transaction(function () use ($l, $request) {
            $l->update($request->columns());
            $this->clearOtherFinals($l);
        });

        return new LevelResource($l);
    }

    public function destroy(string $id)
    {
        Level::findOrFail($id)->delete();

        return response()->noContent();
    }

    public function saveCards(CardsRequest $request, string $id)
    {
        $l = Level::findOrFail($id);
        DB::transaction(function () use ($l, $request) {
            $l->cards()->delete();
            foreach ($request->validated('cards') as $i => $c) {
                $l->cards()->create([
                    'title' => $c['title'],
                    'body' => $c['body'],
                    'tip' => $c['tip'] ?? null,
                    'media_url' => $c['mediaUrl'] ?? null,
                    'order_index' => $i + 1,
                ]);
            }
        });

        return $this->detail($l);
    }

    /** Only one final quiz per section. */
    private function clearOtherFinals(Level $l): void
    {
        if (! $l->is_final_quiz) {
            return;
        }
        $skillIds = Skill::where('section_id', $l->skill->section_id)->pluck('id');
        Level::whereIn('skill_id', $skillIds)->where('id', '!=', $l->id)->update(['is_final_quiz' => false]);
    }

    private function detail(Level $l): LevelDetailResource
    {
        return new LevelDetailResource($l->load(['cards', 'rounds.options'])->loadCount(['cards', 'rounds']));
    }
}
