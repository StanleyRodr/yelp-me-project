<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Sanctum\HasApiTokens;

/**
 * Único modelo Eloquent del proyecto.
 *
 * Existe solo porque Laravel Sanctum lo requiere para emitir y validar
 * tokens (HasApiTokens / createToken). El resto del acceso a datos se hace
 * con DB::table en los controladores.
 */
class User extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'usuarios';

    /**
     * La tabla usuarios no tiene columna remember_token (la API usa tokens).
     */
    protected $rememberTokenName = false;

    /**
     * @var list<string>
     */
    protected $fillable = [
        'nombre',
        'apellidos',
        'telefono',
        'correo',
        'password',
        'rol',
        'foto_perfil',
        'fcm_token',
        'activo',
    ];

    /**
     * @var list<string>
     */
    protected $hidden = [
        'password',
        'fcm_token',
    ];

    /**
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'password' => 'hashed',
            'activo' => 'boolean',
            'deshabilitado_en' => 'datetime',
        ];
    }
}
