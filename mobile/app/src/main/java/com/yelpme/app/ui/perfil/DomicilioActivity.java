package com.yelpme.app.ui.perfil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.databinding.ActivityDomicilioBinding;
import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.ui.mapa.ElegirUbicacionActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Margenes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agregar, editar o quitar el domicilio (PUT|DELETE /api/perfil/domicilio).
 * El punto se elige en {@link ElegirUbicacionActivity}. Devuelve RESULT_OK con
 * {@link #EXTRA_MENSAJE} para el aviso del perfil.
 */
public class DomicilioActivity extends AppCompatActivity {

    public static final String EXTRA_DOMICILIO = "domicilio";
    public static final String EXTRA_MENSAJE = "mensaje";

    private static final String ESTADO_LAT = "lat";
    private static final String ESTADO_LNG = "lng";

    private ActivityDomicilioBinding vista;
    private MapaOsm mapa;
    private Double lat;
    private Double lng;
    private boolean tieneDomicilio;
    private Llamada<?> llamada;

    private final ActivityResultLauncher<Intent> elegirPunto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Intent datos = resultado.getData();
                if (resultado.getResultCode() == RESULT_OK && datos != null) {
                    ponerPunto(datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LAT, 0),
                            datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LNG, 0));
                }
            });

    /** `actual` null = agregar. */
    public static Intent intent(Context contexto, Domicilio actual) {
        Intent intent = new Intent(contexto, DomicilioActivity.class);
        if (actual != null) {
            intent.putExtra(EXTRA_DOMICILIO, new Gson().toJson(actual));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityDomicilioBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);

        vista.barra.tvTitulo.setText(R.string.domicilio_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());

        mapa = new MapaOsm(this, vista.contenedorMapa, getLifecycle());
        mapa.soloLectura(true);

        String json = getIntent().getStringExtra(EXTRA_DOMICILIO);
        Domicilio actual = json != null ? new Gson().fromJson(json, Domicilio.class) : null;
        tieneDomicilio = actual != null;
        vista.btnQuitar.setVisibility(tieneDomicilio ? View.VISIBLE : View.GONE);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ESTADO_LAT)) {
                ponerPunto(savedInstanceState.getDouble(ESTADO_LAT), savedInstanceState.getDouble(ESTADO_LNG));
            }
        } else if (actual != null) {
            vista.etCalle.setText(actual.calle);
            vista.etNumero.setText(actual.numero);
            vista.etColonia.setText(actual.colonia);
            vista.etCodigoPostal.setText(actual.codigoPostal);
            vista.etReferencia.setText(actual.referencia);
            ponerPunto(actual.latitud, actual.longitud);
        }

        vista.btnMarcar.setOnClickListener(v -> elegirPunto.launch(
                ElegirUbicacionActivity.intent(this, lat, lng, getString(R.string.domicilio_punto))));
        vista.btnGuardar.setOnClickListener(v -> guardar());
        vista.btnQuitar.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.domicilio_quitar_pregunta)
                .setNegativeButton(R.string.comun_cancelar, null)
                .setPositiveButton(R.string.domicilio_quitar, (d, i) -> quitar())
                .show());
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        if (lat != null && lng != null) {
            estado.putDouble(ESTADO_LAT, lat);
            estado.putDouble(ESTADO_LNG, lng);
        }
    }

    private void ponerPunto(double latitud, double longitud) {
        lat = latitud;
        lng = longitud;
        vista.tvErrorPunto.setVisibility(View.GONE);
        vista.contenedorMapa.setVisibility(View.VISIBLE);
        vista.btnMarcar.setText(R.string.domicilio_mover);
        mapa.centrar(latitud, longitud, MapaOsm.ZOOM_CALLE);
        mapa.marcador(latitud, longitud, R.drawable.ic_place, getString(R.string.domicilio_punto));
    }

    private Map<String, TextInputLayout> campos() {
        Map<String, TextInputLayout> campos = new LinkedHashMap<>();
        campos.put("calle", vista.tilCalle);
        campos.put("numero", vista.tilNumero);
        campos.put("colonia", vista.tilColonia);
        campos.put("codigo_postal", vista.tilCodigoPostal);
        campos.put("referencia", vista.tilReferencia);
        return campos;
    }

    private boolean validar() {
        Formularios.limpiar(campos().values().toArray(new TextInputLayout[0]));
        boolean ok = true;
        if (Formularios.texto(vista.etCalle).isEmpty()) ok = Formularios.error(vista.tilCalle, R.string.error_obligatorio);
        if (Formularios.texto(vista.etNumero).isEmpty()) ok = Formularios.error(vista.tilNumero, R.string.error_obligatorio);
        if (Formularios.texto(vista.etColonia).isEmpty()) ok = Formularios.error(vista.tilColonia, R.string.error_obligatorio);
        if (!Formularios.texto(vista.etCodigoPostal).matches("\\d{5}")) ok = Formularios.error(vista.tilCodigoPostal, R.string.domicilio_error_cp);
        if (lat == null || lng == null) {
            vista.tvErrorPunto.setVisibility(View.VISIBLE);
            ok = false;
        }
        return ok;
    }

    private void guardar() {
        if (!vista.btnGuardar.isEnabled() || !validar()) {
            return;
        }
        Domicilio d = new Domicilio();
        d.calle = Formularios.texto(vista.etCalle);
        d.numero = Formularios.texto(vista.etNumero);
        d.colonia = Formularios.texto(vista.etColonia);
        d.codigoPostal = Formularios.texto(vista.etCodigoPostal);
        String referencia = Formularios.texto(vista.etReferencia);
        d.referencia = referencia.isEmpty() ? null : referencia;
        d.latitud = lat;
        d.longitud = lng;

        ocupado(true);
        llamada = Llamada.de(ClienteApi.servicio(ServicioPerfil.class).guardarDomicilio(d)).enviar(new Llamada.Respuestas<Domicilio>() {
            @Override
            public void alExito(Domicilio guardado) {
                terminar(R.string.domicilio_guardado);
            }

            @Override
            public void alError(ErrorApi error) {
                ocupado(false);
                boolean bajoCampo = error.esValidacion() && Formularios.mostrarErrores(error, campos());
                if (error.campo("latitud") != null || error.campo("longitud") != null) {
                    vista.tvErrorPunto.setVisibility(View.VISIBLE);
                    bajoCampo = true;
                }
                if (!bajoCampo) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void quitar() {
        ocupado(true);
        llamada = Llamada.de(ClienteApi.servicio(ServicioPerfil.class).quitarDomicilio()).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void nada) {
                terminar(R.string.domicilio_quitado);
            }

            @Override
            public void alError(ErrorApi error) {
                ocupado(false);
                Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void ocupado(boolean ocupado) {
        Formularios.cargando(vista.btnGuardar, ocupado, R.string.domicilio_guardar, R.string.comun_guardando);
        vista.btnQuitar.setEnabled(!ocupado);
        vista.btnMarcar.setEnabled(!ocupado);
    }

    private void terminar(int mensaje) {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_MENSAJE, getString(mensaje)));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }
}
