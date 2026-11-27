<?php

namespace App\Support;

use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

/** Forma pública del domicilio de un usuario (tabla `domicilios`, 1:1). */
class PresentadorDomicilio
{
    /** Domicilio del usuario o null si no tiene. */
    public static function deUsuario(int $usuarioId): ?array
    {
        $fila = DB::table('domicilios')->where('usuario_id', $usuarioId)->first();

        return $fila ? self::datos($fila) : null;
    }

    public static function datos(object $d): array
    {
        return [
            'calle' => $d->calle,
            'numero' => $d->numero,
            'colonia' => $d->colonia,
            'codigo_postal' => $d->codigo_postal,
            'referencia' => $d->referencia,
            'latitud' => (float) $d->latitud,
            'longitud' => (float) $d->longitud,
            'texto' => self::texto($d),
            'updated_at' => Normalizar::fecha($d->updated_at),
        ];
    }

    /** "Calle Número, Colonia, CP" (la app lo usa en `ubicacion_nombre`). */
    public static function texto(object $d): string
    {
        return "{$d->calle} {$d->numero}, {$d->colonia}, {$d->codigo_postal}";
    }

    /**
     * Domicilios en lote (una consulta), por `usuario_id`.
     *
     * @param  array<int>  $ids
     * @return Collection<int, array>
     */
    public static function porUsuarios(array $ids): Collection
    {
        if ($ids === []) {
            return collect();
        }

        return DB::table('domicilios')
            ->whereIn('usuario_id', array_unique($ids))
            ->get()
            ->mapWithKeys(fn ($d) => [$d->usuario_id => self::datos($d)]);
    }
}
