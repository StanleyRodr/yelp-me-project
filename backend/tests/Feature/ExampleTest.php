<?php

namespace Tests\Feature;

use Tests\TestCase;

class ExampleTest extends TestCase
{
    public function test_ping_responde_con_formato_estandar(): void
    {
        $this->getJson('/api/ping')
            ->assertOk()
            ->assertJson(['ok' => true, 'mensaje' => 'API YelpMe funcionando']);
    }

    public function test_ruta_inexistente_responde_404_json(): void
    {
        $this->getJson('/api/no-existe')
            ->assertNotFound()
            ->assertJson(['ok' => false, 'datos' => null]);
    }

    public function test_raiz_indica_rutas_de_la_api(): void
    {
        $this->get('/')->assertOk()->assertJson(['ok' => true]);
    }
}
