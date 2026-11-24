<?php

namespace App\Http\Resources\Admin;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class SectionResource extends JsonResource
{
    public static $wrap = null;

    public function toArray(Request $request): array
    {
        return [
            'id' => (string) $this->id,
            'title' => $this->title,
            'description' => $this->description,
            'orderIndex' => $this->order_index,
            'bagId' => $this->bag_id,
            'certificateTitle' => $this->certificate_title,
            'requiredSectionId' => $this->required_section_id === null ? null : (string) $this->required_section_id,
            'status' => $this->status,
            'skills' => SkillResource::collection($this->whenLoaded('skills')),
        ];
    }
}
