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
}
