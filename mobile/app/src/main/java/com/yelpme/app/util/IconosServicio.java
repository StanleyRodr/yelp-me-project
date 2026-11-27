package com.yelpme.app.util;

import androidx.annotation.DrawableRes;

import com.yelpme.app.R;

/**
 * Ícono (Material Symbols) de un servicio según la columna `servicios.icono`.
 * Mismos nombres que usa el panel web.
 */
public final class IconosServicio {

    private IconosServicio() {
    }

    @DrawableRes
    public static int de(String icono) {
        if (icono == null) {
            return R.drawable.ic_support;
        }
        switch (icono) {
            case "local_police":
                return R.drawable.ic_local_police;
            case "ambulance":
                return R.drawable.ic_ambulance;
            case "fire_truck":
                return R.drawable.ic_fire_truck;
            case "traffic":
                return R.drawable.ic_traffic;
            case "health_and_safety":
                return R.drawable.ic_health_and_safety;
            default:
                return R.drawable.ic_support;
        }
    }
}
