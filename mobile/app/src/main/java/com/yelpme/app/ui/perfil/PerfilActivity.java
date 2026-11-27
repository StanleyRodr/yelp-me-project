package com.yelpme.app.ui.perfil;

import android.os.Bundle;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ActivityPerfilBinding;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.base.BaseActivity;

/** Perfil con los datos de la sesión. Edición, foto y domicilio en el plan 009. */
public class PerfilActivity extends BaseActivity {

    private ActivityPerfilBinding vista;

    @Override
    protected int itemMenu() {
        return R.id.menu_perfil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityPerfilBinding.inflate(getLayoutInflater());
        ponerContenido(vista);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Usuario u = sesion.usuario();
        if (u == null) {
            return;
        }
        vista.tvPerfilNombreCorto.setText(u.nombre);
        vista.tvPerfilNombreCompleto.setText(u.nombreCompleto());
        vista.tvPerfilTelefono.setText(u.telefono);
        vista.tvPerfilCorreo.setText(u.correo);
    }
}
