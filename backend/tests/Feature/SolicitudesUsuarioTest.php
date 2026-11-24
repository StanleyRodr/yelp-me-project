<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Plan 005: solicitudes desde la app. Demo (por título):
 * Carlos: "Incendio…" Pendiente con servicios, "Persona lesionada…" Enviado,
 * "Perro agresivo…" Cancelado. María: "Me siento insegura…" Pendiente sin servicios.
 * Luis está suspendido.
 */
class SolicitudesUsuarioTest extends TestCase
{
    private const CARLOS = 'carlos@correo.com';

    private function id(string $inicioTitulo): int
    {
        return DB::table('solicitudes')->where('titulo', 'like', $inicioTitulo.'%')->value('id');
    }

    private function crear(array $cambios = []): \Illuminate\Testing\TestResponse
    {
        return $this->postJson('/api/solicitudes', array_merge([
            'titulo' => 'Choque en el centro',
            'latitud' => 19.9301,
            'longitud' => -96.8511,
            'ubicacion_nombre' => 'Centro',
            'servicios' => [4, 2],
        ], $cambios));
    }

    public function test_catalogo_de_servicios_para_cualquier_rol(): void
    {
        $this->como(self::CARLOS);
        $this->getJson('/api/servicios')->assertOk()->assertJsonCount(6, 'datos')->assertJsonPath('datos.0.icono', 'local_police');

        $this->como('admin@yelpme.com');
        $this->getJson('/api/servicios')->assertOk();
    }

    public function test_lista_mis_solicitudes_con_filtro(): void
    {
        $this->como(self::CARLOS);

        $this->assertCount(3, $this->getJson('/api/solicitudes')->assertOk()->json('datos'));
        $this->getJson('/api/solicitudes?estado=Pendiente')->assertJsonCount(1, 'datos');
        $this->getJson('/api/solicitudes?estado=Asignada')->assertStatus(422);
    }

    public function test_crear_con_la_forma_del_frontend(): void
    {
        $this->como(self::CARLOS);

        $this->crear(['titulo' => '  Choque en el centro ', 'descripcion' => '  '])
            ->assertCreated()
            ->assertJsonPath('mensaje', 'Solicitud enviada')
            ->assertJsonPath('datos.estado', 'Pendiente')
            ->assertJsonPath('datos.titulo', 'Choque en el centro')
            ->assertJsonPath('datos.descripcion', null)
            ->assertJsonPath('datos.servicios', [2, 4])
            ->assertJsonPath('datos.usuario.nombre', 'Carlos')
            ->assertJsonPath('datos.admin', null)
            ->assertJsonStructure(['datos' => ['id', 'latitud', 'longitud', 'created_at', 'usuario' => ['telefono']]]);
    }

    public function test_crear_valida_campos(): void
    {
        $this->como(self::CARLOS);

        $this->postJson('/api/solicitudes', [])->assertStatus(422)
            ->assertJsonValidationErrors(['titulo', 'latitud', 'longitud'], 'errores');
        $this->crear(['latitud' => 100, 'longitud' => -200])->assertStatus(422)
            ->assertJsonValidationErrors(['latitud', 'longitud'], 'errores');
        $this->crear(['servicios' => [1, 1, 99]])->assertStatus(422);
    }

    public function test_suspendido_solo_avisa_a_sus_contactos(): void
    {
        $this->como('luis@correo.com');

        $this->crear()->assertForbidden()
            ->assertJsonPath('mensaje', 'Tu cuenta está suspendida: solo puedes avisar a tus contactos.');

        $id = $this->crear(['servicios' => []])->assertCreated()->json('datos.id');
        $this->patchJson("/api/solicitudes/{$id}", ['servicios' => [1]])->assertForbidden();
        $this->patchJson("/api/solicitudes/{$id}", ['titulo' => 'Necesito compañía'])->assertOk();
    }

    public function test_modificar_solo_pendiente(): void
    {
        $this->como(self::CARLOS);
        $id = $this->crear()->json('datos.id');

        $this->patchJson("/api/solicitudes/{$id}", ['titulo' => 'Choque con heridos', 'servicios' => [2]])
            ->assertOk()
            ->assertJsonPath('datos.titulo', 'Choque con heridos')
            ->assertJsonPath('datos.servicios', [2]);

        $this->patchJson("/api/solicitudes/{$id}", ['servicios' => []])->assertOk()->assertJsonPath('datos.servicios', []);

        $this->patchJson('/api/solicitudes/'.$this->id('Persona lesionada'), ['titulo' => 'X'])->assertStatus(409);
    }

    public function test_solicitudes_ajenas_responden_404_y_no_hay_borrado(): void
    {
        $id = $this->id('Incendio en pastizal');
        $this->como('ana@correo.com');

        $this->getJson("/api/solicitudes/{$id}")->assertNotFound();
        $this->patchJson("/api/solicitudes/{$id}", ['titulo' => 'X'])->assertNotFound();
        $this->putJson("/api/solicitudes/{$id}/estado", ['estado' => 'Cancelado', 'motivo' => 'x'])->assertNotFound();

        $this->como(self::CARLOS);
        $this->deleteJson("/api/solicitudes/{$id}")->assertStatus(405);
    }

    public function test_cancelar_exige_motivo_y_registra_el_cierre(): void
    {
        $id = $this->id('Incendio en pastizal');
        $this->como(self::CARLOS);

        $this->putJson("/api/solicitudes/{$id}/estado", ['estado' => 'Cancelado'])->assertStatus(422)
            ->assertJsonPath('errores.motivo.0', 'Escribe el motivo de la cancelación.');

        $this->putJson("/api/solicitudes/{$id}/estado", ['estado' => 'Cancelado', 'motivo' => ' Ya se apagó '])
            ->assertOk()
            ->assertJsonPath('mensaje', 'Solicitud cancelada')
            ->assertJsonPath('datos.estado', 'Cancelado')
            ->assertJsonPath('datos.motivo', 'Ya se apagó')
            ->assertJsonPath('datos.cerrado_por.nombre', 'Carlos');

        $this->putJson("/api/solicitudes/{$id}/estado", ['estado' => 'Cancelado', 'motivo' => 'x'])->assertStatus(409);
    }

    public function test_terminar_segun_la_tabla(): void
    {
        $this->como(self::CARLOS);
        $this->putJson('/api/solicitudes/'.$this->id('Incendio en pastizal').'/estado', ['estado' => 'Terminado'])->assertStatus(409);
        $this->putJson('/api/solicitudes/'.$this->id('Persona lesionada').'/estado', ['estado' => 'Terminado'])
            ->assertOk()
            ->assertJsonPath('datos.admin.nombre', 'Administrador')
            ->assertJsonPath('datos.cerrado_por.nombre', 'Carlos');
        $this->putJson('/api/solicitudes/'.$this->id('Perro agresivo').'/estado', ['estado' => 'Terminado'])->assertStatus(409);

        $this->como('maria@correo.com');
        $this->putJson('/api/solicitudes/'.$this->id('Me siento insegura').'/estado', ['estado' => 'Terminado'])->assertOk();
    }

    public function test_admin_no_usa_las_rutas_de_la_app(): void
    {
        $this->como('admin@yelpme.com');
        $this->getJson('/api/solicitudes')->assertForbidden();
        $this->postJson('/api/solicitudes', [])->assertForbidden();
    }
}
