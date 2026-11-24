<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;

/**
 * Reglas de contactos del plan principal (sección 6), con DB::table.
 */
class ReglasContactos
{
    /**
     * Id del usuario con el que se vincula un contacto, o null.
     * Se vincula si el teléfono es de una cuenta de rol "usuario", no es el
     * propio dueño y esa cuenta no bloqueó al dueño (plan 6.2).
     */
    public static function usuarioVinculable(string $telefono, int $duenoId): ?int
    {
        $id = DB::table('usuarios as u')
            ->where('u.telefono', $telefono)
            ->where('u.rol', 'usuario')
            ->where('u.id', '!=', $duenoId)
            ->whereNotExists(function ($q) use ($duenoId) {
                $q->select(DB::raw(1))
                    ->from('bloqueos as b')
                    ->whereColumn('b.usuario_id', 'u.id')
                    ->where('b.bloqueado_id', $duenoId);
            })
            ->value('u.id');

        return $id !== null ? (int) $id : null;
    }

    public static function esTelefonoPropio(string $telefono, int $duenoId): bool
    {
        return DB::table('usuarios')->where('id', $duenoId)->value('telefono') === $telefono;
    }
}
