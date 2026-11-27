<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\Avisos;
use App\Support\PresentadorSolicitud;
use App\Support\ReglasCuenta;
use App\Support\Solicitudes;
use App\Support\TransicionesSolicitud;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Estado de una solicitud desde el panel: enviar ayuda, rechazar (con strike
 * opcional) o terminar. La fila se bloquea: si otro admin ya actuó, 409.
 */
class EstadoSolicitudController extends Controller
{
    private const MENSAJES = [
        'Enviado' => 'Ayuda enviada',
        'Rechazado' => 'Solicitud rechazada',
        'Terminado' => 'Solicitud terminada',
    ];

    public const MOTIVO_STRIKE = 'Broma / mal uso del servicio.';

    /** PUT /api/admin/solicitudes/{id}/estado { estado, motivo?, strike? } */
    public function update(Request $request, int $id): JsonResponse
    {
        $datos = $request->validate([
            'estado' => ['required', Rule::in(array_keys(self::MENSAJES))],
            'motivo' => ['required_if:estado,Rechazado', 'nullable', 'string', 'max:500'],
            'strike' => ['sometimes', 'boolean'],
        ], [
            'motivo.required_if' => 'Escribe el motivo del rechazo.',
        ]);

        $adminId = $request->user()->id;
        $conStrike = $datos['estado'] === 'Rechazado' && ($datos['strike'] ?? false);

        DB::transaction(function () use ($id, $adminId, $datos, $conStrike) {
            $solicitud = Solicitudes::bloquear($id);
            $conServicios = (bool) Solicitudes::servicios($id);

            TransicionesSolicitud::exigir('admin', $conServicios, $solicitud->estado, $datos['estado']);
            Solicitudes::aplicarEstado($solicitud, $datos['estado'], $adminId, $datos['motivo'] ?? null);

            if ($conStrike) {
                ReglasCuenta::darStrike($solicitud->usuario_id, $adminId, self::MOTIVO_STRIKE, $solicitud->id);
            }

            // Push al solicitante con el estado nuevo (plan 012); los cambios del propio usuario no avisan.
            DB::afterCommit(fn () => Avisos::estadoCambiado($id));
        });

        $mensaje = self::MENSAJES[$datos['estado']].($conStrike ? ' y strike registrado' : '');

        return $this->respuesta(PresentadorSolicitud::detalle($id), $mensaje);
    }
}
