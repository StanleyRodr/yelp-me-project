package com.yelpme.app.ui.perfil;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.databinding.ActivityEditarPerfilBinding;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;
import com.yelpme.app.util.Sesion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Editar nombre, apellidos, teléfono y correo (PUT /api/perfil). Envía solo los
 * campos que cambiaron; materno vacío lo quita. OK → RESULT_OK.
 */
public class EditarPerfilActivity extends AppCompatActivity {

    private ActivityEditarPerfilBinding vista;
    private Sesion sesion;
    private Usuario original;
    private Llamada<Usuario> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);

        vista.barra.tvTitulo.setText(R.string.perfil_editar_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());

        sesion = ((YelpMeApp) getApplication()).getSesion();
        original = sesion.usuario();
        if (original == null) {
            finish();
            return;
        }
        if (savedInstanceState == null) {
            vista.etNombre.setText(original.nombre);
            vista.etApellidoPaterno.setText(original.apellidoPaterno);
            vista.etApellidoMaterno.setText(original.apellidoMaterno);
            vista.etTelefono.setText(original.telefono);
            vista.etCorreo.setText(original.correo);
        }

        vista.btnGuardar.setOnClickListener(v -> guardar());
        vista.etCorreo.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                guardar();
                return true;
            }
            return false;
        });
    }

    private Map<String, TextInputLayout> campos() {
        Map<String, TextInputLayout> campos = new LinkedHashMap<>();
        campos.put("nombre", vista.tilNombre);
        campos.put("apellido_paterno", vista.tilApellidoPaterno);
        campos.put("apellido_materno", vista.tilApellidoMaterno);
        campos.put("telefono", vista.tilTelefono);
        campos.put("correo", vista.tilCorreo);
        return campos;
    }

    private boolean validar() {
        Formularios.limpiar(campos().values().toArray(new TextInputLayout[0]));
        boolean ok = true;
        if (Formularios.texto(vista.etNombre).isEmpty()) ok = Formularios.error(vista.tilNombre, R.string.error_obligatorio);
        if (Formularios.texto(vista.etApellidoPaterno).isEmpty()) ok = Formularios.error(vista.tilApellidoPaterno, R.string.error_obligatorio);
        if (!Formularios.TELEFONO.matcher(Formularios.texto(vista.etTelefono)).matches()) ok = Formularios.error(vista.tilTelefono, R.string.error_telefono);
        if (!Formularios.CORREO.matcher(Formularios.texto(vista.etCorreo)).matches()) ok = Formularios.error(vista.tilCorreo, R.string.error_correo);
        return ok;
    }

    /** Solo lo que cambió respecto a la sesión. Materno vacío se manda "" (la API lo guarda null). */
    private Map<String, String> cambios() {
        Map<String, String> cambios = new HashMap<>();
        poner(cambios, "nombre", original.nombre, Formularios.texto(vista.etNombre));
        poner(cambios, "apellido_paterno", original.apellidoPaterno, Formularios.texto(vista.etApellidoPaterno));
        poner(cambios, "apellido_materno", original.apellidoMaterno, Formularios.texto(vista.etApellidoMaterno));
        poner(cambios, "telefono", original.telefono, Formularios.texto(vista.etTelefono));
        poner(cambios, "correo", original.correo, Formularios.texto(vista.etCorreo));
        return cambios;
    }

    private static void poner(Map<String, String> cambios, String campo, String antes, String ahora) {
        if (!Objects.equals(antes == null ? "" : antes, ahora)) {
            cambios.put(campo, ahora);
        }
    }

    private void guardar() {
        if (!vista.btnGuardar.isEnabled()) {
            return;
        }
        if (!validar()) {
            return;
        }
        Map<String, String> cambios = cambios();
        if (cambios.isEmpty()) {
            finish();
            return;
        }

        Formularios.cargando(vista.btnGuardar, true, R.string.perfil_guardar_cambios, R.string.comun_guardando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioPerfil.class).actualizar(cambios)).enviar(new Llamada.Respuestas<Usuario>() {
            @Override
            public void alExito(Usuario usuario) {
                sesion.actualizarUsuario(usuario);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnGuardar, false, R.string.perfil_guardar_cambios, R.string.comun_guardando);
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
