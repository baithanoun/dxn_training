<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('round_options', function (Blueprint $table) {
            $table->id();
            $table->foreignId('round_id')->constrained('game_rounds')->cascadeOnDelete();
            $table->text('text');
            $table->boolean('is_correct');
            $table->boolean('is_neutral')->default(false);
            $table->smallInteger('trust_impact')->default(0);
            $table->text('feedback')->nullable();
            $table->unsignedInteger('xp_bonus')->default(0);
            $table->unsignedInteger('order_index');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('round_options');
    }
};
