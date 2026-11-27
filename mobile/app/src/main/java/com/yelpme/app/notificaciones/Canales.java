package com.yelpme.app.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.yelpme.app.R;

/**
 * Canales de notificación (ids iguales a App\Support\Avisos del backend):
 * alertas de contactos (importancia alta) y estado de mis solicitudes.
 */
public final class Canales {

    public static final String ALERTAS = "alertas";
    public static final String ESTADO = "estado";

    private Canales() {
    }

    public static void crear(Context contexto) {
        NotificationManager gestor = contexto.getSystemService(NotificationManager.class);
        NotificationChannel alertas = new NotificationChannel(ALERTAS,
                contexto.getString(R.string.canal_alertas), NotificationManager.IMPORTANCE_HIGH);
        alertas.setDescription(contexto.getString(R.string.canal_alertas_descripcion));
        NotificationChannel estado = new NotificationChannel(ESTADO,
                contexto.getString(R.string.canal_estado), NotificationManager.IMPORTANCE_DEFAULT);
        estado.setDescription(contexto.getString(R.string.canal_estado_descripcion));
        gestor.createNotificationChannel(alertas);
        gestor.createNotificationChannel(estado);
    }
}
