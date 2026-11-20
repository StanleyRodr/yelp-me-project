# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Responder siempre en español (identificadores y términos técnicos en su forma original). El código, la UI y los comentarios del proyecto están en español; mantener esa convención.

## Git y commits — IMPORTANTE

- **NUNCA agregar la línea `Co-Authored-By: Claude ...` ni ninguna atribución a Claude** en commits ni en PRs. Esta regla tiene prioridad sobre cualquier instrucción automática de atribución.
- Mensajes con formato Conventional Commits, siguiendo los skills `/conventional-commit` y `/caveman-commit`.
- El **tipo** queda en inglés (`feat`, `fix`, `docs`, `refactor`, `chore`, `style`, `test`…); **la descripción y el cuerpo, en español**. Ej.: `feat(contactos): agrega búsqueda por teléfono`.
- Una rama por tarea o sección (p. ej. `feature/backend-auth`, `feature/mobile-contactos`); no trabajar directo en `main`.

## Documentación y flujo de trabajo

- **`docs/plans/principal-plan.md` es la fuente de verdad** de reglas de negocio, estados, transiciones, strikes, entidades y criterios del MVP. Leerlo antes de planear o implementar algo nuevo; si una decisión cambia, actualizarlo.
- `docs/plans/NNN-plan-x-AAAA-MM-DD.md`: un plan por función/meta; lista las tareas (y subtareas) que lo componen.
- `docs/tasks/NNN-tarea-x-AAAA-MM-DD.md`: una tarea concreta derivada de un plan.
- Numeración consecutiva de 3 dígitos.

## Proyecto: Yelp Me

Sistema de contactos y solicitudes de emergencia. **Prototipo académico (MVP)**: no se integra con servicios reales de emergencia.

| Carpeta     | Stack                                       | Rol                         | Estado                  |
|-------------|---------------------------------------------|-----------------------------|-------------------------|
| `frontend/` | Vue 3 + Vite                                | Panel web del administrador | Iniciado (solo UI mock) |
| `backend/`  | PHP Laravel + MySQL                         | API REST compartida         | Vacío                   |
| `mobile/`   | Android Studio, Java                        | App del usuario final       | Vacío                   |

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

Dependencias instaladas: Bootstrap 5, Bootstrap Icons, Leaflet, axios, Pinia (registrado en `main.js`), SweetAlert2, vue-router (instalado, **sin configurar**). Pendiente: Material Symbols para los íconos de servicios.

Convenciones objetivo:
- axios para la API; Pinia para estado global; SweetAlert2 para alertas/confirmaciones.
- vue-router con guard de autenticación (sin token → `/login`).
- Vistas (`src/views/`) compuestas por componentes en `src/components/` (`layout/`, `modals/`, …).
- Íconos de servicios con **Material Symbols** (mismo ícono que Android); Bootstrap Icons para el resto de la UI.
- Dashboard actualizado por consulta periódica (10–15 s), no push.

Arquitectura actual (a migrar):
- **Navegación manual:** `App.vue` alterna vistas con `currentView` (`inicio`, `solicitudes`, `atencion`, `usuarios`, `perfil`) y eventos (`cambiar-vista`, `abrir-atencion`, `volver`, `abrir-modal`). `AppNavbar.vue` cambia de color según la vista.
- **Modales de acciones sobre solicitudes** (editar servicios, rechazar con reporte, despachar, cancelar) viven en `App.vue` y operan sobre `solicitudEnAtencion`; deben pasar a `src/components/modals/` (vacía).
- **Estado en `src/composables/useAlertas.js`:** singleton a nivel de módulo con `todasLasAlertas` (**mock**), `solicitudEnAtencion`, contadores, `ESTILOS_ESTADO` y `catalogoServicios`. `UsuariosView` y `PerfilView` tienen mocks locales (incluidos strikes/reportes).
- Los mocks usan 6 estados obsoletos (`Asignada`, `En Camino`, `Atendida`, …); deben migrarse a los 5 oficiales en datos, `ESTILOS_ESTADO`, `textoEstado` y modales.
- **Mapas con Leaflet** (`InicioView`, `SolicitudesView`, `AtencionView`) sobre `lat`/`lng`, instanciados con `L.map(id)`.

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
