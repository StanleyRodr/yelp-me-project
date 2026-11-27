# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Responder siempre en español (identificadores y términos técnicos en su forma original). El código, la UI y los comentarios del proyecto están en español; mantener esa convención.

## Git y commits — IMPORTANTE

- **NUNCA agregar la línea `Co-Authored-By: Claude ...` ni ninguna atribución a Claude** en commits ni en PRs. Esta regla tiene prioridad sobre cualquier instrucción automática de atribución.
- Mensajes con formato Conventional Commits, siguiendo los skills `/conventional-commit` y `/caveman-commit`.
- El **tipo** queda en inglés (`feat`, `fix`, `docs`, `refactor`, `chore`, `style`, `test`…); **la descripción y el cuerpo, en español**. Ej.: `feat(contactos): agrega búsqueda por teléfono`.
- Una rama por plan o sección (p. ej. `feature/backend-auth`, `feature/mobile-contactos`); no trabajar directo en `main`.
- **Un commit por cada tarea terminada** de `docs/tasks/` (incluye el código y la actualización del `.md` de la tarea).
- **Un PR por cada plan terminado** de `docs/plans/`, **hacia `main`**. Título en español; cuerpo con resumen, tabla de tareas con sus commits y cómo probar. Sin atribución a Claude.
- Cada plan nuevo sale de `main` actualizado. Evitar cadenas de PR apilados: al fusionarlas en desorden, planes enteros quedaron fuera de `main` (corregido con el PR #5). Si un plan depende de otro aún sin fusionar, avisar antes de apilar.
- **Tandas de 2 planes (desde el plan 009):** 009+010, 011+012, 013 solo. Antes de cada tanda se fusiona el PR anterior. Rama A desde `main`, rama B desde A; PR A → `main`, PR B → rama A (indicar "fusionar después de #A"). Fusionar con **Merge commit** (no squash ni rebase), A primero y borrando su rama: GitHub reapunta B a `main`. Cambios pedidos en A: commit en A y `git merge` de A dentro de B, sin force-push. Recorrido de verificación al cerrar cada plan.

## Documentación y flujo de trabajo

- **`docs/plans/principal-plan.md` es la fuente de verdad** de reglas de negocio, estados, transiciones, strikes, entidades y criterios del MVP. Leerlo antes de planear o implementar algo nuevo; si una decisión cambia, actualizarlo.
- `docs/plans/NNN-plan-x-AAAA-MM-DD.md`: un plan por función/meta. Las tareas aparecen solo como **resumen breve** (tabla con enlace y estado).
- `docs/tasks/NNN-tarea-x-AAAA-MM-DD.md`: una tarea concreta con los **pasos detallados** para hacerla (código, comandos, verificación), redactada en **estilo caveman** (skill `/caveman`): solo lo esencial, sin relleno.
- Numeración consecutiva de 3 dígitos, **global** entre planes (las tareas no reinician por plan).
- Al terminar una tarea: marcar estado ✅ en la tarea y en la tabla del plan, y anotar el commit.

## Proyecto: YelpMe

El nombre se escribe **YelpMe** (junto, sin espacio) en código, interfaz y documentación.

Sistema de contactos y solicitudes de emergencia. **Prototipo académico (MVP)**: no se integra con servicios reales de emergencia.

| Carpeta     | Stack                                       | Rol                         | Estado                  |
|-------------|---------------------------------------------|-----------------------------|-------------------------|
| `frontend/` | Vue 3 + Vite                                | Panel web del administrador | Conectado a la API (planes 002, 007–009) |
| `backend/`  | PHP Laravel + MySQL                         | API REST compartida         | API completa del MVP sin FCM (planes 001, 003–006, 008–010) |
| `mobile/`   | Android Studio, Java                        | App del usuario final       | Base, perfil y contactos conectados a la API (planes 008–010); solicitudes en el plan 011 |

**Planes pendientes ya escritos** (con sus tareas 067–081 en `docs/tasks/`): 011 solicitudes en Android, 012 FCM, 013 despliegue (Vercel + Railway + APK en GitHub Releases). Para seguir: abrir el siguiente plan sin terminar, crear su rama desde `main` y ejecutar sus tareas en orden.

Web y Android consumen la **misma API Laravel**.

## Reglas clave (resumen; detalle en el plan principal)

- **CRUD obligatorio desde Android contra la API:** contactos de confianza.
- **Propiedad:** el `usuario_id` sale del token, nunca del body; un usuario no toca registros de otro.
- **Cuentas:** una tabla `usuarios` con `rol` (`admin`/`usuario`), Sanctum. Usuarios se registran en Android; admins se dan de alta en la BD/seeder (contraseña con hash).
- **Contactos mixtos:** nombre + teléfono + parentesco; se vinculan automáticamente (`usuario_contacto_id`) si el teléfono pertenece a un usuario registrado. Solo los vinculados reciben push.
- **Estados de solicitud:** `Pendiente`, `Enviado`, `Rechazado` (admin), `Cancelado` (usuario), `Terminado`. Las transiciones permitidas están en el plan (sección 7.2); el backend valida el estado actual antes de cada acción.
- **Servicios opcionales:** sin servicios = solicitud solo para contactos (informativa para el admin).
- **Strikes:** 3 activos suspenden la cuenta para pedir servicios; rehabilitar los reinicia a 0, con historial de suspensiones.

## Backend (Laravel) — convenciones

Comandos (desde `backend/`): `php artisan serve` · `php artisan db:seed` (datos de demo, idempotente) · `php artisan bd:pruebas` (recrea `YelpMe_pruebas`) · `php artisan test` · `php artisan test --filter=NombreDePrueba`.

- **Pruebas sobre `YelpMe_pruebas`**, nunca `YelpMe`: `phpunit.xml` apunta ahí, `tests/TestCase.php` aborta si no termina en `_pruebas` y cada prueba se revierte (`DatabaseTransactions`). Recrear con `bd:pruebas` tras cambiar el script o el seeder.
- **Autenticación (plan 003):** `POST /api/registro`, `POST /api/login` (app, correo o teléfono), `POST /api/admin/login` (panel), `GET|DELETE /api/sesion`. Detalle y errores en `backend/README.md`.
- **Recuperar contraseña (plan 008):** `POST /api/recuperacion`, `POST /api/recuperacion/verificacion`, `PUT /api/recuperacion` (públicas, `throttle:acceso`). Lógica en `App\Support\Recuperacion` (tabla `codigos_recuperacion`, hash del código, 15 min, 5 intentos); el 422 se lanza fuera de la transacción para no revertir el intento. Con `MAIL_MAILER=log` el código queda en `storage/logs/laravel.log`.
- **Nombre:** `apellido_paterno` (obligatorio) + `apellido_materno` (opcional, vacío → `null`). Nombre compuesto con `PresentadorUsuario::nombreCompleto`; en el panel `nombreCompleto` de `utils/formato.js`.
- **Contactos (plan 004):** `apiResource /api/contactos`, `GET /api/telefonos/{telefono}`, `/api/vinculos`, `/api/bloqueos` (rol usuario). Plan 010: `PUT|DELETE /api/contactos/{id}/favorito` (uno por usuario, `FavoritoController`) y `domicilio` en cada vínculo. Recursos ajenos → 404. Vinculación en `App\Support\ReglasContactos`; normalización de teléfono, correo y fechas en `App\Support\Normalizar`.
- **Solicitudes (plan 005):** app `apiResource /api/solicitudes` (sin destroy) + `PUT /api/solicitudes/{id}/estado`; panel `/api/admin/solicitudes`, `…/{id}/estado`, `…/{id}/servicios`; `GET /api/servicios`. Transiciones **solo** en `App\Support\TransicionesSolicitud` (espejo de `frontend/src/constantes/transiciones.js`: cambiar ambas). Todo cambio de estado: `DB::transaction` + `Solicitudes::bloquear` (FOR UPDATE) + `exigir` + `aplicarEstado`. Strikes con `ReglasCuenta::darStrike` (suspende al tercero).
- **Usuarios y perfil (plan 006):** panel `/api/admin/usuarios` (+ `/cuenta`, `/strikes`, `/suspension`), `/api/admin/strikes/{id}/anulacion`, `/api/admin/actividad`; perfil `/api/perfil`, `/api/perfil/foto`, `/api/perfil/password` (cualquier rol). Acciones por estado de cuenta en `ReglasCuenta::exigirAccion` (espejo de `frontend/src/constantes/cuentas.js`). Listados en lote con `PresentadorCuenta`.
- **No confiar en `$request->user()` para datos que la misma operación lee o cambia** (foto, teléfono, contraseña): leerlos de la BD. En pruebas `Sanctum::actingAs` reutiliza el objeto y queda desactualizado.
- Este PHP no tiene GD: en pruebas usar `$this->imagenPng()` de `tests/TestCase.php`, no `UploadedFile::fake()->image()`.
- Cuidado con `$a + $b` en reglas de validación: no sobrescribe claves; usar `array_merge`.
- Pruebas: `$this->como('correo@demo')` en `tests/TestCase.php` autentica con Sanctum sin login.
- **Proteger rutas nuevas** dentro de los grupos de `routes/api.php`: `auth:sanctum` + `cuenta.activa` + `rol:usuario` (app) o `rol:admin` con prefijo `admin` (panel). El `usuario_id` sale de `$request->user()`, nunca del body.
- Usuario en respuestas: siempre con `App\Support\PresentadorUsuario` (sin `password` ni `fcm_token`). Reglas de strikes/suspensión en `App\Support\ReglasCuenta`; credenciales en `App\Support\Credenciales`.
- Errores con `abort(código, 'mensaje en español')`: el render de `bootstrap/app.php` los devuelve en el formato estándar. Excepción: un 404 con mensaje propio va con `throw new HttpException(404, '...')`, porque el render cambia toda `NotFoundHttpException` (incluido `abort(404, ...)`) por "Recurso no encontrado.".
- **Domicilio (plan 009):** `GET|PUT|DELETE /api/perfil/domicilio` (rol usuario, 1:1 en `domicilios`, PUT crea o reemplaza). `App\Support\PresentadorDomicilio` (`deUsuario`, `porUsuarios` en lote, `texto`). `usuario.veces_suspendido` con `ReglasCuenta::vecesSuspendido`.

- **Controladores sin modelos Eloquent**: `DB::table(...)` / Query Builder. **Única excepción:** `App\Models\User`, solo para Sanctum (`HasApiTokens`).
- Controladores con `index`, `show`, `store`, `update`, `destroy`.
- Rutas en `routes/api.php` (generado con `php artisan install:api`, que instala Sanctum).
- Disco para fotos de perfil en `config/filesystems.php` + `php artisan storage:link`.
- BD: MySQL `localhost:3306`, base **`YelpMe`**. **El esquema vive en `database/script-bd.sql`** (fuente oficial, sin migraciones de Laravel); cualquier cambio de tablas se hace ahí. Por eso `User` usa `protected $table = 'usuarios'` y `.env` va con `SESSION_DRIVER=file` / `CACHE_STORE=file`.
- **Deshabilitar, no borrar:** usuarios, servicios, strikes, suspensiones y solicitudes nunca se borran (`activo = 0`, anulación o estado final). Solo se borran físicamente contactos, bloqueos, filas de `solicitud_servicio` y tokens. Todas las FK son `ON DELETE RESTRICT`.
- FCM (fase final) con la API v1; librería sugerida `kreait/laravel-firebase`.

## Frontend (`frontend/`)

Comandos (desde `frontend/`):

```bash
npm install
npm run dev      # servidor de desarrollo Vite
npm run build    # build de producción
npm run preview  # previsualizar build
```

No hay linter ni tests configurados.

Dependencias: Vue 3, vue-router, Pinia, axios, Bootstrap 5 (build ESM vía `src/plugins/bootstrap.js`; no importar el bundle), Bootstrap Icons, SweetAlert2, Leaflet, Atkinson Hyperlegible Next y Material Symbols (offline, npm). Credenciales de demo: `admin@yelpme.com` / `Admin123*`.

Si el navegador muestra `504 (Outdated Optimize Dep)` tras instalar paquetes: reiniciar con `npm run dev -- --force` y recargar sin caché.

Arquitectura (plan 002):
- **Flujo de datos:** vistas → stores (`src/stores/`) → services (`src/services/`) → API con axios (`services/http.js`, `pedir()` devuelve `datos`). Sin mocks. Los errores llegan como `ErrorApi { message, codigo, errores }` con el mensaje de Laravel; `campo(nombre)` da el error de un input.
- **Sesión:** `main.js` registra `registrarSesionExpirada`: cualquier 401 fuera del login limpia la sesión y manda a `/login?expirada=1`. Al iniciar, `auth.validarSesion()` confirma el token con `GET /api/sesion`.
- **Recarga periódica:** `useConsultaPeriodica` en `AppShell` llama `solicitudes.refrescar()` cada 15 s (silenciosa: sin skeleton, conserva datos si falla); se pausa con la pestaña oculta.
- **Para levantar el panel:** backend con `php artisan serve` (puerto 8000) y frontend con `npm run dev`. `VITE_API_URL` en `frontend/.env` (ver `.env.example`).
- **Reglas que deciden qué botones mostrar:** `constantes/transiciones.js` (`accionesPermitidas`) y `constantes/cuentas.js` (`accionesCuenta`), espejo de `TransicionesSolicitud` y `ReglasCuenta::ACCIONES` del backend (cambiar ambos lados). La validación real es de la API.
- **Rutas** (`router/index.js`): `/login` pública; resto hijas de `components/layout/AppShell.vue` con guard de sesión. `router.onError` recarga una vez si falla la descarga de un módulo.
- **Tema:** `composables/useTema.js` (claro/oscuro en `localStorage['ym_tema']`; sin elección sigue al sistema) pone `data-theme` y `data-bs-theme` en `<html>`; `index.html` repite la lectura para no parpadear. Selector `comunes/SelectorTema.vue`: botón de ícono que alterna Claro ↔ Oscuro (navbar, login y recuperación). Sin elección guardada sigue `prefers-color-scheme` del navegador: si el navegador fija su propio modo, manda sobre el de Windows. Para texto o acentos con color de marca usar `--ym-marca-texto` (en oscuro el navy no se lee); `--ym-marca` es fondo.
- **Diseño:** tokens en `styles/tokens.css` (único archivo con hex; bloque `:root[data-theme='dark']` para el oscuro, espejo de `mobile/.../values(-night)/colors.xml`); tema de Bootstrap por variables en `styles/tema-bootstrap.css`; base, foco y movimiento reducido en `styles/base.css`; SweetAlert2 en `styles/sweetalert.css` + `plugins/alertas.js` (`confirmar`, `exito`, `error`).
- **Piezas comunes** (`components/comunes/`): `EstadoBadge`, `ListaServicios`, `MapaBase` (único lugar que usa Leaflet), `LineaTiempo`, `TarjetaSeccion`, `CampoBusqueda`, `EstadoVacio`, `BotonCarga`, skeletons. Modales en `components/modals/` sobre `ModalBase`; acciones con motivo reutilizan `ModalMotivo`.

Reglas de componentes (plan 002):
- Carpetas por dominio en `src/components/`: `comunes/`, `layout/`, `modals/`, `dashboard/`, `solicitudes/`, `usuarios/`, `perfil/`.
- **Vista = composición:** acomoda secciones en la rejilla y conecta el store. Sin tablas, formularios ni estilos propios salvo la rejilla.
- Solo vistas y modales usan stores de Pinia; secciones y comunes son presentacionales (props + emits).
- `<style scoped>` con solo tokens (`--ym-*`, `--bs-*`); ningún color hex fuera de `src/styles/tokens.css`.
- `v-for` con `:key` estable para todo lo repetido; `v-if` para estados de la vista; `v-show` para lo que se alterna seguido; nunca `v-if` y `v-for` en el mismo elemento.
- Carga: skeleton (placeholders de Bootstrap) en cargas de vista/sección; círculo pequeño solo dentro de botones de acción; nada en recargas periódicas.

Otras convenciones:
- Íconos de servicios y estados con **Material Symbols** (mismo ícono que Android); Bootstrap Icons para el resto de la UI. La columna `servicios.color` no se usa en pantalla.
- Textos de interfaz: verbos concretos en botones ("Enviar ayuda"), errores que dicen cómo corregir, redacción neutra en género ("quien la creó"), sin separadores "·".
- Probar services contra la API sin navegador: `vite.ssrLoadModule` con `localStorage` simulado y la API servida sobre `YelpMe_pruebas` (`DB_DATABASE=YelpMe_pruebas php artisan serve --port=8010`). El límite de 5 logins por minuto también aplica: `php artisan cache:clear` lo reinicia.

## Mobile (`mobile/`) — Android, Java

- **Los diseños (layouts XML) los crea otra persona.** Nuestro trabajo es darles la lógica, ajustarlos al MVP y estandarizarlos.
- Plantilla **Empty Views Activity**, Java.
- **ViewBinding** obligatorio (nada de `findViewById`), también en adapters de **RecyclerView**.
- **Glide** para imágenes; **Retrofit** con header `Authorization: Bearer <token>`.
- GPS con `FusedLocationProviderClient` y permisos en tiempo de ejecución.
- Llamada directa: `ACTION_CALL` con `CALL_PHONE`; si se niega, `ACTION_DIAL`.
- Íconos de servicios con Material Symbols (Vector Asset).
- Estilo: clases internas anónimas cuando sea posible y constructores.
- Colores en `res/values/colors.xml` y textos en `res/values/strings.xml`, compartiendo paleta con la web; nada hardcodeado.

Comandos (desde `mobile/`): `./gradlew assembleDebug` · `./gradlew testDebugUnitTest` · `./gradlew assembleRelease`.

Base (plan 008):
- **Paquetes:** `com.yelpme.app.ui.<pantalla>` (activities), `api` (Retrofit), `modelos`, `util`. `YelpMeApp` arranca tema, `Sesion` y `ClienteApi`.
- **API:** `ClienteApi.servicio(Interfaz.class)` + `Llamada.de(call).enviar(new Llamada.Respuestas<T>() {...})` → `alExito(datos)` / `alError(ErrorApi)` en el hilo principal. `ErrorApi.campo("telefono")` da el error de un campo (`Formularios.mostrarErrores` los pone en sus `TextInputLayout`). Un 401 fuera de login/registro/recuperación borra la sesión y abre el login con aviso (`InterceptorSesion`). Catálogo de servicios en memoria con `Catalogo.servicios(...)`.
- **URL de la API:** `BuildConfig.API_URL` (debug `http://10.0.2.2:8000/api/`, release Railway). Si el emulador o un teléfono no alcanza `10.0.2.2`: `adb reverse tcp:8000 tcp:8000` y `api.url=http://127.0.0.1:8000/api/` en `local.properties`. HTTP sin cifrar solo en debug (`src/debug`).
- **Pantallas con sesión** heredan de `ui/base/BaseActivity`: `itemMenu()` + `ponerContenido(binding)`; header con saludo, menú lateral (por la derecha), barra de servicios (llamada **simulada**, nunca `ACTION_CALL`) y botón del favorito, insets y `aviso(...)` (Snackbar sobre la barra). Las de acceso usan `Margenes.bordeABorde` + `Margenes.aplicar`.
- **Tema:** `values/colors.xml` y `values-night/colors.xml` son espejo de `frontend/src/styles/tokens.css` (cambiar ambos); `colorPrimary` = `ym_marca_texto`. Estilos `Widget.YelpMe.*` y `TextAppearance.YelpMe.*` en `themes.xml`; fuente Atkinson variable en `res/font`. Tema en el menú lateral: un item alterna claro ↔ oscuro (`util/Tema`, sin elección guardada sigue al sistema, igual que la web).
- **Íconos:** vectores generados desde la fuente Material Symbols del frontend (`res/drawable/ic_*.xml`); `IconosServicio.de(icono)` para los servicios.

Perfil (plan 009):
- **Mapa:** `util/MapaOsm(ctx, FrameLayout, getLifecycle())` envuelve osmdroid con ciclo de vida (sin fugas), filtro oscuro igual que la web, `marcador(...)` (un pin), `soloLectura(true)` para vistas previas y gesto propio dentro de un scroll. Para elegir un punto: `ElegirUbicacionActivity.intent(ctx, lat, lng, titulo)` con `StartActivityForResult` → `EXTRA_LAT`/`EXTRA_LNG` (pin fijo al centro, "Mi ubicación" con permiso). StrictMode (`detectActivityLeaks`) en debug.
- **Perfil:** `ServicioPerfil` (perfil, foto, contraseña, domicilio). Pantallas secundarias (`EditarPerfilActivity`, `DomicilioActivity`, `PasswordActivity`) con `layout_barra_secundaria` y resultado al perfil para el Snackbar.
- **Foto:** Photo Picker (sin permisos) + `util/Imagenes.comprimir` (1024 px, JPEG ≤ 1 MB, en hilo aparte) + multipart `foto`. La URL de la foto sale de `APP_URL`: en el emulador se ve con `adb reverse` (`localhost:8000`).
Contactos (plan 010):
- **Pantallas:** `ui/contactos` (`ContactosActivity` lista con búsqueda sin acentos, `DetalleContactoActivity`, `FormContactoActivity.nuevo/editar` con verificación del teléfono a los 600 ms) y `ui/vinculos` (`VinculosActivity` "Quién me tiene" con domicilio en mapa, `BloqueosActivity`). Las pantallas secundarias devuelven `EXTRA_MENSAJE` para el Snackbar de quien las abrió. `ServicioContactos` cubre contactos, favorito, teléfonos, vínculos y bloqueos.
- **Llamadas:** `util/Llamadas` (crear en `onCreate`): `ACTION_CALL` con `CALL_PHONE`, si se niega `ACTION_DIAL`. Solo para contactos; los servicios de la barra siguen simulados.
- **Favorito:** `api/Favorito` en memoria por usuario (la lista lo actualiza; detalle y formulario lo invalidan). La barra muestra su nombre y lo llama; sin favorito abre Contactos.
- Mapa dentro de un diálogo: meterlo en un marco de alto fijo y llamar `MapaOsm.liberar()` al cerrar.

- **Generar íconos nuevos:** glifo de `frontend/node_modules/material-symbols/material-symbols-outlined.woff2` con fontTools (`TransformPen(pen, (1,0,0,-1,0,960))`, viewport 960), mismo formato que los `ic_*.xml` existentes.
