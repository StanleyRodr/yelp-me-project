<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 009: domicilio único del usuario y veces suspendido.
 */
class DomicilioTest extends TestCase
{
    private const MARIA = 'maria@correo.com';   // sin domicilio en el seeder
    private const CARLOS = 'carlos@correo.com'; // con domicilio

    private const DOMICILIO = [
        'calle' => 'Calle Hidalgo',
        'numero' => '12',
        'colonia' => 'Centro',
        'codigo_postal' => '93820',
        'referencia' => 'Casa azul',
        'latitud' => 19.9311,
        'longitud' => -96.8519,
    ];

    private function id(string $correo): int
    {
        return DB::table('usuarios')->where('correo', $correo)->value('id');
    }

    public function test_crear_reemplazar_y_borrar_el_domicilio(): void
    {
        $this->como(self::MARIA);
        $maria = $this->id(self::MARIA);

        $this->getJson('/api/perfil/domicilio')->assertOk()->assertJsonPath('datos', null);

        $this->putJson('/api/perfil/domicilio', self::DOMICILIO)->assertOk()
            ->assertJsonPath('mensaje', 'Domicilio guardado')
            ->assertJsonPath('datos.texto', 'Calle Hidalgo 12, Centro, 93820')
            ->assertJsonPath('datos.latitud', 19.9311);

        // Reemplaza: sigue una sola fila, sin referencia si no se manda.
        $nuevo = array_merge(self::DOMICILIO, ['calle' => 'Calle Juárez', 'referencia' => null]);
        unset($nuevo['referencia']);
        $this->putJson('/api/perfil/domicilio', $nuevo)->assertOk()
            ->assertJsonPath('datos.calle', 'Calle Juárez')
            ->assertJsonPath('datos.referencia', null);
        $this->assertSame(1, DB::table('domicilios')->where('usuario_id', $maria)->count());

        $this->getJson('/api/perfil/domicilio')->assertOk()->assertJsonPath('datos.calle', 'Calle Juárez');

        $this->deleteJson('/api/perfil/domicilio')->assertOk()->assertJsonPath('datos', null);
        $this->assertSame(0, DB::table('domicilios')->where('usuario_id', $maria)->count());

        $this->deleteJson('/api/perfil/domicilio')->assertNotFound()
            ->assertJsonPath('mensaje', 'No tienes un domicilio registrado.');
    }

    public function test_valida_codigo_postal_y_coordenadas(): void
    {
        $this->como(self::MARIA);

        $this->putJson('/api/perfil/domicilio', array_merge(self::DOMICILIO, ['codigo_postal' => '9382']))
            ->assertStatus(422)->assertJsonPath('errores.codigo_postal.0', 'El código postal debe tener 5 dígitos.');

        $sinPunto = self::DOMICILIO;
        unset($sinPunto['latitud'], $sinPunto['longitud']);
        $this->putJson('/api/perfil/domicilio', $sinPunto)->assertStatus(422)
            ->assertJsonValidationErrors(['latitud', 'longitud'], 'errores');

        $this->putJson('/api/perfil/domicilio', array_merge(self::DOMICILIO, ['latitud' => 95]))
            ->assertStatus(422)->assertJsonValidationErrors(['latitud'], 'errores');
    }

    public function test_cada_usuario_ve_solo_el_suyo_y_el_admin_no_usa_la_ruta(): void
    {
        $this->como(self::MARIA);
        $this->getJson('/api/perfil/domicilio')->assertOk()->assertJsonPath('datos', null);
        $this->putJson('/api/perfil/domicilio', self::DOMICILIO)->assertOk();

        // Carlos sigue viendo el suyo, sin cambios.
        $this->como(self::CARLOS);
        $this->getJson('/api/perfil/domicilio')->assertOk()
            ->assertJsonPath('datos.calle', 'Av. Ignacio Zaragoza');

        $this->como('admin@yelpme.com');
        $this->getJson('/api/perfil/domicilio')->assertForbidden();
        $this->putJson('/api/perfil/domicilio', self::DOMICILIO)->assertForbidden();
    }

    public function test_detalle_admin_de_usuario_y_de_solicitud_incluye_domicilio(): void
    {
        $carlos = $this->id(self::CARLOS);
        $solicitud = DB::table('solicitudes')->where('usuario_id', $carlos)->value('id');
        $this->como('admin@yelpme.com');

        $this->getJson("/api/admin/usuarios/{$carlos}")->assertOk()
            ->assertJsonPath('datos.domicilio.codigo_postal', '93820');
        $this->getJson("/api/admin/usuarios/{$this->id(self::MARIA)}")->assertOk()
            ->assertJsonPath('datos.domicilio', null);

        $this->getJson("/api/admin/solicitudes/{$solicitud}")->assertOk()
            ->assertJsonPath('datos.usuario.domicilio.calle', 'Av. Ignacio Zaragoza');
    }

    public function test_sesion_trae_veces_suspendido(): void
    {
        $this->como('luis@correo.com');
        $this->getJson('/api/sesion')->assertOk()->assertJsonPath('datos.veces_suspendido', 2);

        $this->como(self::MARIA);
        $this->getJson('/api/sesion')->assertOk()->assertJsonPath('datos.veces_suspendido', 0);
    }
}
