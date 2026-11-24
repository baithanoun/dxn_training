<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class MediaController extends Controller
{
    public function store(Request $request): JsonResponse
    {
        if (! config('media.uploads_enabled', true)) {
            return response()->json(['message' => 'رفع الصور غير مفعّل على هذا الخادم'], 503);
        }

        $request->validate(['file' => 'required|image|max:5120']);

        $path = $request->file('file')->store('media', 'public');

        return response()->json(['url' => Storage::disk('public')->url($path)], 201);
    }
}
