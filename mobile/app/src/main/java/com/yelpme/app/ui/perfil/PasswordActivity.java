package com.yelpme.app.ui.perfil;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.databinding.ActivityPasswordBinding;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cambiar la contraseña (PUT /api/perfil/password). La API cierra las demás
 * sesiones; esta sigue. OK → RESULT_OK.
 */
public class PasswordActivity extends AppCompatActivity {

    private ActivityPasswordBinding vista;
    private Llamada<Void> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);

        vista.barra.tvTitulo.setText(R.string.password_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.btnGuardar.setOnClickListener(v -> guardar());
        vista.etConfirmacion.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                guardar();
                return true;
            }
            return false;
        });
    }

    private Map<String, TextInputLayout> campos() {
        Map<String, TextInputLayout> campos = new LinkedHashMap<>();
        campos.put("actual", vista.tilActual);
        campos.put("nueva", vista.tilNueva);
        campos.put("nueva_confirmation", vista.tilConfirmacion);
        return campos;
    }

    private boolean validar() {
        Formularios.limpiar(campos().values().toArray(new TextInputLayout[0]));
        boolean ok = true;
        String nueva = String.valueOf(vista.etNueva.getText());
        if (String.valueOf(vista.etActual.getText()).isEmpty()) ok = Formularios.error(vista.tilActual, R.string.error_obligatorio);
        if (nueva.length() < Formularios.MIN_PASSWORD) ok = Formularios.error(vista.tilNueva, R.string.error_password_corta);
        if (!nueva.equals(String.valueOf(vista.etConfirmacion.getText()))) ok = Formularios.error(vista.tilConfirmacion, R.string.error_no_coinciden);
        return ok;
    }

    private void guardar() {
        if (!vista.btnGuardar.isEnabled() || !validar()) {
            return;
        }
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("actual", String.valueOf(vista.etActual.getText()));
        cuerpo.put("nueva", String.valueOf(vista.etNueva.getText()));
        cuerpo.put("nueva_confirmation", String.valueOf(vista.etConfirmacion.getText()));

        Formularios.cargando(vista.btnGuardar, true, R.string.password_guardar, R.string.comun_guardando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioPerfil.class).cambiarPassword(cuerpo)).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void nada) {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnGuardar, false, R.string.password_guardar, R.string.comun_guardando);
                if (!error.esValidacion() || !Formularios.mostrarErrores(error, campos())) {
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
