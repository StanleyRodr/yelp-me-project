package com.yelpme.app.ui.historial;

import android.os.Bundle;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ActivityHistorialBinding;
import com.yelpme.app.ui.base.BaseActivity;

/** Historial de solicitudes. Se conecta en el plan 011. */
public class HistorialActivity extends BaseActivity {

    @Override
    protected int itemMenu() {
        return R.id.menu_historial;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ponerContenido(ActivityHistorialBinding.inflate(getLayoutInflater()));
    }
}
