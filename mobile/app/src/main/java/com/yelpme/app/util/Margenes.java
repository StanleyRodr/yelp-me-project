package com.yelpme.app.util;

import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Insets con edge-to-edge (obligatorio con targetSdk 35+): la vista de arriba
 * recibe la barra de estado y la de abajo la de navegación o el teclado.
 */
public final class Margenes {

    private Margenes() {
    }

    /**
     * Edge-to-edge con íconos claros en la barra de estado (el header es color
     * marca en todas las pantallas). `abajoMarca`: la barra inferior también lo es.
     */
    public static void bordeABorde(ComponentActivity activity, boolean abajoMarca) {
        SystemBarStyle estado = SystemBarStyle.dark(Color.TRANSPARENT);
        SystemBarStyle navegacion = abajoMarca
                ? SystemBarStyle.dark(Color.TRANSPARENT)
                : SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT);
        EdgeToEdge.enable(activity, estado, navegacion);
    }

    public static void aplicar(View arriba, View abajo) {
        int arribaInicial = arriba.getPaddingTop();
        int abajoInicial = abajo.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(arriba.getRootView(), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            arriba.setPadding(arriba.getPaddingLeft(), arribaInicial + barras.top, arriba.getPaddingRight(), arriba.getPaddingBottom());
            abajo.setPadding(abajo.getPaddingLeft(), abajo.getPaddingTop(), abajo.getPaddingRight(),
                    abajoInicial + Math.max(barras.bottom, teclado.bottom));
            return insets;
        });
    }
}
