package com.yelpme.app.notificaciones;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.ui.alertas.AlertaContactoActivity;
import com.yelpme.app.ui.solicitudes.DetalleSolicitudActivity;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Mensajes de FCM (solo datos, ver App\Support\Avisos): arma la notificación
 * en el canal indicado. alerta_contacto → AlertaContactoActivity con acción
 * "Llamar"; estado → detalle de la solicitud.
 */
public class ServicioMensajes extends FirebaseMessagingService {

    private static final String ETIQUETA = "ServicioMensajes";

    @Override
    public void onNewToken(@NonNull String token) {
        if (!((YelpMeApp) getApplication()).getSesion().activa()) {
            return; // se manda al iniciar sesión
        }
        try {
            // Ya estamos en un hilo de fondo: llamada directa.
            ClienteApi.servicio(ServicioAuth.class)
                    .actualizarFcm(Collections.singletonMap("fcm_token", token)).execute();
        } catch (IOException | RuntimeException e) {
            Log.w(ETIQUETA, "No se guardó el token nuevo", e);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage mensaje) {
        Map<String, String> d = mensaje.getData();
        String titulo = d.get("aviso_titulo");
        String cuerpo = d.get("aviso_cuerpo");
        if (titulo == null) {
            return;
        }
        boolean alerta = "alerta_contacto".equals(d.get("tipo"));
        String canal = alerta ? Canales.ALERTAS : Canales.ESTADO;
        int id = (int) (System.currentTimeMillis() & 0x7fffffff);

        NotificationCompat.Builder n = new NotificationCompat.Builder(this, canal)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setColor(ContextCompat.getColor(this, R.color.ym_marca))
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cuerpo))
                .setAutoCancel(true);

        if (alerta) {
            n.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(pendiente(id, AlertaContactoActivity.intent(this, d)));
            String telefono = d.get("telefono");
            if (telefono != null && !telefono.isEmpty()) {
                // Desde la notificación se abre el marcador (sin pedir permiso de llamada).
                Intent marcar = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
                n.addAction(R.drawable.ic_call, getString(R.string.alerta_llamar_corto),
                        PendingIntent.getActivity(this, id + 1, marcar, PendingIntent.FLAG_IMMUTABLE));
            }
        } else {
            long solicitudId = parsear(d.get("solicitud_id"));
            n.setContentIntent(pendiente(id, DetalleSolicitudActivity.intent(this, solicitudId)));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(id, n.build());
        }
    }

    private PendingIntent pendiente(int id, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static long parsear(String texto) {
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
