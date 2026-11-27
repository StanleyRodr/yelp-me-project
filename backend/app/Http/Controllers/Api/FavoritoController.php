<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\PresentadorContacto;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Contacto favorito (plan 010): máximo uno por usuario. Lo llama el botón de
 * la barra de la app. Contacto ajeno o inexistente → 404.
 */
class FavoritoController extends Controller
{
    /** PUT /api/contactos/{id}/favorito — marca este y desmarca los demás. */
    public function update(Request $request, int $id): JsonResponse
    {
        $yo = $request->user()->id;

        DB::transaction(function () use ($yo, $id) {
            // Bloquea la agenda: dos marcados a la vez no dejan dos favoritos.
            $ids = DB::table('contactos')->where('usuario_id', $yo)->lockForUpdate()->pluck('id');
            if (! $ids->contains($id)) {
                abort(404);
            }
            DB::table('contactos')->where('usuario_id', $yo)->where('favorito', true)->update(['favorito' => false]);
            DB::table('contactos')->where('id', $id)->update(['favorito' => true]);
        });

        return $this->respuesta($this->presentar($id), 'Contacto favorito actualizado');
    }

    /** DELETE /api/contactos/{id}/favorito */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $actualizados = DB::table('contactos')
            ->where('id', $id)
            ->where('usuario_id', $request->user()->id)
            ->update(['favorito' => false]);

        // update() devuelve 0 también si ya no era favorito: se confirma que exista.
        if ($actualizados === 0 && ! DB::table('contactos')->where('id', $id)->where('usuario_id', $request->user()->id)->exists()) {
            abort(404);
        }

        return $this->respuesta($this->presentar($id), 'Ya no es tu contacto favorito');
    }

    private function presentar(int $id): array
    {
        return PresentadorContacto::datos(DB::table('contactos')->where('id', $id)->first());
    }
}
