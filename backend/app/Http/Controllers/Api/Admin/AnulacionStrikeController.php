<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Anular un strike dado por error: deja de contar, pero se conserva con quién,
 * cuándo y por qué. No levanta una suspensión ya aplicada: eso es rehabilitar.
 */
class AnulacionStrikeController extends Controller
{
    /** PUT /api/admin/strikes/{id}/anulacion { motivo } */
    public function update(Request $request, int $id): JsonResponse
    {
        $motivo = trim($request->validate(['motivo' => ['required', 'string', 'max:500']])['motivo']);
        $adminId = $request->user()->id;

        $usuarioId = DB::transaction(function () use ($id, $adminId, $motivo) {
            $strike = DB::table('strikes')->where('id', $id)->lockForUpdate()->first() ?? abort(404);

            if (! $strike->activo) {
                abort(409, 'Este strike ya no está activo: no se puede anular.');
            }

            DB::table('strikes')->where('id', $id)->update([
                'activo' => false,
                'anulado_por_id' => $adminId,
                'motivo_anulacion' => $motivo,
                'anulado_en' => now(),
            ]);

            return $strike->usuario_id;
        });

        return $this->respuesta(PresentadorCuenta::detalle($usuarioId), 'Strike anulado');
    }
}
