package com.yelpme.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.databinding.ActivityLoginBinding;
import com.yelpme.app.modelos.RespuestaSesion;
import com.yelpme.app.notificaciones.TokenFcm;
import com.yelpme.app.ui.inicio.InicioActivity;
import com.yelpme.app.ui.recuperar.RecuperarContrasenaActivity;
import com.yelpme.app.ui.registro.RegistroActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

import java.util.HashMap;
import java.util.Map;

/** Inicio de sesión con correo o teléfono (POST /api/login). */
public class LoginActivity extends AppCompatActivity {

    /** Extra: la sesión terminó (401) y se muestra el aviso. */
    public static final String EXTRA_EXPIRADA = "expirada";
    /** Extra: se acaba de cambiar la contraseña por recuperación. */
    public static final String EXTRA_RECUPERADA = "recuperada";

    private ActivityLoginBinding vista;
    private Llamada<RespuestaSesion> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Margenes.bordeABorde(this, false);
        vista = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.encabezado.getRoot(), vista.desplazable);

        mostrarAviso(getIntent());

        vista.btnIniciarSesion.setOnClickListener(v -> entrar());
        vista.etPassword.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
        vista.btnRegistrate.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        vista.btnOlvidaste.setOnClickListener(v -> startActivity(new Intent(this, RecuperarContrasenaActivity.class)));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mostrarAviso(intent);
    }

    private void mostrarAviso(Intent intent) {
        int aviso = intent.getBooleanExtra(EXTRA_RECUPERADA, false) ? R.string.login_recuperada
                : intent.getBooleanExtra(EXTRA_EXPIRADA, false) ? R.string.login_expirada : 0;
        vista.tvAviso.setVisibility(aviso == 0 ? View.GONE : View.VISIBLE);
        if (aviso != 0) {
            vista.tvAviso.setText(aviso);
        }
    }

    private void entrar() {
        if (!vista.btnIniciarSesion.isEnabled()) {
            return; // ya hay una petición en curso (p. ej. Enter + botón)
        }
        Formularios.limpiar(vista.tilIdentificador, vista.tilPassword);
        String identificador = Formularios.texto(vista.etIdentificador);
        String password = vista.etPassword.getText() == null ? "" : vista.etPassword.getText().toString();

        if (identificador.isEmpty()) {
            Formularios.error(vista.tilIdentificador, R.string.login_falta_identificador);
            vista.etIdentificador.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            Formularios.error(vista.tilPassword, R.string.login_falta_password);
            vista.etPassword.requestFocus();
            return;
        }

        // Solo dígitos (con espacios o guiones) = teléfono; lo demás, correo.
        String soloDigitos = identificador.replaceAll("[\\s-]", "");
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put(soloDigitos.matches("\\d+") ? "telefono" : "correo", soloDigitos.matches("\\d+") ? soloDigitos : identificador);
        cuerpo.put("password", password);

        Formularios.cargando(vista.btnIniciarSesion, true, R.string.login_entrar, R.string.login_entrando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioAuth.class).login(cuerpo)).enviar(new Llamada.Respuestas<RespuestaSesion>() {
            @Override
            public void alExito(RespuestaSesion r) {
                ((YelpMeApp) getApplication()).getSesion().guardar(r.token, r.usuario);
                TokenFcm.sincronizar(getApplicationContext());
                startActivity(new Intent(LoginActivity.this, InicioActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnIniciarSesion, false, R.string.login_entrar, R.string.login_entrando);
                if (error.getCodigo() == 401) {
                    vista.tilPassword.setError(error.getMessage());
                    vista.etPassword.requestFocus();
                    return;
                }
                Map<String, com.google.android.material.textfield.TextInputLayout> campos = new HashMap<>();
                campos.put("correo", vista.tilIdentificador);
                campos.put("telefono", vista.tilIdentificador);
                campos.put("password", vista.tilPassword);
                if (!error.esValidacion() || !Formularios.mostrarErrores(error, campos)) {
                    // 403 (cuenta de admin o deshabilitada), 429 o sin conexión.
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }
}
