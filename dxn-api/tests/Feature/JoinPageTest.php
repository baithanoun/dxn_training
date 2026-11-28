<?php

namespace Tests\Feature;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class JoinPageTest extends TestCase
{
    use RefreshDatabase;

    public function test_join_page_shows_leader_number_and_download(): void
    {
        $response = $this->get('/join/LEADER-123');

        $response->assertOk()
            ->assertSee('LEADER-123')
            ->assertSee('انضم عبر القائد')
            ->assertSee('تحميل التطبيق')
            ->assertSee('أدخل رقم القائد هذا عند التسجيل');
    }

    public function test_join_page_rejects_invalid_id(): void
    {
        $this->get('/join/bad id!')->assertNotFound();
        $this->get('/join/'.str_repeat('a', 33))->assertNotFound();
    }
}
