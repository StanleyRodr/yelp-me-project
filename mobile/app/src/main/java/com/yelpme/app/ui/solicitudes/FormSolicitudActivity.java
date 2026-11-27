package com.yelpme.app.ui.solicitudes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioSolicitudes;
import com.yelpme.app.databinding.ActivityFormSolicitudBinding;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.ui.inicio.FormularioSolicitud;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.Margenes;

/**
 * Editar una solicitud Pendiente (PUT /api/solicitudes/{id}): mismos campos que
 * Inicio, precargados. Si ya no está Pendiente, la API responde 409.
 */
public class FormSolicitudActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private ActivityFormSolicitudBinding vista;
    private FormularioSolicitud formulario;
    private ServicioSolicitudes api;
    private long id;
    private Llamada<Solicitud> llamada;

    public static Intent intent(Context contexto, long id) {
        return new Intent(contexto, FormSolicitudActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityFormSolicitudBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);
        api = ClienteApi.servicio(ServicioSolicitudes.class);
        formulario = new FormularioSolicitud(this, vista.formulario, ((YelpMeApp) getApplication()).getSesion());
        id = getIntent().getLongExtra(EXTRA_ID, 0);

        vista.barra.tvTitulo.setText(R.string.solicitud_editar_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.btnGuardar.setOnClickListener(v -> guardar());
        vista.btnGuardar.setEnabled(false);

        boolean restaurado = formulario.restaurar(savedInstanceState);
        llamada = Llamada.de(api.obtener(id)).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud s) {
                formulario.precargar(s, !restaurado);
                vista.btnGuardar.setEnabled(true);
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() != 401) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        formulario.guardar(estado);
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }

    private void guardar() {
        if (!vista.btnGuardar.isEnabled() || !formulario.validar()) {
            return;
        }
        Formularios.cargando(vista.btnGuardar, true, R.string.perfil_guardar_cambios, R.string.comun_guardando);
        llamada = Llamada.de(api.actualizar(id, formulario.datos(true))).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud s) {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnGuardar, false, R.string.perfil_guardar_cambios, R.string.comun_guardando);
                if (error.getCodigo() == 401) {
                    return;
                }
                if (error.getCodigo() == 403) {
                    formulario.refrescarUsuario();
                }
                String titulo = formulario.errorTitulo(error);
                Snackbar.make(vista.raiz, titulo != null ? titulo : error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
