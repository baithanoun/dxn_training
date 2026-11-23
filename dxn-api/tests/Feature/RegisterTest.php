<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class RegisterTest extends TestCase
{
    use RefreshDatabase;

    private function payload(array $overrides = []): array
    {
        return array_merge([
            'fullName' => 'متدرب جديد',
            'email' => 'trainee@example.test',
            'password' => 'password123',
            'dxnId' => 'DXN-1001',
            'gender' => 'male',
        ], $overrides);
    }

    public function test_register_trainee_returns_token(): void
    {
        $response = $this->postJson('/api/auth/register', $this->payload());

        $response->assertCreated()
            ->assertJsonStructure(['token', 'user'])
            ->assertJsonPath('user.role', 'TRAINEE');
        $this->assertIsString($response->json('token'));
        $this->assertIsString($response->json('user.id'));
        $this->assertSame('trainee', User::where('email', 'trainee@example.test')->firstOrFail()->role);
    }

    public function test_register_requires_email_or_phone(): void
    {
        $this->postJson('/api/auth/register', $this->payload(['email' => null]))
            ->assertStatus(422);
    }

    public function test_register_requires_gender(): void
    {
        $this->postJson('/api/auth/register', $this->payload(['gender' => null]))
            ->assertStatus(422)
            ->assertJsonValidationErrors('gender')
            ->assertJsonPath('errors.gender.0', 'اختر الجنس');
    }

    public function test_register_rejects_invalid_gender(): void
    {
        $this->postJson('/api/auth/register', $this->payload(['gender' => 'other']))
            ->assertStatus(422)
            ->assertJsonValidationErrors('gender')
            ->assertJsonPath('errors.gender.0', 'اختر الجنس');
    }

    public function test_register_stores_gender(): void
    {
        $response = $this->postJson('/api/auth/register', $this->payload(['gender' => 'FEMALE']))
            ->assertCreated()
            ->assertJsonPath('user.gender', 'FEMALE');

        $this->assertArrayHasKey('gender', $response->json('user'));
        $this->assertSame('female', User::where('email', 'trainee@example.test')->firstOrFail()->gender);
    }

    public function test_duplicate_dxn_id_422(): void
    {
        User::factory()->create(['dxn_id' => 'DXN-1001']);

        $this->postJson('/api/auth/register', $this->payload(['email' => 'other@example.test']))
            ->assertStatus(422)
            ->assertJsonValidationErrors('dxnId');
    }

    public function test_blank_dxn_id_422(): void
    {
        $this->postJson('/api/auth/register', $this->payload(['dxnId' => '  ']))
            ->assertStatus(422)
            ->assertJsonValidationErrors('dxnId');
    }

    public function test_leader_dxn_id_links_sponsor(): void
    {
        $leader = User::factory()->create(['role' => 'leader', 'dxn_id' => 'LEADER-77']);

        $this->postJson('/api/auth/register', $this->payload(['leaderDxnId' => 'LEADER-77']))
            ->assertCreated()
            ->assertJsonPath('user.sponsorId', (string) $leader->id);

        $this->assertSame($leader->id, User::where('email', 'trainee@example.test')->firstOrFail()->sponsor_id);
    }

    public function test_leader_dxn_id_of_trainee_rejected(): void
    {
        User::factory()->create(['role' => 'trainee', 'dxn_id' => 'TRAINEE-1']);

        $this->postJson('/api/auth/register', $this->payload(['leaderDxnId' => 'TRAINEE-1']))
            ->assertStatus(422)
            ->assertJsonValidationErrors('leaderDxnId');
    }

    public function test_register_cannot_set_role(): void
    {
        $this->postJson('/api/auth/register', $this->payload(['role' => 'admin']))
            ->assertCreated()
            ->assertJsonPath('user.role', 'TRAINEE');

        $this->assertSame('trainee', User::where('email', 'trainee@example.test')->firstOrFail()->role);
    }
}
