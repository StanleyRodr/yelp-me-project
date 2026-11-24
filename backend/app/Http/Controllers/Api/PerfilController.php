<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use App\Support\PresentadorUsuario;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Datos propios (cualquier rol). Consultar el perfil = GET /api/sesion.
 */
class PerfilController extends Controller
{
    /** PUT|PATCH /api/perfil — solo los campos que cambian. */
    public function update(Request $request): JsonResponse
    {
        $yo = $request->user();

        foreach (['correo' => 'correo', 'telefono' => 'telefono'] as $campo => $tipo) {
            if ($request->has($campo)) {
                $request->merge([$campo => Normalizar::$tipo($request->input($campo))]);
            }
        }

        $datos = $request->validate([
            'nombre' => ['sometimes', 'required', 'string', 'max:100'],
            'apellidos' => ['sometimes', 'required', 'string', 'max:100'],
            'telefono' => ['sometimes', 'required', 'digits:10', Rule::unique('usuarios', 'telefono')->ignore($yo->id)],
            'correo' => ['sometimes', 'required', 'email', 'max:150', Rule::unique('usuarios', 'correo')->ignore($yo->id)],
        ]);

        $datos = array_map(fn ($v) => trim($v), $datos);

        DB::transaction(function () use ($yo, $datos) {
            DB::table('usuarios')->where('id', $yo->id)->update($datos + ['updated_at' => now()]);

            if (isset($datos['telefono']) && $datos['telefono'] !== $yo->telefono && $yo->rol === 'usuario') {
                $this->actualizarAgendas($yo->id, $datos['telefono']);
            }
        });

        return $this->respuesta(PresentadorUsuario::porId($yo->id), 'Perfil actualizado');
    }

    /**
     * Plan principal 6.1: el vínculo es por usuario_id y se conserva. En las
     * agendas que me tienen vinculado se pone mi número nuevo, salvo donde ya
     * guardaron ese número aparte (UNIQUE usuario_id + telefono).
     * Además se vinculan contactos que ya tenían mi número nuevo sin cuenta,
     * si su dueño no está bloqueado por mí (igual que al registrarse).
     */
    private function actualizarAgendas(int $yo, string $telefono): void
    {
        // MySQL (error 1093) no deja consultar `contactos` dentro del UPDATE de
        // `contactos`: primero se calculan los dueños con conflicto.
        $conConflicto = DB::table('contactos')->where('telefono', $telefono)->pluck('usuario_id');

        DB::table('contactos')
            ->where('usuario_contacto_id', $yo)
            ->whereNotIn('usuario_id', $conConflicto)
            ->update(['telefono' => $telefono, 'updated_at' => now()]);

        $bloqueados = DB::table('bloqueos')->where('usuario_id', $yo)->pluck('bloqueado_id');

        DB::table('contactos')
            ->where('telefono', $telefono)
            ->whereNull('usuario_contacto_id')
            ->where('usuario_id', '!=', $yo)
            ->whereNotIn('usuario_id', $bloqueados)
            ->update(['usuario_contacto_id' => $yo, 'updated_at' => now()]);
    }
}
