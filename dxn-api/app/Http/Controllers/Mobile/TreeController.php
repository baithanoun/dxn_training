<?php

namespace App\Http\Controllers\Mobile;

use App\Http\Controllers\Controller;
use App\Models\Section;
use App\Services\ProgressService;
use Illuminate\Http\Request;

class TreeController extends Controller
{
    public function __invoke(Request $request, ProgressService $progress)
    {
        $sections = Section::where('status', 'published')
            ->orderBy('order_index')
            ->with(['skills' => fn ($q) => $q->where('status', 'published')->orderBy('order_index')
                ->with(['levels' => fn ($q2) => $q2->where('status', 'published')->orderBy('order_index')
                    ->with(['cards' => fn ($q3) => $q3->orderBy('order_index'),
                            'rounds' => fn ($q4) => $q4->orderBy('order_index')
                                ->with(['options' => fn ($q5) => $q5->orderBy('order_index')])])])])
            ->get();

        $out = [];
        foreach ($sections as $s) {
            $skills = [];
            foreach ($s->skills as $sk) {
                $levels = [];
                foreach ($sk->levels as $lv) {
                    $cards = [];
                    foreach ($lv->cards as $c) {
                        $cards[] = [
                            'id' => (string) $c->id,
                            'title' => $c->title,
                            'body' => $c->body,
                            'tip' => $c->tip,
                            'mediaUrl' => $c->media_url,
                        ];
                    }
                    $rounds = [];
                    foreach ($lv->rounds as $r) {
                        $opts = [];
                        foreach ($r->options as $o) {
                            $opts[] = [
                                'id' => (string) $o->id,
                                'text' => $o->text,
                                'isCorrect' => (bool) $o->is_correct,
                                'isNeutral' => (bool) $o->is_neutral,
                                'trustImpact' => $o->trust_impact,
                                'feedback' => $o->feedback,
                                'xpBonus' => $o->xp_bonus,
                            ];
                        }
                        $rounds[] = [
                            'id' => (string) $r->id,
                            'prompt' => $r->prompt,
                            'initialTrust' => $r->initial_trust,
                            'options' => $opts,
                        ];
                    }
                    $levels[] = [
                        'id' => (string) $lv->id,
                        'title' => $lv->title,
                        'orderIndex' => $lv->order_index,
                        'xpReward' => $lv->xp_reward,
                        'passThreshold' => $lv->pass_threshold,
                        'type' => $lv->type,
                        'cards' => $cards,
                        'rounds' => $rounds,
                    ];
                }
                $skills[] = [
                    'id' => (string) $sk->id,
                    'title' => $sk->title,
                    'orderIndex' => $sk->order_index,
                    'levels' => $levels,
                ];
            }
            $out[] = [
                'id' => (string) $s->id,
                'title' => $s->title,
                'orderIndex' => $s->order_index,
                'bagId' => $s->bag_id,
                'description' => $s->description,
                'certificateTitle' => $s->certificate_title,
                'requiredSectionId' => $s->required_section_id === null ? null : (string) $s->required_section_id,
                'skills' => $skills,
            ];
        }

        return response()->json($out);
    }
}