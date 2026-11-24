<?php

namespace App\Support;

use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

/**
 * Usuarios de la app vistos desde el panel. Misma forma que
 * frontend/src/services/usuariosService.js:
 *  - resumen: usuario + suspendido, strikes_activos, limite_strikes, estado_cuenta, veces_suspendido
 *  - detalle: resumen + suspension_vigente, solicitudes, strikes, suspensiones
 */
class PresentadorCuenta
{
    /**
     * Resúmenes de una lista de usuarios con 2 consultas en total.
     *
     * @param  Collection<int, object>  $usuarios
     */
    public static function resumenes(Collection $usuarios): array
    {
        if ($usuarios->isEmpty()) {
            return [];
        }

        $ids = $usuarios->pluck('id');

        $strikes = DB::table('strikes')
            ->whereIn('usuario_id', $ids)->where('activo', true)
            ->groupBy('usuario_id')
            ->pluck(DB::raw('COUNT(*)'), 'usuario_id');

        $suspensiones = DB::table('suspensiones')
            ->whereIn('usuario_id', $ids)
            ->groupBy('usuario_id')
            ->get(['usuario_id', DB::raw('COUNT(*) as total'), DB::raw('SUM(fecha_rehabilitacion IS NULL) as vigentes')])
            ->keyBy('usuario_id');

        return $usuarios->map(function ($u) use ($strikes, $suspensiones) {
            $susp = $suspensiones->get($u->id);
            $suspendido = (int) ($susp->vigentes ?? 0) > 0;

            return PresentadorUsuario::base($u) + [
                'suspendido' => $suspendido,
                'strikes_activos' => (int) ($strikes[$u->id] ?? 0),
                'limite_strikes' => ReglasCuenta::LIMITE_STRIKES,
                'estado_cuenta' => ReglasCuenta::estadoCuenta($u, $suspendido),
                'veces_suspendido' => (int) ($susp->total ?? 0),
            ];
        })->values()->all();
    }

    /** Detalle de un usuario de la app, o null si no existe o es admin. */
    public static function detalle(int $id): ?array
    {
        $usuario = DB::table('usuarios')->where('id', $id)->where('rol', 'usuario')->first();
        if (! $usuario) {
            return null;
        }

        $nombres = fn (Collection $ids) => DB::table('usuarios')
            ->whereIn('id', $ids->filter()->unique())
            ->get(['id', 'nombre', 'apellidos'])
            ->mapWithKeys(fn ($p) => [$p->id => trim("{$p->nombre} {$p->apellidos}")]);

        $strikes = DB::table('strikes')->where('usuario_id', $id)->orderByDesc('created_at')->orderByDesc('id')->get();
        $suspensiones = DB::table('suspensiones')->where('usuario_id', $id)->orderByDesc('fecha_suspension')->orderByDesc('id')->get();
        $admins = $nombres($strikes->pluck('admin_id')
            ->merge($strikes->pluck('anulado_por_id'))
            ->merge($suspensiones->pluck('suspendido_por_id'))
            ->merge($suspensiones->pluck('rehabilitado_por_id')));

        $solicitudes = DB::table('solicitudes')->where('usuario_id', $id)->orderByDesc('created_at')->orderByDesc('id')->get(['id', 'titulo', 'estado', 'created_at']);
        $servicios = DB::table('solicitud_servicio')->whereIn('solicitud_id', $solicitudes->pluck('id'))->orderBy('servicio_id')->get()->groupBy('solicitud_id');

        $suspension = fn ($s) => [
            'id' => $s->id,
            'usuario_id' => $s->usuario_id,
            'suspendido_por_id' => $s->suspendido_por_id,
            'suspendido_por_nombre' => $s->suspendido_por_id ? ($admins[$s->suspendido_por_id] ?? null) : null,
            'motivo' => $s->motivo,
            'fecha_suspension' => Normalizar::fecha($s->fecha_suspension),
            'rehabilitado_por_id' => $s->rehabilitado_por_id,
            'rehabilitado_por_nombre' => $s->rehabilitado_por_id ? ($admins[$s->rehabilitado_por_id] ?? null) : null,
            'fecha_rehabilitacion' => Normalizar::fecha($s->fecha_rehabilitacion),
        ];

        $vigente = $suspensiones->firstWhere('fecha_rehabilitacion', null);

        return self::resumenes(collect([$usuario]))[0] + [
            'suspension_vigente' => $vigente ? $suspension($vigente) : null,
            'solicitudes' => $solicitudes->map(fn ($s) => [
                'id' => $s->id,
                'titulo' => $s->titulo,
                'estado' => $s->estado,
                'servicios' => ($servicios->get($s->id) ?? collect())->pluck('servicio_id')->map(fn ($v) => (int) $v)->values()->all(),
                'created_at' => Normalizar::fecha($s->created_at),
            ])->values()->all(),
            'strikes' => $strikes->map(fn ($s) => [
                'id' => $s->id,
                'usuario_id' => $s->usuario_id,
                'admin_id' => $s->admin_id,
                'admin_nombre' => $admins[$s->admin_id] ?? null,
                'solicitud_id' => $s->solicitud_id,
                'motivo' => $s->motivo,
                'activo' => (bool) $s->activo,
                'anulado_por_id' => $s->anulado_por_id,
                'anulado_por_nombre' => $s->anulado_por_id ? ($admins[$s->anulado_por_id] ?? null) : null,
                'motivo_anulacion' => $s->motivo_anulacion,
                'anulado_en' => Normalizar::fecha($s->anulado_en),
                'created_at' => Normalizar::fecha($s->created_at),
            ])->values()->all(),
            'suspensiones' => $suspensiones->map($suspension)->values()->all(),
        ];
    }
}
