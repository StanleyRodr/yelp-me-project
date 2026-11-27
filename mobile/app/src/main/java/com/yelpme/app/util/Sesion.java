package com.yelpme.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.yelpme.app.modelos.Usuario;

/** Sesión guardada en SharedPreferences `ym_sesion`: token y usuario (JSON). */
public class Sesion {

    private static final String PREFERENCIAS = "ym_sesion";
    private static final String TOKEN = "token";
    private static final String USUARIO = "usuario";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public Sesion(Context contexto) {
        prefs = contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public void guardar(String token, Usuario usuario) {
        prefs.edit().putString(TOKEN, token).putString(USUARIO, gson.toJson(usuario)).apply();
    }

    public void actualizarUsuario(Usuario usuario) {
        prefs.edit().putString(USUARIO, gson.toJson(usuario)).apply();
    }

    @Nullable
    public String token() {
        return prefs.getString(TOKEN, null);
    }

    @Nullable
    public Usuario usuario() {
        String json = prefs.getString(USUARIO, null);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, Usuario.class);
        } catch (JsonParseException e) {
            return null;
        }
    }

    public boolean activa() {
        return token() != null;
    }

    /** Borra token y usuario (commit inmediato: el interceptor lo consulta en otro hilo). */
    public void cerrar() {
        prefs.edit().clear().commit();
    }
}
