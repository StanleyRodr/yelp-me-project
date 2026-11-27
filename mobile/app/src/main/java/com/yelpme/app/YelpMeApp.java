package com.yelpme.app;

import android.app.Application;

import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.util.Sesion;
import com.yelpme.app.util.Tema;

/** Arranque: tema guardado, sesión y cliente de la API. */
public class YelpMeApp extends Application {

    private Sesion sesion;

    @Override
    public void onCreate() {
        super.onCreate();
        Tema.aplicar(Tema.actual(this));
        sesion = new Sesion(this);
        ClienteApi.iniciar(this, sesion);
    }

    public Sesion getSesion() {
        return sesion;
    }
}
