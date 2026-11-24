<?php

namespace Tests\Unit;

use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use App\Models\User;
use App\Models\UserLevelProgress;
use App\Services\ProgressService;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ProgressServiceTest extends TestCase
{
    use RefreshDatabase;

    private ProgressService $progress;

    protected function setUp(): void
    {
        parent::setUp();
        $this->seed(ContentSeeder::class);
        $this->progress = app(ProgressService::class);
    }

    private function level(int $sectionOrder, int $skillOrder, int $levelOrder): Level
    {
        return Section::where('order_index', $sectionOrder)->firstOrFail()
            ->skills()->where('order_index', $skillOrder)->firstOrFail()
            ->levels()->where('order_index', $levelOrder)->firstOrFail();
    }

    public function test_ordered_levels_follow_skill_then_level_order_and_skip_unpublished(): void
    {
        $section = Section::where('order_index', 1)->firstOrFail();
        $draftSkill = Skill::create(['section_id' => $section->id, 'title' => 'Draft', 'order_index' => 9, 'status' => 'draft']);
        Level::create(['skill_id' => $draftSkill->id, 'title' => 'Draft level', 'type' => 'quiz', 'order_index' => 1, 'status' => 'published']);
        $this->level(1, 1, 1)->delete();

        $ordered = $this->progress->orderedLevels($section)->pluck('id')->all();

        $this->assertCount(2, $ordered);
        $this->assertSame($this->level(1, 2, 1)->id, $ordered[0]);
        $this->assertSame($this->level(1, 3, 1)->id, $ordered[1]);
    }

    public function test_final_level_prefers_flagged_level(): void
    {
        $section = Section::where('order_index', 1)->firstOrFail();
        $this->assertTrue($this->progress->finalLevel($section)->is_final_quiz);
        $this->assertSame($this->level(1, 3, 1)->id, $this->progress->finalLevel($section)->id);

        $flagged = $this->level(1, 1, 1);
        $flagged->update(['is_final_quiz' => true]);

        $this->assertSame($flagged->id, $this->progress->finalLevel($section)->id);
    }

    public function test_level_statuses_and_is_unlocked_follow_completion_chain(): void
    {
        $user = User::factory()->create(['role' => 'trainee']);
        $first = $this->level(1, 1, 1);
        $second = $this->level(1, 2, 1);

        $statuses = $this->progress->levelStatuses($user);
        $this->assertSame('UNLOCKED', $statuses[$first->id]);
        $this->assertSame('LOCKED', $statuses[$second->id]);
        $this->assertTrue($this->progress->isUnlocked($user, $first));
        $this->assertFalse($this->progress->isUnlocked($user, $second));

        UserLevelProgress::create([
            'user_id' => $user->id, 'level_id' => $first->id,
            'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
        ]);

        $statuses = $this->progress->levelStatuses($user);
        $this->assertSame('COMPLETED', $statuses[$first->id]);
        $this->assertSame('UNLOCKED', $statuses[$second->id]);
        $this->assertTrue($this->progress->isUnlocked($user, $second));
    }

    public function test_section_statuses_require_certificate_for_next_section(): void
    {
        $user = User::factory()->create(['role' => 'trainee']);
        $first = Section::where('order_index', 1)->firstOrFail();
        $second = Section::where('order_index', 2)->firstOrFail();

        $statuses = $this->progress->sectionStatuses($user);
        $this->assertSame('LOCKED', $statuses[$second->id]['status']);
        $this->assertFalse($statuses[$second->id]['certificateIssued']);

        Certificate::create([
            'user_id' => $user->id, 'section_id' => $first->id,
            'code' => 'DXN-TEST-1', 'issued_at' => now(),
        ]);

        $statuses = $this->progress->sectionStatuses($user);
        $this->assertSame('COMPLETED', $statuses[$first->id]['status']);
        $this->assertSame('DXN-TEST-1', $statuses[$first->id]['certificateId']);
        $this->assertSame('UNLOCKED', $statuses[$second->id]['status']);
    }

    public function test_completion_percent_counts_completed_published_levels(): void
    {
        $user = User::factory()->create(['role' => 'trainee']);
        $total = Level::where('status', 'published')->count();
        $this->assertSame(0, $this->progress->completionPercent($user));

        UserLevelProgress::create([
            'user_id' => $user->id, 'level_id' => $this->level(1, 1, 1)->id,
            'status' => 'completed', 'score' => 100, 'best_score' => 100, 'attempts' => 1,
        ]);

        $this->assertSame((int) round(1 / $total * 100), $this->progress->completionPercent($user));
    }
}
