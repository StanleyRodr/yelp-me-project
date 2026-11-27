package com.yelpme.app.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Tema de la app: claro, oscuro o sistema (por defecto), como el panel web.
 * Se guarda en SharedPreferences `ym_ajustes` (no se borra al cerrar sesión).
 */
public final class Tema {

    public static final String CLARO = "claro";
    public static final String OSCURO = "oscuro";
    public static final String SISTEMA = "sistema";

    private static final String PREFERENCIAS = "ym_ajustes";
    private static final String CLAVE = "ym_tema";

    private Tema() {
    }

    public static String actual(Context contexto) {
        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getString(CLAVE, SISTEMA);
    }

    public static void elegir(Context contexto, String tema) {
        contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit().putString(CLAVE, tema).apply();
        aplicar(tema);
    }

    public static void aplicar(String tema) {
        int modo;
        switch (tema) {
            case CLARO:
                modo = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case OSCURO:
                modo = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
