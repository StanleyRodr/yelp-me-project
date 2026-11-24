<?php

namespace App\Http\Controllers;

use Illuminate\Http\JsonResponse;

abstract class Controller
{
    /**
     * Respuesta JSON con el formato estándar de la API:
     * { "ok": bool, "mensaje": string, "datos": mixed }
     */
    protected function respuesta(mixed $datos = null, string $mensaje = 'OK', int $codigo = 200): JsonResponse
    {
        return response()->json([
            'ok' => $codigo < 400,
            'mensaje' => $mensaje,
            'datos' => $datos,
        ], $codigo);
    }
}
