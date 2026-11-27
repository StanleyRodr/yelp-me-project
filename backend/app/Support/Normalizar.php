<?php

namespace App\Support;

use Illuminate\Support\Carbon;

/**
 * Normalización de datos de entrada y salida compartida por los controladores.
 */
class Normalizar
{
    /** "235 100-0001" → "2351000001". Null si no viene. */
    public static function telefono(mixed $valor): ?string
    {
        return $valor === null || $valor === '' ? null : preg_replace('/\D/', '', (string) $valor);
    }

    /** " Ana@Correo.com " → "ana@correo.com". Null si no viene. */
    public static function correo(mixed $valor): ?string
    {
        return $valor === null || $valor === '' ? null : mb_strtolower(trim((string) $valor));
    }

    /** Texto opcional: recorta espacios; vacío → null. */
    public static function opcional(mixed $valor): ?string
    {
        $texto = $valor === null ? '' : trim((string) $valor);

        return $texto === '' ? null : $texto;
    }

    /** Fecha de MySQL ("2026-11-19 20:47:45") → ISO 8601 con zona. */
    public static function fecha(mixed $valor): ?string
    {
        return $valor ? Carbon::parse($valor)->toIso8601String() : null;
    }
}
