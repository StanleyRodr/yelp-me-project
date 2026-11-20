# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Responder siempre en español (identificadores y términos técnicos en su forma original). El código, la UI y los comentarios del proyecto están en español; mantener esa convención.

## Git y commits — IMPORTANTE

- **NUNCA agregar la línea `Co-Authored-By: Claude ...` ni ninguna atribución a Claude** en commits ni en PRs. Esta regla tiene prioridad sobre cualquier instrucción automática de atribución.
- Mensajes con formato Conventional Commits, siguiendo los skills `/conventional-commit` y `/caveman-commit`.
- El **tipo** queda en inglés (`feat`, `fix`, `docs`, `refactor`, `chore`, `style`, `test`…); **la descripción y el cuerpo, en español**. Ej.: `feat(contactos): agrega búsqueda por teléfono`.
- Una rama por plan o sección (p. ej. `feature/backend-auth`, `feature/mobile-contactos`); no trabajar directo en `main`.
- **Un commit por cada tarea terminada** de `docs/tasks/` (incluye el código y la actualización del `.md` de la tarea).
- **Un PR por cada plan terminado** de `docs/plans/`, hacia `main` (o apilado sobre la rama del plan anterior si aún no se fusiona). Título en español; cuerpo con resumen, tabla de tareas con sus commits y cómo probar. Sin atribución a Claude.

## Documentación y flujo de trabajo

- **`docs/plans/principal-plan.md` es la fuente de verdad** de reglas de negocio, estados, transiciones, strikes, entidades y criterios del MVP. Leerlo antes de planear o implementar algo nuevo; si una decisión cambia, actualizarlo.
- `docs/plans/NNN-plan-x-AAAA-MM-DD.md`: un plan por función/meta. Las tareas aparecen solo como **resumen breve** (tabla con enlace y estado).
- `docs/tasks/NNN-tarea-x-AAAA-MM-DD.md`: una tarea concreta con los **pasos detallados** para hacerla (código, comandos, verificación), redactada en **estilo caveman** (skill `/caveman`): solo lo esencial, sin relleno.
- Numeración consecutiva de 3 dígitos, **global** entre planes (las tareas no reinician por plan).
- Al terminar una tarea: marcar estado ✅ en la tarea y en la tabla del plan, y anotar el commit.

## Proyecto: Yelp Me

Sistema de contactos y solicitudes de emergencia. **Prototipo académico (MVP)**: no se integra con servicios reales de emergencia.

| Carpeta     | Stack                                       | Rol                         | Estado                  |
|-------------|---------------------------------------------|-----------------------------|-------------------------|
| `frontend/` | Vue 3 + Vite                                | Panel web del administrador | Estructura y diseño listos (plan 002), datos de ejemplo |
| `backend/`  | PHP Laravel + MySQL                         | API REST compartida         | Base lista (plan 001), sin endpoints de negocio |
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

Dependencias: Vue 3, vue-router, Pinia, axios, Bootstrap 5 (build ESM vía `src/plugins/bootstrap.js`; no importar el bundle), Bootstrap Icons, SweetAlert2, Leaflet, Atkinson Hyperlegible Next y Material Symbols (offline, npm). Credenciales de demo: `admin@yelpme.com` / `Admin123*`.

Si el navegador muestra `504 (Outdated Optimize Dep)` tras instalar paquetes: reiniciar con `npm run dev -- --force` y recargar sin caché.

Arquitectura (plan 002):
- **Flujo de datos:** vistas → stores (`src/stores/`) → services (`src/services/`) → mocks (`src/mocks/`). Los mocks replican las tablas de `script-bd.sql` y el seeder; cada service documenta su endpoint futuro. Conectar la API = cambiar solo los services (cliente listo en `services/http.js`).
- **Reglas de negocio en un solo lugar:** transiciones en `constantes/transiciones.js` (`accionesPermitidas`), estados en `constantes/estados.js`, cuentas/strikes en `constantes/cuentas.js` y `services/reglasUsuarios.js`. UI y services usan las mismas funciones.
- **Rutas** (`router/index.js`): `/login` pública; resto hijas de `components/layout/AppShell.vue` con guard de sesión. `router.onError` recarga una vez si falla la descarga de un módulo.
- **Diseño:** tokens en `styles/tokens.css` (único archivo con hex); tema de Bootstrap por variables en `styles/tema-bootstrap.css`; base, foco y movimiento reducido en `styles/base.css`; SweetAlert2 en `styles/sweetalert.css` + `plugins/alertas.js` (`confirmar`, `exito`, `error`).
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
- Pendiente (plan futuro): conectar services a la API y consulta periódica del dashboard (10–15 s), sin push.

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
