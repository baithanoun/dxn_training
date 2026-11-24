<?php

use App\Http\Controllers\AdminSpaController;
use Illuminate\Support\Facades\Route;

Route::get('/privacy', fn () => view('legal.privacy'));
Route::get('/terms', fn () => view('legal.terms'));
Route::get('/account/delete', fn () => view('legal.account-delete'));

// Invokable controller instead of a closure so `php artisan route:cache` works in the image.
Route::get('/admin/{any?}', AdminSpaController::class)->where('any', '.*');
