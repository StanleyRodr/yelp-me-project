<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Support\PresentadorCuenta;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

/**
 * Usuarios de la app (rol "usuario") vistos desde el panel. Los admins no
 * aparecen ni se gestionan aquí.
 */
class UsuarioController extends Controller
{
    /** GET /api/admin/usuarios — por nombre. */
    public function index(): JsonResponse
    {
        $usuarios = DB::table('usuarios')
            ->where('rol', 'usuario')
            ->orderBy('nombre')
            ->orderBy('apellidos')
            ->get();

        return $this->respuesta(PresentadorCuenta::resumenes($usuarios));
    }

    /** GET /api/admin/usuarios/{id} — con historial de solicitudes, strikes y suspensiones. */
    public function show(int $id): JsonResponse
    {
        return $this->respuesta(PresentadorCuenta::detalle($id) ?? abort(404));
    }
}
