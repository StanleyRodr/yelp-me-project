package com.yelpme.app.ui.perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.Respuesta;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.databinding.ActivityPerfilBinding;
import com.yelpme.app.databinding.HojaFotoBinding;
import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.util.Imagenes;
import com.yelpme.app.util.MapaOsm;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Perfil con datos de la API: foto, datos, strikes, suspensiones y domicilio.
 * Recarga en onResume y al deslizar hacia abajo.
 */
public class PerfilActivity extends BaseActivity {

    private ActivityPerfilBinding vista;
    private ServicioPerfil api;
    private MapaOsm mapa;
    private Llamada<Usuario> llamadaUsuario;
    private Llamada<Domicilio> llamadaDomicilio;
    private Llamada<Usuario> llamadaFoto;
    /** Respuestas pendientes de la recarga en curso (sesión + domicilio). */
    private int pendientes;
    private final ExecutorService fondo = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> editarDatos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) {
                    aviso(R.string.perfil_datos_guardados).show();
                }
            });

    /** Photo Picker: no requiere permiso de almacenamiento. */
    private final ActivityResultLauncher<PickVisualMediaRequest> elegirFoto = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    subirFoto(uri);
                }
            });

    @Override
    protected int itemMenu() {
        return R.id.menu_perfil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityPerfilBinding.inflate(getLayoutInflater());
        ponerContenido(vista);
        api = ClienteApi.servicio(ServicioPerfil.class);

        vista.recargaPerfil.setColorSchemeResources(R.color.ym_marca_texto);
        vista.recargaPerfil.setProgressBackgroundColorSchemeResource(R.color.ym_superficie);
        vista.recargaPerfil.setOnRefreshListener(this::cargar);

        vista.btnEditarDatos.setOnClickListener(v -> editarDatos.launch(new Intent(this, EditarPerfilActivity.class)));
        vista.btnFoto.setOnClickListener(v -> opcionesFoto());

        mapa = new MapaOsm(this, vista.mapPerfilPrincipal, getLifecycle());
        mapa.soloLectura(true);

        // Datos guardados mientras llega la respuesta.
        Usuario guardado = sesion.usuario();
        if (guardado != null) {
            mostrar(guardado);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    protected void onDestroy() {
        cancelar();
        if (llamadaFoto != null) {
            llamadaFoto.cancelar();
        }
        fondo.shutdownNow();
        super.onDestroy();
    }

    private void cancelar() {
        if (llamadaUsuario != null) {
            llamadaUsuario.cancelar();
        }
        if (llamadaDomicilio != null) {
            llamadaDomicilio.cancelar();
        }
    }

    /** Sesión y domicilio en paralelo. */
    private void cargar() {
        cancelar();
        pendientes = 2;
        llamadaUsuario = Llamada.de(api.sesion()).enviar(new Llamada.Respuestas<Usuario>() {
            @Override
            public void alExito(Usuario usuario) {
                sesion.actualizarUsuario(usuario);
                mostrar(usuario);
                terminar();
            }

            @Override
            public void alError(ErrorApi error) {
                terminar();
                fallo(error);
            }
        });
        llamadaDomicilio = Llamada.de(api.domicilio()).enviar(new Llamada.Respuestas<Domicilio>() {
            @Override
            public void alExito(Domicilio domicilio) {
                mostrar(domicilio);
                terminar();
            }

            @Override
            public void alError(ErrorApi error) {
                terminar();
                fallo(error);
            }
        });
    }

    private void terminar() {
        pendientes--;
        if (pendientes <= 0) {
            vista.recargaPerfil.setRefreshing(false);
        }
    }

    private void fallo(ErrorApi error) {
        if (error.getCodigo() == 401) {
            return; // InterceptorSesion ya abrió el login.
        }
        aviso(R.string.perfil_error_cargar)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.comun_reintentar, v -> cargar())
                .show();
    }

    private void mostrar(Usuario u) {
        vista.tvPerfilNombreCorto.setText(u.nombre);
        vista.tvPerfilFolio.setText(getString(R.string.perfil_folio, u.id));
        vista.tvPerfilNombreCompleto.setText(u.nombreCompleto());
        vista.tvPerfilTelefono.setText(u.telefono);
        vista.tvPerfilCorreo.setText(u.correo);

        vista.tvAvatarInicial.setText(u.iniciales());
        if (u.fotoPerfil != null) {
            vista.ivFotoPerfil.setVisibility(View.VISIBLE);
            Glide.with(this).load(u.fotoPerfil).circleCrop().into(vista.ivFotoPerfil);
        } else {
            Glide.with(this).clear(vista.ivFotoPerfil);
            vista.ivFotoPerfil.setVisibility(View.GONE);
        }

        int limite = Math.max(u.limiteStrikes, 1);
        vista.tvPerfilStrikes.setText(getString(R.string.perfil_strikes, u.strikesActivos, limite));
        vista.progresoStrikes.setMax(limite);
        vista.progresoStrikes.setProgressCompat(Math.min(u.strikesActivos, limite), false);
        vista.tvAvisoSuspendido.setVisibility(u.suspendido ? View.VISIBLE : View.GONE);
        vista.tvPerfilSuspensiones.setText(u.vecesSuspendido == 0
                ? getString(R.string.perfil_nunca_suspendido)
                : getResources().getQuantityString(R.plurals.perfil_veces_suspendido, u.vecesSuspendido, u.vecesSuspendido));
    }

    // ---------- Foto ----------

    private void opcionesFoto() {
        Usuario u = sesion.usuario();
        BottomSheetDialog hoja = new BottomSheetDialog(this);
        HojaFotoBinding opciones = HojaFotoBinding.inflate(getLayoutInflater());
        opciones.btnQuitarFoto.setVisibility(u != null && u.fotoPerfil != null ? View.VISIBLE : View.GONE);
        opciones.btnGaleria.setOnClickListener(v -> {
            hoja.dismiss();
            elegirFoto.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        opciones.btnQuitarFoto.setOnClickListener(v -> {
            hoja.dismiss();
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.perfil_foto_quitar_pregunta)
                    .setNegativeButton(R.string.comun_cancelar, null)
                    .setPositiveButton(R.string.perfil_foto_quitar, (d, i) -> enviarFoto(ClienteApi.servicio(ServicioPerfil.class).quitarFoto()))
                    .show();
        });
        hoja.setContentView(opciones.getRoot());
        hoja.show();
    }

    /** Comprime en segundo plano y sube como multipart `foto`. */
    private void subirFoto(Uri uri) {
        ocupadoFoto(true);
        fondo.execute(() -> {
            File archivo;
            try {
                archivo = Imagenes.comprimir(getApplicationContext(), uri);
            } catch (IOException | RuntimeException e) {
                principal.post(() -> {
                    if (!isDestroyed()) {
                        ocupadoFoto(false);
                        aviso(R.string.perfil_foto_no_leida).show();
                    }
                });
                return;
            }
            MultipartBody.Part parte = MultipartBody.Part.createFormData("foto", "perfil.jpg",
                    RequestBody.create(archivo, MediaType.get("image/jpeg")));
            principal.post(() -> {
                if (!isDestroyed()) {
                    enviarFoto(api.subirFoto(parte));
                }
            });
        });
    }

    private void enviarFoto(Call<Respuesta<Usuario>> call) {
        ocupadoFoto(true);
        llamadaFoto = Llamada.de(call).enviar(new Llamada.Respuestas<Usuario>() {
            @Override
            public void alExito(Usuario usuario) {
            }

            @Override
            public void alExito(Usuario usuario, String mensaje) {
                ocupadoFoto(false);
                sesion.actualizarUsuario(usuario);
                mostrar(usuario);
                aviso(mensaje).show();
            }

            @Override
            public void alError(ErrorApi error) {
                ocupadoFoto(false);
                String mensaje = error.campo("foto");
                aviso(mensaje != null ? mensaje : error.getMessage()).show();
            }
        });
    }

    private void ocupadoFoto(boolean ocupado) {
        vista.progresoFoto.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        vista.btnFoto.setEnabled(!ocupado);
    }

    // ---------- Domicilio ----------

    private void mostrar(Domicilio d) {
        if (d == null) {
            vista.tvPerfilDomicilio.setText(R.string.perfil_sin_domicilio);
            vista.tvPerfilReferencia.setVisibility(View.GONE);
            vista.mapPerfilPrincipal.setVisibility(View.GONE);
            return;
        }
        vista.tvPerfilDomicilio.setText(d.texto);
        boolean conReferencia = d.referencia != null && !d.referencia.isEmpty();
        vista.tvPerfilReferencia.setVisibility(conReferencia ? View.VISIBLE : View.GONE);
        if (conReferencia) {
            vista.tvPerfilReferencia.setText(getString(R.string.perfil_referencia, d.referencia));
        }
        vista.mapPerfilPrincipal.setVisibility(View.VISIBLE);
        mapa.centrar(d.latitud, d.longitud, MapaOsm.ZOOM_CALLE);
        mapa.marcador(d.latitud, d.longitud, R.drawable.ic_place, d.texto);
    }
}
