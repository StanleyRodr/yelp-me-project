package com.yelpme.app.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.yelpme.app.ui.login.LoginActivity;
import com.yelpme.app.util.Sesion;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 401 fuera de las rutas públicas = token inválido o borrado (otra sesión cambió
 * la contraseña, cuenta deshabilitada…): se borra la sesión y se vuelve al login.
 */
class InterceptorSesion implements Interceptor {

    private static final Pattern RUTA_PUBLICA = Pattern.compile(".*/(login|registro|recuperacion(/verificacion)?)$");

    private final Context app;
    private final Sesion sesion;

    InterceptorSesion(Context app, Sesion sesion) {
        this.app = app;
        this.sesion = sesion;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response respuesta = chain.proceed(chain.request());

        boolean publica = RUTA_PUBLICA.matcher(chain.request().url().encodedPath()).matches();
        // Solo la primera vez: con la sesión ya borrada no se repite la navegación.
        if (respuesta.code() == 401 && !publica && sesion.activa()) {
            sesion.cerrar();
            Intent login = new Intent(app, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(LoginActivity.EXTRA_EXPIRADA, true);
            app.startActivity(login);
        }
        return respuesta;
    }
}
