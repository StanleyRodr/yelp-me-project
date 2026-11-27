<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Support\Credenciales;
use App\Support\Recuperacion;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

/**
 * Recuperar la contraseña en tres pasos: correo → código → contraseña nueva.
 * Público y para cualquier rol (plan principal 5.1).
 */
class RecuperacionController extends Controller
{
    /** POST /api/recuperacion { correo } — misma respuesta exista o no la cuenta. */
    public function store(Request $request): JsonResponse
    {
        $correo = Recuperacion::correo($request);

        $usuario = DB::table('usuarios')->where('correo', $correo)->where('activo', true)->first(['id', 'correo']);
        if ($usuario) {
            Recuperacion::emitir($usuario);
        }

        return $this->respuesta(null, Recuperacion::MENSAJE_NEUTRO);
    }

    /** PUT /api/recuperacion { correo, codigo, password, password_confirmation } */
    public function update(Request $request): JsonResponse
    {
        $correo = Recuperacion::correo($request);
        $datos = $request->validate([
            'codigo' => ['required', 'string'],
            'password' => Credenciales::REGLAS_PASSWORD,
        ]);

        $fila = DB::transaction(function () use ($correo, $datos) {
            $fila = Recuperacion::comprobar($correo, trim($datos['codigo']));
            if (! $fila) {
                return null;
            }

            $ahora = now();
            DB::table('usuarios')->where('id', $fila->usuario_id)
                ->update(['password' => Hash::make($datos['password']), 'updated_at' => $ahora]);
            DB::table('codigos_recuperacion')->where('id', $fila->id)->update(['usado_en' => $ahora]);

            // Se cierran todas las sesiones de la cuenta.
            DB::table('personal_access_tokens')
                ->where('tokenable_type', User::class)
                ->where('tokenable_id', $fila->usuario_id)
                ->delete();

            return $fila;
        });

        $fila ?? Recuperacion::rechazar();

        return $this->respuesta(null, 'Contraseña actualizada. Inicia sesión.');
    }
}
