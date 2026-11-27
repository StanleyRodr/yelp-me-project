package com.yelpme.app.ui.contactos;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioContactos;
import com.yelpme.app.databinding.ActivityFormContactoBinding;
import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.modelos.VerificacionTelefono;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Crear (POST) o editar (PUT, solo lo que cambió) un contacto. Mientras se
 * escribe el teléfono se consulta si tiene cuenta (GET /api/telefonos/{t}).
 * OK → RESULT_OK con {@link #EXTRA_MENSAJE} para el Snackbar de quien lo abrió.
 */
public class FormContactoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_MENSAJE = "mensaje";

    private static final long ESPERA_VERIFICAR_MS = 600;

    private ActivityFormContactoBinding vista;
    private ServicioContactos api;
    private long id;
    /** Contacto como llegó de la API (solo al editar). */
    private Contacto original;
    private Llamada<?> llamada;
    private Llamada<VerificacionTelefono> llamadaVerificar;
    private final Handler espera = new Handler(Looper.getMainLooper());
    private final Runnable verificar = this::verificarTelefono;

    public static Intent nuevo(Context contexto) {
        return new Intent(contexto, FormContactoActivity.class);
    }

    public static Intent editar(Context contexto, long id) {
        return new Intent(contexto, FormContactoActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityFormContactoBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);
        api = ClienteApi.servicio(ServicioContactos.class);

        id = getIntent().getLongExtra(EXTRA_ID, 0);
        vista.barra.tvTitulo.setText(id == 0 ? R.string.contactos_agregar_titulo : R.string.contactos_editar_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.btnCancelar.setOnClickListener(v -> finish());
        vista.btnGuardar.setOnClickListener(v -> guardar());
        vista.etParentesco.setSimpleItems(getResources().getStringArray(R.array.parentescos));

        vista.etTelefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int cuenta, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int cuenta) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                espera.removeCallbacks(verificar);
                ayudaTelefono(getString(R.string.contactos_telefono_ayuda), false);
                vista.tilTelefono.setError(null);
                if (Formularios.TELEFONO.matcher(s.toString()).matches()) {
                    espera.postDelayed(verificar, ESPERA_VERIFICAR_MS);
                }
            }
        });

        // Al editar se precarga; tras rotar, los campos ya conservan lo escrito.
        if (id != 0) {
            cargar(savedInstanceState == null);
        }
    }

    @Override
    protected void onDestroy() {
        espera.removeCallbacks(verificar);
        if (llamada != null) {
            llamada.cancelar();
        }
        if (llamadaVerificar != null) {
            llamadaVerificar.cancelar();
        }
        super.onDestroy();
    }

    private void cargar(boolean llenarCampos) {
        vista.btnGuardar.setEnabled(false);
        llamada = Llamada.de(api.obtener(id)).enviar(new Llamada.Respuestas<Contacto>() {
            @Override
            public void alExito(Contacto c) {
                original = c;
                vista.btnGuardar.setEnabled(true);
                if (llenarCampos) {
                    vista.etNombre.setText(c.nombre);
                    vista.etTelefono.setText(c.telefono);
                    vista.etParentesco.setText(c.parentesco, false);
                }
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() != 401) {
                    Snackbar.make(vista.raiz, R.string.contactos_error_obtener, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar(llenarCampos))
                            .show();
                }
            }
        });
    }

    // ---------- Verificación del teléfono ----------

    private void verificarTelefono() {
        String telefono = Formularios.texto(vista.etTelefono);
        if (original != null && telefono.equals(original.telefono)) {
            return; // es el mismo número del contacto que se edita
        }
        if (llamadaVerificar != null) {
            llamadaVerificar.cancelar();
        }
        llamadaVerificar = Llamada.de(api.verificar(telefono)).enviar(new Llamada.Respuestas<VerificacionTelefono>() {
            @Override
            public void alExito(VerificacionTelefono r) {
                if (!telefono.equals(Formularios.texto(vista.etTelefono))) {
                    return; // ya cambió mientras llegaba la respuesta
                }
                if (r.enAgenda) {
                    vista.tilTelefono.setError(getString(R.string.contactos_tel_en_agenda));
                } else if (r.vinculable) {
                    ayudaTelefono(getString(R.string.contactos_tel_con_cuenta), true);
                } else {
                    ayudaTelefono(getString(R.string.contactos_tel_sin_cuenta), false);
                }
            }

            @Override
            public void alError(ErrorApi error) {
                // 429 u otro error: no se muestra nada; al guardar la API valida igual.
            }
        });
    }

    private void ayudaTelefono(String texto, boolean conCuenta) {
        vista.tilTelefono.setHelperText(texto);
        vista.tilTelefono.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(this,
                conCuenta ? R.color.ym_estado_terminado_texto : R.color.ym_texto_secundario)));
    }

    // ---------- Guardar ----------

    private Map<String, TextInputLayout> campos() {
        Map<String, TextInputLayout> campos = new LinkedHashMap<>();
        campos.put("nombre", vista.tilNombre);
        campos.put("telefono", vista.tilTelefono);
        campos.put("parentesco", vista.tilParentesco);
        return campos;
    }

    private boolean validar() {
        Formularios.limpiar(vista.tilNombre, vista.tilParentesco);
        boolean ok = vista.tilTelefono.getError() == null;
        if (Formularios.texto(vista.etNombre).isEmpty()) ok = Formularios.error(vista.tilNombre, R.string.error_obligatorio);
        if (!Formularios.TELEFONO.matcher(Formularios.texto(vista.etTelefono)).matches()) ok = Formularios.error(vista.tilTelefono, R.string.error_telefono);
        if (Formularios.texto(vista.etParentesco).isEmpty()) ok = Formularios.error(vista.tilParentesco, R.string.error_obligatorio);
        return ok;
    }

    private void guardar() {
        if (!vista.btnGuardar.isEnabled() || !validar()) {
            return;
        }
        String nombre = Formularios.texto(vista.etNombre);
        String telefono = Formularios.texto(vista.etTelefono);
        String parentesco = Formularios.texto(vista.etParentesco);

        Contacto cuerpo;
        if (original == null) {
            cuerpo = new Contacto(nombre, telefono, parentesco);
        } else {
            // Solo lo que cambió (Gson omite los null).
            cuerpo = new Contacto(
                    Objects.equals(nombre, original.nombre) ? null : nombre,
                    Objects.equals(telefono, original.telefono) ? null : telefono,
                    Objects.equals(parentesco, original.parentesco) ? null : parentesco);
            if (cuerpo.nombre == null && cuerpo.telefono == null && cuerpo.parentesco == null) {
                finish();
                return;
            }
        }

        Formularios.cargando(vista.btnGuardar, true, R.string.comun_guardar, R.string.comun_guardando);
        llamada = Llamada.de(original == null ? api.crear(cuerpo) : api.actualizar(id, cuerpo)).enviar(new Llamada.Respuestas<Contacto>() {
            @Override
            public void alExito(Contacto guardado) {
                boolean recienVinculado = guardado.vinculado && (original == null || !original.vinculado);
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_MENSAJE, getString(recienVinculado
                        ? R.string.contactos_guardado_vinculado : R.string.contactos_guardado)));
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnGuardar, false, R.string.comun_guardar, R.string.comun_guardando);
                if (!error.esValidacion() || !Formularios.mostrarErrores(error, campos())) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
