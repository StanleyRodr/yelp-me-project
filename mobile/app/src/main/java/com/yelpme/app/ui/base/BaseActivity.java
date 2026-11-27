package com.yelpme.app.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.YelpMeApp;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Favorito;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.databinding.LayoutBaseBinding;
import com.yelpme.app.databinding.LayoutMenuCabeceraBinding;
import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.contactos.ContactosActivity;
import com.yelpme.app.ui.vinculos.VinculosActivity;
import com.yelpme.app.ui.historial.HistorialActivity;
import com.yelpme.app.ui.inicio.InicioActivity;
import com.yelpme.app.ui.perfil.PerfilActivity;
import com.yelpme.app.util.CerrarSesion;
import com.yelpme.app.util.Margenes;
import com.yelpme.app.util.Sesion;
import com.yelpme.app.util.Llamadas;
import com.yelpme.app.util.Tema;

import java.util.List;

/**
 * Pantallas con sesión: header con saludo y botón de menú, menú lateral,
 * barra inferior de servicios e insets. Cada pantalla pone su contenido con
 * {@link #ponerContenido(ViewBinding)} e indica su ítem con {@link #itemMenu()}.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Extra para ContactosActivity: llegó desde el botón del favorito. */
    public static final String EXTRA_ELEGIR_FAVORITO = "elegir_favorito";

    protected LayoutBaseBinding base;
    protected Sesion sesion;
    private Llamadas llamadas;
    @Nullable
    private Contacto favorito;
    private Insets barrasSistema = Insets.NONE;

    /** R.id.menu_* de la pantalla, para resaltarlo en el menú. */
    @IdRes
    protected abstract int itemMenu();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Margenes.bordeABorde(this, true);
        base = LayoutBaseBinding.inflate(getLayoutInflater());
        super.setContentView(base.getRoot());
        sesion = ((YelpMeApp) getApplication()).getSesion();
        llamadas = new Llamadas(this, texto -> aviso(texto).show());

        configurarInsets();
        configurarMenu();
        configurarBarra();
        mostrarUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        base.navegacion.setCheckedItem(itemMenu());
        mostrarUsuario();
        actualizarFavorito();
    }

    /** Pone la vista de la pantalla dentro del área de contenido. */
    protected void ponerContenido(ViewBinding vista) {
        base.contenido.removeAllViews();
        base.contenido.addView(vista.getRoot());
    }

    /** Snackbar sobre la barra inferior. */
    protected Snackbar aviso(@StringRes int mensaje) {
        return aviso(getString(mensaje));
    }

    protected Snackbar aviso(CharSequence mensaje) {
        Snackbar s = Snackbar.make(base.coordinador, mensaje, Snackbar.LENGTH_LONG);
        if (base.barra.getRoot().getVisibility() == View.VISIBLE) {
            s.setAnchorView(base.barra.getRoot());
        }
        return s;
    }

    // ---------- Header e insets ----------

    private void mostrarUsuario() {
        Usuario u = sesion.usuario();
        base.header.tvSaludoUsuario.setText(u != null && u.nombre != null
                ? getString(R.string.inicio_saludo, u.nombre)
                : getString(R.string.inicio_saludo_simple));
        LayoutMenuCabeceraBinding cabecera = LayoutMenuCabeceraBinding.bind(base.navegacion.getHeaderView(0));
        cabecera.tvNombreMenu.setText(u != null ? u.nombreCompleto() : "");
        cabecera.tvCorreoMenu.setText(u != null ? u.correo : "");
    }

    private void configurarInsets() {
        int arriba = base.header.getRoot().getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(base.coordinador, (v, insets) -> {
            barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            View header = base.header.getRoot();
            header.setPadding(header.getPaddingLeft(), arriba + barrasSistema.top, header.getPaddingRight(), header.getPaddingBottom());
            ajustarAbajo();
            return insets;
        });
    }

    /** El inset inferior va a la barra; si está oculta, al contenido. */
    private void ajustarAbajo() {
        boolean barraVisible = base.barra.getRoot().getVisibility() == View.VISIBLE;
        View barra = base.barra.getRoot();
        barra.setPadding(barra.getPaddingLeft(), barra.getPaddingTop(), barra.getPaddingRight(), barraVisible ? barrasSistema.bottom : 0);
        base.contenido.setPadding(0, 0, 0, barraVisible ? 0 : barrasSistema.bottom);
    }

    // ---------- Menú lateral ----------

    private void configurarMenu() {
        base.header.btnMenu.setOnClickListener(v -> base.drawerBase.openDrawer(GravityCompat.END));
        mostrarItemTema();

        base.navegacion.setNavigationItemSelectedListener(item -> {
            base.drawerBase.closeDrawer(GravityCompat.END);
            int id = item.getItemId();
            if (id == itemMenu()) {
                return true;
            }
            if (id == R.id.menu_inicio) {
                ir(InicioActivity.class);
            } else if (id == R.id.menu_contactos) {
                ir(ContactosActivity.class);
            } else if (id == R.id.menu_historial) {
                ir(HistorialActivity.class);
            } else if (id == R.id.menu_perfil) {
                ir(PerfilActivity.class);
            } else if (id == R.id.menu_vinculos) {
                ir(VinculosActivity.class);
            } else if (id == R.id.menu_tema) {
                Tema.alternar(this);
                return false;
            } else if (id == R.id.menu_cerrar_sesion) {
                CerrarSesion.ejecutar(this);
                return false;
            }
            return true;
        });

        // Atrás con el menú abierto lo cierra.
        OnBackPressedCallback cerrarMenu = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                base.drawerBase.closeDrawer(GravityCompat.END);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, cerrarMenu);
        base.drawerBase.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                cerrarMenu.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                cerrarMenu.setEnabled(false);
            }
        });
    }

    /** Navegación entre pantallas sin apilar copias: la que ya existe pasa al frente. */
    protected void ir(Class<? extends Activity> destino) {
        startActivity(new Intent(this, destino).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    /** El item del menú muestra el tema al que cambia (se recrea al alternar). */
    private void mostrarItemTema() {
        boolean oscuro = Tema.esOscuro(this);
        MenuItem item = base.navegacion.getMenu().findItem(R.id.menu_tema);
        item.setTitle(oscuro ? R.string.menu_tema_claro : R.string.menu_tema_oscuro);
        item.setIcon(oscuro ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }

    // ---------- Barra de servicios ----------

    private void configurarBarra() {
        BarraServicios adapter = new BarraServicios(this::llamadaSimulada);
        base.barra.listaServicios.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        base.barra.listaServicios.setAdapter(adapter);

        base.barra.btnFavorito.setOnClickListener(v -> favorito());

        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                adapter.mostrar(servicios);
                base.barra.getRoot().setVisibility(View.VISIBLE);
                ajustarAbajo();
            }

            @Override
            public void alError(ErrorApi error) {
                // Sin catálogo (p. ej. sin conexión) la barra se oculta; no bloquea la pantalla.
                base.barra.getRoot().setVisibility(View.GONE);
                ajustarAbajo();
            }
        });
    }

    /** Prototipo: nunca abre el marcador; solo informa. */
    private void llamadaSimulada(Servicio servicio) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.barra_simulada_titulo)
                .setIcon(com.yelpme.app.util.IconosServicio.de(servicio.icono))
                .setMessage(getString(R.string.barra_simulada, servicio.nombre))
                .setPositiveButton(R.string.comun_aceptar, null)
                .show();
    }

    /** Con favorito: llamada directa (ACTION_CALL o, sin permiso, el marcador). Sin favorito: a Contactos a elegirlo. */
    protected void favorito() {
        if (favorito != null) {
            llamadas.llamar(favorito.telefono);
            return;
        }
        startActivity(new Intent(this, ContactosActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                .putExtra(EXTRA_ELEGIR_FAVORITO, true));
    }

    /** Pone el nombre del favorito en el botón de la barra (caché en {@link Favorito}). */
    protected void actualizarFavorito() {
        Favorito.obtener(sesion, contacto -> {
            if (isDestroyed()) {
                return;
            }
            favorito = contacto;
            if (contacto == null) {
                base.barra.tvFavorito.setText(R.string.barra_favorito);
                base.barra.btnFavorito.setContentDescription(getString(R.string.cd_favorito));
            } else {
                String nombre = contacto.nombre.trim();
                int espacio = nombre.indexOf(' ');
                base.barra.tvFavorito.setText(espacio > 0 ? nombre.substring(0, espacio) : nombre);
                base.barra.btnFavorito.setContentDescription(getString(R.string.cd_llamar_favorito, contacto.nombre));
            }
        });
    }
}
