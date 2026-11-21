<?php

namespace Tests\Feature\Admin;

use App\Models\User;
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
