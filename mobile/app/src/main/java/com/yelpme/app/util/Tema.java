package com.yelpme.app.util;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Tema de la app: claro u oscuro, como el panel web. Sin elección guardada
 * sigue al sistema. Se guarda en SharedPreferences `ym_ajustes` (no se borra
 * al cerrar sesión); un valor viejo "sistema" cuenta como sin elección.
 */
public final class Tema {

    public static final String CLARO = "claro";
    public static final String OSCURO = "oscuro";

    private static final String PREFERENCIAS = "ym_ajustes";
    private static final String CLAVE = "ym_tema";

    private Tema() {
    }

    /** Aplica el tema guardado. Llamar al arrancar la app. */
    public static void iniciar(Context contexto) {
        String guardado = contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getString(CLAVE, null);
        aplicar(guardado);
    }

    /** Tema que se ve ahora (elegido o el del sistema). */
    public static boolean esOscuro(Context contexto) {
        int modo = contexto.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return modo == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Pasa al tema contrario del que se ve y lo guarda. */
    public static void alternar(Context contexto) {
        String tema = esOscuro(contexto) ? CLARO : OSCURO;
        contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit().putString(CLAVE, tema).apply();
        aplicar(tema);
    }

    private static void aplicar(String tema) {
        int modo;
        if (CLARO.equals(tema)) {
            modo = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (OSCURO.equals(tema)) {
            modo = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
