<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\PresentadorSolicitud;
use App\Support\Solicitudes;
use App\Support\TransicionesSolicitud;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Estado de una solicitud propia, desde la app: cancelar o dar por terminada.
 * Transiciones permitidas en TransicionesSolicitud (plan principal 7.2).
 */
class EstadoSolicitudController extends Controller
{
    private const MENSAJES = [
        'Cancelado' => 'Solicitud cancelada',
        'Terminado' => 'Solicitud terminada',
    ];

    /** PUT /api/solicitudes/{id}/estado { estado: Cancelado|Terminado, motivo } */
    public function update(Request $request, int $id): JsonResponse
    {
        $datos = $request->validate([
            'estado' => ['required', Rule::in(array_keys(self::MENSAJES))],
            'motivo' => ['required_if:estado,Cancelado', 'nullable', 'string', 'max:500'],
        ], [
            'motivo.required_if' => 'Escribe el motivo de la cancelación.',
        ]);

        $usuarioId = $request->user()->id;

        DB::transaction(function () use ($id, $usuarioId, $datos) {
            $solicitud = Solicitudes::bloquear($id, $usuarioId);
            $conServicios = (bool) Solicitudes::servicios($id);

            TransicionesSolicitud::exigir('usuario', $conServicios, $solicitud->estado, $datos['estado']);
            Solicitudes::aplicarEstado($solicitud, $datos['estado'], $usuarioId, $datos['motivo'] ?? null);
        });

        return $this->respuesta(PresentadorSolicitud::porId($id), self::MENSAJES[$datos['estado']]);
    }
}
