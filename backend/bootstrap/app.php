<?php

use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Exceptions\ThrottleRequestsException;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Symfony\Component\HttpKernel\Exception\AccessDeniedHttpException;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;
use Symfony\Component\HttpKernel\Exception\MethodNotAllowedHttpException;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        // No hay ruta web de login: un invitado sin token recibe 401 JSON,
        // aunque el cliente no envíe "Accept: application/json" (p. ej. Retrofit).
        $middleware->redirectGuestsTo(fn () => null);

        $middleware->alias([
            'rol' => \App\Http\Middleware\VerificarRol::class,
            'cuenta.activa' => \App\Http\Middleware\VerificarCuentaActiva::class,
        ]);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        // En la API siempre se responde JSON con el formato estándar
        // { ok, mensaje, datos, errores? }, con mensajes en español.
        $exceptions->shouldRenderJsonWhen(
            fn (Request $request) => $request->is('api/*') || $request->expectsJson()
        );

        $exceptions->render(function (Throwable $e, Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            $errores = null;

            [$codigo, $mensaje] = match (true) {
                $e instanceof ValidationException => [422, 'Los datos enviados no son válidos.'],
                $e instanceof AuthenticationException => [401, 'No autenticado. Inicia sesión.'],
                $e instanceof AuthorizationException,
                $e instanceof AccessDeniedHttpException => [403, 'No tienes permiso para realizar esta acción.'],
                $e instanceof ModelNotFoundException,
                $e instanceof NotFoundHttpException => [404, 'Recurso no encontrado.'],
                $e instanceof MethodNotAllowedHttpException => [405, 'Método no permitido.'],
                $e instanceof ThrottleRequestsException => [429, 'Demasiados intentos. Intenta más tarde.'],
                $e instanceof HttpExceptionInterface => [$e->getStatusCode(), $e->getMessage() ?: 'Error en la solicitud.'],
                default => [500, config('app.debug') ? $e->getMessage() : 'Error interno del servidor.'],
            };

            if ($e instanceof ValidationException) {
                $errores = $e->errors();
            }

            $cuerpo = ['ok' => false, 'mensaje' => $mensaje, 'datos' => null];
            if ($errores !== null) {
                $cuerpo['errores'] = $errores;
            }

            return response()->json($cuerpo, $codigo);
        });
    })->create();
