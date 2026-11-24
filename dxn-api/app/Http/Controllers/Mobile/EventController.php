<?php

namespace App\Http\Controllers\Mobile;

use App\Http\Controllers\Controller;
use App\Models\AnalyticsEvent;
use Illuminate\Http\Request;

class EventController extends Controller
{
    private const ALLOWED = ['app_opened', 'level_started', 'question_answered', 'share_achievement', 'certificate_downloaded'];

    public function __invoke(Request $request)
    {
        $data = $request->validate([
            'events' => 'required|array|min:1|max:100',
            'events.*.eventName' => 'required|string|in:' . implode(',', self::ALLOWED),
            'events.*.levelId' => 'nullable|integer|exists:levels,id',
            'events.*.payload' => 'nullable|array',
            'events.*.deviceInfo' => 'nullable|string|max:500',
            'events.*.occurredAt' => 'nullable|date',
        ]);

        $user = $request->user();
        $count = 0;
        foreach ($data['events'] as $e) {
            $payload = $e['payload'] ?? null;
            if (! empty($e['occurredAt'])) {
                $payload = is_array($payload) ? $payload : [];
                $payload['occurredAt'] = $e['occurredAt'];
            }

            AnalyticsEvent::create([
                'user_id' => $user->id,
                'event_name' => $e['eventName'],
                'level_id' => $e['levelId'] ?? null,
                'payload' => $payload,
                'device_info' => $e['deviceInfo'] ?? null,
                'created_at' => now(),
            ]);
            $count++;
        }

        return response()->json(['accepted' => $count], 202);
    }
}