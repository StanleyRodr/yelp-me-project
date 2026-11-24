<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * Catálogo de servicios de emergencia (fijo, cargado por script-bd.sql).
 * `icono` = nombre en Material Symbols, igual en web y Android.
 */
class ServicioController extends Controller
{
    /** GET /api/servicios — solo activos. */
    public function index(): JsonResponse
    {
        $servicios = DB::table('servicios')
            ->where('activo', true)
            ->orderBy('id')
            ->get(['id', 'nombre', 'icono', 'color'])
            ->map(fn ($s) => (array) $s);

        return $this->respuesta($servicios);
    }
}
