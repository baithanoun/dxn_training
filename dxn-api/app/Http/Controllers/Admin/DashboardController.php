<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\AnalyticsEvent;
use App\Models\Certificate;
use App\Models\GameRound;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use App\Models\UserLevelProgress;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Collection;

class DashboardController extends Controller
{
    public function overview(Request $request)
    {
        $from = $request->input('from', now()->subDays(30)->toDateString());
        $to = $request->input('to', now()->toDateString());
        $days = (int) abs(now()->parse($from)->diffInDays(now()->parse($to)));
        $prevTo = now()->parse($from)->subDay()->toDateString();
        $prevFrom = now()->parse($from)->subDays($days + 1)->toDateString();

        // Stats
        $trainees = User::where('role', 'trainee')->get();
        $avgCompletion = $trainees->count() > 0 ? round($trainees->avg(fn ($u) => app(\App\Services\ProgressService::class)->completionPercent($u)), 1) : 0;
        $avgStreak = $trainees->count() > 0 ? round($trainees->avg('current_streak'), 1) : 0;

        // shortcut: completion/streak are current snapshots with no stored history, so the previous
        // period reuses their current averages; when to upgrade: persist per-trainee progress snapshots.
        $windowMetrics = function (string $fromDate, string $toDate) use ($avgCompletion, $avgStreak): array {
            $start = Carbon::parse($fromDate)->startOfDay()->toDateTimeString();
            $end = Carbon::parse($toDate)->endOfDay()->toDateTimeString();

            return [
                AnalyticsEvent::whereBetween('created_at', [$start, $end])->distinct('user_id')->count('user_id'),
                $avgCompletion,
                $avgStreak,
                Certificate::whereBetween('issued_at', [$start, $end])->count(),
            ];
        };

        $today = now()->toDateString();
        $current = $windowMetrics($today, $today);
        $previous = $windowMetrics($prevFrom, $prevTo);

        $statMeta = [
            ['title' => 'المستخدمون النشطون اليوم', 'format' => fn ($v) => $v, 'icon' => 'Users'],
            ['title' => 'معدل الإكمال العام', 'format' => fn ($v) => $v . '%', 'icon' => 'BarChart3'],
            ['title' => 'متوسط طول الـ Streak', 'format' => fn ($v) => $v . ' يوم', 'icon' => 'Flame'],
            ['title' => 'الشهادات الصادرة اليوم', 'format' => fn ($v) => $v, 'icon' => 'Award'],
        ];

        $stats = [];
        foreach ($statMeta as $index => $meta) {
            $change = $previous[$index] > 0
                ? (int) round(($current[$index] - $previous[$index]) / $previous[$index] * 100)
                : 0;
            $stats[] = [
                'title' => $meta['title'],
                'value' => $meta['format']($current[$index]),
                'change' => $change,
                'changeType' => $change >= 0 ? 'up' : 'down',
                'icon' => $meta['icon'],
            ];
        }

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

    public function analytics(Request $request)
    {
        return response()->json($this->analyticsData($request));
    }

    public function analyticsExport(Request $request)
    {
        $data = $this->analyticsData($request);

        $handle = fopen('php://temp', 'r+');
        fwrite($handle, "\xEF\xBB\xBF"); // BOM
        fputcsv($handle, ['القسم', 'المعرف', 'العنوان', 'المحاولات', 'نسبة الخطأ', 'متوسط الوقت', 'القيمة']);
        foreach ($data['failedQuestions'] as $question) {
            fputcsv($handle, [
                $this->csvSafe('سؤال'),
                $this->csvSafe($question['id']),
                $this->csvSafe($question['question']),
                $this->csvSafe((string) $question['attempts']),
                $this->csvSafe($question['errorRate'] . '%'),
                $this->csvSafe($question['avgTime']),
                '',
            ]);
        }
        foreach ($data['funnel'] as $step) {
            fputcsv($handle, [
                $this->csvSafe('مسار'), '', $this->csvSafe($step['name']), '', '', '',
                $this->csvSafe((string) $step['value']),
            ]);
        }
        rewind($handle);
        $csv = stream_get_contents($handle);
        fclose($handle);

        return response($csv, 200, [
            'Content-Type' => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="analytics.csv"',
        ]);
    }

    public function certificates()
    {
        $certificates = Section::orderBy('order_index')->get()->map(fn (Section $section) => [
            'sectionId' => (string) $section->id,
            'title' => $section->title,
            'issuedCount' => Certificate::where('section_id', $section->id)->count(),
            'isActive' => $section->status === 'published',
        ])->values();

        return response()->json($certificates);
    }

    private function analyticsData(Request $request): array
    {
        [$from, $to] = $this->analyticsWindow($request);
        $base = $this->baseEvents($request, $from, $to);

        $sectionId = $request->input('sectionId');
        $levelIds = $sectionId ? array_keys($this->levelRanks((int) $sectionId)) : null;
        $events = $levelIds === null
            ? $base
            : $base->filter(fn (AnalyticsEvent $e) => $e->level_id !== null && in_array($e->level_id, $levelIds))->values();

        $ranks = $this->levelRanks();
        $completedRanks = [];
        $startedRanks = [];
        foreach ($events as $event) {
            $rank = $event->level_id !== null && isset($ranks[$event->level_id]) ? $ranks[$event->level_id] : null;
            if ($rank === null) {
                continue;
            }
            if ($event->event_name === 'level_completed') {
                $completedRanks[$event->user_id][] = $rank;
            } elseif ($event->event_name === 'level_started') {
                $startedRanks[$event->user_id][] = $rank;
            }
        }
        $nextLevelUsers = 0;
        foreach ($completedRanks as $userId => $doneRanks) {
            foreach ($startedRanks[$userId] ?? [] as $startedRank) {
                foreach ($doneRanks as $doneRank) {
                    if ($startedRank > $doneRank) {
                        $nextLevelUsers++;
                        break 2;
                    }
                }
            }
        }
        // shortcut: "later level" uses global section/skill/level order, not a per-user learning path,
        // when to upgrade: when per-section or branching paths must be reported separately.

        $started = $events->where('event_name', 'level_started');
        $completed = $events->where('event_name', 'level_completed');
        $failed = $events->where('event_name', 'level_failed');

        $funnel = [
            ['name' => 'فتح المستوى', 'value' => $started->count()],
            ['name' => 'إكمال الدرس', 'value' => $completed->concat($failed)->pluck('user_id')->filter()->unique()->count()],
            ['name' => 'اجتياز الاختبار', 'value' => $completed->count()],
            ['name' => 'المستوى التالي', 'value' => $nextLevelUsers],
        ];

        $metrics = $this->summaryMetrics($events);
        $days = $from->copy()->startOfDay()->diffInDays($to->copy()->startOfDay()) + 1;
        $previousEvents = $this->baseEvents(
            $request,
            $from->copy()->subDays($days)->startOfDay(),
            $from->copy()->subDay()->endOfDay()
        );
        // No prior events means there is no baseline to compare against (same convention as overview()).
        $previous = $previousEvents->isEmpty() ? null : $this->summaryMetrics($previousEvents);
        $change = fn (string $key) => $previous === null ? 0 : round($metrics[$key] - ($previous[$key] ?? 0), 1);
        $summary = [
            ['title' => 'Completion Rate', 'value' => $this->percent($metrics['completion']), 'change' => $change('completion')],
            [
                'title' => 'Average Time',
                'value' => $metrics['averageTime'] === null ? '—' : $this->formatSeconds($metrics['averageTime']),
                'change' => $metrics['averageTime'] === null || $previous === null || $previous['averageTime'] === null
                    ? 0 : $change('averageTime'),
            ],
            ['title' => 'First-Attempt Success', 'value' => $this->percent($metrics['firstAttempt']), 'change' => $change('firstAttempt')],
            ['title' => 'Drop-off Rate', 'value' => $this->percent($metrics['dropOff']), 'change' => $change('dropOff')],
        ];

        return [
            'summary' => $summary,
            'funnel' => $funnel,
            'failedQuestions' => $this->failedQuestions($base, $sectionId),
            'retention' => $this->retention($from, $to),
        ];
    }

    private function analyticsWindow(Request $request): array
    {
        $from = $request->input('from', now()->subDays(29)->toDateString());
        $to = $request->input('to', now()->toDateString());

        return [Carbon::parse($from)->startOfDay(), Carbon::parse($to)->endOfDay()];
    }

    private function baseEvents(Request $request, Carbon $from, Carbon $to)
    {
        $query = AnalyticsEvent::whereBetween('created_at', [$from, $to]);
        if ($leaderId = $request->input('leaderId')) {
            $query->whereIn('user_id', User::where('sponsor_id', $leaderId)->pluck('id'));
        }

        return $query->get();
    }

    /** Global order rank per level id; optionally restricted to one section. */
    private function levelRanks(?int $sectionId = null): array
    {
        $ranks = [];
        $sections = Section::orderBy('order_index');
        if ($sectionId !== null) {
            $sections->where('id', $sectionId);
        }
        foreach ($sections->get() as $section) {
            foreach ($section->skills()->orderBy('order_index')->get() as $skill) {
                foreach ($skill->levels()->orderBy('order_index')->get() as $level) {
                    $ranks[$level->id] = count($ranks);
                }
            }
        }

        return $ranks;
    }

    private function summaryMetrics(Collection $events): array
    {
        $completed = $events->where('event_name', 'level_completed');
        $failed = $events->where('event_name', 'level_failed');
        $attempts = $completed->count() + $failed->count();

        $firstCompleted = $completed->filter(fn (AnalyticsEvent $e) => (int) ($e->payload['attempt'] ?? 0) === 1)->count();
        $firstFailed = $failed->filter(fn (AnalyticsEvent $e) => (int) ($e->payload['attempt'] ?? 0) === 1)->count();
        $firstTotal = $firstCompleted + $firstFailed;

        $startedUsers = $events->where('event_name', 'level_started')->pluck('user_id')->filter()->unique()->count();
        $completedUsers = $completed->pluck('user_id')->filter()->unique()->count();

        // shortcut: average time is the mean of per (user, level) timeMs sums; "—" if none were sent,
        // when to upgrade: when sessions must be split by wall-clock gaps instead of user+level.
        $sessions = [];
        foreach ($events as $event) {
            if ($event->event_name !== 'question_answered' || ! is_numeric($event->payload['timeMs'] ?? null)) {
                continue;
            }
            $key = ($event->user_id ?? 'anon') . ':' . ($event->level_id ?? 'none');
            $sessions[$key] = ($sessions[$key] ?? 0) + (float) $event->payload['timeMs'];
        }
        $averageTime = count($sessions) > 0 ? array_sum($sessions) / count($sessions) / 1000 : null;

        return [
            'completion' => $attempts > 0 ? $completed->count() / $attempts * 100 : 0,
            'averageTime' => $averageTime,
            'firstAttempt' => $firstTotal > 0 ? $firstCompleted / $firstTotal * 100 : 0,
            'dropOff' => $startedUsers > 0 ? ($startedUsers - min($startedUsers, $completedUsers)) / $startedUsers * 100 : 0,
        ];
    }

    private function failedQuestions(Collection $base, ?string $sectionId): array
    {
        $answers = $base->filter(fn (AnalyticsEvent $e) => $e->event_name === 'question_answered'
            && is_array($e->payload) && isset($e->payload['roundId']));

        if ($sectionId) {
            $levelIds = Level::whereHas('skill', fn ($q) => $q->where('section_id', $sectionId))->pluck('id');
            $roundIds = GameRound::whereIn('level_id', $levelIds)->pluck('id')
                ->map(fn ($id) => (string) $id)->all();
            $answers = $answers->filter(fn (AnalyticsEvent $e) => in_array((string) $e->payload['roundId'], $roundIds));
        }

        $groups = [];
        foreach ($answers as $event) {
            $groups[(string) $event->payload['roundId']][] = $event;
        }
        $rounds = GameRound::whereIn('id', array_keys($groups))->get()->keyBy('id');

        $result = [];
        foreach ($groups as $roundId => $events) {
            $attempts = count($events);
            $errors = 0;
            $totalMs = 0;
            $timed = 0;
            foreach ($events as $event) {
                if (($event->payload['correct'] ?? null) !== true) {
                    $errors++;
                }
                if (is_numeric($event->payload['timeMs'] ?? null)) {
                    $totalMs += (float) $event->payload['timeMs'];
                    $timed++;
                }
            }
            $result[] = [
                'id' => (string) $roundId,
                'question' => $rounds->get($roundId)?->prompt ?? '—',
                'attempts' => $attempts,
                'errorRate' => $attempts > 0 ? round($errors / $attempts * 100) : 0,
                'avgTime' => $timed > 0 ? $this->formatSeconds($totalMs / $timed / 1000) : '—',
            ];
        }
        usort($result, fn ($a, $b) => [$b['errorRate'], $b['attempts']] <=> [$a['errorRate'], $a['attempts']]);

        return $result;
    }

    private function retention(Carbon $from, Carbon $to): array
    {
        // shortcut: from/to bound the registration cohort; day-n activity may occur up to "to",
        // when to upgrade: when cohorts must include registrations older than the selected range.
        $users = User::whereBetween('created_at', [$from, $to])->get(['id', 'created_at']);
        $activeDays = [];
        foreach (AnalyticsEvent::whereBetween('created_at', [$from, $to])->get(['user_id', 'created_at']) as $event) {
            $activeDays[$event->user_id][$event->created_at->toDateString()] = true;
        }

        $result = [];
        $end = $to->copy()->startOfDay();
        foreach ([1, 7, 30] as $day) {
            $cohort = $users->filter(fn (User $u) => $u->created_at && $u->created_at->copy()->startOfDay()->lte($end->copy()->subDays($day)));
            $kept = $cohort->filter(function (User $u) use ($day, $activeDays) {
                $target = $u->created_at->copy()->startOfDay()->addDays($day)->toDateString();

                return isset($activeDays[$u->id][$target]);
            })->count();
            $result[] = [
                'day' => 'اليوم ' . $day,
                'rate' => $cohort->count() > 0 ? round($kept / $cohort->count() * 100) : 0,
            ];
        }

        return $result;
    }

    private function percent(float $value): string
    {
        return round($value) . '%';
    }

    private function formatSeconds(float $seconds): string
    {
        return (int) round($seconds) . ' ث';
    }

    /** Neutralize spreadsheet formula injection before fputcsv quotes the cell. */
    private function csvSafe(?string $v): string
    {
        $value = $v ?? '';

        return $value !== '' && str_contains('=+-@', $value[0]) ? "'" . $value : $value;
    }
}