package com.yelpme.app.api;

import com.yelpme.app.modelos.RespuestaSesion;
import com.yelpme.app.modelos.Usuario;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/** Cuenta y sesión (plan 003) y recuperación de contraseña (plan 008). */
public interface ServicioAuth {

    /** { nombre, apellido_paterno, apellido_materno?, telefono, correo, password, password_confirmation } */
    @POST("registro")
    Call<Respuesta<RespuestaSesion>> registro(@Body Map<String, String> cuerpo);

    /** { correo | telefono, password } */
    @POST("login")
    Call<Respuesta<RespuestaSesion>> login(@Body Map<String, String> cuerpo);

    @GET("sesion")
    Call<Respuesta<Usuario>> sesion();

    /** Con el token explícito: la sesión local ya se borró al cerrar. */
    @DELETE("sesion")
    Call<Respuesta<Void>> cerrarSesion(@Header("Authorization") String bearer);

    /** { fcm_token }: token de notificaciones del dispositivo (plan 012). */
    @PUT("sesion/fcm")
    Call<Respuesta<Void>> actualizarFcm(@Body Map<String, String> cuerpo);

    /** { correo } → mensaje neutro */
    @POST("recuperacion")
    Call<Respuesta<Void>> pedirCodigo(@Body Map<String, String> cuerpo);

    /** { correo, codigo } → { valido: true } o 422 */
    @POST("recuperacion/verificacion")
    Call<Respuesta<Map<String, Boolean>>> verificar(@Body Map<String, String> cuerpo);

    /** { correo, codigo, password, password_confirmation } */
    @PUT("recuperacion")
    Call<Respuesta<Void>> cambiar(@Body Map<String, String> cuerpo);
}
