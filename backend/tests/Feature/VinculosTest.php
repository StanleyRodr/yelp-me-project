<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 004: quién me tiene como contacto, desvincularme, bloquear y desbloquear.
 * Demo: Carlos está en las agendas de María y José.
 */
class VinculosTest extends TestCase
{
    private const CARLOS = 'carlos@correo.com';
    private const MARIA = 'maria@correo.com';
    private const JOSE = 'jose@correo.com';

    /** Id del contacto que `$duenoCorreo` tiene apuntando a Carlos. */
    private function vinculoDe(string $duenoCorreo): int
    {
        $duenoId = DB::table('usuarios')->where('correo', $duenoCorreo)->value('id');
        $carlosId = DB::table('usuarios')->where('correo', self::CARLOS)->value('id');

        return DB::table('contactos')->where('usuario_id', $duenoId)->where('usuario_contacto_id', $carlosId)->value('id');
    }

    private function carlosEstaEnAgendaDe(string $correo): bool
    {
        return DB::table('contactos')
            ->where('usuario_id', DB::table('usuarios')->where('correo', $correo)->value('id'))
            ->where('telefono', '2351000001')
            ->exists();
    }

    public function test_veo_quien_me_tiene_con_datos_de_su_cuenta(): void
    {
        $this->como(self::CARLOS);

        $vinculos = $this->getJson('/api/vinculos')->assertOk()->json('datos');

        $this->assertSame(['José', 'María'], array_column($vinculos, 'nombre'));
        $this->assertSame('López', $vinculos[1]['apellido_paterno']);
        $this->assertSame('Díaz', $vinculos[1]['apellido_materno']);
    }

    public function test_desvincularme_me_borra_de_su_agenda_sin_bloquear(): void
    {
        $id = $this->vinculoDe(self::MARIA);
        $this->como(self::CARLOS);

        $this->deleteJson("/api/vinculos/{$id}")->assertOk();
        $this->assertFalse($this->carlosEstaEnAgendaDe(self::MARIA));

        $this->como(self::MARIA);
        $this->postJson('/api/contactos', ['nombre' => 'Carlos', 'telefono' => '2351000001', 'parentesco' => 'Pareja'])
            ->assertCreated()->assertJsonPath('datos.vinculado', true);
    }

    public function test_bloquear_borra_el_contacto_e_impide_volver_a_vincular(): void
    {
        $id = $this->vinculoDe(self::JOSE);
        $this->como(self::CARLOS);

        $this->postJson('/api/bloqueos', ['contacto_id' => $id])
            ->assertCreated()
            ->assertJsonPath('datos.nombre', 'José');
        $this->assertFalse($this->carlosEstaEnAgendaDe(self::JOSE));

        $this->como(self::JOSE);
        $this->postJson('/api/contactos', ['nombre' => 'Carlos', 'telefono' => '2351000001', 'parentesco' => 'Amistad'])
            ->assertCreated()->assertJsonPath('datos.vinculado', false);
        $this->getJson('/api/telefonos/2351000001')->assertJsonPath('datos.vinculable', false);
    }

    public function test_desbloquear_vincula_el_contacto_que_ya_me_habia_guardado(): void
    {
        $id = $this->vinculoDe(self::JOSE);
        $this->como(self::CARLOS);
        $bloqueo = $this->postJson('/api/bloqueos', ['contacto_id' => $id])->json('datos.id');

        $this->como(self::JOSE);
        $contacto = $this->postJson('/api/contactos', ['nombre' => 'Carlos', 'telefono' => '2351000001', 'parentesco' => 'Amistad'])->json('datos.id');

        $this->como(self::CARLOS);
        $this->getJson('/api/bloqueos')->assertOk()->assertJsonCount(1, 'datos');
        $this->deleteJson("/api/bloqueos/{$bloqueo}")->assertOk()->assertJsonPath('mensaje', 'Usuario desbloqueado');

        $this->como(self::JOSE);
        $this->getJson("/api/contactos/{$contacto}")->assertJsonPath('datos.vinculado', true);
    }

    public function test_vinculos_y_bloqueos_ajenos_responden_404(): void
    {
        $id = $this->vinculoDe(self::JOSE);

        $this->como('ana@correo.com');
        $this->deleteJson("/api/vinculos/{$id}")->assertNotFound();
        $this->postJson('/api/bloqueos', ['contacto_id' => $id])->assertNotFound();
        $this->postJson('/api/bloqueos', [])->assertStatus(422);

        $this->como(self::CARLOS);
        $bloqueo = $this->postJson('/api/bloqueos', ['contacto_id' => $id])->json('datos.id');

        $this->como(self::JOSE);
        $this->deleteJson("/api/bloqueos/{$bloqueo}")->assertNotFound();
    }

    public function test_admin_no_usa_vinculos_ni_bloqueos(): void
    {
        $this->como('admin@yelpme.com');
        $this->getJson('/api/vinculos')->assertForbidden();
        $this->getJson('/api/bloqueos')->assertForbidden();
    }

    public function test_vinculos_traen_el_domicilio_de_quien_me_tiene(): void
    {
        // Ana (con domicilio) guarda a Carlos.
        $this->como('ana@correo.com');
        $this->postJson('/api/contactos', ['nombre' => 'Carlos', 'telefono' => '2351000001', 'parentesco' => 'Amistad'])->assertCreated();

        $this->como(self::CARLOS);
        $lista = $this->getJson('/api/vinculos')->assertOk()->json('datos');
        $porNombre = array_column($lista, 'domicilio', 'nombre');
        $this->assertSame('Calle Comonfort 37-B, Ojite, 93821', $porNombre['Ana']['texto']);
        $this->assertNull($porNombre['María']); // María no registró domicilio
        $this->assertCount(3, $lista); // Ana, José y María; nadie más

        // Bloquear a Ana la saca de la lista (y su domicilio con ella).
        $vinculoAna = collect($lista)->firstWhere('nombre', 'Ana')['id'];
        $this->postJson('/api/bloqueos', ['contacto_id' => $vinculoAna])->assertSuccessful();
        $this->assertNotContains('Ana', array_column($this->getJson('/api/vinculos')->json('datos'), 'nombre'));
    }
}
