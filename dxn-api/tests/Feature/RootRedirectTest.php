<?php

namespace Tests\Feature;

use Tests\TestCase;

class RootRedirectTest extends TestCase
{
    public function test_root_redirects_to_admin_login(): void
    {
        $this->get('/')->assertRedirect('/admin/login');
    }
}
