package com.yelpme.app.ui.comunes;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.yelpme.app.R;
import com.yelpme.app.util.TransicionesSolicitud;

/**
 * Badge de estado (color + ícono + texto), igual que EstadoBadge del panel:
 * Pendiente emergency, Enviado local_shipping, Terminado check_circle,
 * Rechazado block, Cancelado cancel. El TextView usa @drawable/bg_badge.
 */
public final class BadgeEstado {

    private BadgeEstado() {
    }

    public static void aplicar(TextView vista, String estado) {
        int texto;
        int color;
        int fondo;
        int icono;
        switch (estado == null ? "" : estado) {
            case TransicionesSolicitud.ENVIADO:
                texto = R.string.estado_enviado;
                color = R.color.ym_estado_enviado_texto;
                fondo = R.color.ym_estado_enviado_fondo;
                icono = R.drawable.ic_local_shipping;
                break;
            case TransicionesSolicitud.TERMINADO:
                texto = R.string.estado_terminado;
                color = R.color.ym_estado_terminado_texto;
                fondo = R.color.ym_estado_terminado_fondo;
                icono = R.drawable.ic_check_circle;
                break;
            case TransicionesSolicitud.RECHAZADO:
                texto = R.string.estado_rechazado;
                color = R.color.ym_estado_rechazado_texto;
                fondo = R.color.ym_estado_rechazado_fondo;
                icono = R.drawable.ic_block;
                break;
            case TransicionesSolicitud.CANCELADO:
                texto = R.string.estado_cancelado;
                color = R.color.ym_estado_cancelado_texto;
                fondo = R.color.ym_estado_cancelado_fondo;
                icono = R.drawable.ic_cancel;
                break;
            default:
                texto = R.string.estado_pendiente;
                color = R.color.ym_estado_pendiente_texto;
                fondo = R.color.ym_estado_pendiente_fondo;
                icono = R.drawable.ic_emergency;
        }
        ColorStateList tinta = ColorStateList.valueOf(ContextCompat.getColor(vista.getContext(), color));
        vista.setText(texto);
        vista.setTextColor(tinta);
        vista.setBackgroundResource(R.drawable.bg_badge);
        vista.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(vista.getContext(), fondo)));
        vista.setCompoundDrawablesRelativeWithIntrinsicBounds(icono, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(vista, tinta);
    }
}
