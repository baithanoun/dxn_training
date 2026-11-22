<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Http\Resources\Admin\AdminUserResource;
use App\Models\User;
use Illuminate\Http\Request;

class UserController extends Controller
{
    public function index(Request $request)
    {
        $query = User::query()->with('sponsor');

        if ($search = $request->input('search')) {
            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%")
                  ->orWhere('phone', 'like', "%{$search}%");
                $hash = \App\Models\User::hashDxnId($search);
                $q->orWhere('dxn_id_hash', $hash);
            });
        }

        if ($role = $request->input('role')) {
            $query->where('role', strtolower($role));
        }

        if ($status = $request->input('status')) {
            if ($status === 'active') {
                $query->where('last_activity_date', '>=', now()->subDays(3)->toDateString());
            } elseif ($status === 'inactive') {
                $query->where(function ($q) {
                    $q->whereNull('last_activity_date')
                      ->orWhere('last_activity_date', '<', now()->subDays(3)->toDateString());
                })->where('is_active', true);
            } elseif ($status === 'disabled') {
                $query->where('is_active', false);
            }
        }

        if ($leaderId = $request->input('leaderId')) {
            $query->where('sponsor_id', $leaderId);
        }

        $perPage = min((int) $request->input('perPage', 20), 100);
        $page = (int) $request->input('page', 1);

        $paginator = $query->orderBy('id', 'desc')->paginate($perPage, ['*'], 'page', $page);

        return response()->json([
            'data' => AdminUserResource::collection($paginator->items()),
            'meta' => [
                'page' => $paginator->currentPage(),
                'perPage' => $paginator->perPage(),
                'total' => $paginator->total(),
                'lastPage' => $paginator->lastPage(),
            ],
        ]);
    }

    public function show(string $id)
    {
        $user = User::with('sponsor', 'certificates.section')->findOrFail($id);

        $resource = new AdminUserResource($user);
        $data = $resource->resolve();
        $data['certificates'] = $user->certificates->map(fn ($c) => [
            'sectionId' => (string) $c->section_id,
            'sectionTitle' => $c->section?->title,
            'code' => $c->code,
            'issuedAt' => $c->issued_at?->toDateString(),
        ])->values();
        $data['sectionsProgress'] = [];

        return response()->json($data);
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'fullName' => 'required|string|max:255',
            'email' => 'nullable|email|max:255|unique:users,email',
            'phone' => 'nullable|string|max:32|unique:users,phone',
            'password' => 'required|string|min:8',
            'dxnId' => 'nullable|string|max:64',
            'role' => 'required|in:admin,leader,trainee',
            'leaderId' => 'nullable|exists:users,id',
        ]);

        if (empty($data['email']) && empty($data['phone'])) {
            return response()->json(['message' => 'Email or phone required', 'errors' => ['email' => ['Email or phone is required']]], 422);
        }

        if (! empty($data['leaderId'])) {
            $leader = User::find($data['leaderId']);
            if (! $leader || $leader->role !== 'leader') {
                return response()->json(['message' => 'Leader must have leader role', 'errors' => ['leaderId' => ['Must be a leader']]], 422);
            }
        }

        // role is validated server-side; forceCreate is needed because User's fillable list excludes it.
        $user = User::forceCreate([
            'name' => $data['fullName'],
            'email' => $data['email'] ?? null,
            'phone' => $data['phone'] ?? null,
            'password' => $data['password'],
            'dxn_id' => $data['dxnId'] ?? null,
            'role' => $data['role'],
            'sponsor_id' => $data['leaderId'] ?? null,
        ]);

        return response()->json(new AdminUserResource($user->load('sponsor')), 201);
    }

    public function update(Request $request, string $id)
    {
        $user = User::findOrFail($id);
        $current = $request->user();

        $data = $request->validate([
            'fullName' => 'sometimes|string|max:255',
            'email' => 'sometimes|nullable|email|max:255|unique:users,email,' . $id,
            'phone' => 'sometimes|nullable|string|max:32|unique:users,phone,' . $id,
            'password' => 'sometimes|string|min:8',
            'dxnId' => 'sometimes|nullable|string|max:64',
            'role' => 'sometimes|in:admin,leader,trainee',
            'leaderId' => 'sometimes|nullable|exists:users,id',
            'isActive' => 'sometimes|boolean',
        ]);

        if (isset($data['role']) && (string) $current->id === $id && $data['role'] !== $user->role) {
            return response()->json(['message' => 'Cannot change own role', 'errors' => ['role' => ['Cannot change your own role']]], 422);
        }
        if (isset($data['isActive']) && ! $data['isActive'] && (string) $current->id === $id) {
            return response()->json(['message' => 'Cannot deactivate self', 'errors' => ['isActive' => ['Cannot deactivate yourself']]], 422);
        }

        if (array_key_exists('fullName', $data)) $user->name = $data['fullName'];
        if (array_key_exists('email', $data)) $user->email = $data['email'];
        if (array_key_exists('phone', $data)) $user->phone = $data['phone'];
        if (array_key_exists('password', $data)) $user->password = $data['password'];
        if (array_key_exists('dxnId', $data)) $user->dxn_id = $data['dxnId'];
        if (array_key_exists('role', $data)) $user->role = $data['role'];
        if (array_key_exists('leaderId', $data)) $user->sponsor_id = $data['leaderId'];
        if (array_key_exists('isActive', $data)) {
            $user->is_active = $data['isActive'];
            if (! $data['isActive']) {
                $user->tokens()->delete();
            }
        }
        $user->save();

        if (isset($data['leaderId']) && $data['leaderId']) {
            $leader = User::find($data['leaderId']);
            if (! $leader || $leader->role !== 'leader') {
                return response()->json(['message' => 'Leader must have leader role', 'errors' => ['leaderId' => ['Must be a leader']]], 422);
            }
        }

        return response()->json(new AdminUserResource($user->load('sponsor')));
    }

    public function export(Request $request)
    {
        $query = User::query()->with('sponsor');
        // apply same filters as index
        if ($search = $request->input('search')) {
            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('email', 'like', "%{$search}%")
                  ->orWhere('phone', 'like', "%{$search}%");
                $hash = \App\Models\User::hashDxnId($search);
                $q->orWhere('dxn_id_hash', $hash);
            });
        }
        if ($role = $request->input('role')) {
            $query->where('role', strtolower($role));
        }

        $users = $query->orderBy('id', 'desc')->get();

        $handle = fopen('php://temp', 'r+');
        fwrite($handle, "\xEF\xBB\xBF"); // BOM
        // shortcut: static header written literally because fputcsv quotes cells containing spaces,
        // which changes the published header shape; upgrade by changing the published header to single words.
        fwrite($handle, "الاسم,البريد,الهاتف,DXN ID,الدور,القائد,XP,Streak,آخر نشاط\n");
        foreach ($users as $u) {
            fputcsv($handle, [
                $this->csvSafe($u->name),
                $this->csvSafe((string) $u->email),
                $this->csvSafe((string) $u->phone),
                $this->csvSafe((string) $u->dxn_id),
                $this->csvSafe($u->role),
                $this->csvSafe($u->sponsor?->name),
                $this->csvSafe((string) $u->total_xp),
                $this->csvSafe((string) $u->current_streak),
                $this->csvSafe($u->last_activity_date?->toDateString()),
            ]);
        }
        rewind($handle);
        $csv = stream_get_contents($handle);
        fclose($handle);

        return response($csv, 200, [
            'Content-Type' => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="users.csv"',
        ]);
    }

    /** Neutralize spreadsheet formula injection before fputcsv quotes the cell. */
    private function csvSafe(?string $v): string
    {
        $value = $v ?? '';

        return $value !== '' && str_contains('=+-@', $value[0]) ? "'" . $value : $value;
    }
}