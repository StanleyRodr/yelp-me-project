package com.yelpme.app.api;

import com.yelpme.app.modelos.Servicio;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/** Catálogo de servicios de emergencia (fijo en la BD). */
public interface ServicioCatalogo {

    @GET("servicios")
    Call<Respuesta<List<Servicio>>> servicios();
}
