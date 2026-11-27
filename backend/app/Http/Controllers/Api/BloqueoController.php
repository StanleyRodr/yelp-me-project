<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Bloqueos (plan principal 6.2): B bloquea a A desde un vínculo. Se borra el
 * contacto de la agenda de A y A no puede volver a vincular a B mientras dure.
 */
class BloqueoController extends Controller
{
    /** GET /api/bloqueos */
    public function index(Request $request): JsonResponse
    {
        $bloqueos = DB::table('bloqueos as b')
            ->join('usuarios as u', 'u.id', '=', 'b.bloqueado_id')
            ->where('b.usuario_id', $request->user()->id)
            ->orderBy('u.nombre')
            ->get(['b.id', 'b.created_at', 'u.nombre', 'u.apellido_paterno', 'u.apellido_materno', 'u.telefono'])
            ->map(fn ($b) => $this->presentar($b));

        return $this->respuesta($bloqueos);
    }

    /** POST /api/bloqueos { contacto_id } — id de un vínculo (contacto ajeno que apunta a mí). */
    public function store(Request $request): JsonResponse
    {
        $datos = $request->validate(['contacto_id' => ['required', 'integer']]);
        $yo = $request->user()->id;
        $duenoId = VinculoController::duenoDelVinculo($request, (int) $datos['contacto_id']);

        $id = DB::transaction(function () use ($yo, $duenoId) {
            DB::table('bloqueos')->insertOrIgnore([
                'usuario_id' => $yo,
                'bloqueado_id' => $duenoId,
                'created_at' => now(),
            ]);

            DB::table('contactos')
                ->where('usuario_id', $duenoId)
                ->where('usuario_contacto_id', $yo)
                ->delete();

            return DB::table('bloqueos')->where('usuario_id', $yo)->where('bloqueado_id', $duenoId)->value('id');
        });

        return $this->respuesta($this->presentar($this->buscar($yo, $id)), 'Usuario bloqueado', 201);
    }

    /**
     * DELETE /api/bloqueos/{id} — desbloquear. No restaura el contacto que se
     * borró al bloquear, pero si la otra persona ya me volvió a guardar (sin
     * vínculo por el bloqueo), ese contacto se vincula, como al registrarse.
     */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $yo = $request->user();
        $bloqueo = $this->buscar($yo->id, $id);
        $bloqueadoId = DB::table('bloqueos')->where('id', $bloqueo->id)->value('bloqueado_id');

        DB::transaction(function () use ($yo, $bloqueo, $bloqueadoId) {
            DB::table('bloqueos')->where('id', $bloqueo->id)->delete();

            DB::table('contactos')
                ->where('usuario_id', $bloqueadoId)
                ->where('telefono', DB::table('usuarios')->where('id', $yo->id)->value('telefono'))
                ->whereNull('usuario_contacto_id')
                ->update(['usuario_contacto_id' => $yo->id, 'updated_at' => now()]);
        });

        return $this->respuesta(null, 'Usuario desbloqueado');
    }

    private function buscar(int $yo, int $id): object
    {
        $bloqueo = DB::table('bloqueos as b')
            ->join('usuarios as u', 'u.id', '=', 'b.bloqueado_id')
            ->where('b.id', $id)
            ->where('b.usuario_id', $yo)
            ->first(['b.id', 'b.created_at', 'u.nombre', 'u.apellido_paterno', 'u.apellido_materno', 'u.telefono']);

        return $bloqueo ?? abort(404);
    }

    private function presentar(object $b): array
    {
        return [
            'id' => $b->id,
            'nombre' => $b->nombre,
            'apellido_paterno' => $b->apellido_paterno,
            'apellido_materno' => $b->apellido_materno,
            'telefono' => $b->telefono,
            'desde' => Normalizar::fecha($b->created_at),
        ];
    }
}
