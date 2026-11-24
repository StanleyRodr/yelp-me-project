<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;

/**
 * Forma pública de un usuario en las respuestas de la API.
 * Nunca incluye password ni fcm_token.
 */
class PresentadorUsuario
{
    public static function porId(int $id): ?array
    {
        $usuario = DB::table('usuarios')->where('id', $id)->first();

        return $usuario ? self::datos($usuario) : null;
    }

    public static function datos(object $u): array
    {
        return [
            'id' => $u->id,
            'nombre' => $u->nombre,
            'apellidos' => $u->apellidos,
            'telefono' => $u->telefono,
            'correo' => $u->correo,
            'rol' => $u->rol,
            'foto_perfil' => $u->foto_perfil ? Storage::disk('perfiles')->url($u->foto_perfil) : null,
            'activo' => (bool) $u->activo,
            'suspendido' => ReglasCuenta::estaSuspendido($u->id),
            'strikes_activos' => ReglasCuenta::strikesActivos($u->id),
            'limite_strikes' => ReglasCuenta::LIMITE_STRIKES,
            'created_at' => Normalizar::fecha($u->created_at),
        ];
    }
}
