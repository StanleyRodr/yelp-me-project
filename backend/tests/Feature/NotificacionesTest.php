<?php

namespace Tests\Feature;

use App\Support\Notificador;
use App\Support\NotificadorNulo;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 012: token FCM del dispositivo y envíos de notificaciones.
 */
class NotificacionesTest extends TestCase
{
    private const CARLOS = 'carlos@correo.com';
    private const MARIA = 'maria@correo.com';

    private function token(string $correo): ?string
    {
        return DB::table('usuarios')->where('correo', $correo)->value('fcm_token');
    }

    public function test_guardar_el_token_del_dispositivo(): void
    {
        $this->como(self::CARLOS);

        $this->putJson('/api/sesion/fcm', ['fcm_token' => 'token-carlos'])->assertOk()
            ->assertJsonPath('mensaje', 'Token de notificaciones actualizado.');
        $this->assertSame('token-carlos', $this->token(self::CARLOS));

        $this->putJson('/api/sesion/fcm', [])->assertStatus(422);
    }

    public function test_el_mismo_dispositivo_pasa_a_la_cuenta_que_inicia_sesion(): void
    {
        $this->como(self::CARLOS);
        $this->putJson('/api/sesion/fcm', ['fcm_token' => 'telefono-compartido'])->assertOk();

        $this->como(self::MARIA);
        $this->putJson('/api/sesion/fcm', ['fcm_token' => 'telefono-compartido'])->assertOk();

        $this->assertSame('telefono-compartido', $this->token(self::MARIA));
        $this->assertNull($this->token(self::CARLOS));
    }

    public function test_login_y_registro_mueven_el_token(): void
    {
        DB::table('usuarios')->where('correo', self::CARLOS)->update(['fcm_token' => 'tel-1']);

        $this->postJson('/api/login', ['correo' => self::MARIA, 'password' => 'Usuario123*', 'fcm_token' => 'tel-1'])->assertOk();
        $this->assertSame('tel-1', $this->token(self::MARIA));
        $this->assertNull($this->token(self::CARLOS));

        $this->postJson('/api/registro', [
            'nombre' => 'Nueva', 'apellido_paterno' => 'Persona', 'telefono' => '2359990077',
            'correo' => 'nueva@correo.com', 'password' => 'Usuario123*', 'password_confirmation' => 'Usuario123*',
            'fcm_token' => 'tel-1',
        ])->assertCreated();
        $this->assertSame('tel-1', $this->token('nueva@correo.com'));
        $this->assertNull($this->token(self::MARIA));
    }

    public function test_admin_no_usa_la_ruta_del_token(): void
    {
        $this->como('admin@yelpme.com');
        $this->putJson('/api/sesion/fcm', ['fcm_token' => 'x'])->assertForbidden();
    }

    public function test_sin_credenciales_se_usa_el_notificador_nulo(): void
    {
        config(['services.firebase.credenciales_base64' => null, 'services.firebase.credenciales' => 'no/existe.json']);
        $this->app->forgetInstance(Notificador::class);
        // Volver a registrar el proveedor resuelve con la configuración actual.
        (new \App\Providers\AppServiceProvider($this->app))->register();

        $this->assertInstanceOf(NotificadorNulo::class, $this->app->make(Notificador::class));
    }

    // ---------- Envíos (tarea 073) ----------

    private function ponerToken(string $correo, ?string $token): void
    {
        DB::table('usuarios')->where('correo', $correo)->update(['fcm_token' => $token]);
    }

    private function crearSolicitud(array $cambios = []): int
    {
        return $this->postJson('/api/solicitudes', array_merge([
            'titulo' => 'Me caí en la calle',
            'latitud' => 19.9311,
            'longitud' => -96.8519,
            'ubicacion_nombre' => 'Frente al parque',
            'servicios' => [2],
        ], $cambios))->assertCreated()->json('datos.id');
    }

