<?php

namespace App\Support;

/** Sin credenciales de Firebase: no envía nada (la app y el panel funcionan igual). */
class NotificadorNulo implements Notificador
{
    public function enviar(string $token, string $titulo, string $cuerpo, array $datos = [], string $canal = 'estado'): bool
    {
        return false;
    }
}
