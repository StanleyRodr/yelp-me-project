<?php

namespace Tests\Unit;

use App\Support\TransicionesSolicitud;
use PHPUnit\Framework\TestCase;

/**
 * Matriz completa (2 roles × con/sin servicios × 5 × 5 estados = 100 casos)
 * contra la tabla 7.2 del plan principal. Cualquier combinación no listada
 * aquí debe estar prohibida.
 */
class TransicionesSolicitudTest extends TestCase
{
    private const ESTADOS = ['Pendiente', 'Enviado', 'Rechazado', 'Cancelado', 'Terminado'];

    /** Plan principal 7.2: [rol, conServicios, desde, hacia] permitidos. */
    private const PERMITIDAS = [
        ['admin', true, 'Pendiente', 'Enviado'],
        ['admin', true, 'Pendiente', 'Rechazado'],
        ['admin', true, 'Enviado', 'Terminado'],
        ['admin', false, 'Pendiente', 'Rechazado'],
        ['usuario', true, 'Pendiente', 'Cancelado'],
        ['usuario', true, 'Enviado', 'Terminado'],
        ['usuario', true, 'Enviado', 'Cancelado'],
        ['usuario', false, 'Pendiente', 'Terminado'],
        ['usuario', false, 'Pendiente', 'Cancelado'],
    ];

    public function test_matriz_completa_coincide_con_el_plan(): void
    {
        $revisadas = 0;

        foreach (['admin', 'usuario'] as $rol) {
            foreach ([true, false] as $conServicios) {
                foreach (self::ESTADOS as $desde) {
                    foreach (self::ESTADOS as $hacia) {
                        $esperado = in_array([$rol, $conServicios, $desde, $hacia], self::PERMITIDAS, true);
                        $this->assertSame(
                            $esperado,
                            TransicionesSolicitud::permite($rol, $conServicios, $desde, $hacia),
                            "{$rol} ".($conServicios ? 'con' : 'sin')." servicios: {$desde} → {$hacia}"
                        );
                        $revisadas++;
                    }
                }
            }
        }

        $this->assertSame(100, $revisadas);
    }

    public function test_estados_finales_no_tienen_salida(): void
    {
        foreach (TransicionesSolicitud::FINALES as $final) {
            foreach (['admin', 'usuario'] as $rol) {
                $this->assertSame([], TransicionesSolicitud::permitidas($rol, true, $final));
                $this->assertSame([], TransicionesSolicitud::permitidas($rol, false, $final));
            }
        }
    }

    public function test_admin_edita_servicios_solo_con_servicios_en_pendiente_o_enviado(): void
    {
        $this->assertTrue(TransicionesSolicitud::adminPuedeEditarServicios('Pendiente', true));
        $this->assertTrue(TransicionesSolicitud::adminPuedeEditarServicios('Enviado', true));
        $this->assertFalse(TransicionesSolicitud::adminPuedeEditarServicios('Pendiente', false));
        $this->assertFalse(TransicionesSolicitud::adminPuedeEditarServicios('Terminado', true));
    }
}
