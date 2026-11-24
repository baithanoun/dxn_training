<?php

namespace App\Http\Resources\Admin;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class LevelResource extends JsonResource
{
    public static $wrap = null;

    public function toArray(Request $request): array
    {
        return [
            'id' => (string) $this->id,
            'skillId' => (string) $this->skill_id,
            'title' => $this->title,
            'type' => $this->type,
            'passThreshold' => $this->pass_threshold,
            'xpReward' => $this->xp_reward,
            'orderIndex' => $this->order_index,
            'isFinalQuiz' => $this->is_final_quiz,
            'status' => $this->status,
            'cardsCount' => $this->whenCounted('cards'),
            'roundsCount' => $this->whenCounted('rounds'),
        ];
    }
}
