package com.yelpme.app.ui.recuperar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.databinding.ActivityRecuperarContrasenaBinding;
import com.yelpme.app.ui.login.LoginActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

import java.util.HashMap;
import java.util.Map;

/**
 * Recuperar contraseña en 3 pasos (plan principal 5.1): correo → código →
 * contraseña nueva. Atrás regresa al paso anterior; en el primero cierra.
 */
public class RecuperarContrasenaActivity extends AppCompatActivity {

    private static final int PASO_CORREO = 0;
    private static final int PASO_CODIGO = 1;
    private static final int PASO_PASSWORD = 2;

    private static final String ESTADO_PASO = "paso";
    private static final String ESTADO_CORREO = "correo";
    private static final String ESTADO_CODIGO = "codigo";

    private ActivityRecuperarContrasenaBinding vista;
    private ServicioAuth api;
    private Llamada<?> llamada;

    private int paso = PASO_CORREO;
    private String correo = "";
    private String codigo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Margenes.bordeABorde(this, false);
        vista = ActivityRecuperarContrasenaBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.encabezado.getRoot(), vista.desplazable);
        api = ClienteApi.servicio(ServicioAuth.class);

        if (savedInstanceState != null) {
            paso = savedInstanceState.getInt(ESTADO_PASO, PASO_CORREO);
            correo = savedInstanceState.getString(ESTADO_CORREO, "");
            codigo = savedInstanceState.getString(ESTADO_CODIGO, "");
        }

        vista.encabezado.btnAtras.setVisibility(View.VISIBLE);
        vista.encabezado.btnAtras.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (paso == PASO_CORREO) {
                    finish();
                } else {
                    mostrarPaso(paso - 1);
                }
            }
        });

        vista.btnEnviarCodigo.setOnClickListener(v -> pedirCodigo(vista.btnEnviarCodigo, R.string.recuperar_enviar));
        vista.btnReenviar.setOnClickListener(v -> pedirCodigo(vista.btnReenviar, R.string.recuperar_reenviar));
        vista.btnVerificar.setOnClickListener(v -> verificar());
        vista.btnGuardar.setOnClickListener(v -> guardar());

        mostrarPaso(paso);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putInt(ESTADO_PASO, paso);
        estado.putString(ESTADO_CORREO, correo);
        estado.putString(ESTADO_CODIGO, codigo);
    }

    private void mostrarPaso(int nuevo) {
        paso = nuevo;
        vista.pasoCorreo.setVisibility(paso == PASO_CORREO ? View.VISIBLE : View.GONE);
        vista.pasoCodigo.setVisibility(paso == PASO_CODIGO ? View.VISIBLE : View.GONE);
        vista.pasoPassword.setVisibility(paso == PASO_PASSWORD ? View.VISIBLE : View.GONE);
        vista.tvDescripcionCodigo.setText(getString(R.string.recuperar_paso_codigo, correo));
    }

    /** Paso 1 (o "Enviar otro código"): respuesta neutra; pasa al paso 2 exista o no el correo. */
    private void pedirCodigo(Button boton, @StringRes int textoBoton) {
        if (!boton.isEnabled()) {
            return;
        }
        Formularios.limpiar(vista.tilCorreo);
        String valor = paso == PASO_CORREO ? Formularios.texto(vista.etCorreo) : correo;
        if (!Formularios.CORREO.matcher(valor).matches()) {
            Formularios.error(vista.tilCorreo, R.string.error_correo);
            return;
        }

        Formularios.cargando(boton, true, textoBoton, R.string.recuperar_enviando);
        llamada = Llamada.de(api.pedirCodigo(cuerpo("correo", valor))).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void datos) {
            }

            @Override
            public void alExito(Void datos, String mensaje) {
                Formularios.cargando(boton, false, textoBoton, R.string.recuperar_enviando);
                correo = valor;
                vista.etCodigo.setText(null);
                Formularios.limpiar(vista.tilCodigo);
                mostrarPaso(PASO_CODIGO);
                vista.etCodigo.requestFocus();
                Snackbar.make(vista.raiz, mensaje, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(boton, false, textoBoton, R.string.recuperar_enviando);
                if (paso == PASO_CORREO && error.campo("correo") != null) {
                    vista.tilCorreo.setError(error.campo("correo"));
                } else {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /** Paso 2: el código se comprueba antes de pedir la contraseña nueva. */
    private void verificar() {
        if (!vista.btnVerificar.isEnabled()) {
            return;
        }
        Formularios.limpiar(vista.tilCodigo);
        String valor = Formularios.texto(vista.etCodigo);
        if (!valor.matches("\\d{6}")) {
            Formularios.error(vista.tilCodigo, R.string.recuperar_codigo_incompleto);
            return;
        }

        Map<String, String> cuerpo = cuerpo("correo", correo);
        cuerpo.put("codigo", valor);
        Formularios.cargando(vista.btnVerificar, true, R.string.recuperar_verificar, R.string.recuperar_enviando);
        llamada = Llamada.de(api.verificar(cuerpo)).enviar(new Llamada.Respuestas<Map<String, Boolean>>() {
            @Override
            public void alExito(Map<String, Boolean> datos) {
                Formularios.cargando(vista.btnVerificar, false, R.string.recuperar_verificar, R.string.recuperar_enviando);
                codigo = valor;
                mostrarPaso(PASO_PASSWORD);
                vista.etNueva.requestFocus();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnVerificar, false, R.string.recuperar_verificar, R.string.recuperar_enviando);
                errorDeCodigo(error);
            }
        });
    }

    /** Paso 3: contraseña nueva; la API cierra todas las sesiones de la cuenta. */
    private void guardar() {
        if (!vista.btnGuardar.isEnabled()) {
            return;
        }
        Formularios.limpiar(vista.tilNueva, vista.tilConfirmar);
        String nueva = String.valueOf(vista.etNueva.getText());
        if (nueva.length() < Formularios.MIN_PASSWORD) {
            Formularios.error(vista.tilNueva, R.string.error_password_corta);
            return;
        }
        if (!nueva.equals(String.valueOf(vista.etConfirmar.getText()))) {
            Formularios.error(vista.tilConfirmar, R.string.error_no_coinciden);
            return;
        }

        Map<String, String> cuerpo = cuerpo("correo", correo);
        cuerpo.put("codigo", codigo);
        cuerpo.put("password", nueva);
        cuerpo.put("password_confirmation", nueva);
        Formularios.cargando(vista.btnGuardar, true, R.string.recuperar_guardar, R.string.recuperar_enviando);
        llamada = Llamada.de(api.cambiar(cuerpo)).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void datos) {
                startActivity(new Intent(RecuperarContrasenaActivity.this, LoginActivity.class)
                        .putExtra(LoginActivity.EXTRA_RECUPERADA, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnGuardar, false, R.string.recuperar_guardar, R.string.recuperar_enviando);
                if (error.campo("codigo") != null) {
                    // Venció o se agotaron los intentos entre pasos: se vuelve al código.
                    mostrarPaso(PASO_CODIGO);
                    errorDeCodigo(error);
                } else if (error.campo("password") != null) {
                    vista.tilNueva.setError(error.campo("password"));
                } else {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void errorDeCodigo(ErrorApi error) {
        if (error.campo("codigo") != null) {
            vista.tilCodigo.setError(error.campo("codigo"));
            vista.etCodigo.requestFocus();
        } else {
            Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private static Map<String, String> cuerpo(String clave, String valor) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put(clave, valor);
        return cuerpo;
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }
}
