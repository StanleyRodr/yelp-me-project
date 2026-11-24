<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use App\Support\ReglasCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Suspensión vigente de una cuenta como recurso:
 *  - store   = suspender manualmente (con motivo)
 *  - destroy = rehabilitar: cierra la suspensión (no borra el historial) y
 *              reinicia a 0 los strikes activos
 */
class SuspensionController extends Controller
{
    /** POST /api/admin/usuarios/{id}/suspension { motivo } */
    public function store(Request $request, int $id): JsonResponse
    {
        $motivo = trim($request->validate(['motivo' => ['required', 'string', 'max:500']])['motivo']);
        $adminId = $request->user()->id;

        DB::transaction(function () use ($id, $adminId, $motivo) {
            $usuario = ReglasCuenta::bloquearUsuarioApp($id);
            ReglasCuenta::exigirAccion($usuario, 'suspender');
            ReglasCuenta::suspender($id, $motivo, $adminId);
        });

        return $this->respuesta(PresentadorCuenta::detalle($id), 'Cuenta suspendida', 201);
    }

    /** DELETE /api/admin/usuarios/{id}/suspension — rehabilitar. */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $adminId = $request->user()->id;

        DB::transaction(function () use ($id, $adminId) {
            $usuario = ReglasCuenta::bloquearUsuarioApp($id);
            ReglasCuenta::exigirAccion($usuario, 'rehabilitar');
            ReglasCuenta::rehabilitar($id, $adminId);
        });

        return $this->respuesta(PresentadorCuenta::detalle($id), 'Cuenta rehabilitada');
    }
}
