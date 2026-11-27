package com.yelpme.app.api;

import com.yelpme.app.modelos.NuevaSolicitud;
import com.yelpme.app.modelos.Solicitud;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Solicitudes propias (plan 005). Sin borrar: se cancelan con /estado. */
public interface ServicioSolicitudes {

    /** Más recientes primero. `estado` null = todas. */
    @GET("solicitudes")
    Call<Respuesta<List<Solicitud>>> listar(@Query("estado") String estado);

    @GET("solicitudes/{id}")
    Call<Respuesta<Solicitud>> obtener(@Path("id") long id);

    @POST("solicitudes")
    Call<Respuesta<Solicitud>> crear(@Body NuevaSolicitud solicitud);

    /** Solo en Pendiente. */
    @PUT("solicitudes/{id}")
    Call<Respuesta<Solicitud>> actualizar(@Path("id") long id, @Body NuevaSolicitud cambios);

    /** { estado: Cancelado|Terminado, motivo? } */
    @PUT("solicitudes/{id}/estado")
    Call<Respuesta<Solicitud>> cambiarEstado(@Path("id") long id, @Body Map<String, String> cuerpo);
}
