<?php

namespace App\Http\Controllers\Mobile;

use App\Http\Controllers\Controller;
use App\Models\Level;
use App\Models\User;
use App\Models\UserLevelProgress;
use Illuminate\Http\Request;

class LeaderTeamController extends Controller
{
    public function __invoke(Request $request)
    {
        $leader = $request->user();
        if ($leader->role !== 'leader' && $leader->role !== 'admin') {
            return response()->json(['message' => 'Forbidden'], 403);
        }

        $members = User::where('sponsor_id', $leader->id)->where('is_active', true)->get();
        $totalPublished = Level::where('status', 'published')->count();

        $out = [];
        foreach ($members as $m) {
            $completed = UserLevelProgress::where('user_id', $m->id)->where('status', 'completed')->count();
            $progressPercent = $totalPublished > 0 ? (int) round($completed / $totalPublished * 100) : 0;
            $lastActiveDays = $m->last_activity_date
                ? (int) $m->last_activity_date->copy()->startOfDay()->diffInDays(now()->startOfDay())
                : 999;
            $status = 'INACTIVE';
            if ($progressPercent === 100) {
                $status = 'COMPLETED';
            } elseif ($lastActiveDays <= 3) {
                $status = 'ACTIVE';
            }
            $out[] = [
                'id' => (string) $m->id,
                'name' => $m->name,
                'progressPercent' => $progressPercent,
                'lastActiveDays' => $lastActiveDays,
                'status' => $status,
            ];
        }

        return response()->json($out);
    }
}
