<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Attributes\Hidden;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

#[Fillable(['name', 'gender', 'email', 'phone', 'password', 'dxn_id', 'sponsor_id', 'terms_accepted_at'])]
#[Hidden(['password', 'remember_token'])]
class User extends Authenticatable
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    public function sponsor(): BelongsTo
    {
        return $this->belongsTo(User::class, 'sponsor_id');
    }

    public function certificates(): HasMany
    {
        return $this->hasMany(Certificate::class);
    }

    protected static function booted(): void
    {
        static::saving(function (User $u) {
            // M-5: normalize blank DXN IDs to null before hashing; encrypted empty strings would still be "present".
            if (is_string($u->dxn_id) && trim($u->dxn_id) === '') {
                $u->dxn_id = null;
            }
            if ($u->isDirty('dxn_id')) {
                $u->dxn_id_hash = $u->dxn_id === null ? null : self::hashDxnId($u->dxn_id);
            }
        });
    }

    /** Deterministic lookup hash; normalized so lookups are case/space-insensitive. */
    public static function hashDxnId(string $dxnId): string
    {
        return hash_hmac('sha256', strtoupper(trim($dxnId)), config('app.key'));
    }

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'gender' => 'string',
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
            'dxn_id' => 'encrypted',
            'is_active' => 'boolean',
            'last_activity_date' => 'date',
            'terms_accepted_at' => 'datetime',
        ];
    }
}
