package com.yelpme.app.ui.registro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioAuth;
import com.yelpme.app.databinding.ActivityRegistroBinding;
import com.yelpme.app.modelos.RespuestaSesion;
import com.yelpme.app.ui.inicio.InicioActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registro de usuario (POST /api/registro). La validación real es de la API. */
public class RegistroActivity extends AppCompatActivity {

    private ActivityRegistroBinding vista;
    private Llamada<RespuestaSesion> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Margenes.bordeABorde(this, false);
        vista = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.encabezado.getRoot(), vista.desplazable);

        vista.encabezado.btnAtras.setVisibility(android.view.View.VISIBLE);
        vista.encabezado.btnAtras.setOnClickListener(v -> finish());
        vista.btnYaTengoCuenta.setOnClickListener(v -> finish());
        vista.btnRegistrar.setOnClickListener(v -> registrar());
    }

    /** Nombre del campo en la API → su TextInputLayout (en orden de pantalla). */
    private Map<String, TextInputLayout> campos() {
        Map<String, TextInputLayout> campos = new LinkedHashMap<>();
        campos.put("nombre", vista.tilNombre);
        campos.put("apellido_paterno", vista.tilApellidoPaterno);
        campos.put("apellido_materno", vista.tilApellidoMaterno);
        campos.put("telefono", vista.tilTelefono);
        campos.put("correo", vista.tilCorreo);
        campos.put("password", vista.tilPassword);
        campos.put("password_confirmation", vista.tilConfirmacion);
        return campos;
    }

    private boolean validar() {
        Formularios.limpiar(campos().values().toArray(new TextInputLayout[0]));
        boolean ok = true;
        if (Formularios.texto(vista.etNombre).isEmpty()) ok = Formularios.error(vista.tilNombre, R.string.error_obligatorio);
        if (Formularios.texto(vista.etApellidoPaterno).isEmpty()) ok = Formularios.error(vista.tilApellidoPaterno, R.string.error_obligatorio);
        if (!Formularios.TELEFONO.matcher(Formularios.texto(vista.etTelefono)).matches()) ok = Formularios.error(vista.tilTelefono, R.string.error_telefono);
        if (!Formularios.CORREO.matcher(Formularios.texto(vista.etCorreo)).matches()) ok = Formularios.error(vista.tilCorreo, R.string.error_correo);
        String password = String.valueOf(vista.etPassword.getText());
        if (password.length() < Formularios.MIN_PASSWORD) ok = Formularios.error(vista.tilPassword, R.string.error_password_corta);
        if (!password.equals(String.valueOf(vista.etConfirmacion.getText()))) ok = Formularios.error(vista.tilConfirmacion, R.string.error_no_coinciden);
        return ok;
    }

    private void registrar() {
        if (!vista.btnRegistrar.isEnabled()) {
            return; // ya hay una petición en curso (p. ej. Enter + botón)
        }
        if (!validar()) {
            enfocarPrimerError();
            return;
        }

        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("nombre", Formularios.texto(vista.etNombre));
        cuerpo.put("apellido_paterno", Formularios.texto(vista.etApellidoPaterno));
        String materno = Formularios.texto(vista.etApellidoMaterno);
        if (!materno.isEmpty()) {
            cuerpo.put("apellido_materno", materno);
        }
        cuerpo.put("telefono", Formularios.texto(vista.etTelefono));
        cuerpo.put("correo", Formularios.texto(vista.etCorreo));
        cuerpo.put("password", String.valueOf(vista.etPassword.getText()));
        cuerpo.put("password_confirmation", String.valueOf(vista.etConfirmacion.getText()));

        Formularios.cargando(vista.btnRegistrar, true, R.string.registro_crear, R.string.registro_creando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioAuth.class).registro(cuerpo)).enviar(new Llamada.Respuestas<RespuestaSesion>() {
            @Override
            public void alExito(RespuestaSesion r) {
                ((YelpMeApp) getApplication()).getSesion().guardar(r.token, r.usuario);
                startActivity(new Intent(RegistroActivity.this, InicioActivity.class)
                        .putExtra(InicioActivity.EXTRA_VINCULADOS, r.contactosVinculados)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnRegistrar, false, R.string.registro_crear, R.string.registro_creando);
                if (!error.esValidacion() || !Formularios.mostrarErrores(error, campos())) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void enfocarPrimerError() {
        for (TextInputLayout campo : campos().values()) {
            if (campo.getError() != null && campo.getEditText() != null) {
                campo.getEditText().requestFocus();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }
}
