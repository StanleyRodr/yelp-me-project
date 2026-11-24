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

    /** Suspende si no hay una suspensión vigente. `$adminId` null = automática. */
    public static function suspender(int $usuarioId, string $motivo, ?int $adminId = null): void
    {
        if (self::estaSuspendido($usuarioId)) {
            return;
        }

        DB::table('suspensiones')->insert([
            'usuario_id' => $usuarioId,
            'suspendido_por_id' => $adminId,
            'motivo' => $motivo,
            'fecha_suspension' => now(),
        ]);
    }

    /**
     * Registra un strike activo. Al llegar a LIMITE_STRIKES activos, suspende
     * automáticamente. Llamar dentro de la transacción de la acción que lo origina.
     */
    public static function darStrike(int $usuarioId, int $adminId, string $motivo, ?int $solicitudId = null): void
    {
        DB::table('strikes')->insert([
            'usuario_id' => $usuarioId,
            'admin_id' => $adminId,
            'solicitud_id' => $solicitudId,
            'motivo' => $motivo,
            'activo' => true,
            'created_at' => now(),
        ]);

        if (self::strikesActivos($usuarioId) >= self::LIMITE_STRIKES) {
            self::suspender($usuarioId, 'Suspensión automática por '.self::LIMITE_STRIKES.' strikes.');
        }
    }
}
