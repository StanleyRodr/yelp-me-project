package com.yelpme.app.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Envuelve una llamada de Retrofit y entrega `datos` o un ErrorApi en el hilo
 * principal. Uso:
 * <pre>
 * Llamada.de(servicio.sesion()).enviar(new Llamada.Respuestas&lt;Usuario&gt;() {
 *     public void alExito(Usuario u) { ... }
 *     public void alError(ErrorApi e) { ... }
 * });
 * </pre>
 */
public final class Llamada<T> {

    /** Respuestas de una llamada (se implementa con una clase interna anónima). */
    public abstract static class Respuestas<T> {
        public abstract void alExito(T datos);

        public abstract void alError(ErrorApi error);

        /** Mensaje de éxito de la API, para quien lo necesite (p. ej. recuperación). */
        public void alExito(T datos, String mensaje) {
            alExito(datos);
        }
    }

    private final Call<Respuesta<T>> call;

    private Llamada(Call<Respuesta<T>> call) {
        this.call = call;
    }

    public static <T> Llamada<T> de(Call<Respuesta<T>> call) {
        return new Llamada<>(call);
    }

    public Llamada<T> enviar(Respuestas<T> respuestas) {
        call.enqueue(new Callback<Respuesta<T>>() {
            @Override
            public void onResponse(@NonNull Call<Respuesta<T>> c, @NonNull Response<Respuesta<T>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    respuestas.alExito(r.body().datos, r.body().mensaje);
                    return;
                }
                respuestas.alError(errorDe(r));
            }

            @Override
            public void onFailure(@NonNull Call<Respuesta<T>> c, @NonNull Throwable t) {
                if (c.isCanceled()) {
                    return;
                }
                respuestas.alError(new ErrorApi(ErrorApi.SIN_CONEXION, ClienteApi.mensajeSinConexion(), null));
            }
        });
        return this;
    }

    /** Cancela la llamada (p. ej. en onDestroy); no llama a ninguna respuesta. */
    public void cancelar() {
        call.cancel();
    }

    private static ErrorApi errorDe(Response<?> r) {
        String cuerpo = null;
        try {
            if (r.errorBody() != null) {
                cuerpo = r.errorBody().string();
            }
        } catch (IOException ignorado) {
            // Sin cuerpo legible: queda el mensaje genérico.
        }
        return ErrorApi.desdeCuerpo(r.code(), cuerpo, ClienteApi.mensajeGenerico());
    }
}
