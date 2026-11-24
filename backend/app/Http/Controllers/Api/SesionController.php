<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Support\Credenciales;
use App\Support\Normalizar;
use App\Support\PresentadorUsuario;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Sesión como recurso: store = iniciar (app), show = consultar, destroy = cerrar.
 * show y destroy sirven a la app y al panel (cualquier rol con token).
 */
class SesionController extends Controller
{
    /** POST /api/login — usuarios de la app, por correo o teléfono. */
    public function store(Request $request): JsonResponse
    {
        $request->merge(array_filter([
            'correo' => Normalizar::correo($request->input('correo')),
            'telefono' => Normalizar::telefono($request->input('telefono')),
        ], fn ($v) => $v !== null));

        $datos = $request->validate([
            'correo' => ['nullable', 'required_without:telefono', 'email'],
            'telefono' => ['nullable', 'required_without:correo', 'digits:10'],
            'password' => ['required', 'string'],
            'fcm_token' => ['nullable', 'string', 'max:255'],
        ]);

        $usuario = Credenciales::verificar($datos, $datos['password']);
        Credenciales::exigirAcceso($usuario, 'usuario', 'Esta cuenta es de administrador. Inicia sesión en el panel web.');

        if (! empty($datos['fcm_token'])) {
            DB::table('usuarios')->where('id', $usuario->id)->update([
                'fcm_token' => $datos['fcm_token'],
                'updated_at' => now(),
            ]);
        }

        $token = User::findOrFail($usuario->id)->createToken('app')->plainTextToken;

        return $this->respuesta([
            'token' => $token,
            'usuario' => PresentadorUsuario::porId($usuario->id),
        ], 'Sesión iniciada');
    }

    /** GET /api/sesion — datos del usuario del token (incluye si está suspendido). */
    public function show(Request $request): JsonResponse
    {
        return $this->respuesta(PresentadorUsuario::porId($request->user()->id));
    }

    /** DELETE /api/sesion — borra solo el token actual. */
    public function destroy(Request $request): JsonResponse
    {
        $usuario = $request->user();
        $usuario->currentAccessToken()->delete();

        // El dispositivo deja de recibir notificaciones de esta cuenta.
        if ($usuario->rol === 'usuario') {
            DB::table('usuarios')->where('id', $usuario->id)->update(['fcm_token' => null, 'updated_at' => now()]);
        }

        return $this->respuesta(null, 'Sesión cerrada');
    }
}
