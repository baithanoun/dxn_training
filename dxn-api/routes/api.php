<?php

use App\Http\Controllers\Admin\ContentTreeController;
use App\Http\Controllers\Admin\LevelController;
use App\Http\Controllers\Admin\MediaController;
use App\Http\Controllers\Admin\ReorderController;
use App\Http\Controllers\Admin\RoundsController;
use App\Http\Controllers\Admin\SectionController;
use App\Http\Controllers\Admin\SkillController;
use App\Http\Controllers\Admin\TeamController;
use App\Http\Controllers\Admin\UserController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\Mobile\TreeController;
use App\Http\Controllers\Mobile\ProgressController;
use App\Http\Controllers\Mobile\CompletionController;
use App\Http\Controllers\Mobile\LeaderTeamController;
use App\Http\Controllers\Mobile\EventController;
use Illuminate\Support\Facades\Route;

Route::post('/auth/register', [AuthController::class, 'register'])->middleware('throttle:5,1');
Route::post('/auth/login', [AuthController::class, 'login'])->middleware('throttle:5,1');

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/auth/me', [AuthController::class, 'me']);

    // Mobile endpoints
    Route::get('/sections/tree', TreeController::class);
    Route::get('/users/{userId}/progress', [ProgressController::class, 'progress']);
    Route::get('/users/{userId}/sections-progress', [ProgressController::class, 'sectionsProgress']);
    Route::get('/users/{userId}/profile', [ProgressController::class, 'profile']);
    Route::post('/levels/{levelId}/complete', CompletionController::class)->middleware('throttle:30,1');
    Route::get('/leader/team', LeaderTeamController::class);
    Route::post('/events', EventController::class)->middleware('throttle:60,1');
});

Route::middleware(['auth:sanctum', 'role:admin'])->prefix('admin')->group(function () {
    Route::get('users/export', [UserController::class, 'export']);
    Route::apiResource('users', UserController::class)->only(['index', 'show', 'store', 'update']);
    Route::get('teams', [TeamController::class, 'index']);
    Route::get('teams/{leaderId}', [TeamController::class, 'show']);
    Route::get('content/tree', ContentTreeController::class);
    Route::apiResource('sections', SectionController::class)->only(['store', 'update', 'destroy']);
    Route::apiResource('skills', SkillController::class)->only(['store', 'update', 'destroy']);
    Route::apiResource('levels', LevelController::class)->only(['show', 'store', 'update', 'destroy']);
    Route::put('levels/{id}/cards', [LevelController::class, 'saveCards']);
    Route::put('levels/{id}/rounds', RoundsController::class);
    Route::post('reorder', ReorderController::class);
    Route::post('media', [MediaController::class, 'store']);
});
