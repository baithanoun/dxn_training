<?php

namespace Tests\Feature\Admin;

use App\Models\Section;
use App\Models\User;
use App\Models\UserLevelProgress;
use Carbon\Carbon;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class UsersTest extends TestCase
{
    use RefreshDatabase;

    protected function tearDown(): void
    {
        Carbon::setTestNow();
        parent::tearDown();
    }

    private function admin(): User
    {
        return User::factory()->create(['role' => 'admin']);
    }

    public function test_list_paginates_and_filters_by_role_and_search(): void
    {
        $admin = $this->admin();
        User::factory()->create(['role' => 'leader', 'name' => 'Boss']);
        foreach (['SearchableA', 'SearchableB', 'SearchableC'] as $name) {
            User::factory()->create(['role' => 'trainee', 'name' => $name]);
        }

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users?role=TRAINEE&perPage=2&page=1')
            ->assertOk()
            ->assertJsonPath('meta.page', 1)
            ->assertJsonPath('meta.perPage', 2)
            ->assertJsonPath('meta.total', 3)
            ->assertJsonPath('meta.lastPage', 2)
            ->assertJsonCount(2, 'data');

        $this->getJson('/api/admin/users?role=TRAINEE&perPage=2&page=2')
            ->assertOk()
            ->assertJsonPath('meta.page', 2)
            ->assertJsonCount(1, 'data');

        $this->getJson('/api/admin/users?role=TRAINEE&search=SearchableB')
            ->assertOk()
            ->assertJsonPath('meta.total', 1)
            ->assertJsonPath('data.0.fullName', 'SearchableB')
            ->assertJsonPath('data.0.role', 'TRAINEE');
    }

    public function test_search_by_exact_dxn_id(): void
    {
        $admin = $this->admin();
        $member = User::factory()->create(['role' => 'trainee', 'name' => 'By number', 'dxn_id' => 'DXN-ABC-123']);

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users?search=DXN-ABC-123')
            ->assertOk()
            ->assertJsonPath('meta.total', 1)
            ->assertJsonPath('data.0.id', (string) $member->id)
            ->assertJsonPath('data.0.dxnId', 'DXN-ABC-123');
    }

    public function test_status_filter_inactive_uses_3_days(): void
    {
        Carbon::setTestNow(Carbon::parse('2026-11-22 12:00:00'));
        $admin = $this->admin();
        User::factory()->create(['role' => 'trainee', 'name' => 'Recent', 'last_activity_date' => '2026-11-22']);
        User::factory()->create(['role' => 'trainee', 'name' => 'Boundary', 'last_activity_date' => '2026-11-19']);
        User::factory()->create(['role' => 'trainee', 'name' => 'Old', 'last_activity_date' => '2026-11-18']);
        User::factory()->create(['role' => 'trainee', 'name' => 'Never', 'last_activity_date' => null]);

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users?role=TRAINEE&status=inactive')
            ->assertOk()->assertJsonPath('meta.total', 2)
            ->assertJsonPath('data.0.fullName', 'Never')
            ->assertJsonPath('data.1.fullName', 'Old');

        $this->getJson('/api/admin/users?role=TRAINEE&status=active')
            ->assertOk()->assertJsonPath('meta.total', 2);
    }

    public function test_create_user_validates_unique_email_and_phone(): void
    {
        $admin = $this->admin();
        User::factory()->create(['email' => 'taken@example.test', 'phone' => '+970500000000']);
        $base = ['fullName' => 'New user', 'password' => 'password123', 'role' => 'trainee'];

        $this->actingAs($admin, 'sanctum')->postJson('/api/admin/users', $base + ['email' => 'taken@example.test'])
            ->assertStatus(422)->assertJsonValidationErrors('email');
        $this->postJson('/api/admin/users', $base + ['phone' => '+970500000000'])
            ->assertStatus(422)->assertJsonValidationErrors('phone');
    }

    public function test_admin_can_filter_by_gender(): void
    {
        $admin = $this->admin();
        User::factory()->create(['gender' => 'female', 'name' => 'Female']);
        User::factory()->create(['gender' => 'male', 'name' => 'Male']);
        User::factory()->create(['gender' => null, 'name' => 'Unknown']);

        $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users?gender=FEMALE')
            ->assertOk()
            ->assertJsonPath('meta.total', 1)
            ->assertJsonPath('data.0.fullName', 'Female')
            ->assertJsonPath('data.0.gender', 'FEMALE');
    }

    public function test_admin_can_set_gender(): void
    {
        $admin = $this->admin();

        $created = $this->actingAs($admin, 'sanctum')->postJson('/api/admin/users', [
            'fullName' => 'Gendered user',
            'email' => 'gendered@example.test',
            'password' => 'password123',
            'role' => 'trainee',
            'gender' => 'female',
        ])->assertCreated()->assertJsonPath('gender', 'FEMALE');

        $id = $created->json('id');
        $this->assertSame('female', User::findOrFail($id)->gender);

        $this->putJson("/api/admin/users/{$id}", ['gender' => 'male'])
            ->assertOk()->assertJsonPath('gender', 'MALE');
        $this->assertSame('male', User::findOrFail($id)->gender);
    }

    public function test_admin_rejects_invalid_gender(): void
    {
        $this->actingAs($this->admin(), 'sanctum')->postJson('/api/admin/users', [
            'fullName' => 'Invalid gender',
            'email' => 'invalid-gender@example.test',
            'password' => 'password123',
            'role' => 'trainee',
            'gender' => 'other',
        ])->assertStatus(422)->assertJsonValidationErrors('gender');
    }

    public function test_existing_user_without_gender_serializes_null(): void
    {
        $user = User::factory()->create(['password' => 'secret123']);
        $this->assertNull($user->gender);

        $mobileUser = $this->postJson('/api/auth/login', [
            'login' => $user->email,
            'password' => 'secret123',
        ])->assertOk()->json('user');
        $this->assertArrayHasKey('gender', $mobileUser);
        $this->assertNull($mobileUser['gender']);

        $adminUser = $this->actingAs($this->admin(), 'sanctum')
            ->getJson("/api/admin/users/{$user->id}")->assertOk()->json();
        $this->assertArrayHasKey('gender', $adminUser);
        $this->assertNull($adminUser['gender']);
    }

    public function test_leader_must_have_leader_role(): void
    {
        $admin = $this->admin();
        $trainee = User::factory()->create(['role' => 'trainee']);

        $this->actingAs($admin, 'sanctum')->postJson('/api/admin/users', [
            'fullName' => 'New user', 'email' => 'new@example.test', 'password' => 'password123',
            'role' => 'trainee', 'leaderId' => (string) $trainee->id,
        ])->assertStatus(422)->assertJsonValidationErrors('leaderId');
    }

    public function test_deactivate_revokes_tokens(): void
    {
        $admin = $this->admin();
        $member = User::factory()->create(['role' => 'trainee']);
        $token = $member->createToken('mobile')->plainTextToken;

        $this->actingAs($admin, 'sanctum')->putJson("/api/admin/users/{$member->id}", ['isActive' => false])
            ->assertOk()->assertJsonPath('isActive', false);
        $this->assertFalse((bool) $member->fresh()->is_active);

        app('auth')->forgetGuards();
        $this->getJson('/api/auth/me', ['Authorization' => "Bearer $token"])->assertStatus(401);
    }

    public function test_user_detail_returns_per_section_progress(): void
    {
        $this->seed(ContentSeeder::class);
        $admin = $this->admin();
        $member = User::factory()->create(['role' => 'trainee']);

        $section = Section::where('status', 'published')->orderBy('order_index')->firstOrFail();
        $levels = $section->skills()
            ->where('status', 'published')
            ->orderBy('order_index')
            ->with(['levels' => fn ($q) => $q->where('status', 'published')->orderBy('order_index')])
            ->get()
            ->flatMap(fn ($skill) => $skill->levels);

        $this->assertGreaterThan(0, $levels->count());

        UserLevelProgress::create([
            'user_id' => $member->id,
            'level_id' => $levels->first()->id,
            'status' => 'completed',
            'score' => 100,
            'best_score' => 100,
            'attempts' => 1,
        ]);

        $response = $this->actingAs($admin, 'sanctum')
            ->getJson("/api/admin/users/{$member->id}")
            ->assertOk()
            ->assertJsonCount(Section::where('status', 'published')->count(), 'sectionsProgress');

        $row = collect($response->json('sectionsProgress'))->firstWhere('sectionId', (string) $section->id);
        $this->assertNotNull($row);
        $this->assertSame($section->title, $row['sectionTitle']);
        $this->assertSame(1, $row['completedLevels']);
        $this->assertSame($levels->count(), $row['totalLevels']);
        $this->assertSame((int) round(1 / $levels->count() * 100), $row['progressPercent']);
    }

    public function test_admin_cannot_demote_self(): void
    {
        $admin = $this->admin();

        $this->actingAs($admin, 'sanctum')->putJson("/api/admin/users/{$admin->id}", ['role' => 'trainee'])
            ->assertStatus(422)->assertJsonValidationErrors('role');
        $this->assertSame('admin', $admin->fresh()->role);
    }

    public function test_export_csv_has_bom_and_header(): void
    {
        $admin = $this->admin();
        User::factory()->create(['role' => 'trainee', 'name' => 'Plain User']);

        $response = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users/export')->assertOk();
        $csv = $response->getContent();

        $this->assertSame("\xEF\xBB\xBF", substr($csv, 0, 3));
        $this->assertStringContainsString('text/csv', $response->headers->get('Content-Type'));
        $lines = preg_split('/\R/u', substr($csv, 3));
        $this->assertSame([
            'الاسم', 'البريد', 'الهاتف', 'رقم العضوية', 'الدور', 'القائد', 'XP', 'آخر نشاط', 'الحالة',
        ], str_getcsv($lines[0]));
        $this->assertStringContainsString('Plain User', $csv);
    }

    public function test_export_escapes_commas_and_formulas(): void
    {
        $admin = $this->admin();
        User::factory()->create(['role' => 'trainee', 'name' => '=HYPERLINK("x")']);
        User::factory()->create(['role' => 'trainee', 'name' => 'علي, محمد']);

        $csv = $this->actingAs($admin, 'sanctum')->getJson('/api/admin/users/export')
            ->assertOk()->getContent();
        $rows = array_map('str_getcsv', array_filter(preg_split('/\R/u', substr($csv, 3))));
        $names = array_column($rows, 0);

        $this->assertContains("'=HYPERLINK(\"x\")", $names);
        $this->assertContains('علي, محمد', $names);
    }

    public function test_non_admin_forbidden(): void
    {
        $this->getJson('/api/admin/users')->assertStatus(401);
        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->getJson('/api/admin/users')->assertStatus(403);
    }
}
