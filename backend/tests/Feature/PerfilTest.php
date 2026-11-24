<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

/**
 * Plan 006: perfil propio (datos, teléfono y agendas, foto, contraseña).
 */
class PerfilTest extends TestCase
{
    private const CARLOS = 'carlos@correo.com';

    private function id(string $correo): int
    {
        return DB::table('usuarios')->where('correo', $correo)->value('id');
    }

    public function test_actualizar_datos_valida_unicos_ignorando_los_propios(): void
    {
        $this->como(self::CARLOS);

        $this->patchJson('/api/perfil', ['telefono' => '2351000002'])->assertStatus(422)
            ->assertJsonPath('errores.telefono.0', 'El teléfono ya está registrado.');
        $this->patchJson('/api/perfil', ['correo' => 'CARLOS@correo.com'])->assertOk();
        $this->patchJson('/api/perfil', ['nombre' => ' Carlos Alberto '])->assertOk()
            ->assertJsonPath('mensaje', 'Perfil actualizado')
            ->assertJsonPath('datos.nombre', 'Carlos Alberto');
    }

    public function test_cambiar_telefono_actualiza_las_agendas_que_me_tienen(): void
    {
        $carlos = $this->id(self::CARLOS);
        $this->como(self::CARLOS);

        $this->patchJson('/api/perfil', ['telefono' => '235-100-0099'])->assertOk()->assertJsonPath('datos.telefono', '2351000099');

        $telefonos = DB::table('contactos')->where('usuario_contacto_id', $carlos)->pluck('telefono')->unique()->values()->all();
        $this->assertSame(['2351000099'], $telefonos);
    }

    public function test_cambiar_telefono_respeta_conflictos_y_vincula_contactos_sueltos(): void
    {
        $jose = $this->id('jose@correo.com');
        $carlos = $this->id(self::CARLOS);
        $maria = $this->id('maria@correo.com');
        $ana = $this->id('ana@correo.com');
        $fila = fn ($dueno, $nombre, $tel, $vinculo = null) => DB::table('contactos')->insertGetId([
            'usuario_id' => $dueno, 'nombre' => $nombre, 'telefono' => $tel, 'parentesco' => 'X',
            'usuario_contacto_id' => $vinculo, 'created_at' => now(), 'updated_at' => now(),
        ]);
        $viejoMaria = $fila($maria, 'José', '2351000003', $jose);
        $sueltoMaria = $fila($maria, 'Otro', '2359990077');
        $sueltoAna = $fila($ana, 'José nuevo', '2359990077'); // José bloqueó a Ana

        $this->como('jose@correo.com');
        $this->patchJson('/api/perfil', ['telefono' => '2359990077'])->assertOk();

        $this->assertSame('2359990077', DB::table('contactos')->where('usuario_id', $carlos)->where('usuario_contacto_id', $jose)->value('telefono'));
        $this->assertSame('2351000003', DB::table('contactos')->where('id', $viejoMaria)->value('telefono'));
        $this->assertSame($jose, DB::table('contactos')->where('id', $sueltoMaria)->value('usuario_contacto_id'));
        $this->assertNull(DB::table('contactos')->where('id', $sueltoAna)->value('usuario_contacto_id'));
    }

    public function test_foto_valida_reemplaza_y_quita_sin_dejar_archivos(): void
    {
        Storage::fake('perfiles');
        $carlos = $this->id(self::CARLOS);
        $this->como(self::CARLOS);

        $this->post('/api/perfil/foto', ['foto' => \Illuminate\Http\UploadedFile::fake()->create('doc.pdf', 10, 'application/pdf')], ['Accept' => 'application/json'])
            ->assertStatus(422)->assertJsonPath('errores.foto.0', 'La foto debe ser JPG, PNG o WEBP.');
        $this->post('/api/perfil/foto', ['foto' => $this->imagenPng('grande.png', 1100)], ['Accept' => 'application/json'])
            ->assertStatus(422)->assertJsonPath('errores.foto.0', 'La foto pesa más de 1 MB. Elige una más ligera.');

        $this->post('/api/perfil/foto', ['foto' => $this->imagenPng()], ['Accept' => 'application/json'])
            ->assertOk()->assertJsonPath('mensaje', 'Foto actualizada');
        $primera = DB::table('usuarios')->where('id', $carlos)->value('foto_perfil');
        Storage::disk('perfiles')->assertExists($primera);

        $this->post('/api/perfil/foto', ['foto' => $this->imagenPng('otra.png')], ['Accept' => 'application/json'])->assertOk();
        Storage::disk('perfiles')->assertMissing($primera);
        $segunda = DB::table('usuarios')->where('id', $carlos)->value('foto_perfil');

        $this->deleteJson('/api/perfil/foto')->assertOk()->assertJsonPath('datos.foto_perfil', null);
        Storage::disk('perfiles')->assertMissing($segunda);
    }

    public function test_cambiar_contrasena_valida_y_cierra_otras_sesiones(): void
    {
        $carlos = User::where('correo', self::CARLOS)->first();
        $actual = $carlos->createToken('app')->plainTextToken;
        $otra = $carlos->createToken('app')->plainTextToken;
        $cambio = fn (array $datos) => $this->withToken($actual)->putJson('/api/perfil/password', $datos);

        $cambio(['actual' => 'mala', 'nueva' => 'nuevaClave1', 'nueva_confirmation' => 'nuevaClave1'])
            ->assertStatus(422)->assertJsonPath('errores.actual.0', 'La contraseña actual no es correcta.');
        $this->reiniciarAuth();
        $cambio(['actual' => 'Usuario123*', 'nueva' => 'Usuario123*', 'nueva_confirmation' => 'Usuario123*'])
            ->assertStatus(422)->assertJsonPath('errores.nueva.0', 'La nueva contraseña debe ser distinta de la actual.');
        $this->reiniciarAuth();
        $cambio(['actual' => 'Usuario123*', 'nueva' => 'nuevaClave1', 'nueva_confirmation' => 'nuevaClave1'])->assertOk();

        $this->reiniciarAuth();
        $this->withToken($actual)->getJson('/api/sesion')->assertOk();
        $this->reiniciarAuth();
        $this->withToken($otra)->getJson('/api/sesion')->assertUnauthorized();
        $this->reiniciarAuth();
        $this->postJson('/api/login', ['correo' => self::CARLOS, 'password' => 'nuevaClave1'])->assertOk();
    }

    public function test_admin_tambien_edita_su_perfil(): void
    {
        $this->como('admin@yelpme.com');
        $this->patchJson('/api/perfil', ['apellidos' => 'Principal'])->assertOk()->assertJsonPath('datos.apellidos', 'Principal');
    }
}
