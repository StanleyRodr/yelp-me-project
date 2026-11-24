<?php

namespace App\Providers;

use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        // Registro e inicio de sesión: 5 intentos por minuto por IP + identificador.
        RateLimiter::for('acceso', function (Request $request) {
            $identificador = mb_strtolower((string) ($request->input('correo') ?? $request->input('telefono') ?? ''));

            return Limit::perMinute(5)->by($identificador.'|'.$request->ip());
        });

        // Verificar si un teléfono tiene cuenta: 30 por minuto por usuario.
        // Frena que se recorran números para descubrir quién está registrado.
        RateLimiter::for('telefonos', function (Request $request) {
            return Limit::perMinute(30)->by('telefonos|'.($request->user()?->id ?? $request->ip()));
        });
    }
}
