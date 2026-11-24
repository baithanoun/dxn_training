<?php

namespace Database\Seeders;

use App\Models\GameRound;
use App\Models\Level;
use App\Models\LessonCard;
use App\Models\RoundOption;
use App\Models\Section;
use App\Models\Skill;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class ContentSeeder extends Seeder
{
    public function run(): void
    {
        // withTrashed: a soft-deleted seed must not be re-inserted as duplicate content.
        if (Section::withTrashed()->exists()) {
            return;
        }

        $sections = json_decode(file_get_contents(__DIR__.'/data/content.json'), true, 512, JSON_THROW_ON_ERROR);

        DB::transaction(function () use ($sections) {
            $sectionIdByOrder = [];

            foreach ($sections as $s) {
                $section = Section::create([
                    'title' => $s['title'],
                    'description' => $s['description'],
                    'order_index' => $s['orderIndex'],
                    'bag_id' => $s['bagId'],
                    'certificate_title' => $s['certificateTitle'],
                    'required_section_id' => $s['requiredSectionOrderIndex'] !== null
                        ? $sectionIdByOrder[$s['requiredSectionOrderIndex']]
                        : null,
                    'status' => 'published',
                    'version' => 1,
                ]);
                $sectionIdByOrder[$s['orderIndex']] = $section->id;

                $lastLevel = null;
                foreach ($s['skills'] as $k) {
                    $skill = Skill::create([
                        'section_id' => $section->id,
                        'title' => $k['title'],
                        'order_index' => $k['orderIndex'],
                        'status' => 'published',
                    ]);

                    foreach ($k['levels'] as $l) {
                        $level = Level::create([
                            'skill_id' => $skill->id,
                            'title' => $l['title'],
                            'type' => $l['type'],
                            'pass_threshold' => $l['passThreshold'],
                            'xp_reward' => $l['xpReward'],
                            'order_index' => $l['orderIndex'],
                            'status' => 'published',
                        ]);

                        foreach ($l['cards'] as $i => $c) {
                            LessonCard::create([
                                'level_id' => $level->id,
                                'title' => $c['title'],
                                'body' => $c['body'],
                                'tip' => $c['tip'],
                                'order_index' => $i + 1,
                            ]);
                        }

                        foreach ($l['rounds'] as $i => $r) {
                            $round = GameRound::create([
                                'level_id' => $level->id,
                                'prompt' => $r['prompt'],
                                'initial_trust' => $r['initialTrust'],
                                'order_index' => $i + 1,
                            ]);

                            foreach ($r['options'] as $j => $o) {
                                RoundOption::create([
                                    'round_id' => $round->id,
                                    'text' => $o['text'],
                                    'is_correct' => $o['isCorrect'],
                                    'is_neutral' => $o['isNeutral'],
                                    'trust_impact' => $o['trustImpact'],
                                    'feedback' => $o['feedback'],
                                    'xp_bonus' => $o['xpBonus'],
                                    'order_index' => $j + 1,
                                ]);
                            }
                        }

                        $lastLevel = $level;
                    }
                }

                // Last level of the section (by skill order, then level order) is its final quiz.
                if ($lastLevel !== null) {
                    $lastLevel->update(['is_final_quiz' => true]);
                }
            }
        });
    }
}
