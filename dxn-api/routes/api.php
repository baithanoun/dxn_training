<?php

use App\Http\Controllers\Admin\ContentTreeController;
use App\Http\Controllers\Admin\LevelController;
use App\Http\Controllers\Admin\ReorderController;
use App\Http\Controllers\Admin\SectionController;
use App\Http\Controllers\Admin\SkillController;
use App\Http\Controllers\AuthController;
use Illuminate\Support\Facades\Route;

Route::post('/auth/login', [AuthController::class, 'login'])->middleware('throttle:5,1');

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/auth/me', [AuthController::class, 'me']);
});

Route::middleware(['auth:sanctum', 'role:admin'])->prefix('admin')->group(function () {
    Route::get('content/tree', ContentTreeController::class);
    Route::apiResource('sections', SectionController::class)->only(['store', 'update', 'destroy']);
    Route::apiResource('skills', SkillController::class)->only(['store', 'update', 'destroy']);
    Route::apiResource('levels', LevelController::class)->only(['show', 'store', 'update', 'destroy']);
    Route::put('levels/{id}/cards', [LevelController::class, 'saveCards']);
    Route::post('reorder', ReorderController::class);
});
