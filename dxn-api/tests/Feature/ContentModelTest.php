<?php

namespace Tests\Feature;

use App\Models\Level;
use App\Models\Section;
use App\Models\Skill;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ContentModelTest extends TestCase
{
    use RefreshDatabase;

    public function test_relations_are_ordered_and_soft_delete_hides_children_from_tree(): void
    {
        $section = Section::create(['title' => 'Intro', 'order_index' => 1]);
        Skill::create(['section_id' => $section->id, 'title' => 'Second', 'order_index' => 2]);
        $first = Skill::create(['section_id' => $section->id, 'title' => 'First', 'order_index' => 1]);

        $this->assertSame([1, 2], $section->skills->pluck('order_index')->all());

        $level = Level::create(['skill_id' => $first->id, 'title' => 'L1', 'type' => 'lesson', 'order_index' => 1]);
        Level::create(['skill_id' => $first->id, 'title' => 'L2', 'type' => 'quiz', 'order_index' => 2]);

        $this->assertSame(2, $first->levels()->count());

        $level->delete();

        $this->assertSame(1, $first->levels()->count());
        $this->assertSame(2, Level::withTrashed()->count());
    }
}
