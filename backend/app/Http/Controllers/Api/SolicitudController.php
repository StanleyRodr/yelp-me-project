<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\PresentadorSolicitud;
use App\Support\Solicitudes;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

/**
 * Solicitudes de ayuda del usuario de la app (propias). No hay destroy: las
 * solicitudes nunca se borran; se cancelan con /estado.
 */
class SolicitudController extends Controller
{
    /** GET /api/solicitudes?estado=Pendiente — más recientes primero. */
    public function index(Request $request): JsonResponse
    {
        $datos = $request->validate(['estado' => ['nullable', Rule::in(Solicitudes::ESTADOS)]]);

        $filas = DB::table('solicitudes')
            ->where('usuario_id', $request->user()->id)
            ->when($datos['estado'] ?? null, fn ($q, $estado) => $q->where('estado', $estado))
            ->orderByDesc('created_at')
            ->orderByDesc('id')
            ->get();

        return $this->respuesta(PresentadorSolicitud::lista($filas));
    }

    /** POST /api/solicitudes — nace Pendiente. */
    public function store(Request $request): JsonResponse
    {
        $usuarioId = $request->user()->id;
        $datos = $request->validate($this->reglas('required'));
        $servicios = $datos['servicios'] ?? [];

        if ($servicios) {
            Solicitudes::exigirNoSuspendido($usuarioId);
        }

        $id = DB::transaction(function () use ($datos, $servicios, $usuarioId) {
            $id = DB::table('solicitudes')->insertGetId([
                'usuario_id' => $usuarioId,
                'titulo' => trim($datos['titulo']),
                'descripcion' => $this->textoOpcional($datos['descripcion'] ?? null),
                'latitud' => $datos['latitud'],
                'longitud' => $datos['longitud'],
                'ubicacion_nombre' => $this->textoOpcional($datos['ubicacion_nombre'] ?? null),
                'estado' => 'Pendiente',
                'created_at' => now(),
                'updated_at' => now(),
            ]);

            if ($servicios) {
                Solicitudes::reemplazarServicios($id, $servicios);
            }

            return $id;
        });

        return $this->respuesta(PresentadorSolicitud::porId($id), 'Solicitud enviada', 201);
    }

    /** GET /api/solicitudes/{id} — solo propias (ajena = 404). */
    public function show(Request $request, int $id): JsonResponse
    {
        $existe = DB::table('solicitudes')->where('id', $id)->where('usuario_id', $request->user()->id)->exists();

        return $existe ? $this->respuesta(PresentadorSolicitud::porId($id)) : abort(404);
    }

    /** PUT|PATCH /api/solicitudes/{id} — solo en Pendiente; campos opcionales. */
    public function update(Request $request, int $id): JsonResponse
    {
        $usuarioId = $request->user()->id;
        $datos = $request->validate($this->reglas('sometimes'));

        DB::transaction(function () use ($id, $usuarioId, $datos) {
            $solicitud = Solicitudes::bloquear($id, $usuarioId);

            if ($solicitud->estado !== 'Pendiente') {
                abort(409, "La solicitud está en {$solicitud->estado}: solo puedes modificarla mientras está pendiente.");
            }

            if (array_key_exists('servicios', $datos)) {
                // Suspendido: puede quitar servicios, no agregar.
                if (array_diff($datos['servicios'], Solicitudes::servicios($id))) {
                    Solicitudes::exigirNoSuspendido($usuarioId);
                }
                Solicitudes::reemplazarServicios($id, $datos['servicios']);
            }

            $cambios = array_intersect_key($datos, array_flip(['titulo', 'descripcion', 'latitud', 'longitud', 'ubicacion_nombre']));
            if (isset($cambios['titulo'])) {
                $cambios['titulo'] = trim($cambios['titulo']);
            }
            foreach (['descripcion', 'ubicacion_nombre'] as $campo) {
                if (array_key_exists($campo, $cambios)) {
                    $cambios[$campo] = $this->textoOpcional($cambios[$campo]);
                }
            }

            DB::table('solicitudes')->where('id', $id)->update($cambios + ['updated_at' => now()]);
        });

        return $this->respuesta(PresentadorSolicitud::porId($id), 'Solicitud actualizada');
    }

    private function reglas(string $requerido): array
    {
        return [
            'titulo' => [$requerido, 'required', 'string', 'max:150'],
            'descripcion' => ['nullable', 'string', 'max:2000'],
            'latitud' => [$requerido, 'required', 'numeric', 'between:-90,90'],
            'longitud' => [$requerido, 'required', 'numeric', 'between:-180,180'],
            'ubicacion_nombre' => ['nullable', 'string', 'max:255'],
        ] + Solicitudes::reglasServicios();
    }

    private function textoOpcional(?string $texto): ?string
    {
        $texto = $texto === null ? null : trim($texto);

        return $texto === '' ? null : $texto;
    }
}
