<?php

namespace App\Http\Controllers\Mobile;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Services\ProgressService;
use Illuminate\Http\Request;

class ProgressController extends Controller
{
    public function progress(Request $request, string $userId, ProgressService $progress)
    {
        $user = $this->resolveUser($request, $userId);
        if ($user instanceof \Illuminate\Http\JsonResponse) return $user;

        $statuses = $progress->levelStatuses($user);
        $rows = \App\Models\UserLevelProgress::where('user_id', $user->id)->get()->keyBy('level_id');

        $out = [];
        foreach (\App\Models\Level::where('status', 'published')->get() as $level) {
            $row = $rows->get($level->id);
            $out[(string) $level->id] = [
                'status' => $statuses[$level->id] ?? 'LOCKED',
                'score' => $row?->score ?? 0,
                'attempts' => $row?->attempts ?? 0,
                'bestScore' => $row?->best_score ?? 0,
                'sectionCompleted' => false,
                'passedFinalQuiz' => (bool) ($row?->passed_final_quiz ?? false),
            ];
        }
        return response()->json($out);
    }

    public function sectionsProgress(Request $request, string $userId, ProgressService $progress)
    {
        $user = $this->resolveUser($request, $userId);
        if ($user instanceof \Illuminate\Http\JsonResponse) return $user;

        return response()->json($progress->sectionStatuses($user));
    }

    public function profile(Request $request, string $userId)
    {
        $user = $this->resolveUser($request, $userId);
        if ($user instanceof \Illuminate\Http\JsonResponse) return $user;

        return (new \App\Http\Resources\UserResource($user))->resolve();
    }

    private function resolveUser(Request $request, string $userId)
    {
        $current = $request->user();
        if ((string) $current->id === $userId) {
            return $current;
        }
        if ($current->role === 'admin') {
            $u = User::find($userId);
            return $u ?: response()->json(['message' => 'Not found'], 404);
        }
        // leader can view own team members
        if ($current->role === 'leader') {
            $target = User::find($userId);
            if ($target && (string) $target->sponsor_id === (string) $current->id) {
                return $target;
            }
        }
        return response()->json(['message' => 'Forbidden'], 403);
    }
}