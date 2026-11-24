<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class AccountController extends Controller
{
    public function destroy(Request $request)
    {
        $user = $request->user();

        if ($user->role === 'admin') {
            return response()->json(['message' => 'Admin accounts cannot be deleted this way.'], 422);
        }

        $data = $request->validate(['password' => 'required|string']);

        if (! Hash::check($data['password'], $user->password)) {
            return response()->json(['message' => 'Incorrect password.', 'errors' => ['password' => ['Incorrect password.']]], 422);
        }

        DB::transaction(function () use ($user) {
            $user->tokens()->delete();

            // Remove personal progress and certificates; keep aggregate analytics intact? Plan: delete.
            DB::table('analytics_events')->where('user_id', $user->id)->delete();
            DB::table('user_level_progress')->where('user_id', $user->id)->delete();
            $user->certificates()->delete();

            $user->forceFill([
                'name' => 'مستخدم محذوف',
                'email' => null,
                'phone' => null,
                'dxn_id' => null,
                'dxn_id_hash' => null,
                'gender' => null,
                'password' => Hash::make(bin2hex(random_bytes(16))),
                'is_active' => false,
                'sponsor_id' => null,
                'terms_accepted_at' => null,
            ])->save();
        });

        return response()->noContent();
    }
}
