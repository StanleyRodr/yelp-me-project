<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Recuperacion;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/** Paso 2 de la recuperación: comprobar el código antes de pedir la contraseña nueva. */
class VerificacionRecuperacionController extends Controller
{
    /** POST /api/recuperacion/verificacion { correo, codigo } */
    public function store(Request $request): JsonResponse
    {
        $correo = Recuperacion::correo($request);
        $codigo = $request->validate(['codigo' => ['required', 'string']])['codigo'];

        Recuperacion::verificar($correo, trim($codigo));

        return $this->respuesta(['valido' => true], 'Código correcto.');
    }
}
