package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Usuario tal como lo devuelve la API (PresentadorUsuario). */
public class Usuario {

    public long id;
    public String nombre;
    @SerializedName("apellido_paterno")
    public String apellidoPaterno;
    @SerializedName("apellido_materno")
    public String apellidoMaterno;
    public String telefono;
    public String correo;
    public String rol;
    @SerializedName("foto_perfil")
    public String fotoPerfil;
    public boolean suspendido;
    @SerializedName("strikes_activos")
    public int strikesActivos;
    @SerializedName("limite_strikes")
    public int limiteStrikes;
    @SerializedName("veces_suspendido")
    public int vecesSuspendido;

    /** Iniciales para el avatar sin foto: nombre + paterno. */
    public String iniciales() {
        StringBuilder s = new StringBuilder();
        for (String p : new String[]{nombre, apellidoPaterno}) {
            if (p != null && !p.trim().isEmpty()) {
                s.appendCodePoint(p.trim().codePointAt(0));
            }
        }
        return s.toString().toUpperCase(java.util.Locale.ROOT);
    }

    /** "Nombre Paterno Materno", sin espacios de más si falta el materno. */
    public String nombreCompleto() {
        List<String> partes = new ArrayList<>();
        for (String p : new String[]{nombre, apellidoPaterno, apellidoMaterno}) {
            if (p != null && !p.trim().isEmpty()) {
                partes.add(p.trim());
            }
        }
        return String.join(" ", partes);
    }
}
