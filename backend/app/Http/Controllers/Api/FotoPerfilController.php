<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\PresentadorUsuario;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;

/**
 * Foto de perfil propia en el disco `perfiles` (cualquier rol). En
 * `usuarios.foto_perfil` se guarda la ruta; la respuesta trae la URL pública.
 */
class FotoPerfilController extends Controller
{
    /** POST /api/perfil/foto (multipart, campo `foto`) */
    public function store(Request $request): JsonResponse
    {
        $request->validate([
            'foto' => ['required', 'file', 'image', 'mimes:jpg,jpeg,png,webp', 'max:1024'],
        ], [
            'foto.max' => 'La foto pesa más de 1 MB. Elige una más ligera.',
            'foto.mimes' => 'La foto debe ser JPG, PNG o WEBP.',
            'foto.image' => 'La foto debe ser JPG, PNG o WEBP.',
        ]);

        $id = $request->user()->id;
        $ruta = $request->file('foto')->store('', 'perfiles');

        $this->reemplazar($id, $ruta);

        return $this->respuesta(PresentadorUsuario::porId($id), 'Foto actualizada');
    }

    /** DELETE /api/perfil/foto */
    public function destroy(Request $request): JsonResponse
    {
        $id = $request->user()->id;
        $this->reemplazar($id, null);

        return $this->respuesta(PresentadorUsuario::porId($id), 'Foto eliminada');
    }

    /**
     * Guarda la nueva ruta y borra el archivo anterior (no quedan fotos huérfanas).
     * La ruta anterior se lee de la BD, no de $request->user(): ese objeto puede
     * estar desactualizado.
     */
    private function reemplazar(int $id, ?string $nueva): void
    {
        $anterior = DB::table('usuarios')->where('id', $id)->value('foto_perfil');
        DB::table('usuarios')->where('id', $id)->update(['foto_perfil' => $nueva, 'updated_at' => now()]);

        if ($anterior && $anterior !== $nueva) {
            Storage::disk('perfiles')->delete($anterior);
        }
    }
}
