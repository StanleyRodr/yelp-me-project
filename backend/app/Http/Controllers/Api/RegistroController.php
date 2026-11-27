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
use Illuminate\Support\Facades\Hash;

/**
 * Registro público de usuarios de la app Android (siempre rol "usuario").
 * Los administradores no se registran por la API (se dan de alta en la BD).
 */
class RegistroController extends Controller
{
    /** POST /api/registro */
    public function store(Request $request): JsonResponse
    {
        $request->merge([
            'correo' => Normalizar::correo($request->input('correo')),
            'telefono' => Normalizar::telefono($request->input('telefono')),
        ]);

        $datos = $request->validate([
            'nombre' => ['required', 'string', 'max:100'],
            'apellido_paterno' => ['required', 'string', 'max:60'],
            'apellido_materno' => ['nullable', 'string', 'max:60'],
            'telefono' => ['required', 'digits:10', 'unique:usuarios,telefono'],
            'correo' => ['required', 'email', 'max:150', 'unique:usuarios,correo'],
            'password' => Credenciales::REGLAS_PASSWORD,
            'fcm_token' => ['nullable', 'string', 'max:255'],
        ]);

        [$id, $vinculados] = DB::transaction(function () use ($datos) {
            $ahora = now();

            $id = DB::table('usuarios')->insertGetId([
                'nombre' => trim($datos['nombre']),
                'apellido_paterno' => trim($datos['apellido_paterno']),
                'apellido_materno' => Normalizar::opcional($datos['apellido_materno'] ?? null),
                'telefono' => $datos['telefono'],
                'correo' => $datos['correo'],
                'password' => Hash::make($datos['password']),
                'rol' => 'usuario',
                'fcm_token' => $datos['fcm_token'] ?? null,
                'activo' => true,
                'created_at' => $ahora,
                'updated_at' => $ahora,
            ]);

            // Plan principal 6.1: los contactos de otros usuarios con este
            // teléfono quedan vinculados a la cuenta nueva.
            $vinculados = DB::table('contactos')
                ->where('telefono', $datos['telefono'])
                ->whereNull('usuario_contacto_id')
                ->where('usuario_id', '!=', $id)
                ->update(['usuario_contacto_id' => $id, 'updated_at' => $ahora]);

            return [$id, $vinculados];
        });

        $token = User::findOrFail($id)->createToken('app')->plainTextToken;

        return $this->respuesta([
            'token' => $token,
            'usuario' => PresentadorUsuario::porId($id),
            'contactos_vinculados' => $vinculados,
        ], 'Cuenta creada', 201);
    }
}
