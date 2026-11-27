# 052 — Tarea: BaseActivity y barra inferior

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Quitar el menú y la barra copiados en 4 activities: una `BaseActivity` con menú lateral, barra inferior e insets. Barra con servicios del catálogo (llamada simulada) y botón del favorito.

## Pasos

### 1. Layout común `layout_base.xml`
- `DrawerLayout` (`drawerBase`) → `CoordinatorLayout` con: header (`include layout_header`: título + botón menú 48dp con `cd_menu`), `FrameLayout contenido`, barra inferior (`include layout_barra_servicios`).
- Menú lateral: `NavigationView` con `res/menu/menu_lateral.xml` (Inicio, Contactos, Quién me tiene, Historial, Perfil, Cerrar sesión) e íconos Material Symbols. Abre por la derecha como el diseño (`layout_gravity="end"`).

### 2. `ui/base/BaseActivity extends AppCompatActivity`
```java
protected abstract int itemMenu();               // R.id.menu_inicio, …
protected void ponerContenido(ViewBinding vista) // infla dentro de contenido
```
- `EdgeToEdge.enable` + `ViewCompat.setOnApplyWindowInsetsListener`: padding superior al header, inferior a la barra.
- Resalta el ítem actual (`setCheckedItem`). Navegación con `startActivity` + `FLAG_ACTIVITY_REORDER_TO_FRONT` (no `finish()` en cadena).
- Atrás con drawer abierto → lo cierra (`OnBackPressedCallback`).
- Cerrar sesión → `CerrarSesion.ejecutar(this)`.
- `InicioActivity`, `ContactosActivity`, `HistorialActivity`, `PerfilActivity` heredan de ella; borrar `configurarNavegacionMenu()` y `configurarBarraInferiorEmergencias()` duplicados y los `getChildAt(i)`.

### 3. Barra de servicios (`ui/base/BarraServicios`)
- `RecyclerView` horizontal (`LinearLayoutManager.HORIZONTAL`) + adapter con ViewBinding (`item_servicio_barra.xml`: ícono 32dp + nombre corto, toque ≥ 48dp, `contentDescription` "Llamar a Policía (simulado)").
- Datos: `GET /api/servicios`, caché en memoria por sesión de app (`Catalogo.servicios()`), ícono con `IconosServicio.de(icono)`.
- Toque → `MaterialAlertDialogBuilder`: título "Llamada simulada", mensaje `R.string.barra_simulada` ("Llamada simulada a %1$s. YelpMe es un prototipo y no contacta servicios reales."). **No usa Intents de llamada.**
- Al final, botón fijo del favorito (`ic_star`, "Llamar a mi contacto favorito"): en este plan abre `ContactosActivity` con Snackbar "Elige un contacto favorito". El plan 010 cambia esto por la llamada.
- Sin conexión: barra oculta (no bloquea la pantalla).

### 4. Borrar del diseño viejo
`bottomMenuLayout` con PNG y números `911`/`065`; `menu_drawer.xml` de TextViews; resaltado `#E8B938`.

## Verificación
- Navegar por las 4 pantallas desde el menú; Atrás funciona; header y barra no quedan bajo las barras del sistema (probar API 35+).
- Tocar Policía → diálogo simulado; nunca se abre el marcador.
- TalkBack lee cada botón de la barra.

## Resultado
Commit: `refactor(mobile): unifica menú lateral y barra en BaseActivity`.

Notas: probado en emulador: saludo con el nombre, barra con los 6 servicios del catálogo (`Catalogo` en memoria), diálogo de llamada simulada, favorito → Contactos con aviso, menú lateral por la derecha con cabecera (nombre y correo), "Quién me tiene" avisa que llega después (plan 010), selector de tema (Claro/Oscuro/Sistema) y sin conexión la barra se oculta. "Ayuda de:" de Inicio muestra los servicios del catálogo como chips. Sin tinte de elevación en el tema (`elevationOverlayEnabled=false`) para que diálogos y menú usen `ym_superficie`.
