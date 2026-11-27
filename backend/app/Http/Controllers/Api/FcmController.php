<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\TokensFcm;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/** Token de notificaciones del dispositivo (Firebase lo rota a veces). Rol usuario. */
class FcmController extends Controller
{
    /** PUT /api/sesion/fcm { fcm_token } */
    public function update(Request $request): JsonResponse
    {
        $datos = $request->validate(['fcm_token' => ['required', 'string', 'max:255']]);
        TokensFcm::asignar($request->user()->id, $datos['fcm_token']);

        return $this->respuesta(null, 'Token de notificaciones actualizado.');
    }
}
