<?php

namespace Tests\Feature;

use App\Models\Certificate;
use App\Models\Level;
use App\Models\Section;
use App\Models\User;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class AccountDeletionTest extends TestCase
{
    use RefreshDatabase;

    public function test_register_requires_accept_terms(): void
    {
        $payload = [
            'fullName' => 'New User',
            'email' => 'new@example.test',
            'password' => 'secret123',
            'dxnId' => 'DXN-1',
            'gender' => 'male',
        ];

        $this->postJson('/api/auth/register', $payload)->assertStatus(422);

        $this->postJson('/api/auth/register', $payload + ['acceptTerms' => true])->assertCreated();
    }

    public function test_delete_requires_correct_password(): void
    {
        $user = User::factory()->create(['role' => 'trainee', 'password' => 'secret123']);
        $token = $user->createToken('mobile')->plainTextToken;

        $this->withHeader('Authorization', 'Bearer '.$token)
            ->deleteJson('/api/account', ['password' => 'wrong'])
            ->assertStatus(422);

        $this->assertNotNull($user->fresh());
        $this->assertTrue($user->fresh()->is_active);
    }

    public function test_delete_account_anonymizes_and_revokes_tokens(): void
    {
        $user = User::factory()->create(['role' => 'trainee', 'password' => 'secret123']);
        $token = $user->createToken('mobile')->plainTextToken;

        $this->seed(ContentSeeder::class);

        $section = Section::firstOrFail();
        $level = Level::firstOrFail();
        Certificate::create([
            'user_id' => $user->id,
            'section_id' => $section->id,
            'code' => 'CERT-TEST',
            'issued_at' => now(),
        ]);
        DB::table('user_level_progress')->insert([
            'user_id' => $user->id,
            'level_id' => $level->id,
            'status' => 'completed',
            'score' => 100,
            'best_score' => 100,
            'attempts' => 1,
            'created_at' => now(),
            'updated_at' => now(),
        ]);
        DB::table('analytics_events')->insert([
            'user_id' => $user->id,
            'event_name' => 'level_completed',
            'created_at' => now(),
        ]);

        $this->withHeader('Authorization', 'Bearer '.$token)
            ->deleteJson('/api/account', ['password' => 'secret123'])
            ->assertNoContent();

        $fresh = $user->fresh();
        $this->assertSame('مستخدم محذوف', $fresh->name);
        $this->assertNull($fresh->email);
        $this->assertNull($fresh->phone);
        $this->assertNull($fresh->dxn_id);
        $this->assertFalse($fresh->is_active);
        $this->assertSame(0, DB::table('user_level_progress')->where('user_id', $user->id)->count());
        $this->assertSame(0, DB::table('analytics_events')->where('user_id', $user->id)->count());
        $this->assertSame(0, Certificate::where('user_id', $user->id)->count());

        app('auth')->forgetGuards();
        $this->getJson('/api/auth/me', ['Authorization' => 'Bearer '.$token])->assertStatus(401);
    }

    public function test_deleted_user_cannot_login(): void
    {
        $user = User::factory()->create(['role' => 'trainee', 'email' => 'gone@example.test', 'password' => 'secret123']);

        $this->withHeader('Authorization', 'Bearer '.$user->createToken('x')->plainTextToken)
            ->deleteJson('/api/account', ['password' => 'secret123'])
            ->assertNoContent();

        $this->postJson('/api/auth/login', ['login' => 'gone@example.test', 'password' => 'secret123'])
            ->assertStatus(401);
    }

    public function test_admin_cannot_self_delete(): void
    {
        $admin = User::factory()->create(['role' => 'admin', 'password' => 'secret123']);
        $token = $admin->createToken('dashboard')->plainTextToken;

        $this->withHeader('Authorization', 'Bearer '.$token)
            ->deleteJson('/api/account', ['password' => 'secret123'])
            ->assertStatus(422);
    }

    public function test_leader_team_excludes_deleted_member(): void
    {
        $leader = User::factory()->create(['role' => 'leader', 'password' => 'secret123']);
        $member = User::factory()->create(['role' => 'trainee', 'sponsor_id' => $leader->id, 'password' => 'secret123']);

        $this->actingAs($member, 'sanctum')
            ->deleteJson('/api/account', ['password' => 'secret123'])
            ->assertNoContent();

        $this->actingAs($leader, 'sanctum')
            ->getJson('/api/leader/team')
            ->assertOk()
            ->assertJsonCount(0);
    }
}
