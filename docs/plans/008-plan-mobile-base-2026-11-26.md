# 008 — Plan: Base mobile y ajustes transversales

> Fecha: 2026-11-26 · Rama: `feature/mobile-base` (desde `main`)
> Referencia: `docs/plans/principal-plan.md` (secciones 5, 5.1, 6.4, 10.4, 10.5) · planes 003 (autenticación) y 007 (panel)

## Objetivo

Dejar la app Android lista para crecer: el proyecto de la otra persona entra a git sin datos personales, se estandariza con la identidad de la web (paleta, tipografía, íconos, tema claro/oscuro), se conecta a la API con Retrofit y el usuario puede registrarse, iniciar sesión, recuperar su contraseña y cerrar sesión. También se hacen los cambios transversales que la app necesita desde el inicio: apellidos en dos columnas, recuperación de contraseña en la API y el panel, y modo oscuro en la web.

**No incluye:** perfil (plan 009), contactos (010), solicitudes (011), FCM (012), despliegue (013).

## Punto de partida
- `mobile/` sin trackear: prototipo visual (7 activities, 11 layouts), todo con `findViewById`, datos fijos en código, SharedPreferences y osmdroid. Sin ViewBinding, Retrofit, Glide ni GPS.
- Datos personales reales en `SesionManager.java`, `MainActivity.java` y `activity_perfil.xml`.
- Paleta naranja/amarillo/verde, fuentes descargables (Lilita One, Raleway), 20 PNG de 2000×2000 (duplicados), íconos de Bomberos y Tránsito cruzados.
- `targetSdk 37` fuerza edge-to-edge; casi ninguna pantalla aplica insets.
- API: `usuarios.apellidos` (un campo); sin recuperación de contraseña. Panel: solo tema claro.

## Decisiones
- **Primer commit = proyecto tal como llegó**, quitando solo datos personales y archivos de máquina (`.idea/deploymentTargetSelector.xml`, `.idea/deviceManager.xml`). Así el historial muestra qué se recibió.
- **Apellidos:** `apellido_paterno` (obligatorio) + `apellido_materno` (opcional) en BD, API, panel y app. Se cambia `script-bd.sql` y el seeder; la BD `YelpMe` se recrea (solo hay datos de demo).
- **Recuperación de contraseña:** reglas de la sección 5.1 del plan principal. Endpoints públicos con `throttle:acceso`.
- **Modo oscuro:** tokens oscuros en `tokens.css` (`[data-theme="dark"]`) y `values-night/colors.xml` con los mismos valores. Selector Claro / Oscuro / Sistema.
- **Android:**
  - Paquete `com.yelpme.app`, `minSdk 30`, Java 11. ViewBinding y BuildConfig activados.
  - `BuildConfig.API_URL`: debug `http://10.0.2.2:8000/api/`, release `https://yelpme-api.up.railway.app/api/` (se ajusta en el plan 013).
  - HTTP sin cifrar solo en debug (`src/debug/res/xml/network_security_config.xml`).
  - Activities renombradas con sufijo `Activity`. `BaseActivity` con menú lateral, barra inferior e insets.
  - Se borran: CRUD de varias direcciones, `dialog_direccion_predeterminada`, `dialog_contacto_emergencia` (el favorito se marca en el detalle del contacto, plan 010). `RecuperarContrasenaActivity` se conserva y se conecta.
  - Barra inferior: servicios del catálogo con llamada **simulada** + botón del favorito (en este plan lleva a Contactos; el plan 010 activa la llamada).
- Pruebas del backend siguen sobre `YelpMe_pruebas`. Android: `./gradlew assembleDebug` + recorrido manual en emulador contra `php artisan serve`.

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 042 | [Importar proyecto Android](../tasks/042-tarea-importar-android-2026-11-26.md) | Proyecto original a git sin datos personales ni archivos de máquina. | ✅ |
| 043 | [Apellidos en dos columnas](../tasks/043-tarea-apellidos-2026-11-26.md) | BD, seeder, API, pruebas y panel con paterno/materno. | ✅ |
| 044 | [Recuperar contraseña: API](../tasks/044-tarea-recuperar-api-2026-11-26.md) | Tabla de códigos, 3 endpoints, correo y pruebas. | ⬜ |
| 045 | [Recuperar contraseña: panel](../tasks/045-tarea-recuperar-panel-2026-11-26.md) | Vista de 3 pasos en el panel web. | ⬜ |
| 046 | [Modo oscuro en la web](../tasks/046-tarea-modo-oscuro-web-2026-11-26.md) | Tokens oscuros, selector de tema, Bootstrap, SweetAlert y mapa. | ⬜ |
| 047 | [Configuración Android](../tasks/047-tarea-configuracion-android-2026-11-26.md) | Gradle, dependencias, BuildConfig, manifest, red y renombres. | ⬜ |
| 048 | [Tema y recursos Android](../tasks/048-tarea-tema-android-2026-11-26.md) | colors claro/oscuro, Atkinson, dimens, strings, Material Symbols, limpieza de PNG. | ⬜ |
| 049 | [Cliente API Android](../tasks/049-tarea-cliente-api-android-2026-11-26.md) | Retrofit, token, errores uniformes, 401 global, sesión guardada. | ⬜ |
| 050 | [Login, registro y sesión](../tasks/050-tarea-login-registro-android-2026-11-26.md) | Entrada automática, login correo/teléfono, registro, cerrar sesión. | ⬜ |
| 051 | [Recuperar contraseña: app](../tasks/051-tarea-recuperar-android-2026-11-26.md) | Los 3 pasos del diseño conectados a la API. | ⬜ |
| 052 | [BaseActivity y barra inferior](../tasks/052-tarea-base-activity-2026-11-26.md) | Menú lateral, insets, barra con servicios simulados y botón del favorito. | ⬜ |
| 053 | [Verificación del plan 008](../tasks/053-tarea-verificacion-008-2026-11-26.md) | Pruebas, build, recorrido en emulador y documentación. | ⬜ |

---

## Criterios de terminado

- [ ] `mobile/` en git sin datos personales; `local.properties`, `build/` y archivos de máquina ignorados.
- [ ] Apellidos en dos columnas en BD, API, panel y app; `php artisan test` en verde.
- [ ] Recuperación de contraseña funciona en panel y app (código en `laravel.log`).
- [ ] Web y app con tema claro, oscuro y sistema; colores iguales a los tokens.
- [ ] Android: ViewBinding en todo, sin `findViewById`, sin textos ni colores hardcodeados en lo tocado.
- [ ] Registro, login (correo o teléfono), entrada automática y cierre de sesión contra la API.
- [ ] Barra inferior con servicios del catálogo y aviso simulado.
- [ ] `./gradlew assembleDebug` y `npm run build` sin errores.
- [ ] Commits por tarea, en español, sin atribución; PR a `main` al terminar.
