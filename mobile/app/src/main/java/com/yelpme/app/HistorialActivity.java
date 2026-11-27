package com.yelpme.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    static class RegistroHistorial {
        String folio, fecha, estado, emergencia, lugar, comentarios;
        int[] iconosAyuda;

        RegistroHistorial(String folio, String fecha, String estado, String emergencia, String lugar, String comentarios, int[] iconosAyuda) {
            this.folio = folio;
            this.fecha = fecha;
            this.estado = estado;
            this.emergencia = emergencia;
            this.lugar = lugar;
            this.comentarios = comentarios;
            this.iconosAyuda = iconosAyuda;
        }
    }

    private DrawerLayout drawerLayout;
    private SesionManager sesion;
    private CardView cardListaHistorial, cardDetalleHistorial;
    private LinearLayout llContenedorHistorial;
    private Spinner spinnerFiltro;

    private final List<RegistroHistorial> listaRegistros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        sesion = new SesionManager(this);

        LinearLayout header = findViewById(R.id.headerLayout);
        ((TextView) header.getChildAt(0)).setText("¡Hola " + sesion.getNombre() + "!");

        drawerLayout = findViewById(R.id.drawerLayoutHistorial);
        ImageButton btnMenu = findViewById(R.id.btnMenuHamburguesa);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        configurarNavegacionMenu();
        configurarBarraInferiorEmergencias();

        cardListaHistorial = findViewById(R.id.cardListaHistorial);
        cardDetalleHistorial = findViewById(R.id.cardDetalleHistorial);
        llContenedorHistorial = findViewById(R.id.llContenedorHistorial);
        spinnerFiltro = findViewById(R.id.spinnerFiltroHistorial);

        // Cargar mapa en el detalle del historial
        MapaHelper.cargarMapaEnContenedor(this, findViewById(R.id.mapHistorialDetalle), MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Lugar del incidente");

        listaRegistros.add(new RegistroHistorial("Solicitud #00001", "12/09/2026", "ATENDIDA", "Falla eléctrica", "Col. Centro #14", "Cables caídos cerca de poste", new int[]{android.R.drawable.ic_menu_compass}));
        listaRegistros.add(new RegistroHistorial("Solicitud #00002", "15/09/2026", "ATENDIDA", "Accidente vial", "Av. Principal", "Choque leve sin heridos", new int[]{android.R.drawable.ic_menu_directions, android.R.drawable.ic_input_add}));
        listaRegistros.add(new RegistroHistorial("Solicitud #00003", "18/09/2026", "CANCELADA", "Fuga de agua", "Calle Norte #8", "Falsa alarma", new int[]{android.R.drawable.ic_menu_compass}));
        listaRegistros.add(new RegistroHistorial("Solicitud #00004", "20/09/2026", "ATENDIDA", "Conato de incendio", "Mercado Municipal", "Humo en local comercial", new int[]{android.R.drawable.ic_dialog_info, android.R.drawable.ic_menu_compass}));
        listaRegistros.add(new RegistroHistorial("Solicitud #00005", "21/09/2026", "EN PROCESO", "Emergencia médica", "Parque Central", "Persona desmayada", new int[]{android.R.drawable.ic_input_add}));

        String[] opcionesFiltro = {"Todas", "ATENDIDA", "EN PROCESO", "CANCELADA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcionesFiltro);
        spinnerFiltro.setAdapter(adapter);

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderizarHistorial(opcionesFiltro[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btnVolverHistorial).setOnClickListener(v -> {
            cardDetalleHistorial.setVisibility(View.GONE);
            cardListaHistorial.setVisibility(View.VISIBLE);
        });
    }

    private void configurarBarraInferiorEmergencias() {
        LinearLayout bottomBar = findViewById(R.id.bottomMenuLayout);
        String[] telefonos = {"911", "065", "911", "911", "911"};
        String[] servicios = {"Policía", "Cruz Roja", "Bomberos", "Protección Civil", "Tránsito"};

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            bottomBar.getChildAt(i).setOnClickListener(v -> {
                Toast.makeText(this, "Marcando a " + servicios[idx], Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefonos[idx])));
            });
        }

        bottomBar.getChildAt(5).setOnClickListener(v -> {
            String tel = sesion.getTelefonoEmergencia();
            Toast.makeText(this, "Marcando a " + sesion.getNombreContactoEmergencia(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel)));
        });
    }

    private void renderizarHistorial(String filtroEstado) {
        llContenedorHistorial.removeAllViews();

        for (RegistroHistorial reg : listaRegistros) {
            if (!filtroEstado.equals("Todas") && !reg.estado.equals(filtroEstado)) {
                continue;
            }

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(8, 28, 8, 28);
            fila.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvFolio = new TextView(this);
            LinearLayout.LayoutParams paramsFolio = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvFolio.setLayoutParams(paramsFolio);
            tvFolio.setText(reg.folio);
            tvFolio.setTextColor(Color.parseColor("#1E1E2C"));
            tvFolio.setTextSize(14f);

            TextView tvFecha = new TextView(this);
            tvFecha.setText(reg.fecha);
            tvFecha.setTextColor(Color.parseColor("#4A4A4A"));
            tvFecha.setTextSize(13f);

            fila.addView(tvFolio);
            fila.addView(tvFecha);

            fila.setOnClickListener(v -> abrirDetalleSolicitud(reg));

            View divisor = new View(this);
            LinearLayout.LayoutParams paramsDiv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            divisor.setLayoutParams(paramsDiv);
            divisor.setBackgroundColor(Color.parseColor("#F5C36A"));

            llContenedorHistorial.addView(fila);
            llContenedorHistorial.addView(divisor);
        }
    }

    private void abrirDetalleSolicitud(RegistroHistorial reg) {
        ((TextView) findViewById(R.id.tvTituloSolicitudHistorial)).setText(reg.folio.replace("#0000", "#000"));
        TextView tvEstado = findViewById(R.id.tvEstadoSolicitudHistorial);
        tvEstado.setText(reg.estado);

        if (reg.estado.equals("ATENDIDA")) {
            tvEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8BC34A")));
        } else if (reg.estado.equals("EN PROCESO")) {
            tvEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8F00")));
        } else {
            tvEstado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
        }

        ((TextView) findViewById(R.id.tvHistEmergencia)).setText(reg.emergencia);
        ((TextView) findViewById(R.id.tvHistLugar)).setText(reg.lugar);
        ((TextView) findViewById(R.id.tvHistComentarios)).setText(reg.comentarios);

        LinearLayout llAyudas = findViewById(R.id.llHistAyudas);
        llAyudas.removeAllViews();
        for (int iconRes : reg.iconosAyuda) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
            params.setMarginEnd(14);
            iv.setLayoutParams(params);
            iv.setImageResource(iconRes);
            llAyudas.addView(iv);
        }

        cardListaHistorial.setVisibility(View.GONE);
        cardDetalleHistorial.setVisibility(View.VISIBLE);
    }

    private void configurarNavegacionMenu() {
        TextView menuSolicitudes = findViewById(R.id.menuSolicitudes);
        TextView menuContactos = findViewById(R.id.menuContactos);
        TextView menuHistorial = findViewById(R.id.menuHistorial);
        TextView menuPerfil = findViewById(R.id.menuPerfil);
        TextView menuCerrarSesion = findViewById(R.id.menuCerrarSesion);

        menuSolicitudes.setBackgroundColor(Color.TRANSPARENT);
        menuContactos.setBackgroundColor(Color.TRANSPARENT);
        menuHistorial.setBackgroundColor(Color.parseColor("#E8B938"));
        menuPerfil.setBackgroundColor(Color.TRANSPARENT);

        menuSolicitudes.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, SolicitudesActivity.class));
            finish();
        });

        menuContactos.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, ContactosActivity.class));
            finish();
        });

        menuHistorial.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

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
}