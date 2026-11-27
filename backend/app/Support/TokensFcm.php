<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;

/**
 * Token FCM del dispositivo (un dispositivo por usuario, plan 012). Si el mismo
 * teléfono lo usaba otra cuenta, se le quita: el aviso solo llega a quien tiene
 * la sesión abierta.
 */
class TokensFcm
{
    public static function asignar(int $usuarioId, ?string $token): void
    {
        if ($token === null || $token === '') {
            return;
        }
        DB::table('usuarios')->where('fcm_token', $token)->where('id', '!=', $usuarioId)->update(['fcm_token' => null]);
        DB::table('usuarios')->where('id', $usuarioId)->update(['fcm_token' => $token, 'updated_at' => now()]);
    }
}