    public function test_crear_solicitud_avisa_a_los_contactos_vinculados_con_token(): void
    {
        // Carlos tiene a María y José (vinculados) y a "Mamá" (sin cuenta).
        $this->ponerToken(self::MARIA, 'token-maria');
        $this->ponerToken('jose@correo.com', 'token-jose');
        $this->ponerToken('ana@correo.com', 'token-ana'); // no es contacto de Carlos

        $this->como(self::CARLOS);
        $this->crearSolicitud();

        $this->assertCount(2, $this->notificador->enviados);
        $alerta = $this->notificador->a('token-maria')[0];
        $this->assertSame('Carlos pidió ayuda', $alerta['titulo']);
        $this->assertSame('Me caí en la calle', $alerta['cuerpo']);
        $this->assertSame('alertas', $alerta['canal']);
        $this->assertSame('alerta_contacto', $alerta['datos']['tipo']);
        $this->assertSame('Carlos Ramírez Soto', $alerta['datos']['nombre']);
        $this->assertSame('2351000001', $alerta['datos']['telefono']);
        $this->assertSame('Frente al parque', $alerta['datos']['ubicacion_nombre']);
        $this->assertSame(19.9311, (float) $alerta['datos']['latitud']);
        $this->assertIsString($alerta['datos']['longitud']);
        $this->assertCount(1, $this->notificador->a('token-jose'));
        $this->assertSame([], $this->notificador->a('token-ana'));
    }

    public function test_no_avisa_a_cuentas_deshabilitadas_ni_sin_token(): void
    {
        $this->ponerToken(self::MARIA, 'token-maria');
        $this->ponerToken('jose@correo.com', null);
        DB::table('usuarios')->where('correo', self::MARIA)->update(['activo' => false]);

        $this->como(self::CARLOS);
        $this->crearSolicitud(['servicios' => []]);

        $this->assertSame([], $this->notificador->enviados);
    }

    public function test_cambios_del_admin_avisan_al_solicitante(): void
    {
        $this->ponerToken(self::CARLOS, 'token-carlos');
        $this->como(self::CARLOS);
        $enviar = $this->crearSolicitud();
        $rechazar = $this->crearSolicitud();
        $this->notificador->enviados = [];

        $this->como('admin@yelpme.com');
        $this->putJson("/api/admin/solicitudes/{$enviar}/estado", ['estado' => 'Enviado'])->assertOk();
        $this->putJson("/api/admin/solicitudes/{$enviar}/estado", ['estado' => 'Terminado'])->assertOk();
        $this->putJson("/api/admin/solicitudes/{$rechazar}/estado", ['estado' => 'Rechazado', 'motivo' => 'Era una prueba'])->assertOk();

        $avisos = $this->notificador->a('token-carlos');
        $this->assertSame(['Tu ayuda va en camino', 'Tu solicitud se marcó como terminada', 'Tu solicitud fue rechazada'],
            array_column($avisos, 'titulo'));
        $this->assertSame('Era una prueba', $avisos[2]['cuerpo']);
        $this->assertSame(['tipo' => 'estado', 'solicitud_id' => (string) $enviar, 'estado' => 'Enviado'], $avisos[0]['datos']);
        $this->assertSame('estado', $avisos[0]['canal']);
    }

    public function test_cambios_del_propio_usuario_no_avisan(): void
    {
        $this->ponerToken(self::CARLOS, 'token-carlos');
        $this->como(self::CARLOS);
        $id = $this->crearSolicitud();
        $this->putJson("/api/solicitudes/{$id}/estado", ['estado' => 'Cancelado', 'motivo' => 'Ya estoy bien'])->assertOk();

        $this->assertSame([], $this->notificador->a('token-carlos'));
    }

    public function test_si_el_cambio_falla_no_hay_aviso(): void
    {
        $this->ponerToken(self::CARLOS, 'token-carlos');
        $this->como(self::CARLOS);
        $id = $this->crearSolicitud(['servicios' => []]);
        $this->notificador->enviados = [];

        // Sin servicios el admin no puede enviar: 409 y nada que avisar.
        $this->como('admin@yelpme.com');
        $this->putJson("/api/admin/solicitudes/{$id}/estado", ['estado' => 'Enviado'])->assertStatus(409);

        $this->assertSame([], $this->notificador->enviados);
    }

    public function test_un_notificador_que_falla_no_cambia_la_respuesta(): void
    {
        $this->ponerToken(self::MARIA, 'token-maria');
        $this->app->instance(Notificador::class, new class implements Notificador {
            public function enviar(string $token, string $titulo, string $cuerpo, array $datos = [], string $canal = 'estado'): bool
            {
                throw new \RuntimeException('FCM caído');
            }
        });

        $this->como(self::CARLOS);
        $this->crearSolicitud();
    }
}
