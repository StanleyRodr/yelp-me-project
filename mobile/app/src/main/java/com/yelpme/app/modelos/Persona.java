package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

/** Persona breve en una solicitud (admin que atendió, quien cerró). */
public class Persona {

    public long id;
    public String nombre;
    @SerializedName("apellido_paterno")
    public String apellidoPaterno;
    @SerializedName("apellido_materno")
    public String apellidoMaterno;

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
