<?php

namespace Tests\Feature;

use App\Mail\CodigoRecuperacion;
use Illuminate\Routing\Middleware\ThrottleRequests;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Mail;
use Tests\TestCase;

/**
 * Plan 008 (tarea 044): recuperar la contraseña con código por correo.
 * Reglas: plan principal 5.1.
 */
class RecuperacionTest extends TestCase
{
    private const MARIA = 'maria@correo.com';

    private const NEUTRO = 'Si el correo está registrado, te enviamos un código.';

    protected function setUp(): void
    {
        parent::setUp();
        Mail::fake();
    }

    /** Pide un código y devuelve el que se mandó por correo. */
    private function pedirCodigo(string $correo = self::MARIA): string
    {
        $this->postJson('/api/recuperacion', ['correo' => $correo])->assertOk();

        $codigo = null;
        Mail::assertSent(CodigoRecuperacion::class, function (CodigoRecuperacion $m) use (&$codigo, $correo) {
            $codigo = $m->codigo;

            return $m->hasTo($correo);
        });

        return $codigo;
    }

    private function verificar(string $codigo, string $correo = self::MARIA): \Illuminate\Testing\TestResponse
    {
        return $this->postJson('/api/recuperacion/verificacion', ['correo' => $correo, 'codigo' => $codigo]);
    }

    private function cambiar(string $codigo, array $cambios = []): \Illuminate\Testing\TestResponse
    {
        return $this->putJson('/api/recuperacion', array_merge([
            'correo' => self::MARIA,
            'codigo' => $codigo,
            'password' => 'nuevaClave1',
            'password_confirmation' => 'nuevaClave1',
        ], $cambios));
    }

    private function otro(string $codigo): string
    {
        return str_pad((string) (((int) $codigo + 1) % 1000000), 6, '0', STR_PAD_LEFT);
    }

    public function test_correo_registrado_recibe_un_codigo_de_6_digitos(): void
    {
        $this->postJson('/api/recuperacion', ['correo' => ' MARIA@correo.com '])
            ->assertOk()
            ->assertJsonPath('mensaje', self::NEUTRO);

        Mail::assertSent(CodigoRecuperacion::class, fn ($m) => $m->hasTo(self::MARIA) && preg_match('/^\d{6}$/', $m->codigo));
        $this->assertNotSame('', DB::table('codigos_recuperacion')->value('codigo'));
    }

    public function test_correo_inexistente_responde_igual_sin_enviar(): void
    {
        $this->postJson('/api/recuperacion', ['correo' => 'nadie@correo.com'])
            ->assertOk()
            ->assertJsonPath('mensaje', self::NEUTRO);

        Mail::assertNothingSent();
    }

    public function test_cuenta_deshabilitada_no_recibe_codigo(): void
    {
        DB::table('usuarios')->where('correo', self::MARIA)->update(['activo' => false]);

        $this->postJson('/api/recuperacion', ['correo' => self::MARIA])->assertOk()->assertJsonPath('mensaje', self::NEUTRO);

        Mail::assertNothingSent();
    }

    public function test_admin_tambien_recupera(): void
    {
        $codigo = $this->pedirCodigo('admin@yelpme.com');

        $this->verificar($codigo, 'admin@yelpme.com')->assertOk()->assertJsonPath('datos.valido', true);
    }

    public function test_codigo_correcto_e_incorrecto(): void
    {
        $codigo = $this->pedirCodigo();

        $this->verificar($this->otro($codigo))
            ->assertUnprocessable()
            ->assertJsonPath('errores.codigo.0', 'El código no es válido o ya venció.');
        $this->assertSame(1, (int) DB::table('codigos_recuperacion')->value('intentos'));

        $this->verificar($codigo)->assertOk()->assertJsonPath('datos.valido', true);
    }

    public function test_al_quinto_fallo_el_codigo_ya_no_sirve(): void
    {
        $this->withoutMiddleware(ThrottleRequests::class);
        $codigo = $this->pedirCodigo();

        for ($i = 0; $i < 5; $i++) {
            $this->verificar($this->otro($codigo))->assertUnprocessable();
        }

        $this->verificar($codigo)->assertUnprocessable();
    }

    public function test_codigo_vencido(): void
    {
        $codigo = $this->pedirCodigo();

        $this->travel(16)->minutes();

        $this->verificar($codigo)->assertUnprocessable();
    }

    public function test_pedir_otro_anula_el_anterior(): void
    {
        $primero = $this->pedirCodigo();
        Mail::fake();
        $segundo = $this->pedirCodigo();

        if ($primero !== $segundo) {
            $this->verificar($primero)->assertUnprocessable();
        }
        $this->verificar($segundo)->assertOk();
        $this->assertSame(1, DB::table('codigos_recuperacion')->whereNull('usado_en')->count());
    }

    public function test_cambio_de_contrasena_cierra_todas_las_sesiones(): void
    {
        $tokenViejo = $this->postJson('/api/login', ['correo' => self::MARIA, 'password' => 'Usuario123*'])->json('datos.token');
        $codigo = $this->pedirCodigo();

        $this->cambiar($codigo)->assertOk()->assertJsonPath('mensaje', 'Contraseña actualizada. Inicia sesión.');

        $this->reiniciarAuth();
        $this->withToken($tokenViejo)->getJson('/api/sesion')->assertUnauthorized();
        $this->reiniciarAuth();
        $this->withToken('')->postJson('/api/login', ['correo' => self::MARIA, 'password' => 'nuevaClave1'])->assertOk();

        // El código ya se usó.
        $this->cambiar($codigo, ['password' => 'otraClave2', 'password_confirmation' => 'otraClave2'])->assertUnprocessable();
    }

    public function test_cambio_con_codigo_incorrecto_no_toca_la_contrasena(): void
    {
        $codigo = $this->pedirCodigo();

        $this->cambiar($this->otro($codigo))->assertUnprocessable()->assertJsonPath('errores.codigo.0', 'El código no es válido o ya venció.');

        $this->assertSame(1, (int) DB::table('codigos_recuperacion')->value('intentos'));
        $this->postJson('/api/login', ['correo' => self::MARIA, 'password' => 'Usuario123*'])->assertOk();
    }

    public function test_contrasena_sin_confirmacion(): void
    {
        $codigo = $this->pedirCodigo();

        $this->cambiar($codigo, ['password_confirmation' => 'distinta'])
            ->assertUnprocessable()
            ->assertJsonValidationErrors('password', 'errores');
    }
}
