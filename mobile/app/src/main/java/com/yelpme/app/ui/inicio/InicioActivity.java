package com.yelpme.app.ui.inicio;

import android.os.Bundle;

import com.google.android.material.chip.Chip;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.databinding.ActivityInicioBinding;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.util.IconosServicio;

import java.util.List;

/** Pantalla principal (crear solicitud). El envío se conecta en el plan 011. */
public class InicioActivity extends BaseActivity {

    /** Extra: se entró sin poder confirmar la sesión (sin conexión). */
    public static final String EXTRA_SIN_CONEXION = "sin_conexion";
    /** Extra: contactos vinculados al registrarse. */
    public static final String EXTRA_VINCULADOS = "vinculados";

    private ActivityInicioBinding vista;

    @Override
    protected int itemMenu() {
        return R.id.menu_inicio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityInicioBinding.inflate(getLayoutInflater());
        ponerContenido(vista);

        mostrarServicios();
        vista.btnLimpiar.setOnClickListener(v -> limpiar());
        vista.btnEnviarSolicitud.setOnClickListener(v -> aviso(R.string.inicio_envio_pendiente).show());

        if (savedInstanceState == null) {
            avisosDeEntrada();
        }
    }

    /** "Ayuda de:": un chip por servicio del catálogo. */
    private void mostrarServicios() {
        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                vista.grupoServicios.removeAllViews();
                for (Servicio s : servicios) {
                    Chip chip = new Chip(InicioActivity.this);
                    chip.setText(s.nombre);
                    chip.setCheckable(true);
                    chip.setChipIconResource(IconosServicio.de(s.icono));
                    chip.setChipIconVisible(true);
                    chip.setTag(s.id);
                    vista.grupoServicios.addView(chip);
                }
            }

            @Override
            public void alError(ErrorApi error) {
                // Sin catálogo: la solicitud puede ir solo a contactos (plan 011).
            }
        });
    }

    private void limpiar() {
        vista.etEmergencia.setText(null);
        vista.etLugar.setText(null);
        vista.etComentarios.setText(null);
        vista.grupoServicios.clearCheck();
    }

    private void avisosDeEntrada() {
        int vinculados = getIntent().getIntExtra(EXTRA_VINCULADOS, 0);
        if (getIntent().getBooleanExtra(EXTRA_SIN_CONEXION, false)) {
            aviso(R.string.sesion_sin_conexion).show();
        } else if (vinculados > 0) {
            aviso(getResources().getQuantityString(R.plurals.registro_vinculados, vinculados, vinculados)).show();
        }
    }
}
