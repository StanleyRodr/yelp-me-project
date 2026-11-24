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

    /** Acciones del admin por estado de cuenta. Igual a frontend/src/constantes/cuentas.js. */
    private const ACCIONES = [
        'Activo' => ['reportar', 'suspender', 'deshabilitar'],
        'Suspendido' => ['rehabilitar', 'reportar', 'deshabilitar'],
        'Deshabilitado' => ['reactivar'],
    ];

    private const VERBOS = [
        'reportar' => 'reportar',
        'suspender' => 'suspender',
        'rehabilitar' => 'rehabilitar',
        'deshabilitar' => 'deshabilitar',
        'reactivar' => 'reactivar',
    ];

    /** Deshabilitado > Suspendido > Activo. `$suspendido` evita una consulta si ya se conoce. */
    public static function estadoCuenta(object $usuario, ?bool $suspendido = null): string
    {
        if (! $usuario->activo) {
            return 'Deshabilitado';
        }

        return ($suspendido ?? self::estaSuspendido($usuario->id)) ? 'Suspendido' : 'Activo';
    }

    /** 409 si la acción no corresponde al estado actual de la cuenta. */
    public static function exigirAccion(object $usuario, string $accion): void
    {
        $estado = self::estadoCuenta($usuario);

        if (! in_array($accion, self::ACCIONES[$estado], true)) {
            abort(409, "La cuenta está {$estado}: no se puede ".self::VERBOS[$accion].'. Recarga para ver su estado actual.');
        }
    }

    /** Cierra la suspensión vigente y reinicia a 0 los strikes activos (no los anula). */
    public static function rehabilitar(int $usuarioId, int $adminId): void
    {
        DB::table('suspensiones')
            ->where('usuario_id', $usuarioId)
            ->whereNull('fecha_rehabilitacion')
            ->update(['rehabilitado_por_id' => $adminId, 'fecha_rehabilitacion' => now()]);

        DB::table('strikes')
            ->where('usuario_id', $usuarioId)
            ->where('activo', true)
            ->update(['activo' => false]);
    }

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
