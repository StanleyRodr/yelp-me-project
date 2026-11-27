<?php

namespace App\Support;

/**
 * Envío de notificaciones push (plan 012). El sistema funciona igual sin
 * Firebase: sin credenciales se usa NotificadorNulo.
 */
interface Notificador
{
    /**
     * Devuelve true si FCM aceptó el mensaje. Nunca lanza: los errores se registran.
     *
     * @param  array<string, string>  $datos  Datos para la app (solo texto).
     */
    public function enviar(string $token, string $titulo, string $cuerpo, array $datos = [], string $canal = 'estado'): bool;
}
