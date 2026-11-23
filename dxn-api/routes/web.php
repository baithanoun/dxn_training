<?php

use App\Http\Controllers\AdminSpaController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

// Invokable controller instead of a closure so `php artisan route:cache` works in the image.
Route::get('/admin/{any?}', AdminSpaController::class)->where('any', '.*');
