<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Actividad del admin con sesión (perfil del panel): solicitudes que atendió
 * (`solicitudes.admin_id`) y strikes que dio (`strikes.admin_id`).
 */
class ActividadController extends Controller
{
    /** GET /api/admin/actividad */
    public function index(Request $request): JsonResponse
    {
        $yo = $request->user()->id;

        return $this->respuesta([
            'solicitudes' => PresentadorCuenta::solicitudesBreves(DB::table('solicitudes')->where('admin_id', $yo)),
            'strikes_dados' => DB::table('strikes')->where('admin_id', $yo)->count(),
        ]);
    }
}
