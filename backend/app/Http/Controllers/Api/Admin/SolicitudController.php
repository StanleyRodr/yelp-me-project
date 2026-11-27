<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorDomicilio;
use App\Support\PresentadorSolicitud;
use App\Support\Solicitudes;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Solicitudes de todos los usuarios, para el panel (solo lectura; los cambios
 * van por /estado y /servicios).
 */
class SolicitudController extends Controller
{
    /** GET /api/admin/solicitudes?estado= — más recientes primero. */
    public function index(Request $request): JsonResponse
    {
        $datos = $request->validate(['estado' => ['nullable', Rule::in(Solicitudes::ESTADOS)]]);

        $filas = DB::table('solicitudes')
            ->when($datos['estado'] ?? null, fn ($q, $estado) => $q->where('estado', $estado))
            ->orderByDesc('created_at')
            ->orderByDesc('id')
            ->get();

        return $this->respuesta(PresentadorSolicitud::lista($filas));
    }

    /** GET /api/admin/solicitudes/{id} */
    public function show(int $id): JsonResponse
    {
        $solicitud = PresentadorSolicitud::porId($id) ?? abort(404);
        // Solo en el detalle (en el listado sería una consulta extra por fila).
        $solicitud['usuario']['domicilio'] = PresentadorDomicilio::deUsuario($solicitud['usuario_id']);

        return $this->respuesta($solicitud);
    }
}
