# 056 — Tarea: Mapa reutilizable Android

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
Reemplazar `MapaHelper` por un mapa con ciclo de vida correcto y un selector de punto reutilizable (domicilio ahora, solicitud en el plan 011).

## Pasos

### 1. `util/MapaOsm`
- Constructor `MapaOsm(Context, FrameLayout contenedor, Lifecycle lifecycle)`: crea `MapView`, lo agrega al contenedor y se registra como `DefaultLifecycleObserver` (`onResume` → `mapa.onResume()`, `onPause` → `onPause()`, `onDestroy` → `onDetach()`).
- `Configuration.getInstance()`: `setUserAgentValue(BuildConfig.APPLICATION_ID)`, preferencias con `context.getSharedPreferences("osmdroid", MODE_PRIVATE)` (no `PreferenceManager` deprecado).
- Métodos: `centrar(lat, lng, zoom)`, `marcador(lat, lng, @DrawableRes icono, String titulo)`, `soloLectura(boolean)` (desactiva gestos para vistas previas), `alTocar(Consumer<GeoPoint>)`.
- Dentro de `ScrollView`: `setOnTouchListener` que llama `getParent().requestDisallowInterceptTouchEvent(true)` en `ACTION_DOWN` y `false` en `ACTION_UP/CANCEL`.
- Modo oscuro: `mapa.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERTED_COLORS)` **no**; usar `ColorMatrix` de brillo 0.85 (igual que la web, tarea 046).
- Atribución "© OpenStreetMap" visible (`CopyrightOverlay`).

### 2. `ui/mapa/ElegirUbicacionActivity`
- Pantalla completa: mapa + pin fijo al centro (`ic_place` sobre el mapa) + botón "Usar esta ubicación" + botón "Mi ubicación" (en el plan 011 usa GPS; aquí opcional si hay permiso).
- Entrada: extras `lat`, `lng` (centro inicial; por defecto Misantla 19.9311, -96.8519), `titulo`.
- Salida: `setResult(RESULT_OK, intent con lat/lng)`. Se abre con `registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), …)`.

### 3. Borrar `MapaHelper` y los `mapContainer` que ya no se usan
Los layouts conservan un `FrameLayout` solo donde se muestre mapa (perfil, detalle de solicitud, historial).

## Verificación
- Abrir/cerrar `ElegirUbicacionActivity` 10 veces: sin `Leaked` en Logcat (StrictMode en debug: `detectActivityLeaks`).
- Mapa dentro de scroll se arrastra sin mover la pantalla.

## Resultado
Commit: `refactor(mobile): agrega mapa con ciclo de vida y selector de ubicación`.

Notas: `util/MapaOsm` (ciclo de vida, `centro()`, un marcador teñido con `ym_marca_texto`, filtro oscuro = brightness 0.85 × contrast 1.1 como la web, `CopyrightOverlay`). `ui/mapa/ElegirUbicacionActivity` con `intent(ctx, lat, lng, titulo)`; pin fijo al centro (`ic_place`), "Mi ubicación" pide permiso si falta y usa `getCurrentLocation`; conserva el punto al rotar. StrictMode (`detectActivityLeaks`) en debug desde `YelpMeApp`. Se borró `MapaHelper`; `mapContainer` de Inicio se queda para el plan 011. Ícono `ic_place` generado de la fuente (`place.fill`). Recorrido en emulador en la tarea 060 (la pantalla se abre desde Perfil).
