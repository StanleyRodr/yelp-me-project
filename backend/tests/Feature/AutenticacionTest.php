<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Route;
use Tests\TestCase;

/**
 * Plan 003: registro, sesión de la app y del panel, roles y cuenta activa.
 * Usa los datos de demo del seeder (ver backend/README.md).
 */
class AutenticacionTest extends TestCase
{
    private const PASS_USUARIO = 'Usuario123*';
    private const PASS_ADMIN = 'Admin123*';

    private function registro(array $cambios = []): \Illuminate\Testing\TestResponse
    {
        return $this->postJson('/api/registro', array_merge([
            'nombre' => 'Rosa',
            'apellido_paterno' => 'Prueba',
            'apellido_materno' => 'Segunda',
            'telefono' => '2359990001', // teléfono del contacto "Mamá" de Carlos (no vinculado)
            'correo' => 'rosa@correo.com',
            'password' => 'secreto123',
            'password_confirmation' => 'secreto123',
        ], $cambios));
    }

    private function loginApp(array $datos): \Illuminate\Testing\TestResponse
    {
        return $this->postJson('/api/login', $datos);
    }

    private function token(string $correo, string $password, string $ruta = '/api/login'): string
    {
        $token = $this->postJson($ruta, ['correo' => $correo, 'password' => $password])->json('datos.token');
        $this->reiniciarAuth();

        return $token;
    }

    // ---------- Registro ----------

    public function test_registro_crea_usuario_con_token_y_vincula_contactos(): void
    {
        $respuesta = $this->registro(['correo' => ' ROSA@Correo.com ', 'telefono' => '235 999 0001']);

        $respuesta->assertCreated()
            ->assertJsonPath('ok', true)
            ->assertJsonPath('datos.usuario.rol', 'usuario')
            ->assertJsonPath('datos.usuario.correo', 'rosa@correo.com')
            ->assertJsonPath('datos.usuario.telefono', '2359990001')
            ->assertJsonPath('datos.contactos_vinculados', 1)
            ->assertJsonMissingPath('datos.usuario.password')
            ->assertJsonMissingPath('datos.usuario.fcm_token');

        $id = $respuesta->json('datos.usuario.id');
        $this->assertSame($id, DB::table('contactos')->where('telefono', '2359990001')->value('usuario_contacto_id'));
        $this->assertNotEmpty($respuesta->json('datos.token'));
    }

    public function test_registro_nunca_crea_admins(): void
    {
        $this->registro(['rol' => 'admin'])->assertCreated()->assertJsonPath('datos.usuario.rol', 'usuario');
    }

    public function test_registro_sin_apellido_materno(): void
    {
        $this->registro(['apellido_materno' => null])
            ->assertCreated()
            ->assertJsonPath('datos.usuario.apellido_paterno', 'Prueba')
            ->assertJsonPath('datos.usuario.apellido_materno', null);
    }

    public function test_registro_exige_apellido_paterno(): void
    {
        $this->registro(['apellido_paterno' => '  '])
            ->assertUnprocessable()
            ->assertJsonValidationErrors('apellido_paterno', 'errores');
    }

    public function test_registro_rechaza_duplicados_y_confirmacion_distinta(): void
    {
        $this->registro([
            'telefono' => '2351000001',
            'correo' => 'CARLOS@correo.com',
            'password_confirmation' => 'otra',
        ])->assertStatus(422)
            ->assertJsonPath('errores.telefono.0', 'El teléfono ya está registrado.')
            ->assertJsonPath('errores.correo.0', 'El correo ya está registrado.')
            ->assertJsonPath('errores.password.0', 'La confirmación de contraseña no coincide.');
    }

    // ---------- Sesión de la app ----------

    public function test_login_app_por_correo_guarda_fcm(): void
    {
        $this->loginApp(['correo' => 'carlos@correo.com', 'password' => self::PASS_USUARIO, 'fcm_token' => 'fcm-1'])
            ->assertOk()
            ->assertJsonPath('datos.usuario.nombre', 'Carlos');

        $this->assertSame('fcm-1', DB::table('usuarios')->where('correo', 'carlos@correo.com')->value('fcm_token'));
    }

    public function test_login_app_por_telefono(): void
    {
        $this->loginApp(['telefono' => '235-100-0001', 'password' => self::PASS_USUARIO])->assertOk();
    }

    public function test_login_app_mismo_mensaje_si_la_cuenta_no_existe(): void
    {
        $mala = $this->loginApp(['correo' => 'carlos@correo.com', 'password' => 'mala'])->assertStatus(401)->json('mensaje');
        $inexistente = $this->loginApp(['correo' => 'nadie@correo.com', 'password' => 'mala'])->assertStatus(401)->json('mensaje');

        $this->assertSame($mala, $inexistente);
    }

    public function test_login_app_pide_correo_o_telefono(): void
    {
        $this->loginApp(['password' => 'x'])->assertStatus(422)->assertJsonValidationErrorFor('correo', 'errores');
    }

