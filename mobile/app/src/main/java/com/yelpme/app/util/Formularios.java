package com.yelpme.app.util;

import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputLayout;
import com.yelpme.app.api.ErrorApi;

import java.util.Map;
import java.util.regex.Pattern;

/** Ayudas para formularios con TextInputLayout. */
public final class Formularios {

    public static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static final Pattern TELEFONO = Pattern.compile("^\\d{10}$");
    public static final int MIN_PASSWORD = 8;

    private Formularios() {
    }

    /** Texto recortado del campo ("" si está vacío). */
    public static String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    public static void limpiar(TextInputLayout... campos) {
        for (TextInputLayout c : campos) {
            c.setError(null);
        }
    }

    /** Pone un error local; devuelve false para encadenar validaciones. */
    public static boolean error(TextInputLayout campo, @StringRes int mensaje) {
        campo.setError(campo.getContext().getString(mensaje));
        return false;
    }

    /**
     * Errores de un 422 bajo su campo. `campos` asocia el nombre en la API con su
     * TextInputLayout. Devuelve true si al menos uno quedó bajo un campo.
     */
    public static boolean mostrarErrores(ErrorApi error, Map<String, TextInputLayout> campos) {
        boolean alguno = false;
        TextInputLayout primero = null;
        for (Map.Entry<String, TextInputLayout> e : campos.entrySet()) {
            String mensaje = error.campo(e.getKey());
            if (mensaje != null) {
                e.getValue().setError(mensaje);
                if (primero == null) {
                    primero = e.getValue();
                }
                alguno = true;
            }
        }
        if (primero != null && primero.getEditText() != null) {
            primero.getEditText().requestFocus();
        }
        return alguno;
    }

    /** Botón deshabilitado con texto de espera mientras la llamada está en curso. */
    public static void cargando(Button boton, boolean cargando, @StringRes int textoNormal, @StringRes int textoCargando) {
        boton.setEnabled(!cargando);
        boton.setText(cargando ? textoCargando : textoNormal);
    }
}
