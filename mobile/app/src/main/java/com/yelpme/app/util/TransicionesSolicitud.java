package com.yelpme.app.util;

import com.yelpme.app.modelos.Solicitud;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Acciones del usuario sobre su solicitud. Espejo del lado usuario de
 * backend/app/Support/TransicionesSolicitud.php (cambiar ambos). Solo decide
 * qué botones mostrar; la API valida.
 */
public final class TransicionesSolicitud {

    public enum Accion { EDITAR, TERMINAR, CANCELAR }

    public static final String PENDIENTE = "Pendiente";
    public static final String ENVIADO = "Enviado";
    public static final String RECHAZADO = "Rechazado";
    public static final String CANCELADO = "Cancelado";
    public static final String TERMINADO = "Terminado";

    private TransicionesSolicitud() {
    }

    /**
     * Con servicios: Pendiente → editar, cancelar; Enviado → terminar, cancelar.
     * Sin servicios: Pendiente → editar, terminar, cancelar. Finales: ninguna.
     */
    public static List<Accion> delUsuario(Solicitud s) {
        boolean conServicios = s.servicios != null && !s.servicios.isEmpty();
        if (PENDIENTE.equals(s.estado)) {
            return conServicios
                    ? Arrays.asList(Accion.EDITAR, Accion.CANCELAR)
                    : Arrays.asList(Accion.EDITAR, Accion.TERMINAR, Accion.CANCELAR);
        }
        if (ENVIADO.equals(s.estado) && conServicios) {
            return Arrays.asList(Accion.TERMINAR, Accion.CANCELAR);
        }
        return Collections.emptyList();
    }

    /** Pendiente o Enviado: la que se muestra arriba en Inicio. */
    public static boolean esActiva(Solicitud s) {
        return PENDIENTE.equals(s.estado) || ENVIADO.equals(s.estado);
    }
}
