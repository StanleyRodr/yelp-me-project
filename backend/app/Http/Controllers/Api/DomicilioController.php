<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Support\Normalizar;
use App\Support\PresentadorDomicilio;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpKernel\Exception\HttpException;

/** Domicilio único del usuario de la app (plan 009). El usuario sale del token. */
class DomicilioController extends Controller
{
    /** GET /api/perfil/domicilio — domicilio o null. */
    public function show(Request $request): JsonResponse
    {
        return $this->respuesta(PresentadorDomicilio::deUsuario($request->user()->id));
    }

    /** PUT /api/perfil/domicilio — crea o reemplaza. */
    public function update(Request $request): JsonResponse
    {
        $datos = $request->validate([
            'calle' => ['required', 'string', 'max:120'],
            'numero' => ['required', 'string', 'max:20'],
            'colonia' => ['required', 'string', 'max:100'],
            'codigo_postal' => ['required', 'digits:5'],
            'referencia' => ['nullable', 'string', 'max:255'],
            'latitud' => ['required', 'numeric', 'between:-90,90'],
            'longitud' => ['required', 'numeric', 'between:-180,180'],
        ], [
            'codigo_postal.digits' => 'El código postal debe tener 5 dígitos.',
            'latitud.required' => 'Marca el domicilio en el mapa.',
            'longitud.required' => 'Marca el domicilio en el mapa.',
        ]);

        $datos = array_map(fn ($v) => is_string($v) ? Normalizar::opcional($v) : $v, $datos);
        $yo = $request->user()->id;

        DB::table('domicilios')->updateOrInsert(
            ['usuario_id' => $yo],
            $datos + ['referencia' => null, 'updated_at' => now()],
        );

        return $this->respuesta(PresentadorDomicilio::deUsuario($yo), 'Domicilio guardado');
    }

    /** DELETE /api/perfil/domicilio */
    public function destroy(Request $request): JsonResponse
    {
        $borrados = DB::table('domicilios')->where('usuario_id', $request->user()->id)->delete();
        if ($borrados === 0) {
            // HttpException (no abort): el render cambia cualquier NotFoundHttpException por "Recurso no encontrado.".
            throw new HttpException(404, 'No tienes un domicilio registrado.');
        }

        return $this->respuesta(null, 'Domicilio eliminado');
    }
}
