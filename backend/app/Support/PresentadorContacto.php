<?php

namespace App\Support;

/**
 * Forma pública de un contacto de la agenda. No expone el id ni los datos de
 * la cuenta vinculada: solo si está vinculado.
 */
class PresentadorContacto
{
    public static function datos(object $c): array
    {
        return [
            'id' => $c->id,
            'nombre' => $c->nombre,
            'telefono' => $c->telefono,
            'parentesco' => $c->parentesco,
            'vinculado' => $c->usuario_contacto_id !== null,
            'created_at' => Normalizar::fecha($c->created_at),
            'updated_at' => Normalizar::fecha($c->updated_at),
        ];
    }
}
