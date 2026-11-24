<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\ReglasCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

/**
 * Cambio de contraseña propia (cualquier rol). Cierra las demás sesiones.
 */
class PasswordController extends Controller
{
    /** PUT /api/perfil/password { actual, nueva, nueva_confirmation } */
    public function update(Request $request): JsonResponse
    {
        $datos = $request->validate([
            'actual' => ['required', 'string'],
            'nueva' => ['required', 'string', 'min:8', 'max:72', 'confirmed', 'different:actual'],
        ], [
            'nueva.different' => 'La nueva contraseña debe ser distinta de la actual.',
        ]);

        $yo = $request->user();

        $hashActual = DB::table('usuarios')->where('id', $yo->id)->value('password');

        if (! Hash::check($datos['actual'], $hashActual)) {
            throw ValidationException::withMessages(['actual' => 'La contraseña actual no es correcta.']);
        }

        DB::table('usuarios')->where('id', $yo->id)->update([
            'password' => Hash::make($datos['nueva']),
            'updated_at' => now(),
        ]);

        // Otros dispositivos deben volver a iniciar sesión; este sigue.
        ReglasCuenta::cerrarSesiones($yo->id, $yo->currentAccessToken()->id);

        return $this->respuesta(null, 'Contraseña actualizada');
    }
}
