package com.yelpme.app.ui.alertas;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.databinding.ActivityAlertaContactoBinding;
import com.yelpme.app.ui.login.LoginActivity;
import com.yelpme.app.util.Llamadas;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Margenes;

import java.util.Locale;
import java.util.Map;

/**
 * Un contacto pidió ayuda (notificación tipo alerta_contacto): quién, qué pasa,
 * dónde (mapa), llamarle y cómo llegar. Los datos llegan en el intent; requiere
 * sesión (sin ella abre el login).
 */
public class AlertaContactoActivity extends AppCompatActivity {

    private static final String[] CLAVES = {"nombre", "telefono", "titulo", "latitud", "longitud", "ubicacion_nombre"};

    private ActivityAlertaContactoBinding vista;

    /** Copia los datos del mensaje de FCM a los extras. */
    public static Intent intent(Context contexto, Map<String, String> datos) {
        Intent intent = new Intent(contexto, AlertaContactoActivity.class);
        for (String clave : CLAVES) {
            intent.putExtra(clave, datos.get(clave));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        if (!((YelpMeApp) getApplication()).getSesion().activa()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        vista = ActivityAlertaContactoBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);
        Llamadas llamadas = new Llamadas(this, texto -> Snackbar.make(vista.raiz, texto, Snackbar.LENGTH_LONG).show());

        Intent e = getIntent();
        String nombre = texto(e.getStringExtra("nombre"));
        String telefono = texto(e.getStringExtra("telefono"));
        String lugar = texto(e.getStringExtra("ubicacion_nombre"));
        double lat = numero(e.getStringExtra("latitud"), MapaOsm.LAT_INICIAL);
        double lng = numero(e.getStringExtra("longitud"), MapaOsm.LNG_INICIAL);
        String primerNombre = nombre.contains(" ") ? nombre.substring(0, nombre.indexOf(' ')) : nombre;

        vista.barra.tvTitulo.setText(R.string.alerta_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.tvQuien.setText(getString(R.string.alerta_quien, nombre));
        vista.tvTituloAlerta.setText(texto(e.getStringExtra("titulo")));
        vista.tvLugar.setText(lugar.isEmpty() ? getString(R.string.solicitud_sin_lugar) : lugar);

        MapaOsm mapa = new MapaOsm(this, vista.contenedorMapa, getLifecycle());
        mapa.soloLectura(true);
        mapa.centrar(lat, lng, MapaOsm.ZOOM_CALLE);
        mapa.marcador(lat, lng, R.drawable.ic_place, nombre);

        vista.btnLlamar.setText(getString(R.string.alerta_llamar, primerNombre));
        vista.btnLlamar.setEnabled(!telefono.isEmpty());
        vista.btnLlamar.setOnClickListener(v -> llamadas.llamar(telefono));
        vista.btnComoLlegar.setOnClickListener(v -> {
            // Abre la app de mapas del teléfono en el punto de la solicitud.
            String punto = String.format(Locale.ROOT, "%f,%f", lat, lng);
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + punto + "?q=" + punto)));
            } catch (ActivityNotFoundException ex) {
                Snackbar.make(vista.raiz, R.string.alerta_sin_mapas, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static double numero(String valor, double porDefecto) {
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException | NullPointerException e) {
            return porDefecto;
        }
    }
}
