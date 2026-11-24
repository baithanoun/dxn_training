<?php

namespace Tests\Feature\Admin;

use App\Models\Level;
use App\Models\User;
use Database\Seeders\ContentSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

class MediaTest extends TestCase
{
    use RefreshDatabase;

    private function admin(): User
    {
        return User::factory()->create(['role' => 'admin']);
    }

    public function test_uploads_image_returns_url(): void
    {
        Storage::fake('public');

        $response = $this->actingAs($this->admin(), 'sanctum')
            ->postJson('/api/admin/media', ['file' => UploadedFile::fake()->image('a.jpg')])
            ->assertCreated()
            ->assertJsonStructure(['url']);

        // Storage::fake yields a relative /storage URL; production APP_URL makes it absolute.
        $url = $response->json('url');
        $this->assertStringContainsString('/storage/media/', $url);
        $path = substr($url, strpos($url, '/storage/') + strlen('/storage/'));
        Storage::disk('public')->assertExists($path);
    }

    public function test_rejects_non_image(): void
    {
        Storage::fake('public');

        $this->actingAs($this->admin(), 'sanctum')
            ->postJson('/api/admin/media', ['file' => UploadedFile::fake()->create('doc.pdf', 10, 'application/pdf')])
            ->assertStatus(422)
            ->assertJsonValidationErrors('file');
    }

    public function test_rejects_over_5mb(): void
    {
        Storage::fake('public');

        $this->actingAs($this->admin(), 'sanctum')
            ->postJson('/api/admin/media', ['file' => UploadedFile::fake()->image('a.jpg')->size(6000)])
            ->assertStatus(422)
            ->assertJsonValidationErrors('file');
    }

    public function test_replacing_cards_deletes_unreferenced_media_file(): void
    {
        Storage::fake('public');
        $this->seed(ContentSeeder::class);
        $admin = $this->admin();
        $level = Level::firstOrFail();

        Storage::disk('public')->put('media/old.jpg', 'x');
        $url = Storage::disk('public')->url('media/old.jpg');
        $level->cards()->create(['title' => 'Old', 'body' => 'body', 'media_url' => $url, 'order_index' => 1]);

        $this->actingAs($admin, 'sanctum')
            ->putJson("/api/admin/levels/{$level->id}/cards", [
                'cards' => [['title' => 'New', 'body' => 'new body']],
            ])->assertOk();

        Storage::disk('public')->assertMissing('media/old.jpg');
    }

    public function test_kept_card_media_is_not_deleted(): void
    {
        Storage::fake('public');
        $this->seed(ContentSeeder::class);
        $admin = $this->admin();
        $level = Level::firstOrFail();

        Storage::disk('public')->put('media/keep.jpg', 'x');
        $url = Storage::disk('public')->url('media/keep.jpg');
        $level->cards()->create(['title' => 'Keep', 'body' => 'body', 'media_url' => $url, 'order_index' => 1]);

        $this->actingAs($admin, 'sanctum')
            ->putJson("/api/admin/levels/{$level->id}/cards", [
                'cards' => [['title' => 'Keep', 'body' => 'body', 'mediaUrl' => $url]],
            ])->assertOk();

        Storage::disk('public')->assertExists('media/keep.jpg');
    }

    public function test_media_shared_with_another_card_is_not_deleted(): void
    {
        Storage::fake('public');
        $this->seed(ContentSeeder::class);
        $admin = $this->admin();
        $level = Level::firstOrFail();
        $otherLevel = Level::where('id', '!=', $level->id)->firstOrFail();

        Storage::disk('public')->put('media/shared.jpg', 'x');
        $url = Storage::disk('public')->url('media/shared.jpg');
        $level->cards()->create(['title' => 'First', 'body' => 'body', 'media_url' => $url, 'order_index' => 1]);
        $otherLevel->cards()->create(['title' => 'Second', 'body' => 'body', 'media_url' => $url, 'order_index' => 1]);

        $this->actingAs($admin, 'sanctum')
            ->putJson("/api/admin/levels/{$level->id}/cards", [
                'cards' => [['title' => 'Replaced', 'body' => 'body']],
            ])->assertOk();

        Storage::disk('public')->assertExists('media/shared.jpg');
    }

    public function test_media_upload_disabled_returns_503(): void
    {
        config(['media.uploads_enabled' => false]);

        $this->actingAs($this->admin(), 'sanctum')
            ->postJson('/api/admin/media', ['file' => UploadedFile::fake()->image('a.jpg')])
            ->assertStatus(503)
            ->assertJsonPath('message', 'رفع الصور غير مفعّل على هذا الخادم');
    }

    public function test_requires_admin(): void
    {
        Storage::fake('public');

        $this->postJson('/api/admin/media', ['file' => UploadedFile::fake()->image('a.jpg')])
            ->assertStatus(401);
        $this->actingAs(User::factory()->create(['role' => 'trainee']), 'sanctum')
            ->postJson('/api/admin/media', ['file' => UploadedFile::fake()->image('a.jpg')])
            ->assertStatus(403);
    }
}
