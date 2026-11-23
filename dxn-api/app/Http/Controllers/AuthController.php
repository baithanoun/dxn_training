<?php

namespace App\Http\Controllers;

use App\Http\Requests\RegisterRequest;
use App\Http\Resources\UserResource;
use App\Models\Setting;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    public function register(RegisterRequest $request)
    {
        $data = $request->validated();

        $dxnId = trim($data['dxnId']);
        if ($dxnId === '') {
            return response()->json(['message' => 'رقم العضوية مطلوب', 'errors' => ['dxnId' => ['رقم العضوية مطلوب']]], 422);
        }

        $hash = User::hashDxnId($dxnId);
        if (User::where('dxn_id_hash', $hash)->exists()) {
            return response()->json(['message' => 'رقم العضوية مسجّل مسبقاً', 'errors' => ['dxnId' => ['رقم العضوية مسجّل مسبقاً']]], 422);
        }

        $sponsorId = null;
        if (! empty($data['leaderDxnId'])) {
            $leader = User::where('dxn_id_hash', User::hashDxnId($data['leaderDxnId']))
                ->where('role', 'leader')
                ->first();
            if (! $leader) {
                return response()->json(['message' => 'لم يتم العثور على قائد بهذا الرقم', 'errors' => ['leaderDxnId' => ['لم يتم العثور على قائد بهذا الرقم']]], 422);
            }
            $sponsorId = $leader->id;
        }

        // role is server-owned: forceCreate bypasses the model's fillable list so the
        // in-memory model returned to UserResource matches the DB default (trainee).
        $user = User::forceCreate([
            'name' => $data['fullName'],
            'gender' => $data['gender'],
            'email' => $data['email'] ?? null,
            'phone' => $data['phone'] ?? null,
            'password' => $data['password'],
            'dxn_id' => $dxnId,
            'role' => 'trainee',
            'sponsor_id' => $sponsorId,
        ]);

        $tokenName = $request->header('X-Client') === 'android' ? 'mobile' : 'dashboard';

        return response()->json([
            'token' => $user->createToken($tokenName)->plainTextToken,
            'user' => new UserResource($user),
        ], 201);
    }

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

        $tokenName = $request->header('X-Client') === 'android' ? 'mobile' : 'dashboard';
        $token = $tokenName === 'dashboard'
            ? $user->createToken('dashboard', ['*'], now()->addMinutes((int) Setting::get('session_minutes', Setting::DEFAULTS['session_minutes'])))
            : $user->createToken('mobile');

        return response()->json([
            'token' => $token->plainTextToken,
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
