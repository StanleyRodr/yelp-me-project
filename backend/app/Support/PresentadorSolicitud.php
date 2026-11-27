<?php

namespace App\Support;

use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

/**
 * Forma pública de una solicitud. Igual a la que usa el frontend:
 * columnas de `solicitudes` + `servicios` (ids) + `usuario`, `admin`, `cerrado_por`.
 * Listados con 3 consultas en total (sin consultas por fila).
 */
class PresentadorSolicitud
{
    public static function porId(int $id): ?array
    {
        $fila = DB::table('solicitudes')->where('id', $id)->first();

        return $fila ? self::lista(collect([$fila]))[0] : null;
    }

    /** @param  Collection<int, object>  $filas */
    public static function lista(Collection $filas): array
    {
        if ($filas->isEmpty()) {
            return [];
        }

        $servicios = DB::table('solicitud_servicio')
            ->whereIn('solicitud_id', $filas->pluck('id'))
            ->orderBy('servicio_id')
            ->get()
            ->groupBy('solicitud_id');

        $idsPersonas = $filas->pluck('usuario_id')
            ->merge($filas->pluck('admin_id'))
            ->merge($filas->pluck('cerrado_por_id'))
            ->filter()->unique();

        $personas = DB::table('usuarios')
            ->whereIn('id', $idsPersonas)
            ->get(['id', 'nombre', 'apellido_paterno', 'apellido_materno', 'telefono'])
            ->keyBy('id');

        $persona = function (?int $id, bool $conTelefono = false) use ($personas) {
            $p = $id ? $personas->get($id) : null;
            if (! $p) {
                return null;
            }
            $datos = ['id' => $p->id, 'nombre' => $p->nombre, 'apellido_paterno' => $p->apellido_paterno, 'apellido_materno' => $p->apellido_materno];

            return $conTelefono ? $datos + ['telefono' => $p->telefono] : $datos;
        };

        return $filas->map(fn ($s) => [
            'id' => $s->id,
            'usuario_id' => $s->usuario_id,
            'titulo' => $s->titulo,
            'descripcion' => $s->descripcion,
            'latitud' => (float) $s->latitud,
            'longitud' => (float) $s->longitud,
            'ubicacion_nombre' => $s->ubicacion_nombre,
            'estado' => $s->estado,
            'admin_id' => $s->admin_id,
            'cerrado_por_id' => $s->cerrado_por_id,
            'motivo' => $s->motivo,
            'enviado_en' => Normalizar::fecha($s->enviado_en),
            'cerrado_en' => Normalizar::fecha($s->cerrado_en),
            'created_at' => Normalizar::fecha($s->created_at),
            'updated_at' => Normalizar::fecha($s->updated_at),
            'servicios' => ($servicios->get($s->id) ?? collect())->pluck('servicio_id')->map(fn ($v) => (int) $v)->values()->all(),
            // Teléfono solo del solicitante (el admin lo llama); de los admins, solo nombre.
            'usuario' => $persona($s->usuario_id, true),
            'admin' => $persona($s->admin_id),
            'cerrado_por' => $persona($s->cerrado_por_id),
        ])->values()->all();
    }
}
