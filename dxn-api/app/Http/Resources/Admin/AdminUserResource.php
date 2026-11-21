<?php

namespace App\Http\Resources\Admin;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class AdminUserResource extends JsonResource
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
            'leaderId' => $this->sponsor_id === null ? null : (string) $this->sponsor_id,
            'leaderName' => $this->sponsor?->name,
            'totalXp' => $this->total_xp,
            'currentStreak' => $this->current_streak,
            'lastActivityDate' => $this->last_activity_date?->toDateString(),
            'isActive' => $this->is_active,
            'certificatesCount' => $this->resource->certificates()->count(),
            'createdAt' => $this->created_at?->toDateString(),
        ];
    }
}