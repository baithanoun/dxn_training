<?php

namespace App\Http\Resources\Admin;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class SkillResource extends JsonResource
{
    public static $wrap = null;

    public function toArray(Request $request): array
    {
        return [
            'id' => (string) $this->id,
            'sectionId' => (string) $this->section_id,
            'title' => $this->title,
            'icon' => $this->icon,
            'orderIndex' => $this->order_index,
            'status' => $this->status,
            'levels' => LevelResource::collection($this->whenLoaded('levels')),
        ];
    }
}
