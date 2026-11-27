package com.yelpme.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class ContactosActivity extends AppCompatActivity {

    static class Contacto {
        String apodo, nombreUsuario, id, telefono, direccion;

        Contacto(String apodo, String nombreUsuario, String id, String telefono, String direccion) {
            this.apodo = apodo;
            this.nombreUsuario = nombreUsuario;
            this.id = id;
            this.telefono = telefono;
            this.direccion = direccion;
        }
    }

    private DrawerLayout drawerLayout;
    private SesionManager sesion;
    private CardView cardLista, cardDetalle, cardNuevo;
    private LinearLayout llContenedorLista;
    private TextView tvContactoEmergenciaActual;
    private EditText etBuscar;

    private final List<Contacto> listaContactos = new ArrayList<>();
    private Contacto contactoSeleccionadoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        sesion = new SesionManager(this);

        LinearLayout header = findViewById(R.id.headerLayout);
        ((TextView) header.getChildAt(0)).setText("¡Hola " + sesion.getNombre() + "!");

        drawerLayout = findViewById(R.id.drawerLayoutContactos);
        findViewById(R.id.btnMenuHamburguesa).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        configurarNavegacionMenu();
        configurarBarraInferiorEmergencias();

        cardLista = findViewById(R.id.cardListaContactos);
        cardDetalle = findViewById(R.id.cardDetalleContacto);
        cardNuevo = findViewById(R.id.cardNuevoContacto);

        llContenedorLista = findViewById(R.id.llContenedorListaContactos);
        tvContactoEmergenciaActual = findViewById(R.id.tvContactoEmergenciaActual);
        etBuscar = findViewById(R.id.etBuscarContacto);

        // Cargar mapas en las vistas de Contacto
        MapaHelper.cargarMapaEnContenedor(this, findViewById(R.id.mapContactoDetalle), MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Ubicación del contacto");
        MapaHelper.cargarMapaEnContenedor(this, findViewById(R.id.mapContactoNuevo), MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Ubicación");

        tvContactoEmergenciaActual.setText(sesion.getNombreContactoEmergencia());

        listaContactos.add(new Contacto("Contacto 1", "Carlos López", "#001", "2351112233", "Calle Hidalgo #12"));
        listaContactos.add(new Contacto("Contacto 2", "María García", "#002", "2354445566", "Av. Juárez #45"));
        listaContactos.add(new Contacto("Contacto 3", "Juan Pérez", "#003", "2357778899", "Col. Centro #89"));

        renderizarListaContactos("");

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarListaContactos(s.toString());
            }
        });

        findViewById(R.id.btnAgregarContacto).setOnClickListener(v -> mostrarVista(cardNuevo));
        findViewById(R.id.btnCambiarEmergencia).setOnClickListener(v -> mostrarModalEmergencia());
        findViewById(R.id.btnVolverDeDetalle).setOnClickListener(v -> mostrarVista(cardLista));

        findViewById(R.id.btnLlamarContacto).setOnClickListener(v -> {
            if (contactoSeleccionadoActual != null) {
                marcarNumero(contactoSeleccionadoActual.telefono, contactoSeleccionadoActual.apodo);
            }
        });

        findViewById(R.id.btnEliminarContacto).setOnClickListener(v -> {
            if (contactoSeleccionadoActual != null) {
                listaContactos.remove(contactoSeleccionadoActual);
                renderizarListaContactos("");
                mostrarVista(cardLista);
                Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
            }
        });

        EditText etApodo = findViewById(R.id.etNuevoApodo);
        EditText etUsuario = findViewById(R.id.etNuevoNombreUsuario);
        EditText etId = findViewById(R.id.etNuevoId);
        EditText etTel = findViewById(R.id.etNuevoTelefono);
        EditText etDir = findViewById(R.id.etNuevaDireccion);

        findViewById(R.id.btnCancelarNuevoContacto).setOnClickListener(v -> mostrarVista(cardLista));

        findViewById(R.id.btnGuardarNuevoContacto).setOnClickListener(v -> {
            String apodo = etApodo.getText().toString().trim();
            String tel = etTel.getText().toString().trim();

            if (apodo.isEmpty() || tel.isEmpty()) {
                Toast.makeText(this, "Apodo y teléfono son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            listaContactos.add(new Contacto(
                    apodo,
                    etUsuario.getText().toString().trim(),
                    etId.getText().toString().trim(),
                    tel,
                    etDir.getText().toString().trim()
            ));

            etApodo.setText(""); etUsuario.setText(""); etId.setText(""); etTel.setText(""); etDir.setText("");
            renderizarListaContactos("");
            mostrarVista(cardLista);
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarVista(CardView vistaActiva) {
        cardLista.setVisibility(vistaActiva == cardLista ? View.VISIBLE : View.GONE);
        cardDetalle.setVisibility(vistaActiva == cardDetalle ? View.VISIBLE : View.GONE);
        cardNuevo.setVisibility(vistaActiva == cardNuevo ? View.VISIBLE : View.GONE);
    }

    private void renderizarListaContactos(String filtro) {
        llContenedorLista.removeAllViews();
        for (Contacto c : listaContactos) {
            if (!c.apodo.toLowerCase().contains(filtro.toLowerCase())) continue;

            TextView itemView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 115);
            params.setMargins(0, 0, 0, 18);
            itemView.setLayoutParams(params);
            itemView.setBackgroundResource(R.drawable.bg_item_contacto);
            itemView.setText(c.apodo);
            itemView.setTextColor(Color.parseColor("#1E1E2C"));
            itemView.setTextSize(15f);
            itemView.setGravity(android.view.Gravity.CENTER_VERTICAL);
            itemView.setPadding(36, 0, 36, 0);

            itemView.setOnClickListener(v -> abrirDetalleContacto(c));
            llContenedorLista.addView(itemView);
        }
    }

    private void abrirDetalleContacto(Contacto c) {
        contactoSeleccionadoActual = c;
        ((TextView) findViewById(R.id.tvAvatarInicial)).setText(c.apodo.substring(0, 1).toUpperCase());
        ((TextView) findViewById(R.id.tvDetalleApodo)).setText(c.apodo);
        ((TextView) findViewById(R.id.tvDetalleNombreUsuario)).setText(c.nombreUsuario);
        ((TextView) findViewById(R.id.tvDetalleId)).setText(c.id);
        ((TextView) findViewById(R.id.tvDetalleTelefono)).setText(c.telefono);
        ((TextView) findViewById(R.id.tvDetalleDireccionContacto)).setText("Dirección: " + c.direccion);
        mostrarVista(cardDetalle);
    }

    private void mostrarModalEmergencia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contacto_emergencia, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ListView lvModal = view.findViewById(R.id.lvContactosModal);
        List<String> nombres = new ArrayList<>();
        for (Contacto c : listaContactos) nombres.add(c.apodo);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, nombres);
        lvModal.setAdapter(adapter);

        final int[] indiceElegido = {-1};
        lvModal.setOnItemClickListener((parent, v, position, id) -> indiceElegido[0] = position);

        view.findViewById(R.id.btnCerrarModalEmergencia).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmarEmergencia).setOnClickListener(v -> {
            if (indiceElegido[0] != -1) {
                Contacto elegido = listaContactos.get(indiceElegido[0]);
                sesion.guardarContactoEmergencia(elegido.apodo, elegido.telefono);
                tvContactoEmergenciaActual.setText(elegido.apodo + " (" + elegido.telefono + ")");
                Toast.makeText(this, "Contacto de emergencia guardado", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void configurarBarraInferiorEmergencias() {
        LinearLayout bottomBar = findViewById(R.id.bottomMenuLayout);
        String[] telefonos = {"911", "065", "911", "911", "911"};
        String[] servicios = {"Policía", "Cruz Roja", "Bomberos", "Protección Civil", "Tránsito"};

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            bottomBar.getChildAt(i).setOnClickListener(v -> marcarNumero(telefonos[idx], servicios[idx]));
        }
        bottomBar.getChildAt(5).setOnClickListener(v ->
                marcarNumero(sesion.getTelefonoEmergencia(), sesion.getNombreContactoEmergencia()));
    }

    private void marcarNumero(String telefono, String servicio) {
        Toast.makeText(this, "Marcando a " + servicio + " (" + telefono + ")", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
        startActivity(intent);
    }

    private void configurarNavegacionMenu() {
        TextView menuSolicitudes = findViewById(R.id.menuSolicitudes);
        TextView menuContactos = findViewById(R.id.menuContactos);
        TextView menuHistorial = findViewById(R.id.menuHistorial);
        TextView menuPerfil = findViewById(R.id.menuPerfil);
        TextView menuCerrarSesion = findViewById(R.id.menuCerrarSesion);

        menuSolicitudes.setBackgroundColor(Color.TRANSPARENT);
        menuContactos.setBackgroundColor(Color.parseColor("#E8B938"));
        menuHistorial.setBackgroundColor(Color.TRANSPARENT);
        menuPerfil.setBackgroundColor(Color.TRANSPARENT);

        menuSolicitudes.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, SolicitudesActivity.class));
            finish();
        });

        menuContactos.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

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
}