<?php

namespace App\Support;

/**
 * Transiciones de estado de una solicitud (plan principal 7.2).
 * Misma tabla que frontend/src/constantes/transiciones.js: si cambia una,
 * cambiar la otra.
 */
class TransicionesSolicitud
{
    public const FINALES = ['Rechazado', 'Cancelado', 'Terminado'];

    /** [rol][conServicios 1|0][desde] => destinos permitidos */
    private const TABLA = [
        'admin' => [
            1 => ['Pendiente' => ['Enviado', 'Rechazado'], 'Enviado' => ['Terminado']],
            0 => ['Pendiente' => ['Rechazado']],
        ],
        'usuario' => [
            1 => ['Pendiente' => ['Cancelado'], 'Enviado' => ['Terminado', 'Cancelado']],
            0 => ['Pendiente' => ['Terminado', 'Cancelado']],
        ],
    ];

    /** Destinos que puede asignar `$rol` partiendo de `$desde`. */
    public static function permitidas(string $rol, bool $conServicios, string $desde): array
    {
        return self::TABLA[$rol][(int) $conServicios][$desde] ?? [];
    }

    public static function permite(string $rol, bool $conServicios, string $desde, string $hacia): bool
    {
        return in_array($hacia, self::permitidas($rol, $conServicios, $desde), true);
    }

    /**
     * 409 si la transición no está permitida. Cubre también la concurrencia:
     * si otro admin ya la cambió, el estado actual ya no permite la acción.
     */
    public static function exigir(string $rol, bool $conServicios, string $desde, string $hacia): void
    {
        if (! self::permite($rol, $conServicios, $desde, $hacia)) {
            abort(409, "La solicitud está en {$desde}: no se puede pasar a {$hacia}. Recarga para ver su estado actual.");
        }
    }

    /** El admin corrige servicios solo en solicitudes con servicios, en Pendiente o Enviado. */
    public static function adminPuedeEditarServicios(string $estado, bool $conServicios): bool
    {
        return $conServicios && in_array($estado, ['Pendiente', 'Enviado'], true);
    }
}
