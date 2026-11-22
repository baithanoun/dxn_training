<?php

namespace App\Providers;

use App\Models\Setting;
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        RateLimiter::for('completion', fn (Request $request): Limit => Limit::perMinute(
            (int) Setting::get('quiz_rate_limit_per_minute', Setting::DEFAULTS['quiz_rate_limit_per_minute'])
        )->by($request->user()?->id ?: $request->ip()));
    }
}
