<?php

namespace App\Providers;

use App\Support\Notificador;
use App\Support\NotificadorFcm;
use App\Support\NotificadorNulo;
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;
use Kreait\Firebase\Contract\Messaging;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        // Notificaciones push (plan 012): FCM si hay credenciales; si no, no se envía nada.
        $this->app->singleton(Notificador::class, function ($app) {
            $credenciales = self::credencialesFirebase();
            if ($credenciales === null) {
                return new NotificadorNulo();
            }
            config(['firebase.projects.app.credentials' => $credenciales]);

            return new NotificadorFcm($app->make(Messaging::class));
        });
    }

    /**
     * Producción: FIREBASE_CREDENTIALS_BASE64 (JSON en base64, tiene prioridad).
     * Local: archivo FIREBASE_CREDENTIALS (por defecto storage/app/firebase/credenciales.json).
     * Devuelve el arreglo o la ruta absoluta, o null si no hay credenciales usables.
     */
    private static function credencialesFirebase(): array|string|null
    {
        $base64 = config('services.firebase.credenciales_base64');
        if ($base64) {
            $json = json_decode((string) base64_decode($base64, true), true);

            return is_array($json) && isset($json['private_key']) ? $json : null;
        }

        $ruta = (string) config('services.firebase.credenciales');
        $esAbsoluta = str_starts_with($ruta, '/') || preg_match('/^[A-Za-z]:/', $ruta) === 1;
        $absoluta = $esAbsoluta ? $ruta : base_path($ruta);

        return is_readable($absoluta) ? $absoluta : null;
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
