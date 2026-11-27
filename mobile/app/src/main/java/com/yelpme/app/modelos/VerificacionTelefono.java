package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

/** GET /api/telefonos/{telefono}: si se vincularía y si ya está en mi agenda. */
public class VerificacionTelefono {

    public String telefono;
    public boolean vinculable;
    @SerializedName("en_agenda")
    public boolean enAgenda;
}
