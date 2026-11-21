<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;

class AdminSeeder extends Seeder
{
    public function run(): void
    {
        User::unguarded(fn () => User::updateOrCreate(
            ['email' => env('ADMIN_EMAIL', 'admin@dxn.local')],
            ['name' => 'Admin', 'role' => 'admin', 'password' => env('ADMIN_PASSWORD'), 'is_active' => true],
        ));
    }
}
