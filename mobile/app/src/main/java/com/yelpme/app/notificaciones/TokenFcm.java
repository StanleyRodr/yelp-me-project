package com.yelpme.app.notificaciones;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yelpme.app.BuildConfig;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;

import java.util.Collections;

/**
 * Token de notificaciones del dispositivo (plan 012). Se manda a la API al
 * iniciar sesión, al registrarse, al abrir la app con sesión y cuando Firebase
 * lo rota. Sin google-services.json (FCM_ACTIVO = false) no hace nada.
 */
public final class TokenFcm {

    private static final String ETIQUETA = "TokenFcm";

    private TokenFcm() {
    }

    public static boolean disponible(Context contexto) {
        return BuildConfig.FCM_ACTIVO && !FirebaseApp.getApps(contexto).isEmpty();
    }

    /** Obtiene el token actual y lo guarda en la API (sesión ya iniciada). */
    public static void sincronizar(Context contexto) {
        if (!disponible(contexto)) {
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tarea -> {
            if (tarea.isSuccessful() && tarea.getResult() != null) {
                enviar(tarea.getResult());
            } else {
                Log.w(ETIQUETA, "Sin token de FCM", tarea.getException());
            }
        });
    }

    /** PUT /api/sesion/fcm; si falla, se reintenta la próxima vez que abra la app. */
    public static void enviar(String token) {
        Llamada.de(ClienteApi.servicio(ServicioAuth.class).actualizarFcm(Collections.singletonMap("fcm_token", token)))
                .enviar(new Llamada.Respuestas<Void>() {
                    @Override
                    public void alExito(Void nada) {
                    }

                    @Override
                    public void alError(ErrorApi error) {
                        Log.w(ETIQUETA, "No se guardó el token: " + error.getMessage());
                    }
                });
    }
}
