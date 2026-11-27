package com.yelpme.app.modelos;

import com.google.gson.annotations.SerializedName;

/** Domicilio único del usuario (PresentadorDomicilio, plan 009). */
public class Domicilio {

    public String calle;
    public String numero;
    public String colonia;
    @SerializedName("codigo_postal")
    public String codigoPostal;
    public String referencia;
    public double latitud;
    public double longitud;
    /** "Calle Número, Colonia, CP" (solo lectura, lo arma la API) */
    public String texto;
}
