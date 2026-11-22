<?php

namespace Tests\Feature\Mobile;

use App\Models\AnalyticsEvent;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class EventsTest extends TestCase
{
    use RefreshDatabase;

    private function user(): User
    {
        return User::factory()->create(['role' => 'trainee']);
    }

    public function test_accepts_allowed_events_202(): void
    {
        $user = $this->user();

        $this->actingAs($user, 'sanctum')->postJson('/api/events', ['events' => [
            ['eventName' => 'app_opened'],
            ['eventName' => 'question_answered', 'payload' => ['roundId' => 1, 'correct' => true]],
        ]])->assertStatus(202)->assertJsonPath('accepted', 2);

        $this->assertSame(2, AnalyticsEvent::where('user_id', $user->id)->count());
        $this->assertSame(1, AnalyticsEvent::where('user_id', $user->id)->where('event_name', 'app_opened')->count());
        $this->assertSame(
            ['roundId' => 1, 'correct' => true],
            AnalyticsEvent::where('event_name', 'question_answered')->firstOrFail()->payload
        );
    }

    public function test_rejects_unknown_event_422(): void
    {
        $this->actingAs($this->user(), 'sanctum')->postJson('/api/events', ['events' => [
            ['eventName' => 'not_an_event'],
        ]])->assertStatus(422)->assertJsonValidationErrors('events.0.eventName');
    }

    public function test_rejects_more_than_100(): void
    {
        $events = array_fill(0, 101, ['eventName' => 'app_opened']);

        $this->actingAs($this->user(), 'sanctum')->postJson('/api/events', ['events' => $events])
            ->assertStatus(422)->assertJsonValidationErrors('events');
    }

    public function test_client_cannot_send_level_completed(): void
    {
        $this->actingAs($this->user(), 'sanctum')->postJson('/api/events', ['events' => [
            ['eventName' => 'level_completed'],
        ]])->assertStatus(422)->assertJsonValidationErrors('events.0.eventName');
    }
}
