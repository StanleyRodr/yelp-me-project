package com.yelpme.app.ui.inicio;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.databinding.ActivityInicioBinding;

/** Pantalla principal (crear solicitud). El formulario se conecta en el plan 011. */
public class InicioActivity extends AppCompatActivity {

    /** Extra: se entró sin poder confirmar la sesión (sin conexión). */
    public static final String EXTRA_SIN_CONEXION = "sin_conexion";
    /** Extra: contactos vinculados al registrarse. */
    public static final String EXTRA_VINCULADOS = "vinculados";

    private ActivityInicioBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        if (savedInstanceState == null) {
            avisosDeEntrada();
        }
    }

    private void avisosDeEntrada() {
        int vinculados = getIntent().getIntExtra(EXTRA_VINCULADOS, 0);
        if (getIntent().getBooleanExtra(EXTRA_SIN_CONEXION, false)) {
            Snackbar.make(vista.getRoot(), R.string.sesion_sin_conexion, Snackbar.LENGTH_LONG).show();
        } else if (vinculados > 0) {
            Snackbar.make(vista.getRoot(),
                    getResources().getQuantityString(R.plurals.registro_vinculados, vinculados, vinculados),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
