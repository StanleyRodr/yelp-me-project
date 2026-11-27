package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Solicitud de ayuda (PresentadorSolicitud). Estados: Pendiente, Enviado, Rechazado, Cancelado, Terminado. */
public class Solicitud {

    public long id;
    public String titulo;
    public String descripcion;
    public double latitud;
    public double longitud;
    @SerializedName("ubicacion_nombre")
    public String ubicacionNombre;
    public String estado;
    /** Motivo de rechazo o cancelación. */
    public String motivo;
    /** Ids del catálogo de servicios. */
    public List<Long> servicios = new ArrayList<>();
    public Persona admin;
    @SerializedName("cerrado_por")
    public Persona cerradoPor;
    @SerializedName("enviado_en")
    public String enviadoEn;
    @SerializedName("cerrado_en")
    public String cerradoEn;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;
}
