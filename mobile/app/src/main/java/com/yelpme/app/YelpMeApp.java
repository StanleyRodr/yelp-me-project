package com.yelpme.app;

import android.app.Application;
import android.os.StrictMode;

import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.util.Sesion;
import com.yelpme.app.util.Tema;

/** Arranque: tema guardado, sesión y cliente de la API. */
public class YelpMeApp extends Application {

    private Sesion sesion;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            // Fugas de activities (p. ej. el mapa) en Logcat, etiqueta StrictMode.
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }
        Tema.iniciar(this);
        sesion = new Sesion(this);
        ClienteApi.iniciar(this, sesion);
    }

    public Sesion getSesion() {
        return sesion;
    }
}
