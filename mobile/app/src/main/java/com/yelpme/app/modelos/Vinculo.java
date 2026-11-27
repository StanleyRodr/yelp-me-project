package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

/**
 * Persona que me tiene como contacto (GET /api/vinculos) o a quien bloqueé
 * (GET /api/bloqueos, sin domicilio). `id` es el del vínculo o del bloqueo.
 */
public class Vinculo {

    public long id;
    public String nombre;
    @SerializedName("apellido_paterno")
    public String apellidoPaterno;
    @SerializedName("apellido_materno")
    public String apellidoMaterno;
    public String telefono;
    public String desde;
    /** Solo en vínculos; null si no registró domicilio. */
    public Domicilio domicilio;

    public String nombreCompleto() {
        StringBuilder s = new StringBuilder();
        for (String p : new String[]{nombre, apellidoPaterno, apellidoMaterno}) {
            if (p != null && !p.trim().isEmpty()) {
                if (s.length() > 0) {
                    s.append(' ');
                }
                s.append(p.trim());
            }
        }
        return s.toString();
    }
}
