<?php

namespace Tests\Feature;

use App\Support\ReglasCuenta;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 005: atención de solicitudes desde el panel.
 */
class SolicitudesPanelTest extends TestCase
{
    private const ADMIN = 'admin@yelpme.com';
    private const LAURA = 'laura.admin@yelpme.com';

    private function id(string $inicioTitulo): int
    {
        return DB::table('solicitudes')->where('titulo', 'like', $inicioTitulo.'%')->value('id');
    }

    private function estado(int $id, array $datos): \Illuminate\Testing\TestResponse
    {
        return $this->putJson("/api/admin/solicitudes/{$id}/estado", $datos);
    }

    public function test_listado_detalle_y_filtro(): void
    {
        $this->como(self::ADMIN);

        $this->getJson('/api/admin/solicitudes')->assertOk()->assertJsonCount(11, 'datos');
        $this->getJson('/api/admin/solicitudes?estado=Rechazado')->assertJsonCount(5, 'datos');
        $this->getJson('/api/admin/solicitudes/'.$this->id('Incendio en pastizal'))->assertOk()->assertJsonPath('datos.servicios', [3, 5]);
        $this->getJson('/api/admin/solicitudes/99999')->assertNotFound();
    }

    public function test_usuario_no_entra_al_panel(): void
    {
        $this->como('carlos@correo.com');
        $this->getJson('/api/admin/solicitudes')->assertForbidden();
    }

    public function test_enviar_y_segundo_admin_recibe_409(): void
    {
        $id = $this->id('Incendio en pastizal');

        $this->como(self::ADMIN);
        $this->estado($id, ['estado' => 'Enviado'])
            ->assertOk()
            ->assertJsonPath('mensaje', 'Ayuda enviada')
            ->assertJsonPath('datos.admin.nombre', 'Administrador');
        $this->assertNotNull(DB::table('solicitudes')->where('id', $id)->value('enviado_en'));

        $this->como(self::LAURA);
        $this->estado($id, ['estado' => 'Enviado'])->assertStatus(409);
        $this->estado($id, ['estado' => 'Rechazado', 'motivo' => 'x'])->assertStatus(409);
    }

    public function test_terminar_conserva_quien_la_despacho(): void
    {
        $this->como(self::LAURA);

        $this->estado($this->id('Persona lesionada'), ['estado' => 'Terminado'])
            ->assertOk()
            ->assertJsonPath('datos.admin.nombre', 'Administrador')
            ->assertJsonPath('datos.cerrado_por.nombre', 'Laura');
    }

    public function test_rechazar_exige_motivo_y_el_strike_suspende_al_tercero(): void
    {
        $ana = DB::table('usuarios')->where('correo', 'ana@correo.com')->value('id');
        $this->como('ana@correo.com');
        $id = $this->postJson('/api/solicitudes', ['titulo' => 'Robo en la tienda', 'latitud' => 19.93, 'longitud' => -96.85, 'servicios' => [1]])->json('datos.id');

        $this->como(self::ADMIN);
        $this->estado($id, ['estado' => 'Rechazado'])->assertStatus(422)
            ->assertJsonPath('errores.motivo.0', 'Escribe el motivo del rechazo.');

        $this->estado($id, ['estado' => 'Rechazado', 'motivo' => 'Falsa alarma', 'strike' => true])
            ->assertOk()
            ->assertJsonPath('mensaje', 'Solicitud rechazada y strike registrado')
            ->assertJsonPath('datos.motivo', 'Falsa alarma');

        $this->assertSame(3, ReglasCuenta::strikesActivos($ana));
        $this->assertTrue(ReglasCuenta::estaSuspendido($ana));
        $this->assertSame($id, (int) DB::table('strikes')->where('usuario_id', $ana)->latest('id')->value('solicitud_id'));
    }

    public function test_rechazar_sin_strike_no_suma(): void
    {
        $maria = DB::table('usuarios')->where('correo', 'maria@correo.com')->value('id');
        $this->como(self::ADMIN);

        $this->estado($this->id('Me siento insegura'), ['estado' => 'Rechazado', 'motivo' => 'Mal uso'])
            ->assertOk()->assertJsonPath('mensaje', 'Solicitud rechazada');
        $this->assertSame(0, ReglasCuenta::strikesActivos($maria));
    }

    public function test_solicitud_sin_servicios_no_se_envia(): void
    {
        $this->como(self::ADMIN);
        $this->estado($this->id('Me siento insegura'), ['estado' => 'Enviado'])->assertStatus(409);
    }

    public function test_corregir_servicios(): void
    {
        $id = $this->id('Incendio en pastizal');
        $this->como(self::ADMIN);

        $this->putJson("/api/admin/solicitudes/{$id}/servicios", ['servicios' => []])->assertStatus(422)
            ->assertJsonPath('errores.servicios.0', 'Selecciona al menos un servicio.');
        $this->putJson("/api/admin/solicitudes/{$id}/servicios", ['servicios' => [99]])->assertStatus(422);
        $this->putJson("/api/admin/solicitudes/{$id}/servicios", ['servicios' => [3, 5, 2]])
            ->assertOk()->assertJsonPath('datos.servicios', [2, 3, 5]);

        $this->putJson('/api/admin/solicitudes/'.$this->id('Me siento insegura').'/servicios', ['servicios' => [1]])->assertStatus(409);
        $this->putJson('/api/admin/solicitudes/'.$this->id('Choque entre motocicleta').'/servicios', ['servicios' => [1]])->assertStatus(409);
    }
}
