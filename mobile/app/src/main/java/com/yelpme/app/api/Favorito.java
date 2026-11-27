package com.yelpme.app.api;

import androidx.annotation.Nullable;

import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.util.Sesion;

import java.util.List;

/**
 * Contacto favorito del usuario en memoria (botón de la barra). Se guarda por
 * usuario: otra sesión vuelve a pedirlo. Lo actualiza la lista de contactos y
 * se invalida cuando el detalle o el formulario cambian algo.
 */
public final class Favorito {

    private static long usuarioId = -1;
    private static boolean cargado;
    @Nullable
    private static Contacto favorito;

    private Favorito() {
    }

    /** Callback con el favorito o null (sin favorito o sin conexión). */
    public interface Respuesta {
        void listo(@Nullable Contacto favorito);
    }

    public static void obtener(Sesion sesion, Respuesta respuesta) {
        Usuario u = sesion.usuario();
        long id = u != null ? u.id : -1;
        if (cargado && id == usuarioId) {
            respuesta.listo(favorito);
            return;
        }
        Llamada.de(ClienteApi.servicio(ServicioContactos.class).listar()).enviar(new Llamada.Respuestas<List<Contacto>>() {
            @Override
            public void alExito(List<Contacto> contactos) {
                usuarioId = id;
                desdeLista(contactos);
                respuesta.listo(favorito);
            }

            @Override
            public void alError(ErrorApi error) {
                respuesta.listo(null);
            }
        });
    }

    /** La lista de la API trae el favorito primero. */
    public static void desdeLista(@Nullable List<Contacto> contactos) {
        favorito = contactos != null && !contactos.isEmpty() && contactos.get(0).favorito ? contactos.get(0) : null;
        cargado = true;
    }

    public static void invalidar() {
        cargado = false;
        favorito = null;
    }
}
