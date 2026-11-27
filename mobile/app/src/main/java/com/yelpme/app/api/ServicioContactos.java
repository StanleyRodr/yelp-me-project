package com.yelpme.app.api;

import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.modelos.VerificacionTelefono;
import com.yelpme.app.modelos.Vinculo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Contactos de confianza, vínculos y bloqueos (planes 004 y 010). */
public interface ServicioContactos {

    /** El favorito primero, luego por nombre. */
    @GET("contactos")
    Call<Respuesta<List<Contacto>>> listar();

    @GET("contactos/{id}")
    Call<Respuesta<Contacto>> obtener(@Path("id") long id);

    @POST("contactos")
    Call<Respuesta<Contacto>> crear(@Body Contacto contacto);

    @PUT("contactos/{id}")
    Call<Respuesta<Contacto>> actualizar(@Path("id") long id, @Body Contacto contacto);

    @DELETE("contactos/{id}")
    Call<Respuesta<Void>> eliminar(@Path("id") long id);

    @PUT("contactos/{id}/favorito")
    Call<Respuesta<Contacto>> marcarFavorito(@Path("id") long id);

    @DELETE("contactos/{id}/favorito")
    Call<Respuesta<Contacto>> quitarFavorito(@Path("id") long id);

    @GET("telefonos/{telefono}")
    Call<Respuesta<VerificacionTelefono>> verificar(@Path("telefono") String telefono);

    @GET("vinculos")
    Call<Respuesta<List<Vinculo>>> vinculos();

    @DELETE("vinculos/{id}")
    Call<Respuesta<Void>> desvincular(@Path("id") long id);

    @GET("bloqueos")
    Call<Respuesta<List<Vinculo>>> bloqueos();

    /** { contacto_id: id del vínculo } */
    @POST("bloqueos")
    Call<Respuesta<Vinculo>> bloquear(@Body Map<String, Long> cuerpo);

    @DELETE("bloqueos/{id}")
    Call<Respuesta<Void>> desbloquear(@Path("id") long id);
}
