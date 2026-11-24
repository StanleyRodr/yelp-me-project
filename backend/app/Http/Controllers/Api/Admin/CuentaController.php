<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use App\Support\ReglasCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Deshabilitar o reactivar una cuenta de la app. Nunca se borra (plan
 * principal 10.1.1): `activo = 0` y se cierran sus sesiones.
 */
class CuentaController extends Controller
{
    /** PUT /api/admin/usuarios/{id}/cuenta { activo: bool } */
    public function update(Request $request, int $id): JsonResponse
    {
        $activo = $request->validate(['activo' => ['required', 'boolean']])['activo'];
        $activo = filter_var($activo, FILTER_VALIDATE_BOOLEAN);

        DB::transaction(function () use ($id, $activo) {
            $usuario = ReglasCuenta::bloquearUsuarioApp($id);
            ReglasCuenta::exigirAccion($usuario, $activo ? 'reactivar' : 'deshabilitar');

            DB::table('usuarios')->where('id', $id)->update([
                'activo' => $activo,
                'deshabilitado_en' => $activo ? null : now(),
                'updated_at' => now(),
            ]);

            if (! $activo) {
                ReglasCuenta::cerrarSesiones($id);
            }
        });

        return $this->respuesta(PresentadorCuenta::detalle($id), $activo ? 'Cuenta reactivada' : 'Cuenta deshabilitada');
    }
}
