<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Bloquea tokens de cuentas deshabilitadas (activo = 0), aunque el token
 * se haya emitido antes de deshabilitarla.
 */
class VerificarCuentaActiva
{
    public function handle(Request $request, Closure $next): Response
    {
        if ($request->user() && ! $request->user()->activo) {
            abort(403, 'Esta cuenta está deshabilitada.');
        }

        return $next($request);
    }
}
