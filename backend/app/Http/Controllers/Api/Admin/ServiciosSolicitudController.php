<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorSolicitud;
use App\Support\Solicitudes;
use App\Support\TransicionesSolicitud;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * El admin corrige los servicios según la situación real (plan principal 7.3):
 * solo en solicitudes con servicios, en Pendiente o Enviado, con al menos uno.
 */
class ServiciosSolicitudController extends Controller
{
    /** PUT /api/admin/solicitudes/{id}/servicios { servicios: [ids] } */
    public function update(Request $request, int $id): JsonResponse
    {
        // array_merge (no "+"): debe reemplazar la regla base de "servicios" para exigir mínimo uno.
        $request->validate(array_merge(Solicitudes::reglasServicios('required'), [
            'servicios' => ['required', 'array', 'min:1'],
        ]), [
            'servicios.min' => 'Selecciona al menos un servicio.',
            'servicios.required' => 'Selecciona al menos un servicio.',
        ]);

        DB::transaction(function () use ($id, $request) {
            $solicitud = Solicitudes::bloquear($id);
            $conServicios = (bool) Solicitudes::servicios($id);

            if (! TransicionesSolicitud::adminPuedeEditarServicios($solicitud->estado, $conServicios)) {
                abort(409, "La solicitud está en {$solicitud->estado}: solo se corrigen servicios de solicitudes con servicios en Pendiente o Enviado.");
            }

            Solicitudes::reemplazarServicios($id, $request->input('servicios'));
            DB::table('solicitudes')->where('id', $id)->update(['updated_at' => now()]);
        });

        return $this->respuesta(PresentadorSolicitud::detalle($id), 'Servicios actualizados');
    }
}
