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
    }
}
