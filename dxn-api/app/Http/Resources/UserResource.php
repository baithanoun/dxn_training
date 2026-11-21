<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class UserResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => (string) $this->id,
            'fullName' => $this->name,
            'email' => $this->email,
            'phone' => $this->phone,
            'dxnId' => $this->dxn_id,
            'role' => strtoupper($this->role),
            'sponsorId' => $this->sponsor_id === null ? null : (string) $this->sponsor_id,
            'totalXp' => $this->total_xp,
            'currentStreak' => $this->current_streak,
            'hearts' => $this->hearts,
            'gems' => $this->gems,
            'isActive' => $this->is_active,
        ];
    }
}
