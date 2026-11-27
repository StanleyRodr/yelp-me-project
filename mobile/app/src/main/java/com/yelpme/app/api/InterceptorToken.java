package com.yelpme.app.api;

import androidx.annotation.NonNull;

import com.yelpme.app.util.Sesion;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Agrega `Accept: application/json` y el token Bearer de la sesión guardada. */
class InterceptorToken implements Interceptor {

    private final Sesion sesion;

    InterceptorToken(Sesion sesion) {
        this.sesion = sesion;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder peticion = chain.request().newBuilder().header("Accept", "application/json");
        String token = sesion.token();
        // Respeta un Authorization explícito (p. ej. al cerrar sesión con el token anterior).
        if (token != null && chain.request().header("Authorization") == null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return chain.proceed(peticion.build());
    }
}
