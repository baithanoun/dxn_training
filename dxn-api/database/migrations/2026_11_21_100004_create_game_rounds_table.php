<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('game_rounds', function (Blueprint $table) {
            $table->id();
            $table->foreignId('level_id')->constrained('levels')->cascadeOnDelete();
            $table->string('title')->nullable();
            $table->text('prompt');
            $table->tinyInteger('initial_trust')->default(50);
            $table->unsignedInteger('order_index');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('game_rounds');
    }
};
