package com.yelpme.app.ui.contactos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Favorito;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioContactos;
import com.yelpme.app.databinding.ActivityDetalleContactoBinding;
import com.yelpme.app.modelos.Contacto;
import com.yelpme.app.util.Llamadas;
import com.yelpme.app.util.Margenes;

import java.util.Locale;

/**
 * Detalle de un contacto: llamar, marcar o quitar favorito, editar y eliminar
 * (DELETE del CRUD). Si el contacto ya no existe (404) avisa y cierra.
 * Devuelve {@link FormContactoActivity#EXTRA_MENSAJE} al eliminar.
 */
public class DetalleContactoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private ActivityDetalleContactoBinding vista;
    private ServicioContactos api;
    private Llamadas llamadas;
    private long id;
    private Contacto contacto;
    private Llamada<?> llamada;

    private final ActivityResultLauncher<Intent> editar = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Intent datos = resultado.getData();
                if (resultado.getResultCode() == RESULT_OK && datos != null) {
                    Favorito.invalidar();
                    aviso(datos.getStringExtra(FormContactoActivity.EXTRA_MENSAJE));
                }
            });

    public static Intent intent(Context contexto, long id) {
        return new Intent(contexto, DetalleContactoActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityDetalleContactoBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);
        api = ClienteApi.servicio(ServicioContactos.class);
        llamadas = new Llamadas(this, texto -> aviso(getString(texto)));
        id = getIntent().getLongExtra(EXTRA_ID, 0);

        vista.barra.tvTitulo.setText(R.string.contactos_detalle_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.btnLlamar.setOnClickListener(v -> {
            if (contacto != null) {
                llamadas.llamar(contacto.telefono);
            }
        });
        vista.btnFavorito.setOnClickListener(v -> alternarFavorito());
        vista.btnEditar.setOnClickListener(v -> editar.launch(FormContactoActivity.editar(this, id)));
        vista.btnEliminar.setOnClickListener(v -> {
            if (contacto != null) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.contactos_eliminar_pregunta, contacto.nombre))
                        .setNegativeButton(R.string.comun_cancelar, null)
                        .setPositiveButton(R.string.contactos_eliminar, (d, i) -> eliminar())
                        .show();
            }
        });
        habilitar(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }

    private void cargar() {
        llamada = Llamada.de(api.obtener(id)).enviar(new Llamada.Respuestas<Contacto>() {
            @Override
            public void alExito(Contacto c) {
                mostrar(c);
            }

            @Override
            public void alError(ErrorApi error) {
                manejarError(error, true);
            }
        });
    }

    private void mostrar(Contacto c) {
        contacto = c;
        habilitar(true);
        vista.tvInicial.setText(c.nombre == null || c.nombre.isEmpty()
                ? "" : new String(Character.toChars(c.nombre.codePointAt(0))).toUpperCase(Locale.ROOT));
        vista.tvNombre.setText(c.nombre);
        vista.tvTelefono.setText(c.telefono);
        vista.tvParentesco.setText(c.parentesco);
        vista.tvVinculo.setText(c.vinculado ? R.string.contactos_vinculado : R.string.contactos_sin_cuenta);
        vista.tvVinculo.setCompoundDrawablesRelativeWithIntrinsicBounds(c.vinculado ? R.drawable.ic_link : 0, 0, 0, 0);
        vista.btnFavorito.setText(c.favorito ? R.string.contactos_quitar_favorito : R.string.contactos_marcar_favorito);
        vista.btnFavorito.setIconResource(c.favorito ? R.drawable.ic_star : R.drawable.ic_star_border);
    }

    private void habilitar(boolean habilitado) {
        vista.btnLlamar.setEnabled(habilitado);
        vista.btnFavorito.setEnabled(habilitado);
        vista.btnEditar.setEnabled(habilitado);
        vista.btnEliminar.setEnabled(habilitado);
    }

    private void alternarFavorito() {
        if (contacto == null) {
            return;
        }
        boolean marcar = !contacto.favorito;
        habilitar(false);
        llamada = Llamada.de(marcar ? api.marcarFavorito(id) : api.quitarFavorito(id)).enviar(new Llamada.Respuestas<Contacto>() {
            @Override
            public void alExito(Contacto c) {
                Favorito.invalidar();
                mostrar(c);
                aviso(getString(marcar ? R.string.contactos_es_favorito : R.string.contactos_ya_no_favorito, c.nombre));
            }

            @Override
            public void alError(ErrorApi error) {
                habilitar(true);
                manejarError(error, false);
            }
        });
    }

    private void eliminar() {
        habilitar(false);
        llamada = Llamada.de(api.eliminar(id)).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void nada) {
                Favorito.invalidar();
                setResult(RESULT_OK, new Intent().putExtra(FormContactoActivity.EXTRA_MENSAJE, getString(R.string.contactos_eliminado)));
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                habilitar(true);
                manejarError(error, false);
            }
        });
    }

    /** 404: el contacto ya no existe (se borró en otro lado); se avisa a la lista y se cierra. */
    private void manejarError(ErrorApi error, boolean alCargar) {
        if (error.getCodigo() == 401) {
            return;
        }
        if (error.getCodigo() == 404) {
            Favorito.invalidar();
            setResult(RESULT_OK, new Intent().putExtra(FormContactoActivity.EXTRA_MENSAJE, getString(R.string.contactos_ya_no_existe)));
            finish();
            return;
        }
        Snackbar s = Snackbar.make(vista.raiz, error.getMessage(), alCargar ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
        if (alCargar) {
            s.setAction(R.string.comun_reintentar, v -> cargar());
        }
        s.show();
    }

    private void aviso(String texto) {
        if (texto != null) {
            Snackbar.make(vista.raiz, texto, Snackbar.LENGTH_LONG).show();
        }
    }
}
