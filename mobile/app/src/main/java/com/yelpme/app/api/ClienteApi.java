package com.yelpme.app.api;

import android.content.Context;

import com.yelpme.app.BuildConfig;
import com.yelpme.app.R;
import com.yelpme.app.util.Sesion;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit único de la app contra BuildConfig.API_URL. Lo inicializa YelpMeApp.
 * Las interfaces de servicio se piden con {@link #servicio(Class)}.
 */
public final class ClienteApi {

    private static Retrofit retrofit;
    private static Context app;

    private ClienteApi() {
    }

    public static void iniciar(Context contexto, Sesion sesion) {
        app = contexto.getApplicationContext();

        OkHttpClient.Builder http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new InterceptorToken(sesion))
                .addInterceptor(new InterceptorSesion(app, sesion));

        if (BuildConfig.DEBUG) {
            // Solo encabezados: el cuerpo llevaría contraseñas al Logcat.
            HttpLoggingInterceptor registro = new HttpLoggingInterceptor();
            registro.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            http.addInterceptor(registro);
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(http.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T servicio(Class<T> tipo) {
        return retrofit.create(tipo);
    }

    static String mensajeSinConexion() {
        return app.getString(R.string.error_sin_conexion);
    }

    static String mensajeGenerico() {
        return app.getString(R.string.error_generico);
    }
}
