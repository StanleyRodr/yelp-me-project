package com.yelpme.app.util;

import static com.yelpme.app.util.TransicionesSolicitud.Accion.CANCELAR;
import static com.yelpme.app.util.TransicionesSolicitud.Accion.EDITAR;
import static com.yelpme.app.util.TransicionesSolicitud.Accion.TERMINAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.yelpme.app.modelos.Solicitud;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Mismas reglas que TransicionesSolicitud.php (lado usuario): 5 estados × con/sin servicios. */
public class TransicionesSolicitudTest {

    private static Solicitud solicitud(String estado, boolean conServicios) {
        Solicitud s = new Solicitud();
        s.estado = estado;
        s.servicios = conServicios ? Arrays.asList(1L, 2L) : Collections.emptyList();
        return s;
    }

    private static List<TransicionesSolicitud.Accion> acciones(String estado, boolean conServicios) {
        return TransicionesSolicitud.delUsuario(solicitud(estado, conServicios));
    }

    @Test
    public void conServicios() {
        assertEquals(Arrays.asList(EDITAR, CANCELAR), acciones("Pendiente", true));
        assertEquals(Arrays.asList(TERMINAR, CANCELAR), acciones("Enviado", true));
        assertTrue(acciones("Rechazado", true).isEmpty());
        assertTrue(acciones("Cancelado", true).isEmpty());
        assertTrue(acciones("Terminado", true).isEmpty());
    }

    @Test
    public void sinServicios() {
        assertEquals(Arrays.asList(EDITAR, TERMINAR, CANCELAR), acciones("Pendiente", false));
        // Sin servicios el admin no la envía: Enviado no ocurre, y si llegara no hay acciones.
        assertTrue(acciones("Enviado", false).isEmpty());
        assertTrue(acciones("Rechazado", false).isEmpty());
        assertTrue(acciones("Cancelado", false).isEmpty());
        assertTrue(acciones("Terminado", false).isEmpty());
    }

    @Test
    public void soloPendienteYEnviadoSonActivas() {
        assertTrue(TransicionesSolicitud.esActiva(solicitud("Pendiente", true)));
        assertTrue(TransicionesSolicitud.esActiva(solicitud("Enviado", true)));
        assertFalse(TransicionesSolicitud.esActiva(solicitud("Terminado", true)));
        assertFalse(TransicionesSolicitud.esActiva(solicitud("Rechazado", false)));
        assertFalse(TransicionesSolicitud.esActiva(solicitud("Cancelado", false)));
    }
}
