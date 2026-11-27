package com.yelpme.app;

import android.content.Context;
import android.content.SharedPreferences;

public class SesionManager {

    private static final String PREF_NAME = "YelpMESesion";
    private final SharedPreferences prefs;

    public SesionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarUsuario(String nombre, String apellidoP, String apellidoM, String correo, String direccion) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nombre", nombre);
        editor.putString("nombreCompleto", (nombre + " " + apellidoP + " " + apellidoM).trim());
        editor.putString("correo", correo);
        editor.putString("direccion", direccion);
        editor.apply();
    }

    public String getNombre() {
        return prefs.getString("nombre", "");
    }

    public String getNombreCompleto() {
        return prefs.getString("nombreCompleto", "");
    }

    public String getCorreo() {
        return prefs.getString("correo", "");
    }

    public String getDireccion() {
        return prefs.getString("direccion", "Sin dirección registrada");
    }

    public void guardarContactoEmergencia(String nombre, String telefono) {
        prefs.edit()
                .putString("contactoEmergenciaNombre", nombre)
                .putString("contactoEmergenciaTel", telefono)
                .apply();
    }

    public String getNombreContactoEmergencia() {
        return prefs.getString("contactoEmergenciaNombre", "Contacto de emergencia");
    }

    public String getTelefonoEmergencia() {
        return prefs.getString("contactoEmergenciaTel", "911");
    }
}