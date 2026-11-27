package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Cuerpo de POST/PUT /api/solicitudes. En PUT, Gson omite los campos null (no cambian). */
public class NuevaSolicitud {

    public String titulo;
    public String descripcion;
    public Double latitud;
    public Double longitud;
    @SerializedName("ubicacion_nombre")
    public String ubicacionNombre;
    public List<Long> servicios;
}
