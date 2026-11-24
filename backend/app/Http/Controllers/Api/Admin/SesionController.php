<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Support\Credenciales;
use App\Support\Normalizar;
use App\Support\PresentadorUsuario;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/**
 * Inicio de sesión del panel web (solo rol "admin").
 * Consultar y cerrar sesión usan /api/sesion (SesionController de la app).
 */
class SesionController extends Controller
{
    /** POST /api/admin/login */
    public function store(Request $request): JsonResponse
    {
        $request->merge(['correo' => Normalizar::correo($request->input('correo'))]);

        $datos = $request->validate([
            'correo' => ['required', 'email'],
            'password' => ['required', 'string'],
        ]);

        $usuario = Credenciales::verificar(['correo' => $datos['correo']], $datos['password']);
        Credenciales::exigirAcceso($usuario, 'admin', 'Esta cuenta no tiene acceso al panel de administración.');

        $token = User::findOrFail($usuario->id)->createToken('panel')->plainTextToken;

        return $this->respuesta([
            'token' => $token,
            'usuario' => PresentadorUsuario::porId($usuario->id),
        ], 'Sesión iniciada');
    }
}