    public function test_admin_no_entra_a_la_app(): void
    {
        $this->loginApp(['correo' => 'admin@yelpme.com', 'password' => self::PASS_ADMIN])
            ->assertForbidden()
            ->assertJsonPath('mensaje', 'Esta cuenta es de administrador. Inicia sesión en el panel web.');
    }

    public function test_usuario_suspendido_entra_y_se_informa(): void
    {
        $this->loginApp(['correo' => 'luis@correo.com', 'password' => self::PASS_USUARIO])
            ->assertOk()
            ->assertJsonPath('datos.usuario.suspendido', true)
            ->assertJsonPath('datos.usuario.strikes_activos', 3);
    }

    public function test_cuenta_deshabilitada_no_entra_ni_usa_token_previo(): void
    {
        $token = $this->token('jose@correo.com', self::PASS_USUARIO);
        DB::table('usuarios')->where('correo', 'jose@correo.com')->update(['activo' => false]);

        $this->loginApp(['correo' => 'jose@correo.com', 'password' => self::PASS_USUARIO])
            ->assertForbidden()->assertJsonPath('mensaje', 'Esta cuenta está deshabilitada.');

        $this->withToken($token)->getJson('/api/sesion')
            ->assertForbidden()->assertJsonPath('mensaje', 'Esta cuenta está deshabilitada.');
    }

    public function test_consultar_y_cerrar_sesion(): void
    {
        $this->loginApp(['correo' => 'carlos@correo.com', 'password' => self::PASS_USUARIO, 'fcm_token' => 'fcm-1']);
        $this->reiniciarAuth();
        $token = $this->token('carlos@correo.com', self::PASS_USUARIO);
        $otro = $this->token('carlos@correo.com', self::PASS_USUARIO);

        $this->withToken($token)->getJson('/api/sesion')->assertOk()->assertJsonPath('datos.correo', 'carlos@correo.com');
        $this->reiniciarAuth();

        $this->withToken($token)->deleteJson('/api/sesion')->assertOk()->assertJsonPath('mensaje', 'Sesión cerrada');
        $this->reiniciarAuth();

        $this->assertNull(DB::table('usuarios')->where('correo', 'carlos@correo.com')->value('fcm_token'));
        $this->withToken($token)->getJson('/api/sesion')->assertUnauthorized();
        $this->reiniciarAuth();
        $this->withToken($otro)->getJson('/api/sesion')->assertOk();
    }

    public function test_sesion_sin_token_responde_401_json(): void
    {
        $this->get('/api/sesion', ['Accept' => 'text/html'])
            ->assertUnauthorized()
            ->assertJsonPath('mensaje', 'No autenticado. Inicia sesión.');
    }

    // ---------- Panel ----------

    public function test_login_panel_admin(): void
    {
        $this->postJson('/api/admin/login', ['correo' => ' Admin@YelpMe.com ', 'password' => self::PASS_ADMIN])
            ->assertOk()
            ->assertJsonPath('datos.usuario.rol', 'admin');

        $this->assertSame('panel', DB::table('personal_access_tokens')->latest('id')->value('name'));
    }

    public function test_usuario_no_entra_al_panel(): void
    {
        $this->postJson('/api/admin/login', ['correo' => 'carlos@correo.com', 'password' => self::PASS_USUARIO])
            ->assertForbidden()
            ->assertJsonPath('mensaje', 'Esta cuenta no tiene acceso al panel de administración.');
    }

    // ---------- Roles ----------

    public function test_rutas_protegidas_por_rol(): void
    {
        Route::middleware(['auth:sanctum', 'cuenta.activa', 'rol:admin'])->get('/api/admin/prueba-rol', fn () => response()->json(['ok' => true]));
        Route::middleware(['auth:sanctum', 'cuenta.activa', 'rol:usuario'])->get('/api/prueba-rol', fn () => response()->json(['ok' => true]));

        $admin = $this->token('admin@yelpme.com', self::PASS_ADMIN, '/api/admin/login');
        $usuario = $this->token('carlos@correo.com', self::PASS_USUARIO);

        $casos = [
            [$admin, '/api/admin/prueba-rol', 200],
            [$usuario, '/api/admin/prueba-rol', 403],
            [$admin, '/api/prueba-rol', 403],
            [$usuario, '/api/prueba-rol', 200],
        ];

        foreach ($casos as [$token, $ruta, $codigo]) {
            $this->withToken($token)->getJson($ruta)->assertStatus($codigo);
            $this->reiniciarAuth();
        }
    }

    // ---------- Límite de intentos ----------

    public function test_sexto_intento_en_un_minuto_responde_429(): void
    {
        foreach (range(1, 5) as $_) {
            $this->loginApp(['correo' => 'ana@correo.com', 'password' => 'mala'])->assertStatus(401);
        }

        $this->loginApp(['correo' => 'ana@correo.com', 'password' => 'mala'])
            ->assertStatus(429)
            ->assertJsonPath('mensaje', 'Demasiados intentos. Intenta más tarde.');
    }
}
