package com.yelpme.app.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.ui.login.LoginActivity;

/** Cerrar sesión con confirmación: borra la sesión local y avisa a la API. */
public final class CerrarSesion {

    private CerrarSesion() {
    }

    public static void ejecutar(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.sesion_cerrar_titulo)
                .setMessage(R.string.sesion_cerrar_mensaje)
                .setNegativeButton(R.string.comun_cancelar, null)
                .setPositiveButton(R.string.sesion_cerrar_confirmar, (d, w) -> cerrar(activity))
                .show();
    }

    private static void cerrar(Activity activity) {
        Sesion sesion = ((YelpMeApp) activity.getApplication()).getSesion();
        String token = sesion.token();
        sesion.cerrar();

        // Se intenta borrar el token en la API; si falla (sin red), la sesión local ya no existe.
        if (token != null) {
            Llamada.de(ClienteApi.servicio(ServicioAuth.class).cerrarSesion("Bearer " + token))
                    .enviar(new Llamada.Respuestas<Void>() {
                        @Override
                        public void alExito(Void datos) {
                        }

                        @Override
                        public void alError(ErrorApi error) {
                        }
                    });
        }

        activity.startActivity(new Intent(activity, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        activity.finish();
    }
}
