<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Kreait\Firebase\Contract\Messaging;
use Kreait\Firebase\Exception\Messaging\InvalidMessage;
use Kreait\Firebase\Exception\Messaging\NotFound;
use Kreait\Firebase\Messaging\AndroidConfig;
use Kreait\Firebase\Messaging\CloudMessage;
use Throwable;

/**
 * Envío con Firebase Cloud Messaging (API v1, kreait/laravel-firebase).
 * Un token que ya no existe se borra del usuario; cualquier error se
 * registra y no interrumpe la operación que avisaba.
 */
class NotificadorFcm implements Notificador
{
    public function __construct(private readonly Messaging $messaging)
    {
    }

    public function enviar(string $token, string $titulo, string $cuerpo, array $datos = [], string $canal = 'estado'): bool
    {
        // Solo datos (sin bloque "notification"): la app arma la notificación siempre,
        // también en segundo plano, y así al tocarla abre la pantalla correcta.
        $mensaje = CloudMessage::new()
            ->toToken($token)
            ->withData(array_map('strval', $datos) + [
                'aviso_titulo' => $titulo,
                'aviso_cuerpo' => $cuerpo,
                // Canal de Android (los crea la app): alertas de contactos o estado de mis solicitudes.
                'canal' => $canal,
            ])
            ->withAndroidConfig(AndroidConfig::fromArray(['priority' => 'high']));

        try {
            $this->messaging->send($mensaje);

            return true;
        } catch (NotFound|InvalidMessage $e) {
            // Token de un dispositivo que ya no existe (app desinstalada, token rotado).
            DB::table('usuarios')->where('fcm_token', $token)->update(['fcm_token' => null]);
            Log::warning('FCM: token inválido, se borró del usuario.', ['error' => $e->getMessage()]);
        } catch (Throwable $e) {
            Log::warning('FCM: no se pudo enviar la notificación.', ['error' => $e->getMessage()]);
        }

        return false;
    }
}
