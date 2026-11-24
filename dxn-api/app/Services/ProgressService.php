<?php

namespace App\Services;

use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use App\Models\UserLevelProgress;
use Illuminate\Support\Collection;

class ProgressService
{
    /** Published, non-trashed levels of a section in skill then level order. */
    public function orderedLevels(Section $section): Collection
    {
        return $section->skills()
            ->where('status', 'published')
            ->orderBy('order_index')
            ->with(['levels' => fn ($q) => $q->where('status', 'published')->orderBy('order_index')])
            ->get()
            ->flatMap(fn ($skill) => $skill->levels);
    }

    /** Final level of a section: flagged is_final_quiz, else last in order. */
    public function finalLevel(Section $section): ?Level
    {
        $levels = $this->orderedLevels($section);
        $flagged = $levels->firstWhere('is_final_quiz', true);

        return $flagged ?? $levels->last();
    }

    /** Per-level status map for a user. */
    public function levelStatuses(User $user): array
    {
        $progress = UserLevelProgress::where('user_id', $user->id)->get()->keyBy('level_id');
        $result = [];
        foreach (Section::where('status', 'published')->orderBy('order_index')->get() as $section) {
            $levels = $this->orderedLevels($section);
            $sectionUnlocked = $this->sectionUnlocked($user, $section);
            $prevCompleted = true;
            foreach ($levels as $level) {
                if (! $sectionUnlocked) {
                    $result[$level->id] = 'LOCKED';

                    continue;
                }
                $row = $progress->get($level->id);
                if ($row && $row->status === 'completed') {
                    $result[$level->id] = 'COMPLETED';
                    $prevCompleted = true;
                } elseif ($row && $row->status === 'attempted') {
                    $result[$level->id] = 'UNLOCKED';
                    $prevCompleted = false;
                } else {
                    if ($prevCompleted) {
                        $result[$level->id] = 'UNLOCKED';
                        $prevCompleted = false;
                    } else {
                        $result[$level->id] = 'LOCKED';
                    }
                }
            }
        }

        return $result;
    }

    /** Per-section status map for a user. */
    public function sectionStatuses(User $user): array
    {
        // M-1: load level statuses and all certificates once for the whole section loop.
        $statuses = $this->levelStatuses($user);
        $certificates = Certificate::where('user_id', $user->id)
            ->get(['section_id', 'code', 'issued_at'])->keyBy('section_id');

        $result = [];
        foreach (Section::where('status', 'published')->orderBy('order_index')->get() as $section) {
            $cert = $certificates->get($section->id);
            if ($cert) {
                $result[$section->id] = [
                    'status' => 'COMPLETED',
                    'certificateIssued' => true,
                    'certificateId' => $cert->code,
                    'completedAt' => $cert->issued_at?->toDateString(),
                ];

                continue;
            }
            $requiredId = $section->required_section_id;
            if ($requiredId && ! $certificates->has($requiredId)) {
                $result[$section->id] = ['status' => 'LOCKED', 'certificateIssued' => false, 'certificateId' => null, 'completedAt' => null];

                continue;
            }
            $levels = $this->orderedLevels($section);
            $hasUnlocked = $levels->contains(fn ($l) => ($statuses[$l->id] ?? 'LOCKED') !== 'LOCKED');
            // M-2: without a certificate the section is never COMPLETED, even if every level is done.
            $result[$section->id] = [
                'status' => $hasUnlocked ? 'UNLOCKED' : 'LOCKED',
                'certificateIssued' => false,
                'certificateId' => null,
                'completedAt' => null,
            ];
        }

        return $result;
    }

    public function isUnlocked(User $user, Level $level): bool
    {
        $statuses = $this->levelStatuses($user);

        return ($statuses[$level->id] ?? 'LOCKED') !== 'LOCKED';
    }

    public function completionPercent(User $user): int
    {
        $published = Level::where('status', 'published')->count();
        if ($published === 0) {
            return 0;
        }
        $completed = UserLevelProgress::where('user_id', $user->id)->where('status', 'completed')->count();

        return (int) round($completed / $published * 100);
    }

    /**
     * Published-section completion for the admin user detail.
     *
     * @return array<int, array{sectionId: string, sectionTitle: string, completedLevels: int, totalLevels: int, progressPercent: int}>
     */
    public function sectionProgress(User $user): array
    {
        $completed = UserLevelProgress::where('user_id', $user->id)
            ->where('status', 'completed')
            ->pluck('level_id')
            ->flip();

        $result = [];
        foreach (Section::where('status', 'published')->orderBy('order_index')->get() as $section) {
            $levels = $this->orderedLevels($section);
            $total = $levels->count();
            $done = $levels->filter(fn (Level $level) => $completed->has($level->id))->count();

            $result[] = [
                'sectionId' => (string) $section->id,
                'sectionTitle' => $section->title,
                'completedLevels' => $done,
                'totalLevels' => $total,
                'progressPercent' => $total === 0 ? 0 : (int) round($done / $total * 100),
            ];
        }

        return $result;
    }

    private function sectionUnlocked(User $user, Section $section): bool
    {
        if (! $section->required_section_id) {
            return true;
        }

        return Certificate::where('user_id', $user->id)->where('section_id', $section->required_section_id)->exists();
    }
}
