package com.yelpme.app.api;

import com.yelpme.app.modelos.Servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo de servicios (GET /api/servicios) en memoria mientras la app vive:
 * es fijo, no hace falta pedirlo en cada pantalla.
 */
public final class Catalogo {

    private static List<Servicio> enMemoria;

    private Catalogo() {
    }

    /** Entrega el catálogo (de memoria o de la API) o el error. */
    public static void servicios(Llamada.Respuestas<List<Servicio>> respuestas) {
        if (enMemoria != null) {
            respuestas.alExito(enMemoria);
            return;
        }
        Llamada.de(ClienteApi.servicio(ServicioCatalogo.class).servicios()).enviar(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                enMemoria = Collections.unmodifiableList(new ArrayList<>(servicios != null ? servicios : Collections.emptyList()));
                respuestas.alExito(enMemoria);
            }

            @Override
            public void alError(ErrorApi error) {
                respuestas.alError(error);
            }
        });
    }
}
