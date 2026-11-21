<?php

namespace App\Http\Controllers;

use App\Http\Resources\UserResource;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    public function login(Request $request)
    {
        $data = $request->validate(['login' => 'required|string', 'password' => 'required|string']);

        $user = User::where('email', $data['login'])->orWhere('phone', $data['login'])->first();

        if (! $user || ! Hash::check($data['password'], $user->password)) {
            return response()->json(['message' => 'Invalid credentials'], 401);
        }
        if (! $user->is_active) {
            return response()->json(['message' => 'Account is inactive'], 403);
        }

        return response()->json([
            'token' => $user->createToken('dashboard')->plainTextToken,
            'user' => new UserResource($user),
        ]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->noContent();
    }

    public function me(Request $request)
    {
        return new UserResource($request->user());
    }
}
