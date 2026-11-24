<?php

namespace App\Http\Resources\Admin;

use Illuminate\Http\Request;

/** Expects cards and rounds.options to be loaded (see LevelController::detail). */
class LevelDetailResource extends LevelResource
{
    public function toArray(Request $request): array
    {
        return parent::toArray($request) + [
            'cards' => $this->cards->map(fn ($c) => [
                'id' => (string) $c->id,
                'title' => $c->title,
                'body' => $c->body,
                'tip' => $c->tip,
                'mediaUrl' => $c->media_url,
                'orderIndex' => $c->order_index,
            ])->all(),
            'rounds' => $this->rounds->map(fn ($r) => [
                'id' => (string) $r->id,
                'title' => $r->title,
                'prompt' => $r->prompt,
                'initialTrust' => $r->initial_trust,
                'orderIndex' => $r->order_index,
                'options' => $r->options->map(fn ($o) => [
                    'id' => (string) $o->id,
                    'text' => $o->text,
                    'isCorrect' => $o->is_correct,
                    'isNeutral' => $o->is_neutral,
                    'trustImpact' => $o->trust_impact,
                    'feedback' => $o->feedback,
                    'xpBonus' => $o->xp_bonus,
                    'orderIndex' => $o->order_index,
                ])->all(),
            ])->all(),
        ];
    }
}
