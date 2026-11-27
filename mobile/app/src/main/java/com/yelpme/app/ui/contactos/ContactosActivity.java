package com.yelpme.app.ui.contactos;

import android.content.Intent;
import android.os.Bundle;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ActivityContactosBinding;
import com.yelpme.app.ui.base.BaseActivity;

/** Contactos de confianza. El CRUD contra la API llega en el plan 010. */
public class ContactosActivity extends BaseActivity {

    @Override
    protected int itemMenu() {
        return R.id.menu_contactos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ponerContenido(ActivityContactosBinding.inflate(getLayoutInflater()));
        if (savedInstanceState == null) {
            avisoFavorito(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        avisoFavorito(intent);
    }

    /** Llegó desde el botón del favorito de la barra. */
    private void avisoFavorito(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_ELEGIR_FAVORITO, false)) {
            aviso(R.string.contactos_elige_favorito).show();
        }
    }

    @Override
    protected void favorito() {
        aviso(R.string.contactos_elige_favorito).show();
    }
}
