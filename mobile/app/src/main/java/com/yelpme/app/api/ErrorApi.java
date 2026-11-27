package com.yelpme.app.api;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Error uniforme de la API (igual que ErrorApi del panel web):
 * - codigo: HTTP (0 = sin conexión)
 * - getMessage(): mensaje de la API, ya en español
 * - errores: errores por campo de un 422
 */
public class ErrorApi extends Exception {

    public static final int SIN_CONEXION = 0;

    private static final Gson GSON = new Gson();

    private final int codigo;
    private final Map<String, List<String>> errores;

    public ErrorApi(int codigo, String mensaje, @Nullable Map<String, List<String>> errores) {
        super(mensaje);
        this.codigo = codigo;
        this.errores = errores != null ? errores : Collections.emptyMap();
    }

    /** Construye el error desde el cuerpo JSON de una respuesta fallida. */
    public static ErrorApi desdeCuerpo(int codigo, @Nullable String cuerpo, String mensajePorDefecto) {
        try {
            Respuesta<?> r = GSON.fromJson(cuerpo, Respuesta.class);
            if (r != null && r.mensaje != null) {
                return new ErrorApi(codigo, r.mensaje, r.errores);
            }
        } catch (JsonParseException ignorado) {
            // Cuerpo que no es JSON (p. ej. un 502 de un proxy): mensaje genérico.
        }
        return new ErrorApi(codigo, mensajePorDefecto, null);
    }

    public int getCodigo() {
        return codigo;
    }

    public Map<String, List<String>> getErrores() {
        return errores;
    }

    /** Primer mensaje de un campo (para su TextInputLayout) o null. */
    @Nullable
    public String campo(String nombre) {
        List<String> lista = errores.get(nombre);
        return lista == null || lista.isEmpty() ? null : lista.get(0);
    }

    public boolean esValidacion() {
        return codigo == 422;
    }
}
