package com.yelpme.app.ui.inicio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yelpme.app.R;
import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.util.Ubicacion;

/**
 * "No pudimos obtener tu ubicación" (plan principal 7.5): usar mi domicilio
 * (solo si tiene), reintentar o marcar en el mapa. Con el permiso negado para
 * siempre, "Abrir ajustes" en lugar de reintentar.
 */
public final class DialogoSinUbicacion {

    public interface Acciones {
        void usarDomicilio(Domicilio domicilio);

        void reintentar();

        void marcarEnMapa();
    }

    private DialogoSinUbicacion() {
    }

    public static void mostrar(Activity activity, Ubicacion.Motivo motivo, @Nullable Domicilio domicilio, Acciones acciones) {
        int texto;
        switch (motivo) {
            case SIN_PERMISO:
            case SIN_PERMISO_PERMANENTE:
                texto = R.string.ubicacion_falla_permiso;
                break;
            case GPS_APAGADO:
                texto = R.string.ubicacion_falla_gps;
                break;
            case TIEMPO_AGOTADO:
                texto = R.string.ubicacion_falla_tiempo;
                break;
            default:
                texto = R.string.ubicacion_falla_error;
        }
        boolean ajustes = motivo == Ubicacion.Motivo.SIN_PERMISO_PERMANENTE;

        MaterialAlertDialogBuilder dialogo = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.ubicacion_falla_titulo)
                .setMessage(texto)
                .setNeutralButton(R.string.ubicacion_marcar_mapa, (d, i) -> acciones.marcarEnMapa());
        if (ajustes) {
            dialogo.setNegativeButton(R.string.ubicacion_abrir_ajustes, (d, i) -> activity.startActivity(
                    new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null))));
        } else {
            dialogo.setNegativeButton(R.string.ubicacion_reintentar, (d, i) -> acciones.reintentar());
        }
        if (domicilio != null) {
            dialogo.setPositiveButton(R.string.ubicacion_usar_domicilio, (d, i) -> acciones.usarDomicilio(domicilio));
        }
        dialogo.show();
    }
}
