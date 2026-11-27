package com.yelpme.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private SesionManager sesion;

    private EditText etEmergencia, etLugar, etComentarios;
    private Button btnLimpiar, btnEnviar;
    private ImageButton btnPolicia, btnCruzRoja, btnBomberos, btnProteccion, btnTransito, btnContacto;

    private View rootLayout;
    private CardView cardTuSolicitud;
    private TextView tvBannerEstado, tvDetalleEmergencia, tvDetalleLugar, tvDetalleComentarios;
    private LinearLayout llDetalleAyudas;
    private FrameLayout mapDetalleContainer;
    private Button btnEditarSolicitud, btnCancelarSolicitud;

    private final List<Integer> ayudasSeleccionadas = new ArrayList<>();
    private String estadoActual = "SIN_ATENDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        sesion = new SesionManager(this);

        TextView tvSaludo = findViewById(R.id.tvSaludoUsuario);
        tvSaludo.setText("¡Hola " + sesion.getNombre() + "!");

        drawerLayout = findViewById(R.id.drawerLayoutSolicitudes);
        findViewById(R.id.btnMenuHamburguesa).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        configurarNavegacionMenu();
        configurarBarraInferiorEmergencias();

        rootLayout = findViewById(R.id.main);
        etEmergencia = findViewById(R.id.etEmergencia);
        etLugar = findViewById(R.id.etLugar);
        etComentarios = findViewById(R.id.etComentarios);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnEnviar = findViewById(R.id.btnEnviarSolicitud);

        btnPolicia = findViewById(R.id.btnSelectPolicia);
        btnCruzRoja = findViewById(R.id.btnSelectCruzRoja);
        btnBomberos = findViewById(R.id.btnSelectBomberos);
        btnProteccion = findViewById(R.id.btnSelectProteccion);
        btnTransito = findViewById(R.id.btnSelectTransito);
        btnContacto = findViewById(R.id.btnSelectContacto);

        if (!sesion.getDireccion().equals("Sin dirección registrada")) {
            etLugar.setText(sesion.getDireccion());
        }

        cardTuSolicitud = findViewById(R.id.cardTuSolicitud);
        tvBannerEstado = findViewById(R.id.tvBannerEstado);
        tvDetalleEmergencia = findViewById(R.id.tvDetalleEmergencia);
        tvDetalleLugar = findViewById(R.id.tvDetalleLugar);
        tvDetalleComentarios = findViewById(R.id.tvDetalleComentarios);
        llDetalleAyudas = findViewById(R.id.llDetalleAyudas);
        mapDetalleContainer = findViewById(R.id.mapDetalleContainer);
        btnEditarSolicitud = findViewById(R.id.btnEditarSolicitud);
        btnCancelarSolicitud = findViewById(R.id.btnCancelarSolicitud);

        // Cargar mapas en los contenedores de Solicitudes
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        MapaHelper.cargarMapaEnContenedor(this, mapContainer, MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Ubicación de emergencia");
        MapaHelper.cargarMapaEnContenedor(this, mapDetalleContainer, MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Punto de ayuda");

        configurarBotonAyuda(btnPolicia, android.R.drawable.ic_dialog_alert);
        configurarBotonAyuda(btnCruzRoja, android.R.drawable.ic_input_add);
        configurarBotonAyuda(btnBomberos, android.R.drawable.ic_dialog_info);
        configurarBotonAyuda(btnProteccion, android.R.drawable.ic_menu_compass);
        configurarBotonAyuda(btnTransito, android.R.drawable.ic_menu_directions);
        configurarBotonAyuda(btnContacto, android.R.drawable.ic_menu_call);

        restaurarSolicitudActiva();

        tvBannerEstado.setOnClickListener(v -> {
            if (estadoActual.equals("SIN_ATENDER")) {
                actualizarEstadoSolicitud("EN_PROCESO");
            } else if (estadoActual.equals("EN_PROCESO")) {
                actualizarEstadoSolicitud("ATENDIDA");
            } else {
                actualizarEstadoSolicitud("SIN_ATENDER");
            }
            guardarSolicitudActiva();
        });

        btnLimpiar.setOnClickListener(v -> limpiarFormulario());

        btnEnviar.setOnClickListener(v -> {
            String emergencia = etEmergencia.getText().toString().trim();
            String lugar = etLugar.getText().toString().trim();

            if (emergencia.isEmpty() || lugar.isEmpty()) {
                Toast.makeText(this, "Por favor completa emergencia y lugar", Toast.LENGTH_SHORT).show();
                return;
            }
            mostrarModalResumen(emergencia, lugar);
        });

        btnEditarSolicitud.setOnClickListener(v -> {
            etEmergencia.setText(tvDetalleEmergencia.getText().toString());
            etLugar.setText(tvDetalleLugar.getText().toString());
            String comentariosActuales = tvDetalleComentarios.getText().toString();
            etComentarios.setText(comentariosActuales.equals("Sin comentarios") ? "" : comentariosActuales);

            Toast.makeText(this, "Datos cargados arriba para editar", Toast.LENGTH_SHORT).show();
        });

        btnCancelarSolicitud.setOnClickListener(v -> {
            cardTuSolicitud.setVisibility(View.GONE);
            rootLayout.setBackgroundColor(Color.parseColor("#FDA836"));
            borrarSolicitudActiva();
            Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarModalResumen(String emergencia, String lugar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View vistaModal = LayoutInflater.from(this).inflate(R.layout.dialog_resumen, null);
        builder.setView(vistaModal);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvEmergenciaModal = vistaModal.findViewById(R.id.tvResumenEmergencia);
        TextView tvLugarModal = vistaModal.findViewById(R.id.tvResumenLugar);
        FrameLayout mapModal = vistaModal.findViewById(R.id.mapResumenContainer);
        LinearLayout llAyudasModal = vistaModal.findViewById(R.id.llAyudasSeleccionadas);
        ImageButton btnCerrarModal = vistaModal.findViewById(R.id.btnCerrarModal);
        Button btnAceptarModal = vistaModal.findViewById(R.id.btnModalAceptar);
        Button btnCancelarModal = vistaModal.findViewById(R.id.btnModalCancelar);

        tvEmergenciaModal.setText("Emergencia: " + emergencia);
        tvLugarModal.setText("Lugar: " + lugar);

        // Cargar mapa dentro del modal verde
        MapaHelper.cargarMapaEnContenedor(this, mapModal, MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Ubicación seleccionada");

        for (int iconRes : ayudasSeleccionadas) {
            ImageView iconView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, 110);
            params.setMarginEnd(20);
            iconView.setLayoutParams(params);
            iconView.setImageResource(iconRes);
            llAyudasModal.addView(iconView);
        }

        btnCerrarModal.setOnClickListener(v -> dialog.dismiss());
        btnCancelarModal.setOnClickListener(v -> dialog.dismiss());

        btnAceptarModal.setOnClickListener(v -> {
            tvDetalleEmergencia.setText(emergencia);
            tvDetalleLugar.setText(lugar);

            String comentarios = etComentarios.getText().toString().trim();
            tvDetalleComentarios.setText(comentarios.isEmpty() ? "Sin comentarios" : comentarios);

            llDetalleAyudas.removeAllViews();
            for (int iconRes : ayudasSeleccionadas) {
                ImageView icon = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
                params.setMarginEnd(14);
                icon.setLayoutParams(params);
                icon.setImageResource(iconRes);
                llDetalleAyudas.addView(icon);
            }

            cardTuSolicitud.setVisibility(View.VISIBLE);
            actualizarEstadoSolicitud("SIN_ATENDER");
            guardarSolicitudActiva();

            limpiarFormulario();
            dialog.dismiss();

            Toast.makeText(this, "Solicitud creada", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    public void actualizarEstadoSolicitud(String estado) {
        estadoActual = estado;
        switch (estado) {
            case "SIN_ATENDER":
                tvBannerEstado.setText("SIN ATENDER");
                tvBannerEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                rootLayout.setBackgroundColor(Color.parseColor("#FF3D00"));
                mapDetalleContainer.setVisibility(View.VISIBLE);
                break;

            case "EN_PROCESO":
                tvBannerEstado.setText("EN PROCESO");
                tvBannerEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8F00")));
                rootLayout.setBackgroundColor(Color.parseColor("#FFA000"));
                mapDetalleContainer.setVisibility(View.VISIBLE);
                break;

            case "ATENDIDA":
                tvBannerEstado.setText("ATENDIDA");
                tvBannerEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8BC34A")));
                rootLayout.setBackgroundColor(Color.parseColor("#9CCC65"));
                mapDetalleContainer.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void guardarSolicitudActiva() {
        SharedPreferences prefs = getSharedPreferences("SolicitudActiva", Context.MODE_PRIVATE);
        StringBuilder iconosStr = new StringBuilder();
        for (int id : ayudasSeleccionadas) {
            iconosStr.append(id).append(",");
        }
        prefs.edit()
                .putBoolean("activa", true)
                .putString("emergencia", tvDetalleEmergencia.getText().toString())
                .putString("lugar", tvDetalleLugar.getText().toString())
                .putString("comentarios", tvDetalleComentarios.getText().toString())
                .putString("estado", estadoActual)
                .putString("iconos", iconosStr.toString())
                .apply();
    }

    private void restaurarSolicitudActiva() {
        SharedPreferences prefs = getSharedPreferences("SolicitudActiva", Context.MODE_PRIVATE);
        if (prefs.getBoolean("activa", false)) {
            tvDetalleEmergencia.setText(prefs.getString("emergencia", ""));
            tvDetalleLugar.setText(prefs.getString("lugar", ""));
            tvDetalleComentarios.setText(prefs.getString("comentarios", "Sin comentarios"));

            String iconosGuardados = prefs.getString("iconos", "");
            llDetalleAyudas.removeAllViews();
            if (!iconosGuardados.isEmpty()) {
                for (String idStr : iconosGuardados.split(",")) {
                    if (!idStr.isEmpty()) {
                        ImageView icon = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
                        params.setMarginEnd(14);
                        icon.setLayoutParams(params);
                        icon.setImageResource(Integer.parseInt(idStr));
                        llDetalleAyudas.addView(icon);
                    }
                }
            }

            cardTuSolicitud.setVisibility(View.VISIBLE);
            actualizarEstadoSolicitud(prefs.getString("estado", "SIN_ATENDER"));
        }
    }

    private void borrarSolicitudActiva() {
        getSharedPreferences("SolicitudActiva", Context.MODE_PRIVATE).edit().clear().apply();
    }

    private void configurarBarraInferiorEmergencias() {
        findViewById(R.id.navPolicia).setOnClickListener(v -> marcarNumero("911", "Policía"));
        findViewById(R.id.navCruzRoja).setOnClickListener(v -> marcarNumero("065", "Cruz Roja"));
        findViewById(R.id.navBomberos).setOnClickListener(v -> marcarNumero("911", "Bomberos"));
        findViewById(R.id.navProteccionCivil).setOnClickListener(v -> marcarNumero("911", "Protección Civil"));
        findViewById(R.id.navTransito).setOnClickListener(v -> marcarNumero("911", "Tránsito"));
        findViewById(R.id.navContactoEmergencia).setOnClickListener(v ->
                marcarNumero(sesion.getTelefonoEmergencia(), sesion.getNombreContactoEmergencia()));
    }

    private void marcarNumero(String telefono, String servicio) {
        Toast.makeText(this, "Llamando a " + servicio, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono)));
    }

    private void configurarNavegacionMenu() {
        TextView menuSolicitudes = findViewById(R.id.menuSolicitudes);
        TextView menuContactos = findViewById(R.id.menuContactos);
        TextView menuHistorial = findViewById(R.id.menuHistorial);
        TextView menuPerfil = findViewById(R.id.menuPerfil);
        TextView menuCerrarSesion = findViewById(R.id.menuCerrarSesion);

        menuSolicitudes.setBackgroundColor(Color.parseColor("#E8B938"));
        menuContactos.setBackgroundColor(Color.TRANSPARENT);
        menuHistorial.setBackgroundColor(Color.TRANSPARENT);
        menuPerfil.setBackgroundColor(Color.TRANSPARENT);

        menuSolicitudes.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

        menuContactos.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, ContactosActivity.class));
            finish();
        });

        menuHistorial.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, HistorialActivity.class));
            finish();
        });

        menuPerfil.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, PerfilActivity.class));
            finish();
        });

        menuCerrarSesion.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }

    private void configurarBotonAyuda(ImageButton boton, int drawableId) {
        boton.setAlpha(0.6f);
        boton.setOnClickListener(v -> {
            if (ayudasSeleccionadas.contains(drawableId)) {
                ayudasSeleccionadas.remove(Integer.valueOf(drawableId));
                boton.setAlpha(0.6f);
            } else {
                ayudasSeleccionadas.add(drawableId);
                boton.setAlpha(1.0f);
            }
        });
    }

    private void limpiarFormulario() {
        etEmergencia.setText("");
        etLugar.setText("");
        etComentarios.setText("");
        ayudasSeleccionadas.clear();

        btnPolicia.setAlpha(0.6f);
        btnCruzRoja.setAlpha(0.6f);
        btnBomberos.setAlpha(0.6f);
        btnProteccion.setAlpha(0.6f);
        btnTransito.setAlpha(0.6f);
        btnContacto.setAlpha(0.6f);
    }
}