<?php

namespace Tests\Support;

use App\Support\Notificador;

/** Guarda los envíos para revisarlos en las pruebas (nunca llama a Firebase). */
class NotificadorFalso implements Notificador
{
    /** @var array<int, array{token: string, titulo: string, cuerpo: string, datos: array, canal: string}> */
    public array $enviados = [];

    public function enviar(string $token, string $titulo, string $cuerpo, array $datos = [], string $canal = 'estado'): bool
    {
        $this->enviados[] = compact('token', 'titulo', 'cuerpo', 'datos', 'canal');

        return true;
    }

    /** Envíos a un token. */
    public function a(string $token): array
    {
        return array_values(array_filter($this->enviados, fn ($e) => $e['token'] === $token));
    }
}
