<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use App\Support\ReglasCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Reportar a un usuario: strike manual (sin solicitud). Al tercero activo,
 * la cuenta se suspende automáticamente (plan principal 8).
 */
class StrikeController extends Controller
{
    /** POST /api/admin/usuarios/{id}/strikes { motivo } */
    public function store(Request $request, int $id): JsonResponse
    {
        $motivo = trim($request->validate(['motivo' => ['required', 'string', 'max:500']])['motivo']);
        $adminId = $request->user()->id;

        $suspendidoAhora = DB::transaction(function () use ($id, $adminId, $motivo) {
            $usuario = ReglasCuenta::bloquearUsuarioApp($id);
            ReglasCuenta::exigirAccion($usuario, 'reportar');

            $antes = ReglasCuenta::estaSuspendido($id);
            ReglasCuenta::darStrike($id, $adminId, $motivo);

            return ! $antes && ReglasCuenta::estaSuspendido($id);
        });

        $mensaje = $suspendidoAhora ? 'Strike registrado y cuenta suspendida' : 'Strike registrado';

        return $this->respuesta(PresentadorCuenta::detalle($id), $mensaje, 201);
    }
}
