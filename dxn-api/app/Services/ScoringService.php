<?php

namespace App\Services;

use App\Models\Level;
use App\Models\RoundOption;

class ScoringService
{
    /** Score 0..100 for a level given the submitted answers. */
    public function score(Level $level, array $answers): int
    {
        $rounds = $level->rounds()->with('options')->orderBy('order_index')->get();
        if ($rounds->isEmpty()) {
            return 100;
        }

        $byRound = collect($answers)->keyBy('roundId');

        if ($level->type === 'chat_battle') {
            $first = $rounds->first();
            $trust = $first->initial_trust;
            foreach ($rounds as $r) {
                $a = $byRound->get((string) $r->id);
                $opt = $a ? $r->options->firstWhere('id', (int) $a['optionId']) : null;
                if ($opt) {
                    $trust += $opt->trust_impact;
                }
            }
            return max(0, min(100, $trust));
        }

        if ($level->type === 'order_sequence') {
            $correct = 0;
            foreach ($rounds as $r) {
                $a = $byRound->get((string) $r->id);
                $ordered = $a['orderedOptionIds'] ?? [];
                $expected = $r->options->sortBy('order_index')->pluck('id')->map(fn ($x) => (string) $x)->all();
                if ($ordered === $expected) {
                    $correct++;
                }
            }
            return (int) round($correct / $rounds->count() * 100);
        }

        // quiz / match_pairs: round is correct if chosen option isCorrect
        $correct = 0;
        foreach ($rounds as $r) {
            $a = $byRound->get((string) $r->id);
            $opt = $a ? $r->options->firstWhere('id', (int) $a['optionId']) : null;
            if ($opt && $opt->is_correct) {
                $correct++;
            }
        }
        return (int) round($correct / $rounds->count() * 100);
    }

    public function passed(Level $level, int $score): bool
    {
        return $score >= $level->pass_threshold;
    }
}