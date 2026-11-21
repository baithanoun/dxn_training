<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\AnalyticsEvent;
use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use App\Models\UserLevelProgress;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    public function overview(Request $request)
    {
        $from = $request->input('from', now()->subDays(30)->toDateString());
        $to = $request->input('to', now()->toDateString());
        $prevFrom = now()->parse($from)->subDays(now()->parse($to)->diffInDays(now()->parse($from)) + 1)->toDateString();
        $prevTo = now()->parse($from)->subDay()->toDateString();

        // Stats
        $todayUsers = AnalyticsEvent::whereDate('created_at', now()->toDateString())->distinct('user_id')->count('user_id');
        $trainees = User::where('role', 'trainee')->get();
        $avgCompletion = $trainees->count() > 0 ? round($trainees->avg(fn ($u) => app(\App\Services\ProgressService::class)->completionPercent($u)), 1) : 0;
        $avgStreak = $trainees->count() > 0 ? round($trainees->avg('current_streak'), 1) : 0;
        $certCount = Certificate::count();

        $stats = [
            ['title' => 'المستخدمون النشطون اليوم', 'value' => $todayUsers, 'change' => 0, 'changeType' => 'up', 'icon' => 'Users'],
            ['title' => 'متوسط إكمال التدريب', 'value' => $avgCompletion . '%', 'change' => 0, 'changeType' => 'up', 'icon' => 'BarChart3'],
            ['title' => 'متوسط Streak', 'value' => $avgStreak . ' أيام', 'change' => 0, 'changeType' => 'up', 'icon' => 'Flame'],
            ['title' => 'الشهادات الصادرة', 'value' => $certCount, 'change' => 0, 'changeType' => 'up', 'icon' => 'Award'],
        ];

        // Activity per day
        $activity = [];
        $period = new \DatePeriod(new \DateTime($from), new \DateInterval('P1D'), (new \DateTime($to))->modify('+1 day'));
        foreach ($period as $date) {
            $d = $date->format('Y-m-d');
            $sessions = AnalyticsEvent::where('event_name', 'app_opened')->whereDate('created_at', $d)->count();
            $activeUsers = AnalyticsEvent::whereDate('created_at', $d)->distinct('user_id')->count('user_id');
            $activity[] = ['date' => $d, 'sessions' => $sessions, 'activeUsers' => $activeUsers];
        }

        // Failed skills (top 5)
        $failedSkills = [];
        $skills = \App\Models\Skill::where('status', 'published')->get();
        foreach ($skills as $skill) {
            $levelIds = $skill->levels()->where('status', 'published')->pluck('id');
            $completed = AnalyticsEvent::whereIn('level_id', $levelIds)->where('event_name', 'level_completed')->count();
            $failed = AnalyticsEvent::whereIn('level_id', $levelIds)->where('event_name', 'level_failed')->count();
            $total = $completed + $failed;
            if ($total > 0) {
                $failedSkills[] = ['name' => $skill->title, 'failureRate' => round($failed / $total * 100)];
            }
        }
        usort($failedSkills, fn ($a, $b) => $b['failureRate'] <=> $a['failureRate']);
        $failedSkills = array_slice($failedSkills, 0, 5);

        // Inactive users (top 10)
        $inactiveUsers = User::where('role', 'trainee')
            ->where(function ($q) {
                $q->whereNull('last_activity_date')
                  ->orWhere('last_activity_date', '<', now()->subDays(3)->toDateString());
            })
            ->orderBy('last_activity_date')
            ->limit(10)
            ->get()
            ->map(fn ($u) => [
                'id' => (string) $u->id,
                'name' => $u->name,
                'lastActive' => $u->last_activity_date?->toDateString(),
                'xp' => $u->total_xp,
                'role' => strtoupper($u->role),
            ])->values()->toArray();

        // Certificates by section
        $certificatesBySection = [];
        foreach (Section::where('status', 'published')->get() as $s) {
            $certificatesBySection[] = [
                'sectionId' => (string) $s->id,
                'title' => $s->title,
                'issuedCount' => Certificate::where('section_id', $s->id)->count(),
            ];
        }

        // Bags progress
        $bagsProgress = [];
        foreach (Section::where('status', 'published')->get() as $s) {
            $levels = $s->skills()->with('levels')->get()->flatMap(fn ($sk) => $sk->levels)->filter(fn ($l) => $l->status === 'published');
            $total = $levels->count();
            $completed = $total > 0 ? UserLevelProgress::whereIn('level_id', $levels->pluck('id'))->where('status', 'completed')->count() : 0;
            $percent = $total > 0 ? (int) round($completed / ($total * max(1, User::where('role', 'trainee')->count())) * 100) : 0;
            $bagsProgress[] = [
                'sectionId' => (string) $s->id,
                'title' => $s->title,
                'percent' => min(100, $percent),
            ];
        }

        return response()->json([
            'stats' => $stats,
            'activity' => $activity,
            'failedSkills' => $failedSkills,
            'inactiveUsers' => $inactiveUsers,
            'certificatesBySection' => $certificatesBySection,
            'bagsProgress' => $bagsProgress,
        ]);
    }
}