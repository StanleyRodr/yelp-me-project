package com.yelpme.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class PerfilActivity extends AppCompatActivity {

    static class Direccion {
        String etiqueta, calle, colonia, numero, cp, referencia;

        Direccion(String etiqueta, String calle, String colonia, String numero, String cp, String referencia) {
            this.etiqueta = etiqueta;
            this.calle = calle;
            this.colonia = colonia;
            this.numero = numero;
            this.cp = cp;
            this.referencia = referencia;
        }

        String getDireccionCompleta() {
            if (colonia.isEmpty() && numero.isEmpty() && cp.isEmpty()) {
                return calle;
            }
            return calle + ", " + colonia + ", " + numero + ", " + cp;
        }
    }

    private DrawerLayout drawerLayout;
    private SesionManager sesion;
    private LinearLayout layoutPrincipalPerfil, llContenedorDirecciones;
    private CardView cardFormularioDireccion;
    private TextView tvPerfilDireccionPrincipal, tvTituloFormDireccion;
    private EditText etBuscarDireccion;

    private EditText etEtiqueta, etCalle, etColonia, etNumero, etCP, etUbicacion;
    private Button btnGuardarDireccion;

    private final List<Direccion> listaDirecciones = new ArrayList<>();
    private Direccion direccionEnEdicion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sesion = new SesionManager(this);

        LinearLayout header = findViewById(R.id.headerLayout);
        ((TextView) header.getChildAt(0)).setText("¡Hola " + sesion.getNombre() + "!");

        ((TextView) findViewById(R.id.tvPerfilNombreCorto)).setText(sesion.getNombre());
        ((TextView) findViewById(R.id.tvPerfilNombreCompleto)).setText(sesion.getNombreCompleto());
        ((TextView) findViewById(R.id.tvPerfilCorreo)).setText(sesion.getCorreo());

        drawerLayout = findViewById(R.id.drawerLayoutPerfil);
        findViewById(R.id.btnMenuHamburguesa).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        configurarNavegacionMenu();
        configurarBarraInferiorEmergencias();

        layoutPrincipalPerfil = findViewById(R.id.layoutVistaPrincipalPerfil);
        cardFormularioDireccion = findViewById(R.id.cardFormularioDireccion);
        llContenedorDirecciones = findViewById(R.id.llContenedorDirecciones);
        tvPerfilDireccionPrincipal = findViewById(R.id.tvPerfilDireccionPrincipal);
        etBuscarDireccion = findViewById(R.id.etBuscarDireccion);

        // Cargar mapas en Perfil
        MapaHelper.cargarMapaEnContenedor(this, findViewById(R.id.mapPerfilPrincipal), MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Mi ubicación");
        MapaHelper.cargarMapaEnContenedor(this, findViewById(R.id.mapPerfilForm), MapaHelper.LAT_DEFAULT, MapaHelper.LON_DEFAULT, "Nueva dirección");

        tvPerfilDireccionPrincipal.setText(sesion.getDireccion());

        tvTituloFormDireccion = findViewById(R.id.tvTituloFormDireccion);
        etEtiqueta = findViewById(R.id.etDirEtiqueta);
        etCalle = findViewById(R.id.etDirCalle);
        etColonia = findViewById(R.id.etDirColonia);
        etNumero = findViewById(R.id.etDirNumero);
        etCP = findViewById(R.id.etDirCP);
        etUbicacion = findViewById(R.id.etDirUbicacion);
        btnGuardarDireccion = findViewById(R.id.btnGuardarDireccion);

        listaDirecciones.add(new Direccion("Dirección predeterminada", sesion.getDireccion(), "", "", "", ""));
        listaDirecciones.add(new Direccion("Casa", "Av. Reforma", "Centro", "120", "93820", "Portón blanco"));
        listaDirecciones.add(new Direccion("Escuela", "Km 1.5 Carretera a Loma del Cojolite", "ITSM", "sn", "93821", "Campus principal"));

        renderizarDirecciones("");

        etBuscarDireccion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderizarDirecciones(s.toString());
            }
        });

        findViewById(R.id.btnAgregarDireccion).setOnClickListener(v -> abrirFormularioDireccion(null));
        findViewById(R.id.btnCambiarDireccionPredeterminada).setOnClickListener(v -> mostrarModalDireccionPredeterminada());

        findViewById(R.id.btnCancelarDireccion).setOnClickListener(v -> {
            cardFormularioDireccion.setVisibility(View.GONE);
            layoutPrincipalPerfil.setVisibility(View.VISIBLE);
        });

        btnGuardarDireccion.setOnClickListener(v -> guardarOActualizarDireccion());
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

    private void renderizarDirecciones(String filtro) {
        llContenedorDirecciones.removeAllViews();

        for (Direccion dir : listaDirecciones) {
            if (!dir.etiqueta.toLowerCase().contains(filtro.toLowerCase())) continue;

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(Gravity.CENTER_VERTICAL);
            fila.setPadding(4, 16, 4, 16);

            TextView tvNombre = new TextView(this);
            tvNombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvNombre.setText(dir.etiqueta);
            tvNombre.setTextColor(Color.parseColor("#1E1E2C"));
            tvNombre.setTextSize(14f);

            ImageButton btnEditar = new ImageButton(this);
            LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(78, 78);
            paramsBtn.setMarginEnd(14);
            btnEditar.setLayoutParams(paramsBtn);
            btnEditar.setBackgroundResource(R.drawable.bg_circulo_avatar);
            btnEditar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnEditar.setImageResource(android.R.drawable.ic_menu_edit);
            btnEditar.setColorFilter(Color.WHITE);
            btnEditar.setPadding(16, 16, 16, 16);
            btnEditar.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            btnEditar.setOnClickListener(v -> abrirFormularioDireccion(dir));

            ImageButton btnEliminar = new ImageButton(this);
            btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(78, 78));
            btnEliminar.setBackgroundResource(R.drawable.bg_circulo_avatar);
            btnEliminar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")));
            btnEliminar.setImageResource(android.R.drawable.ic_menu_delete);
            btnEliminar.setColorFilter(Color.WHITE);
            btnEliminar.setPadding(16, 16, 16, 16);
            btnEliminar.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            btnEliminar.setOnClickListener(v -> {
                listaDirecciones.remove(dir);
                renderizarDirecciones("");
                Toast.makeText(this, "Dirección eliminada", Toast.LENGTH_SHORT).show();
            });

            fila.addView(tvNombre);
            fila.addView(btnEditar);
            fila.addView(btnEliminar);

            View divisor = new View(this);
            divisor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divisor.setBackgroundColor(Color.parseColor("#F5C36A"));

            llContenedorDirecciones.addView(fila);
            llContenedorDirecciones.addView(divisor);
        }
    }

    private void abrirFormularioDireccion(Direccion dir) {
        direccionEnEdicion = dir;

        if (dir == null) {
            tvTituloFormDireccion.setText("Nueva Dirección");
            btnGuardarDireccion.setText("Guardar");
            etEtiqueta.setText("");
            etCalle.setText("");
            etColonia.setText("");
            etNumero.setText("");
            etCP.setText("");
            etUbicacion.setText("");
        } else {
            tvTituloFormDireccion.setText("Actualizar Dirección");
            btnGuardarDireccion.setText("Actualizar");
            etEtiqueta.setText(dir.etiqueta);
            etCalle.setText(dir.calle);
            etColonia.setText(dir.colonia);
            etNumero.setText(dir.numero);
            etCP.setText(dir.cp);
            etUbicacion.setText(dir.referencia);
        }

        layoutPrincipalPerfil.setVisibility(View.GONE);
        cardFormularioDireccion.setVisibility(View.VISIBLE);
    }

    private void guardarOActualizarDireccion() {
        String etiqueta = etEtiqueta.getText().toString().trim();
        String calle = etCalle.getText().toString().trim();

        if (etiqueta.isEmpty() || calle.isEmpty()) {
            Toast.makeText(this, "Ingresa al menos nombre y calle", Toast.LENGTH_SHORT).show();
            return;
        }

        if (direccionEnEdicion == null) {
            listaDirecciones.add(new Direccion(
                    etiqueta,
                    calle,
                    etColonia.getText().toString().trim(),
                    etNumero.getText().toString().trim(),
                    etCP.getText().toString().trim(),
                    etUbicacion.getText().toString().trim()
            ));
            Toast.makeText(this, "Dirección agregada", Toast.LENGTH_SHORT).show();
        } else {
            direccionEnEdicion.etiqueta = etiqueta;
            direccionEnEdicion.calle = calle;
            direccionEnEdicion.colonia = etColonia.getText().toString().trim();
            direccionEnEdicion.numero = etNumero.getText().toString().trim();
            direccionEnEdicion.cp = etCP.getText().toString().trim();
            direccionEnEdicion.referencia = etUbicacion.getText().toString().trim();
            Toast.makeText(this, "Dirección actualizada", Toast.LENGTH_SHORT).show();
        }

        renderizarDirecciones("");
        cardFormularioDireccion.setVisibility(View.GONE);
        layoutPrincipalPerfil.setVisibility(View.VISIBLE);
    }

    private void mostrarModalDireccionPredeterminada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_direccion_predeterminada, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ListView lvModal = view.findViewById(R.id.lvDireccionesModal);
        List<String> etiquetas = new ArrayList<>();
        for (Direccion d : listaDirecciones) etiquetas.add(d.etiqueta);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, etiquetas);
        lvModal.setAdapter(adapter);

        final int[] posicionElegida = {-1};
        lvModal.setOnItemClickListener((parent, v, position, id) -> posicionElegida[0] = position);

        view.findViewById(R.id.btnCerrarModalDireccion).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmarDireccionModal).setOnClickListener(v -> {
            if (posicionElegida[0] != -1) {
                Direccion elegida = listaDirecciones.get(posicionElegida[0]);
                String nuevaDir = elegida.getDireccionCompleta();
                tvPerfilDireccionPrincipal.setText(nuevaDir);
                sesion.guardarUsuario(sesion.getNombre(), "", "", sesion.getCorreo(), nuevaDir);
                Toast.makeText(this, "Dirección principal actualizada a: " + elegida.etiqueta, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void configurarNavegacionMenu() {
        TextView menuSolicitudes = findViewById(R.id.menuSolicitudes);
        TextView menuContactos = findViewById(R.id.menuContactos);
        TextView menuHistorial = findViewById(R.id.menuHistorial);
        TextView menuPerfil = findViewById(R.id.menuPerfil);
        TextView menuCerrarSesion = findViewById(R.id.menuCerrarSesion);

        menuSolicitudes.setBackgroundColor(Color.TRANSPARENT);
        menuContactos.setBackgroundColor(Color.TRANSPARENT);
        menuHistorial.setBackgroundColor(Color.TRANSPARENT);
        menuPerfil.setBackgroundColor(Color.parseColor("#E8B938"));

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

        menuHistorial.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, HistorialActivity.class));
            finish();
        });

        menuPerfil.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));

        menuCerrarSesion.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }
}