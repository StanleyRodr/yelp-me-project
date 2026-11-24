<?php

namespace Tests\Feature;

use App\Support\ReglasCuenta;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 006: gestión de usuarios de la app desde el panel.
 * Demo: Ana 2 strikes activos; Luis suspendido (3 strikes, 2 suspensiones).
 */
class UsuariosPanelTest extends TestCase
{
    private const ADMIN = 'admin@yelpme.com';

    private function id(string $correo): int
    {
        return DB::table('usuarios')->where('correo', $correo)->value('id');
    }

    public function test_listado_solo_usuarios_de_la_app_con_estado_de_cuenta(): void
    {
        $this->como(self::ADMIN);

        $datos = $this->getJson('/api/admin/usuarios')->assertOk()->json('datos');

        $this->assertSame(['Ana', 'Carlos', 'José', 'Luis', 'María'], array_column($datos, 'nombre'));
        $luis = collect($datos)->firstWhere('nombre', 'Luis');
        $this->assertSame(['Suspendido', 3, 2], [$luis['estado_cuenta'], $luis['strikes_activos'], $luis['veces_suspendido']]);
    }

    public function test_detalle_con_la_forma_del_frontend(): void
    {
        $this->como(self::ADMIN);

        $this->getJson('/api/admin/usuarios/'.$this->id('luis@correo.com'))
            ->assertOk()
            ->assertJsonStructure(['datos' => [
                'estado_cuenta', 'veces_suspendido', 'suspension_vigente',
                'solicitudes' => [['id', 'titulo', 'estado', 'servicios', 'created_at']],
                'strikes' => [['admin_nombre', 'anulado_por_nombre', 'solicitud_id', 'activo']],
                'suspensiones' => [['suspendido_por_nombre', 'rehabilitado_por_nombre']],
            ]])
            ->assertJsonMissingPath('datos.password')
            ->assertJsonMissingPath('datos.fcm_token');
    }

    public function test_no_gestiona_admins_y_usuario_no_entra(): void
    {
        $laura = $this->id('laura.admin@yelpme.com');
        $this->como(self::ADMIN);
        $this->getJson("/api/admin/usuarios/{$laura}")->assertNotFound();
        $this->putJson("/api/admin/usuarios/{$laura}/cuenta", ['activo' => false])->assertNotFound();
        $this->postJson("/api/admin/usuarios/{$laura}/strikes", ['motivo' => 'x'])->assertNotFound();

        $this->como('carlos@correo.com');
        $this->getJson('/api/admin/usuarios')->assertForbidden();
    }

    public function test_deshabilitar_cierra_sesiones_y_reactivar(): void
    {
        $jose = $this->id('jose@correo.com');
        $token = \App\Models\User::find($jose)->createToken('app')->plainTextToken;
        $this->como(self::ADMIN);

        $this->putJson("/api/admin/usuarios/{$jose}/cuenta", ['activo' => true])->assertStatus(409)
            ->assertJsonPath('mensaje', 'La cuenta está activa: no se puede reactivar. Recarga para ver su estado actual.');

        $this->putJson("/api/admin/usuarios/{$jose}/cuenta", ['activo' => false])
            ->assertOk()->assertJsonPath('datos.estado_cuenta', 'Deshabilitado');
        $this->assertSame(0, DB::table('personal_access_tokens')->where('tokenable_id', $jose)->count());

        $this->reiniciarAuth();
        $this->withToken($token)->getJson('/api/sesion')->assertUnauthorized();

        $this->como(self::ADMIN);
        $this->postJson("/api/admin/usuarios/{$jose}/strikes", ['motivo' => 'x'])->assertStatus(409);
        $this->putJson("/api/admin/usuarios/{$jose}/cuenta", ['activo' => true])
            ->assertOk()->assertJsonPath('datos.estado_cuenta', 'Activo');
    }

