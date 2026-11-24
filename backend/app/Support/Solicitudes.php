<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Operaciones de datos de solicitudes compartidas por la app y el panel.
 */
class Solicitudes
{
    public const ESTADOS = ['Pendiente', 'Enviado', 'Rechazado', 'Cancelado', 'Terminado'];

    /**
     * Fila de la solicitud bloqueada para un cambio (SELECT … FOR UPDATE).
     * Usar dentro de DB::transaction: un segundo admin espera y luego ve el
     * estado ya cambiado (plan 7.4). Con `$usuarioId`, solo si es suya; si no, 404.
     */
    public static function bloquear(int $id, ?int $usuarioId = null): object
    {
        $consulta = DB::table('solicitudes')->where('id', $id)->lockForUpdate();

        if ($usuarioId !== null) {
            $consulta->where('usuario_id', $usuarioId);
        }

        return $consulta->first() ?? abort(404);
    }

    /** @return int[] */
    public static function servicios(int $id): array
    {
        return DB::table('solicitud_servicio')->where('solicitud_id', $id)->pluck('servicio_id')->map(fn ($v) => (int) $v)->all();
    }

    public static function reemplazarServicios(int $id, array $servicios): void
    {
        DB::table('solicitud_servicio')->where('solicitud_id', $id)->delete();

        DB::table('solicitud_servicio')->insert(
            array_map(fn ($s) => ['solicitud_id' => $id, 'servicio_id' => (int) $s], array_values(array_unique($servicios)))
        );
    }

    /** Reglas de validación para la lista de servicios (ids de servicios activos, sin repetir). */
    public static function reglasServicios(string $requerido = 'sometimes'): array
    {
        return [
            'servicios' => [$requerido, 'array'],
            'servicios.*' => ['integer', 'distinct', Rule::exists('servicios', 'id')->where('activo', true)],
        ];
    }

    /** 403 si la cuenta está suspendida (plan 8: no puede pedir ni agregar servicios). */
    public static function exigirNoSuspendido(int $usuarioId): void
    {
        if (ReglasCuenta::estaSuspendido($usuarioId)) {
            abort(403, 'Tu cuenta está suspendida: solo puedes avisar a tus contactos.');
        }
    }
}
