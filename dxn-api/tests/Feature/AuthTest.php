<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class AuthTest extends TestCase
{
    use RefreshDatabase;

    private function user(array $attrs = []): User
    {
        return User::factory()->create(array_merge([
            'email' => 'admin@dxn.local',
            'role' => 'admin',
            'password' => 'secret123',
        ], $attrs));
    }

    public function test_admin_can_login_with_email(): void
    {
        $u = $this->user();
        $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'secret123'])
            ->assertOk()
            ->assertJsonStructure(['token', 'user'])
            ->assertJsonPath('user.role', 'ADMIN')
            ->assertJsonPath('user.id', (string) $u->id);
    }

    public function test_user_can_login_with_phone(): void
    {
        $this->user(['phone' => '+970599000000', 'role' => 'trainee']);
        $this->postJson('/api/auth/login', ['login' => '+970599000000', 'password' => 'secret123'])->assertOk();
    }

    public function test_wrong_password_returns_401(): void
    {
        $u = $this->user();
        $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'nope'])->assertStatus(401);
    }

    public function test_inactive_user_returns_403(): void
    {
        $u = $this->user(['is_active' => false]);
        $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'secret123'])->assertStatus(403);
    }

    public function test_me_requires_token(): void
    {
        $this->getJson('/api/auth/me')->assertStatus(401);
    }

    public function test_logout_revokes_token(): void
    {
        $u = $this->user();
        $token = $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'secret123'])->json('token');
        $h = ['Authorization' => "Bearer $token"];
        $this->postJson('/api/auth/logout', [], $h)->assertNoContent();
        app('auth')->forgetGuards(); // the guard caches the user across requests in one test
        $this->getJson('/api/auth/me', $h)->assertStatus(401);
    }

    public function test_login_is_rate_limited(): void
    {
        $u = $this->user();
        for ($i = 1; $i <= 5; $i++) {
            $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'bad'])->assertStatus(401);
        }
        $this->postJson('/api/auth/login', ['login' => $u->email, 'password' => 'bad'])->assertStatus(429);
    }

    public function test_dxn_id_is_encrypted_and_hashed(): void
    {
        $u = $this->user(['dxn_id' => 'dxn-123']);
        $raw = DB::table('users')->where('id', $u->id)->first();
        $this->assertNotSame('dxn-123', $raw->dxn_id);
        $this->assertSame(User::hashDxnId('DXN-123'), $raw->dxn_id_hash);
        $this->assertSame('dxn-123', $u->fresh()->dxn_id);
    }

    public function test_role_middleware_blocks_non_admin(): void
    {
        $trainee = $this->user(['email' => 't@x.com', 'role' => 'trainee']);
        $admin = $this->user();
        $this->actingAs($trainee, 'sanctum')->getJson('/api/admin/ping')->assertStatus(403);
        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/ping')->assertOk();
    }
}
