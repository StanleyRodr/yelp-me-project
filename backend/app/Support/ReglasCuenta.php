<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;

/**
 * Reglas de cuenta del plan principal (sección 8), consultadas con DB::table.
 * Las reutilizan autenticación y, en planes siguientes, la gestión de usuarios.
 */
class ReglasCuenta
{
    public const LIMITE_STRIKES = 3;

    public static function strikesActivos(int $usuarioId): int
    {
        return DB::table('strikes')
            ->where('usuario_id', $usuarioId)
            ->where('activo', true)
            ->count();
    }

    /** Suspensión vigente = sin fecha de rehabilitación. */
    public static function estaSuspendido(int $usuarioId): bool
    {
        return DB::table('suspensiones')
            ->where('usuario_id', $usuarioId)
            ->whereNull('fecha_rehabilitacion')
            ->exists();
    }
}
