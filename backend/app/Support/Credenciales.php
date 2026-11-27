<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

/**
 * Verificación de credenciales compartida por el login de la app y del panel.
 */
class Credenciales
{
    /** Contraseña nueva (registro y recuperación). Espera `password_confirmation`. */
    public const REGLAS_PASSWORD = ['required', 'string', 'min:8', 'max:72', 'confirmed'];

    public const MENSAJE_INCORRECTAS = 'Correo, teléfono o contraseña incorrectos.';

    /** Hash bcrypt (cost 12) de un valor aleatorio descartado: nunca coincide con nada. */
    private const HASH_FICTICIO = '$2y$12$dXr4RMCR8Knh4BS1NAE/dexr1TJ9RfXteKG9Dw.FDMakpnI..UnuW';

    /**
     * Devuelve la fila de `usuarios` si la contraseña coincide; si no, 401.
     * Mismo mensaje y tiempo similar exista o no la cuenta: no revela qué
     * correos o teléfonos están registrados.
     *
     * @param  array{correo?: string|null, telefono?: string|null}  $identificador
     */
    public static function verificar(array $identificador, string $password): object
    {
        $consulta = DB::table('usuarios');

        if (! empty($identificador['correo'])) {
            $consulta->where('correo', $identificador['correo']);
        } else {
            $consulta->where('telefono', $identificador['telefono'] ?? '');
        }

        $usuario = $consulta->first();

        // Sin cuenta se compara contra un hash ficticio para igualar el tiempo.
        $hash = $usuario->password ?? self::HASH_FICTICIO;

        if (! Hash::check($password, $hash) || ! $usuario) {
            abort(401, self::MENSAJE_INCORRECTAS);
        }

        return $usuario;
    }

    /** 403 si la cuenta no tiene el rol esperado o está deshabilitada. */
    public static function exigirAcceso(object $usuario, string $rol, string $mensajeRol): void
    {
        if ($usuario->rol !== $rol) {
            abort(403, $mensajeRol);
        }

        if (! $usuario->activo) {
            abort(403, 'Esta cuenta está deshabilitada.');
        }
    }
}
