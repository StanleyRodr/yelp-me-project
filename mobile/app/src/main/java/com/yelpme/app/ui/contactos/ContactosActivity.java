package com.yelpme.app.ui.contactos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioContactos;
import com.yelpme.app.databinding.ActivityContactosBinding;
import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.ui.base.BaseActivity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Contactos de confianza: consultar del CRUD (GET /api/contactos). El favorito
 * llega primero. Búsqueda local por nombre, teléfono o parentesco.
 */
public class ContactosActivity extends BaseActivity {

    private ActivityContactosBinding vista;
    private AdaptadorContactos adaptador;
    private Llamada<List<Contacto>> llamada;
    private List<Contacto> todos = new ArrayList<>();
    private boolean cargado;

    /** Formulario y detalle devuelven un mensaje para el Snackbar. */
    private final ActivityResultLauncher<Intent> abrir = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Intent datos = resultado.getData();
                String mensaje = datos != null ? datos.getStringExtra(FormContactoActivity.EXTRA_MENSAJE) : null;
                if (resultado.getResultCode() == RESULT_OK && mensaje != null) {
                    aviso(mensaje).show();
                }
            });

    @Override
    protected int itemMenu() {
        return R.id.menu_contactos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityContactosBinding.inflate(getLayoutInflater());
        ponerContenido(vista);

        adaptador = new AdaptadorContactos(c -> abrir.launch(DetalleContactoActivity.intent(this, c.id)));
        vista.rvContactos.setLayoutManager(new LinearLayoutManager(this));
        vista.rvContactos.setAdapter(adaptador);

        vista.recargaContactos.setColorSchemeResources(R.color.ym_marca_texto);
        vista.recargaContactos.setProgressBackgroundColorSchemeResource(R.color.ym_superficie);
        vista.recargaContactos.setOnRefreshListener(this::cargar);
        vista.btnAgregarContacto.setOnClickListener(v -> abrir.launch(FormContactoActivity.nuevo(this)));
        vista.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int cuenta, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int cuenta) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filtrar();
            }
        });

        if (savedInstanceState == null) {
            avisoFavorito(getIntent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        avisoFavorito(intent);
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }

    /** Llegó desde el botón del favorito de la barra sin tener favorito. */
    private void avisoFavorito(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_ELEGIR_FAVORITO, false)) {
            aviso(R.string.contactos_elige_favorito).show();
        }
    }

    private void cargar() {
        if (llamada != null) {
            llamada.cancelar();
        }
        if (!cargado) {
            vista.recargaContactos.setRefreshing(true);
        }
        llamada = Llamada.de(ClienteApi.servicio(ServicioContactos.class).listar()).enviar(new Llamada.Respuestas<List<Contacto>>() {
            @Override
            public void alExito(List<Contacto> contactos) {
                vista.recargaContactos.setRefreshing(false);
                cargado = true;
                todos = contactos != null ? contactos : new ArrayList<>();
                filtrar();
            }

            @Override
            public void alError(ErrorApi error) {
                vista.recargaContactos.setRefreshing(false);
                if (error.getCodigo() != 401) {
                    aviso(R.string.contactos_error_cargar)
                            .setDuration(Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar())
                            .show();
                }
            }
        });
    }

    private void filtrar() {
        String busqueda = sinAcentos(String.valueOf(vista.etBuscar.getText()).trim());
        List<Contacto> visibles = new ArrayList<>();
        for (Contacto c : todos) {
            if (busqueda.isEmpty()
                    || sinAcentos(c.nombre).contains(busqueda)
                    || sinAcentos(c.parentesco).contains(busqueda)
                    || (c.telefono != null && c.telefono.contains(busqueda))) {
                visibles.add(c);
            }
        }
        adaptador.submitList(visibles);
        if (!cargado) {
            vista.tvVacio.setVisibility(View.GONE);
            return;
        }
        vista.tvVacio.setVisibility(visibles.isEmpty() ? View.VISIBLE : View.GONE);
        vista.tvVacio.setText(todos.isEmpty() ? R.string.contactos_vacio : R.string.contactos_sin_resultados);
    }

    /** Minúsculas y sin acentos: "Mamá" coincide con "mama". */
    private static String sinAcentos(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
