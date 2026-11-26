<?php

use App\Http\Controllers\AdminSpaController;
use Illuminate\Support\Facades\Route;

// The site root has no page of its own; send visitors to the admin sign-in.
Route::redirect('/', '/admin/login');

Route::get('/privacy', fn () => view('legal.privacy'));
Route::get('/terms', fn () => view('legal.terms'));
Route::get('/account/delete', fn () => view('legal.account-delete'));

// Invokable controller instead of a closure so `php artisan route:cache` works in the image.
Route::get('/admin/{any?}', AdminSpaController::class)->where('any', '.*');
