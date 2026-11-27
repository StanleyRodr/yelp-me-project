package com.yelpme.app.api;

import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.modelos.Usuario;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

/** Perfil propio (plan 006) y domicilio (plan 009). Consultar el perfil = GET /sesion. */
public interface ServicioPerfil {

    @GET("sesion")
    Call<Respuesta<Usuario>> sesion();

    /** Solo los campos que cambian: nombre, apellido_paterno, apellido_materno, telefono, correo. */
    @PUT("perfil")
    Call<Respuesta<Usuario>> actualizar(@Body Map<String, String> datos);

    /** Parte `foto` (JPG/PNG/WEBP, máx. 1 MB). */
    @Multipart
    @POST("perfil/foto")
    Call<Respuesta<Usuario>> subirFoto(@Part MultipartBody.Part foto);

    @DELETE("perfil/foto")
    Call<Respuesta<Usuario>> quitarFoto();

    /** { actual, nueva, nueva_confirmation } */
    @PUT("perfil/password")
    Call<Respuesta<Void>> cambiarPassword(@Body Map<String, String> datos);

    /** Domicilio o null. */
    @GET("perfil/domicilio")
    Call<Respuesta<Domicilio>> domicilio();

    @PUT("perfil/domicilio")
    Call<Respuesta<Domicilio>> guardarDomicilio(@Body Domicilio domicilio);

    @DELETE("perfil/domicilio")
    Call<Respuesta<Void>> quitarDomicilio();
}
