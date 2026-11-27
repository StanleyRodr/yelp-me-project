package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

/** Respuesta de login y registro. `contactosVinculados` solo viene en el registro. */
public class RespuestaSesion {

    public String token;
    public Usuario usuario;
    @SerializedName("contactos_vinculados")
    public int contactosVinculados;
}
