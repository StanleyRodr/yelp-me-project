<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Consentimiento mínimo (plan principal 6.2): quién me tiene como contacto
 * vinculado y desvincularme. Un "vínculo" es un contacto ajeno que apunta a mí.
 */
class VinculoController extends Controller
{
    /** GET /api/vinculos */
    public function index(Request $request): JsonResponse
    {
        $vinculos = DB::table('contactos as c')
            ->join('usuarios as u', 'u.id', '=', 'c.usuario_id')
            ->where('c.usuario_contacto_id', $request->user()->id)
            ->orderBy('u.nombre')
            ->get(['c.id', 'c.created_at', 'u.nombre', 'u.apellido_paterno', 'u.apellido_materno', 'u.telefono'])
            ->map(fn ($v) => [
                'id' => $v->id,
                // Datos de la cuenta de quien me agregó, no el alias que me puso.
                'nombre' => $v->nombre,
                'apellido_paterno' => $v->apellido_paterno,
                'apellido_materno' => $v->apellido_materno,
                'telefono' => $v->telefono,
                'desde' => Normalizar::fecha($v->created_at),
            ]);

        return $this->respuesta($vinculos);
    }

    /** DELETE /api/vinculos/{id} — me borra de la agenda de quien me agregó. */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $duenoId = self::duenoDelVinculo($request, $id);

        DB::table('contactos')
            ->where('usuario_id', $duenoId)
            ->where('usuario_contacto_id', $request->user()->id)
            ->delete();

        return $this->respuesta(null, 'Te desvinculaste de este contacto');
    }

    /** Dueño del contacto que apunta a mí, o 404. Lo reutiliza el bloqueo. */
    public static function duenoDelVinculo(Request $request, int $contactoId): int
    {
        $duenoId = DB::table('contactos')
            ->where('id', $contactoId)
            ->where('usuario_contacto_id', $request->user()->id)
            ->value('usuario_id');

        return $duenoId !== null ? (int) $duenoId : abort(404);
    }
}
