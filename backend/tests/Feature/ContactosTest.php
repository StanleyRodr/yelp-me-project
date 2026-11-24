<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 004: CRUD de contactos y verificación de teléfono.
 * Demo: Carlos tiene a María y José (vinculados) y a "Mamá" (sin cuenta);
 * José bloqueó a Ana.
 */
class ContactosTest extends TestCase
{
    private const CARLOS = 'carlos@correo.com';
    private const ANA = 'ana@correo.com';

    private function crear(array $cambios = []): \Illuminate\Testing\TestResponse
    {
        return $this->postJson('/api/contactos', array_merge([
            'nombre' => 'Ana Torres',
            'telefono' => '2351000004',
            'parentesco' => 'Amistad',
        ], $cambios));
    }

    public function test_lista_solo_mi_agenda_ordenada_y_sin_datos_de_la_cuenta(): void
    {
        $this->como(self::CARLOS);

        $respuesta = $this->getJson('/api/contactos')->assertOk();

        $this->assertSame(['José Hernández', 'Mamá', 'María López'], array_column($respuesta->json('datos'), 'nombre'));
        $this->assertSame([true, false, true], array_column($respuesta->json('datos'), 'vinculado'));
        $respuesta->assertJsonMissingPath('datos.0.usuario_contacto_id');
    }

    public function test_crear_normaliza_y_vincula_si_tiene_cuenta(): void
    {
        $this->como(self::CARLOS);

        $this->crear(['nombre' => '  Ana Torres ', 'telefono' => '235-100-0004'])
            ->assertCreated()
            ->assertJsonPath('mensaje', 'Contacto agregado')
            ->assertJsonPath('datos.nombre', 'Ana Torres')
            ->assertJsonPath('datos.telefono', '2351000004')
            ->assertJsonPath('datos.vinculado', true);
    }

    public function test_crear_sin_cuenta_queda_sin_vincular(): void
    {
        $this->como(self::CARLOS);
        $this->crear(['telefono' => '2359990077'])->assertCreated()->assertJsonPath('datos.vinculado', false);
    }

    public function test_crear_no_vincula_si_esa_cuenta_me_bloqueo(): void
    {
        $this->como(self::ANA);
        $this->crear(['nombre' => 'José', 'telefono' => '2351000003'])->assertCreated()->assertJsonPath('datos.vinculado', false);
    }

    public function test_crear_no_vincula_cuentas_de_admin(): void
    {
        $this->como(self::CARLOS);
        $this->crear(['nombre' => 'Admin', 'telefono' => '0000000000'])->assertCreated()->assertJsonPath('datos.vinculado', false);
    }

    public function test_crear_valida_campos_duplicado_y_numero_propio(): void
    {
        $this->como(self::CARLOS);

        $this->postJson('/api/contactos', [])->assertStatus(422)
            ->assertJsonValidationErrors(['nombre', 'telefono', 'parentesco'], 'errores');
        $this->crear(['telefono' => '2351000002'])->assertStatus(422)
            ->assertJsonPath('errores.telefono.0', 'Ya tienes un contacto con ese teléfono.');
        $this->crear(['telefono' => '2351000001'])->assertStatus(422)
            ->assertJsonPath('errores.telefono.0', 'No puedes agregarte a ti mismo como contacto.');
    }

    public function test_modificar_parcial_conserva_vinculo_y_cambiar_telefono_lo_recalcula(): void
    {
        $this->como(self::CARLOS);
        $id = $this->crear()->json('datos.id');

        $this->patchJson("/api/contactos/{$id}", ['parentesco' => 'Compañera'])
            ->assertOk()
            ->assertJsonPath('datos.parentesco', 'Compañera')
            ->assertJsonPath('datos.vinculado', true);

        $this->putJson("/api/contactos/{$id}", ['nombre' => 'Ana', 'telefono' => '2359990050', 'parentesco' => 'Amistad'])
            ->assertOk()
            ->assertJsonPath('datos.vinculado', false);

        $this->patchJson("/api/contactos/{$id}", ['telefono' => '2359990050'])->assertOk();
        $this->patchJson("/api/contactos/{$id}", ['telefono' => '2351000002'])->assertStatus(422);
    }

    public function test_eliminar_borra_el_contacto(): void
    {
        $this->como(self::CARLOS);
        $id = $this->crear()->json('datos.id');

        $this->deleteJson("/api/contactos/{$id}")->assertOk()->assertJsonPath('mensaje', 'Contacto eliminado');
        $this->getJson("/api/contactos/{$id}")->assertNotFound();
        $this->assertFalse(DB::table('contactos')->where('id', $id)->exists());
    }

    public function test_contactos_ajenos_responden_404(): void
    {
        $this->como(self::CARLOS);
        $id = $this->crear()->json('datos.id');

        $this->como(self::ANA);
        $this->getJson("/api/contactos/{$id}")->assertNotFound();
        $this->patchJson("/api/contactos/{$id}", ['nombre' => 'X'])->assertNotFound();
        $this->deleteJson("/api/contactos/{$id}")->assertNotFound();

        $this->assertTrue(DB::table('contactos')->where('id', $id)->exists());
    }

    public function test_admin_y_sin_token_no_usan_contactos(): void
    {
        $this->getJson('/api/contactos')->assertUnauthorized();

        $this->como('admin@yelpme.com');
        $this->getJson('/api/contactos')->assertForbidden();
    }

    public function test_verificar_telefono_no_distingue_bloqueo_de_sin_cuenta(): void
    {
        $this->como(self::CARLOS);
        $this->getJson('/api/telefonos/235-100-0004')->assertOk()
            ->assertExactJson(['ok' => true, 'mensaje' => 'OK', 'datos' => ['telefono' => '2351000004', 'vinculable' => true, 'en_agenda' => false]]);
        $this->getJson('/api/telefonos/2351000002')->assertJsonPath('datos.en_agenda', true);
        $this->getJson('/api/telefonos/123')->assertStatus(422);

        $this->como(self::ANA);
        $bloqueado = $this->getJson('/api/telefonos/2351000003')->json('datos');
        $sinCuenta = $this->getJson('/api/telefonos/2359990099')->json('datos');
        $this->assertSame([false, false], [$bloqueado['vinculable'], $sinCuenta['vinculable']]);
    }
}
