package com.yelpme.app.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.inicio.InicioActivity;
import com.yelpme.app.util.Sesion;

/**
 * Entrada de la app. Con token guardado lo confirma con GET /api/sesion:
 * válido → Inicio; 401/403 → login; sin conexión → Inicio con datos guardados.
 */
public class SplashActivity extends AppCompatActivity {

    private boolean esperando = true;
    private Llamada<Usuario> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splash.setKeepOnScreenCondition(() -> esperando);

        Sesion sesion = ((YelpMeApp) getApplication()).getSesion();
        if (!sesion.activa()) {
            ir(new Intent(this, LoginActivity.class));
            return;
        }

        llamada = Llamada.de(ClienteApi.servicio(ServicioAuth.class).sesion()).enviar(new Llamada.Respuestas<Usuario>() {
            @Override
            public void alExito(Usuario usuario) {
                sesion.actualizarUsuario(usuario);
                ir(new Intent(SplashActivity.this, InicioActivity.class));
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() == ErrorApi.SIN_CONEXION) {
                    ir(new Intent(SplashActivity.this, InicioActivity.class)
                            .putExtra(InicioActivity.EXTRA_SIN_CONEXION, true));
                } else if (error.getCodigo() == 401) {
                    // InterceptorSesion ya borró la sesión y abrió el login con el aviso.
                    finish();
                } else {
                    sesion.cerrar();
                    ir(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }
        });
    }

    private void ir(Intent destino) {
        esperando = false;
        startActivity(destino);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (llamada != null && esperando) {
            llamada.cancelar();
        }
        super.onDestroy();
    }
}
