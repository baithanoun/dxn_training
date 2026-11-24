<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('levels', function (Blueprint $table) {
            $table->id();
            $table->foreignId('skill_id')->constrained('skills')->cascadeOnDelete();
            $table->string('title');
            $table->string('type', 32);
            $table->tinyInteger('pass_threshold')->default(80);
            $table->unsignedInteger('xp_reward')->default(40);
            $table->unsignedInteger('order_index');
            $table->boolean('is_final_quiz')->default(false);
            $table->string('status', 16)->default('draft');
            $table->timestamps();
            $table->softDeletes();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('levels');
    }
};
