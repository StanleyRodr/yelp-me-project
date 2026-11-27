<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use App\Support\PresentadorContacto;
use App\Support\ReglasContactos;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;
use Illuminate\Validation\ValidationException;

/**
 * CRUD de la agenda de contactos de confianza (requisito: CRUD desde Android).
 * El dueño siempre es el usuario del token; contactos ajenos responden 404.
 */
class ContactoController extends Controller
{
    /** GET /api/contactos — el favorito primero. */
    public function index(Request $request): JsonResponse
    {
        $contactos = DB::table('contactos')
            ->where('usuario_id', $request->user()->id)
            ->orderByDesc('favorito')
            ->orderBy('nombre')
            ->get()
            ->map(fn ($c) => PresentadorContacto::datos($c));

        return $this->respuesta($contactos);
    }

    /** POST /api/contactos */
    public function store(Request $request): JsonResponse
    {
        $duenoId = $request->user()->id;
        $datos = $this->validar($request, $duenoId);

        $id = DB::table('contactos')->insertGetId([
            'usuario_id' => $duenoId,
            'nombre' => $datos['nombre'],
            'telefono' => $datos['telefono'],
            'parentesco' => $datos['parentesco'],
            'usuario_contacto_id' => ReglasContactos::usuarioVinculable($datos['telefono'], $duenoId),
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        return $this->respuesta($this->presentar($id), 'Contacto agregado', 201);
    }

    /** GET /api/contactos/{id} */
    public function show(Request $request, int $id): JsonResponse
    {
        return $this->respuesta(PresentadorContacto::datos($this->propio($request, $id)));
    }

    /** PUT|PATCH /api/contactos/{id} — se pueden enviar solo los campos que cambian. */
    public function update(Request $request, int $id): JsonResponse
    {
        $contacto = $this->propio($request, $id);
        $datos = $this->validar($request, $contacto->usuario_id, $contacto->id);

        $cambios = array_merge($datos, ['updated_at' => now()]);

        // Otro teléfono = otra persona: se recalcula el vínculo.
        if (isset($datos['telefono']) && $datos['telefono'] !== $contacto->telefono) {
            $cambios['usuario_contacto_id'] = ReglasContactos::usuarioVinculable($datos['telefono'], $contacto->usuario_id);
        }

        DB::table('contactos')->where('id', $contacto->id)->update($cambios);

        return $this->respuesta($this->presentar($contacto->id), 'Contacto actualizado');
    }

    /** DELETE /api/contactos/{id} — borrado físico (decisión del dueño sobre su agenda). */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $contacto = $this->propio($request, $id);
        DB::table('contactos')->where('id', $contacto->id)->delete();

        return $this->respuesta(null, 'Contacto eliminado');
    }

    /** Contacto del usuario del token, o 404 (no revela contactos ajenos). */
    private function propio(Request $request, int $id): object
    {
        $contacto = DB::table('contactos')
            ->where('id', $id)
            ->where('usuario_id', $request->user()->id)
            ->first();

        // Mismo 404 genérico para ajeno o inexistente: no revela si existe.
        return $contacto ?? abort(404);
    }

    private function presentar(int $id): array
    {
        return PresentadorContacto::datos(DB::table('contactos')->where('id', $id)->first());
    }

    /**
     * Valida y normaliza. En update ($ignorarId) los campos son opcionales.
     */
    private function validar(Request $request, int $duenoId, ?int $ignorarId = null): array
    {
        if ($request->has('telefono')) {
            $request->merge(['telefono' => Normalizar::telefono($request->input('telefono'))]);
        }

        $requerido = $ignorarId ? 'sometimes' : 'required';

        $datos = $request->validate([
            'nombre' => [$requerido, 'required', 'string', 'max:100'],
            'telefono' => [
                $requerido, 'required', 'digits:10',
                Rule::unique('contactos', 'telefono')->where('usuario_id', $duenoId)->ignore($ignorarId),
            ],
            'parentesco' => [$requerido, 'required', 'string', 'max:50'],
        ], [
            'telefono.unique' => 'Ya tienes un contacto con ese teléfono.',
        ]);

        if (isset($datos['telefono']) && ReglasContactos::esTelefonoPropio($datos['telefono'], $duenoId)) {
            throw ValidationException::withMessages(['telefono' => 'No puedes agregarte a ti mismo como contacto.']);
        }

        return array_map(fn ($v) => is_string($v) ? trim($v) : $v, $datos);
    }
}
