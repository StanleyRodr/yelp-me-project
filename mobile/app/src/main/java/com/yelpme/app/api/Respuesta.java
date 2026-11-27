package com.yelpme.app.api;

import java.util.List;
import java.util.Map;

/** Formato de todas las respuestas de la API: { ok, mensaje, datos, errores? }. */
public class Respuesta<T> {

    public boolean ok;
    public String mensaje;
    public T datos;
    public Map<String, List<String>> errores;
}
