<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use App\Support\ReglasContactos;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;

/**
 * ¿Ese número se vinculará si lo agrego? Plan principal 6.1: no expone datos
 * personales. Sin cuenta, cuenta de admin o cuenta que me bloqueó responden
 * igual (vinculable: false), así no se distingue un bloqueo.
 */
class TelefonoController extends Controller
{
    /** GET /api/telefonos/{telefono} */
    public function show(Request $request, string $telefono): JsonResponse
    {
        $telefono = Normalizar::telefono($telefono);
        Validator::make(['telefono' => $telefono], ['telefono' => ['required', 'digits:10']])->validate();

        $duenoId = $request->user()->id;

        return $this->respuesta([
            'telefono' => $telefono,
            'vinculable' => ReglasContactos::usuarioVinculable($telefono, $duenoId) !== null,
            'en_agenda' => DB::table('contactos')->where('usuario_id', $duenoId)->where('telefono', $telefono)->exists(),
        ]);
    }
}
