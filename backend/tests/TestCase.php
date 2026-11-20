<?php

namespace Tests;

use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Foundation\Testing\TestCase as BaseTestCase;
use Illuminate\Support\Facades\DB;

abstract class TestCase extends BaseTestCase
{
    // Cada prueba corre en una transacción que se revierte al terminar.
    use DatabaseTransactions;

    protected function setUp(): void
    {
        parent::setUp();

        // Protección: las pruebas nunca deben tocar la BD de desarrollo.
        $bd = DB::connection()->getDatabaseName();
        if (! str_ends_with($bd, '_pruebas')) {
            $this->fail("Las pruebas deben usar una BD terminada en _pruebas (actual: {$bd}). Ejecuta `php artisan bd:pruebas`.");
        }
    }

    /** Olvida el usuario autenticado entre peticiones de una misma prueba. */
    protected function reiniciarAuth(): void
    {
        $this->app['auth']->forgetGuards();
    }
}
