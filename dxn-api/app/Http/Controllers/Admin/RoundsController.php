<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Requests\Admin\RoundsRequest;
use App\Http\Resources\Admin\LevelDetailResource;
use App\Models\Level;
use Illuminate\Support\Facades\DB;

class RoundsController extends Controller
{
    public function __invoke(RoundsRequest $request, string $id)
    {
        $l = Level::findOrFail($id);
        DB::transaction(function () use ($l, $request) {
            $l->rounds()->delete(); // options cascade via FK
            foreach ($request->validated('rounds') as $i => $r) {
                $round = $l->rounds()->create([
                    'title' => $r['title'] ?? null,
                    'prompt' => $r['prompt'],
                    'initial_trust' => $r['initialTrust'],
                    'order_index' => $i + 1,
                ]);
                foreach ($r['options'] as $j => $o) {
                    $round->options()->create([
                        'text' => $o['text'],
                        'is_correct' => $o['isCorrect'],
                        'is_neutral' => $o['isNeutral'],
                        'trust_impact' => $o['trustImpact'],
                        'feedback' => $o['feedback'] ?? null,
                        'xp_bonus' => $o['xpBonus'] ?? 0,
                        'order_index' => $j + 1,
                    ]);
                }
            }
        });

        return new LevelDetailResource($l->load(['cards', 'rounds.options'])->loadCount(['cards', 'rounds']));
    }
}
