<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Resources\Admin\AdminUserResource;
use App\Models\User;
use Illuminate\Http\Request;

class TeamController extends Controller
{
    public function index()
    {
        $leaders = User::where('role', 'leader')->with('sponsor')->get();
        $totalPublished = \App\Models\Level::where('status', 'published')->count();

        $out = [];
        foreach ($leaders as $leader) {
            $members = User::where('sponsor_id', $leader->id)->get();
            $memberCount = $members->count();
            $completion = 0;
            if ($memberCount > 0 && $totalPublished > 0) {
                $totalCompleted = 0;
                foreach ($members as $m) {
                    $completed = \App\Models\UserLevelProgress::where('user_id', $m->id)->where('status', 'completed')->count();
                    $totalCompleted += $completed;
                }
                $completion = (int) round(($totalCompleted / ($memberCount * $totalPublished)) * 100);
            }
            $activeToday = $members->filter(fn ($m) => $m->last_activity_date && $m->last_activity_date->isToday())->count();

            $out[] = [
                'leaderId' => (string) $leader->id,
                'leader' => new AdminUserResource($leader),
                'leaderName' => $leader->name,
                'members' => $memberCount,
                'completion' => $completion,
                'activeToday' => $activeToday,
            ];
        }

        return response()->json($out);
    }

    public function show(string $leaderId)
    {
        $leader = User::where('role', 'leader')->findOrFail($leaderId);
        $members = User::where('sponsor_id', $leader->id)->get();
        $totalPublished = \App\Models\Level::where('status', 'published')->count();

        $memberData = [];
        foreach ($members as $m) {
            $completed = \App\Models\UserLevelProgress::where('user_id', $m->id)->where('status', 'completed')->count();
            $progressPercent = $totalPublished > 0 ? (int) round($completed / $totalPublished * 100) : 0;
            $resource = new AdminUserResource($m);
            $data = $resource->resolve();
            $data['progressPercent'] = $progressPercent;
            $memberData[] = $data;
        }

        return response()->json([
            'leader' => new AdminUserResource($leader),
            'members' => $memberData,
        ]);
    }
}