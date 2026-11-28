<?php

use App\Http\Controllers\AdminSpaController;
use App\Http\Controllers\AppConfigController;
use App\Models\Setting;
use Illuminate\Support\Facades\Route;

// The site root has no page of its own; send visitors to the admin sign-in.
Route::redirect('/', '/admin/login');

Route::get('/privacy', fn () => view('legal.privacy'));
Route::get('/terms', fn () => view('legal.terms'));
Route::get('/account/delete', fn () => view('legal.account-delete'));

Route::get('/join/{dxnId}', function (string $dxnId) {
    if (! preg_match('/^[A-Za-z0-9-]{1,32}$/', $dxnId)) {
        abort(404);
    }

    $value = fn (string $key): mixed => Setting::get($key, Setting::DEFAULTS[$key] ?? null);

    $downloadUrl = $value('app_download_url');
    if (! $downloadUrl) {
        $manifest = public_path(AppConfigController::LATEST_APK_MANIFEST);
        if (is_file($manifest)) {
            $data = json_decode((string) file_get_contents($manifest), true);
            $valid = is_array($data)
                && is_int($data['versionCode'] ?? null)
                && is_string($data['versionName'] ?? null)
                && is_string($data['file'] ?? null)
                && preg_match('/^[A-Za-z0-9._-]+\.apk$/', $data['file'])
                && is_file(public_path('downloads/'.$data['file']));
            if ($valid) {
                $downloadUrl = asset('downloads/'.$data['file']);
            }
        }
    }

    return view('join', [
        'dxnId' => $dxnId,
        'teamName' => (string) $value('team_name'),
        'teamTagline' => (string) $value('team_tagline'),
        'downloadUrl' => $downloadUrl ? (string) $downloadUrl : null,
    ]);
});

// Invokable controller instead of a closure so `php artisan route:cache` works in the image.
Route::get('/admin/{any?}', AdminSpaController::class)->where('any', '.*');
