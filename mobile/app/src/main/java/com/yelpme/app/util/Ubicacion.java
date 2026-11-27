package com.yelpme.app.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yelpme.app.R;

import java.util.Map;

/**
 * Ubicación actual para una solicitud (plan principal 7.5): pide permisos, pide
 * encender la ubicación si está apagada y espera hasta 10 s. Si falla, el
 * resultado dice por qué para ofrecer domicilio, reintentar o mapa
 * ({@code DialogoSinUbicacion}). Crear en onCreate: registra sus launchers.
 */
public class Ubicacion {

    public interface Resultado {
        void alObtener(double lat, double lng);

        void alFallar(Motivo motivo);
    }

    public enum Motivo { SIN_PERMISO, SIN_PERMISO_PERMANENTE, GPS_APAGADO, TIEMPO_AGOTADO, ERROR }

    /** De dónde salió el punto de la solicitud. */
    public enum Origen { GPS, DOMICILIO, MAPA }

    private static final long LIMITE_MS = 10_000;

    private final ComponentActivity activity;
    private final FusedLocationProviderClient cliente;
    private final ActivityResultLauncher<String[]> permisos;
    private final ActivityResultLauncher<IntentSenderRequest> encenderGps;
    private final Handler principal = new Handler(Looper.getMainLooper());
    private Resultado pendiente;

    public Ubicacion(ComponentActivity activity) {
        this.activity = activity;
        this.cliente = LocationServices.getFusedLocationProviderClient(activity);
        this.permisos = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::alResponderPermisos);
        this.encenderGps = activity.registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), r -> {
            // Si no acepta encender o mejorar la precisión, igual se intenta leer:
            // a veces solo faltaba la "precisión de ubicación" de Google.
            leer(r.getResultCode() == Activity.RESULT_OK ? null : Motivo.GPS_APAGADO);
        });
    }

    public void obtener(Resultado resultado) {
        pendiente = resultado;
        if (tienePermiso()) {
            revisarAjustes();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.ubicacion_permiso_titulo)
                    .setMessage(R.string.ubicacion_permiso_explicacion)
                    .setNegativeButton(R.string.comun_cancelar, (d, i) -> fallar(Motivo.SIN_PERMISO))
                    .setPositiveButton(R.string.comun_continuar, (d, i) -> pedirPermisos())
                    .setOnCancelListener(d -> fallar(Motivo.SIN_PERMISO))
                    .show();
        } else {
            pedirPermisos();
        }
    }

    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisos() {
        permisos.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void alResponderPermisos(Map<String, Boolean> concedidos) {
        if (tienePermiso()) {
            revisarAjustes();
            return;
        }
        // Negado sin opción de volver a preguntar: solo queda Ajustes.
        boolean permanente = !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        fallar(permanente ? Motivo.SIN_PERMISO_PERMANENTE : Motivo.SIN_PERMISO);
    }

    /** Ubicación apagada → el sistema ofrece encenderla. */
    private void revisarAjustes() {
        LocationSettingsRequest ajustes = new LocationSettingsRequest.Builder()
                .addLocationRequest(new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build())
                .build();
        LocationServices.getSettingsClient(activity).checkLocationSettings(ajustes)
                .addOnSuccessListener(activity, r -> leer(null))
                .addOnFailureListener(activity, e -> {
                    if (e instanceof ResolvableApiException) {
                        encenderGps.launch(new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build());
                    } else {
                        fallar(Motivo.GPS_APAGADO);
                    }
                });
    }

    /** `motivoSiFalla`: motivo a informar si no llega punto (null = según el resultado). */
    @SuppressLint("MissingPermission") // tienePermiso() se revisó antes
    private void leer(Motivo motivoSiFalla) {
        CancellationTokenSource cancelar = new CancellationTokenSource();
        Runnable limite = cancelar::cancel;
        principal.postDelayed(limite, LIMITE_MS);
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelar.getToken())
                .addOnCompleteListener(activity, tarea -> {
                    principal.removeCallbacks(limite);
                    Location punto = tarea.isSuccessful() ? tarea.getResult() : null;
                    if (punto != null) {
                        exito(punto);
                        return;
                    }
                    // Sin lectura nueva: la última conocida antes de rendirse.
                    cliente.getLastLocation().addOnCompleteListener(activity, ultima -> {
                        if (ultima.isSuccessful() && ultima.getResult() != null) {
                            exito(ultima.getResult());
                        } else {
                            if (motivoSiFalla != null) {
                                fallar(motivoSiFalla);
                            } else {
                                fallar(tarea.isCanceled() || cancelar.getToken().isCancellationRequested()
                                        ? Motivo.TIEMPO_AGOTADO : Motivo.ERROR);
                            }
                        }
                    });
                });
    }

    private void exito(Location punto) {
        Resultado r = pendiente;
        pendiente = null;
        if (r != null) {
            r.alObtener(punto.getLatitude(), punto.getLongitude());
        }
    }

    private void fallar(Motivo motivo) {
        Resultado r = pendiente;
        pendiente = null;
        if (r != null) {
            r.alFallar(motivo);
        }
    }
}