    public function test_reportar_suspende_al_tercer_strike(): void
    {
        $ana = $this->id('ana@correo.com');
        $this->como(self::ADMIN);

        $this->postJson("/api/admin/usuarios/{$ana}/strikes", [])->assertStatus(422);
        $this->postJson("/api/admin/usuarios/{$ana}/strikes", ['motivo' => 'Llamadas de broma'])
            ->assertCreated()
            ->assertJsonPath('mensaje', 'Strike registrado y cuenta suspendida')
            ->assertJsonPath('datos.estado_cuenta', 'Suspendido')
            ->assertJsonPath('datos.strikes.0.solicitud_id', null);

        $this->postJson("/api/admin/usuarios/{$ana}/suspension", ['motivo' => 'x'])->assertStatus(409);
    }

    public function test_anular_conserva_historial_y_no_levanta_la_suspension(): void
    {
        $luis = $this->id('luis@correo.com');
        $strike = DB::table('strikes')->where('usuario_id', $luis)->where('activo', true)->value('id');
        $this->como(self::ADMIN);

        $this->putJson("/api/admin/strikes/{$strike}/anulacion", [])->assertStatus(422);
        $this->putJson("/api/admin/strikes/{$strike}/anulacion", ['motivo' => 'Error de captura'])
            ->assertOk()
            ->assertJsonPath('datos.strikes_activos', 2)
            ->assertJsonPath('datos.estado_cuenta', 'Suspendido');

        $fila = DB::table('strikes')->where('id', $strike)->first();
        $this->assertSame('Error de captura', $fila->motivo_anulacion);
        $this->assertNotNull($fila->anulado_por_id);

        $this->putJson("/api/admin/strikes/{$strike}/anulacion", ['motivo' => 'x'])->assertStatus(409);
        $this->putJson('/api/admin/strikes/99999/anulacion', ['motivo' => 'x'])->assertNotFound();
    }

    public function test_rehabilitar_reinicia_strikes_y_conserva_historial(): void
    {
        $luis = $this->id('luis@correo.com');
        $this->como(self::ADMIN);

        $this->deleteJson("/api/admin/usuarios/{$luis}/suspension")
            ->assertOk()
            ->assertJsonPath('mensaje', 'Cuenta rehabilitada')
            ->assertJsonPath('datos.estado_cuenta', 'Activo')
            ->assertJsonPath('datos.strikes_activos', 0)
            ->assertJsonPath('datos.veces_suspendido', 2)
            ->assertJsonPath('datos.suspension_vigente', null);

        $this->assertSame(6, DB::table('strikes')->where('usuario_id', $luis)->count());
        $this->assertSame(0, DB::table('strikes')->where('usuario_id', $luis)->whereNotNull('anulado_por_id')->count());

        $this->deleteJson("/api/admin/usuarios/{$luis}/suspension")->assertStatus(409);
    }

    public function test_suspension_manual_impide_pedir_servicios(): void
    {
        $carlos = $this->id('carlos@correo.com');
        $this->como(self::ADMIN);

        $this->postJson("/api/admin/usuarios/{$carlos}/suspension", ['motivo' => 'Uso indebido'])
            ->assertCreated()
            ->assertJsonPath('datos.suspension_vigente.suspendido_por_nombre', 'Administrador General');
        $this->assertTrue(ReglasCuenta::estaSuspendido($carlos));

        $this->como('carlos@correo.com');
        $this->postJson('/api/solicitudes', ['titulo' => 'X', 'latitud' => 19.9, 'longitud' => -96.8, 'servicios' => [1]])->assertForbidden();
    }

    public function test_actividad_del_admin(): void
    {
        $this->como(self::ADMIN);

        $datos = $this->getJson('/api/admin/actividad')->assertOk()->json('datos');
        $titulos = array_column($datos['solicitudes'], 'titulo');

        $this->assertContains('Persona lesionada por caída', $titulos);
        $this->assertCount(4, $titulos);
        $this->assertSame(6, $datos['strikes_dados']);
    }
}
