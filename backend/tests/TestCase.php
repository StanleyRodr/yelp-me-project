<?php

namespace Tests;

use App\Models\User;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Laravel\Sanctum\Sanctum;
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

    /**
     * PNG real de 1×1 sin la extensión GD (UploadedFile::fake()->image la requiere).
     * `$rellenoKb` agrega bytes al final para probar el límite de tamaño.
     */
    protected function imagenPng(string $nombre = 'foto.png', int $rellenoKb = 0): \Illuminate\Http\UploadedFile
    {
        $ruta = tempnam(sys_get_temp_dir(), 'ym');
        file_put_contents($ruta, base64_decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==').str_repeat("\0", $rellenoKb * 1024));

        return new \Illuminate\Http\UploadedFile($ruta, $nombre, 'image/png', null, true);
    }

    /** Autentica como un usuario del demo (por correo) sin pasar por el login. */
    protected function como(string $correo): User
    {
        $this->reiniciarAuth();
        $usuario = User::where('correo', $correo)->firstOrFail();
        Sanctum::actingAs($usuario);

        return $usuario;
    }
}
