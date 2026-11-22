<?php

namespace App\Http\Controllers\Mobile;

use App\Http\Controllers\Controller;
use App\Http\Requests\CompletionRequest;
use App\Models\AnalyticsEvent;
use App\Models\Level;
use App\Models\UserLevelProgress;
use App\Models\Certificate;
use App\Services\ProgressService;
use App\Services\ScoringService;
use Illuminate\Support\Facades\DB;

class CompletionController extends Controller
{
    public function __invoke(CompletionRequest $request, string $levelId, ProgressService $progress, ScoringService $scoring)
    {
        $level = Level::where('status', 'published')->find($levelId);
        if (! $level) {
            return response()->json(['message' => 'Not found'], 404);
        }

        $user = $request->user();
        $answers = $request->validated('answers') ?? [];
        $score = $scoring->score($level, $answers);
        $passed = $scoring->passed($level, $score);

        $xpFirst = (float) (\App\Models\Setting::get('xp_first_try_multiplier', 1.5));
        $xpReplay = (float) (\App\Models\Setting::get('xp_replay_ratio', 0.25));

        $result = DB::transaction(function () use ($user, $level, $score, $passed, $progress, $xpFirst, $xpReplay) {
            $row = UserLevelProgress::where('user_id', $user->id)
                ->where('level_id', $level->id)
                ->lockForUpdate()
                ->first();

            // M-3: authoritative unlock check under the row lock (the FormRequest
            // pre-check only preserves 409-before-validation).
            if (! $progress->isUnlocked($user, $level)) {
                return ['locked' => true];
            }

            // I-1: first ever attempt gets the multiplier; a first pass after a
            // failure gets the base reward; only an improved replay gets the ratio.
            $firstAttempt = ! $row;
            $wasCompleted = $row?->status === 'completed';
            $prevBest = (int) ($row?->best_score ?? 0);

            $xpAwarded = 0;
            if ($passed && ! $wasCompleted) {
                $xpAwarded = $firstAttempt
                    ? (int) round($level->xp_reward * $xpFirst)
                    : (int) $level->xp_reward;
            } elseif ($passed && $wasCompleted && $score > $prevBest) {
                $xpAwarded = (int) floor($level->xp_reward * $xpReplay);
            }

            if (! $row) {
                $row = new UserLevelProgress();
                $row->user_id = $user->id;
                $row->level_id = $level->id;
            }
            $row->attempts = $row->attempts + 1;
            $row->score = $score;
            $row->best_score = max((int) ($row->best_score ?? 0), $score);
            if ($passed) {
                $row->status = 'completed';
                if (! $row->completed_at) {
                    $row->completed_at = now();
                }
            } elseif ($row->status !== 'completed') {
                $row->status = 'attempted';
            }
            if ($level->is_final_quiz && $passed) {
                $row->passed_final_quiz = true;
            }
            $row->save();

            if ($xpAwarded > 0) {
                $user->total_xp = $user->total_xp + $xpAwarded;
            }

            // Streak
            $today = now()->toDateString();
            $last = $user->last_activity_date?->toDateString();
            if ($last === $today) {
                // unchanged
            } elseif ($last === now()->subDay()->toDateString()) {
                $user->current_streak = $user->current_streak + 1;
            } else {
                $user->current_streak = 1;
            }
            $user->last_activity_date = $today;
            $user->save();

            // Certificate
            $certificateIssued = false;
            $certificateTitle = null;
            $certificateId = null;
            $section = $level->skill->section;
            $finalLevel = app(ProgressService::class)->finalLevel($section);
            if ($passed && $finalLevel && $finalLevel->id === $level->id) {
                $existing = Certificate::where('user_id', $user->id)->where('section_id', $section->id)->first();
                if (! $existing) {
                    $cert = Certificate::create([
                        'user_id' => $user->id,
                        'section_id' => $section->id,
                        'code' => 'DXN-' . strtoupper(\Illuminate\Support\Str::random(10)),
                        'issued_at' => now(),
                    ]);
                    $certificateIssued = true;
                    $certificateTitle = $section->certificate_title;
                    $certificateId = $cert->code;
                }
            }

            // I-3: server-owned audit event for every graded attempt.
            AnalyticsEvent::create([
                'user_id' => $user->id,
                'event_name' => $passed ? 'level_completed' : 'level_failed',
                'level_id' => $level->id,
                'payload' => ['score' => $score, 'attempt' => $row->attempts],
                'created_at' => now(),
            ]);

            return [
                'xpAwarded' => $xpAwarded,
                'totalXp' => $user->total_xp,
                'currentStreak' => $user->current_streak,
                'certificateIssued' => $certificateIssued,
                'certificateTitle' => $certificateTitle,
                'certificateId' => $certificateId,
            ];
        });

        if (! empty($result['locked'])) {
            return response()->json(['message' => 'المستوى مقفل'], 409);
        }

        return response()->json([
            'success' => true,
            'score' => $score,
            'passed' => $passed,
            'xpAwarded' => $result['xpAwarded'],
            'totalXp' => $result['totalXp'],
            'currentStreak' => $result['currentStreak'],
            'certificateIssued' => $result['certificateIssued'],
            'certificateTitle' => $result['certificateTitle'],
            'certificateId' => $result['certificateId'],
        ]);
    }
}