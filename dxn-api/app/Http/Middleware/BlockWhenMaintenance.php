<?php

namespace App\Http\Middleware;

use App\Models\Setting;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class BlockWhenMaintenance
{
    public function handle(Request $request, Closure $next): Response
    {
        // Read the DB on every request so an admin toggle takes effect immediately.
        if (Setting::get('maintenance_mode', Setting::DEFAULTS['maintenance_mode'])) {
            return response()->json(['message' => 'التطبيق تحت الصيانة'], 503);
        }

        return $next($request);
    }
}
