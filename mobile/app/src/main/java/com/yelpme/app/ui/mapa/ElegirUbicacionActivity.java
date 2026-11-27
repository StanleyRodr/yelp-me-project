package com.yelpme.app.ui.mapa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.databinding.ActivityElegirUbicacionBinding;
import com.yelpme.app.R;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Margenes;

import org.osmdroid.util.GeoPoint;

/**
 * Selector de un punto en pantalla completa: el pin queda al centro y se mueve
 * el mapa. Se abre con {@link #intent} y `StartActivityForResult`; devuelve
 * {@link #EXTRA_LAT} / {@link #EXTRA_LNG}. Lo usan el domicilio (plan 009) y
 * la solicitud (plan 011).
 */
public class ElegirUbicacionActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";
    public static final String EXTRA_TITULO = "titulo";

    private static final String ESTADO_LAT = "estado_lat";
    private static final String ESTADO_LNG = "estado_lng";

    private ActivityElegirUbicacionBinding vista;
    private MapaOsm mapa;

    private final ActivityResultLauncher<String> pedirPermiso = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    irAMiUbicacion();
                } else {
                    aviso(R.string.ubicacion_sin_permiso);
                }
            });

    /** `lat`/`lng` null = Misantla. */
    public static Intent intent(Context contexto, Double lat, Double lng, String titulo) {
        Intent intent = new Intent(contexto, ElegirUbicacionActivity.class);
        if (lat != null && lng != null) {
            intent.putExtra(EXTRA_LAT, lat).putExtra(EXTRA_LNG, lng);
        }
        return intent.putExtra(EXTRA_TITULO, titulo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityElegirUbicacionBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barraUbicacion, vista.panelUbicacion);

        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        if (titulo != null) {
            vista.tvTituloUbicacion.setText(titulo);
        }

        mapa = new MapaOsm(this, vista.contenedorMapa, getLifecycle());
        // Al rotar se conserva el punto que se estaba eligiendo.
        if (savedInstanceState != null) {
            mapa.centrar(savedInstanceState.getDouble(ESTADO_LAT), savedInstanceState.getDouble(ESTADO_LNG), MapaOsm.ZOOM_CALLE);
        } else {
            Intent entrada = getIntent();
            mapa.centrar(entrada.getDoubleExtra(EXTRA_LAT, MapaOsm.LAT_INICIAL),
                    entrada.getDoubleExtra(EXTRA_LNG, MapaOsm.LNG_INICIAL), MapaOsm.ZOOM_CALLE);
        }

        vista.btnAtras.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        vista.btnMiUbicacion.setOnClickListener(v -> {
            if (tienePermiso()) {
                irAMiUbicacion();
            } else {
                pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
        vista.btnUsarUbicacion.setOnClickListener(v -> {
            GeoPoint centro = mapa.centro();
            setResult(RESULT_OK, new Intent()
                    .putExtra(EXTRA_LAT, centro.getLatitude())
                    .putExtra(EXTRA_LNG, centro.getLongitude()));
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        GeoPoint centro = mapa.centro();
        estado.putDouble(ESTADO_LAT, centro.getLatitude());
        estado.putDouble(ESTADO_LNG, centro.getLongitude());
    }

    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission") // tienePermiso() se revisa antes de llamar
    private void irAMiUbicacion() {
        vista.btnMiUbicacion.setEnabled(false);
        LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener(this, tarea -> {
                    vista.btnMiUbicacion.setEnabled(true);
                    if (tarea.isSuccessful() && tarea.getResult() != null) {
                        mapa.centrar(tarea.getResult().getLatitude(), tarea.getResult().getLongitude(), MapaOsm.ZOOM_CALLE);
                    } else {
                        aviso(R.string.ubicacion_sin_gps);
                    }
                });
    }

    private void aviso(int texto) {
        Snackbar.make(vista.getRoot(), texto, Snackbar.LENGTH_LONG).setAnchorView(vista.panelUbicacion).show();
    }
}
