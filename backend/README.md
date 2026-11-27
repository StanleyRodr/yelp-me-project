# YelpMe — Backend (API)

API REST en Laravel 12 que consumen el panel web (Vue) y la app Android. Contexto completo del proyecto en [`docs/plans/principal-plan.md`](../docs/plans/principal-plan.md).

## Requisitos

- PHP 8.2+ y Composer
- MySQL 8 en `localhost:3306`

## Instalación

```bash
cd backend
composer install
cp .env.example .env          # luego llenar DB_USERNAME / DB_PASSWORD
php artisan key:generate
php artisan storage:link
```

## Base de datos

El esquema **no** se crea con migraciones de Laravel. La fuente oficial es [`database/script-bd.sql`](../database/script-bd.sql), en la raíz del repositorio:

```bash
mysql -u root -p < ../database/script-bd.sql
```

El script crea la base `YelpMe`, las tablas y los datos iniciales (administrador inicial y catálogo de servicios).

> ⚠️ **No ejecutar `php artisan migrate`.** El proyecto no tiene migraciones; cualquier cambio de tablas se hace en `script-bd.sql`.

### Datos de prueba

```bash
php artisan db:seed           # idempotente: no duplica si ya se cargaron
```

| Rol | Correo | Contraseña | Caso de demo |
|---|---|---|---|
| admin | `admin@yelpme.com` | `Admin123*` | Admin inicial (del script) |
| admin | `laura.admin@yelpme.com` | `Admin123*` | Segundo admin |
| usuario | `carlos@correo.com` | `Usuario123*` | Contactos vinculados y no vinculados; solicitudes Pendiente, Enviado, Cancelado |
| usuario | `maria@correo.com` | `Usuario123*` | Solicitudes sin servicios (solo contactos) |
| usuario | `jose@correo.com` | `Usuario123*` | Bloqueó a Ana; solicitud Terminado |
| usuario | `ana@correo.com` | `Usuario123*` | 2 strikes activos |
| usuario | `luis@correo.com` | `Usuario123*` | Suspendido (3 strikes), con una suspensión anterior rehabilitada |

Teléfonos de usuarios: `2351000001` a `2351000005` (mismo orden de la tabla).

## Ejecutar

```bash
php artisan serve             # http://localhost:8000
```

## Pruebas

Las pruebas usan una BD aislada, **`YelpMe_pruebas`**, nunca `YelpMe`:

```bash
php artisan bd:pruebas        # recrea YelpMe_pruebas desde ../database/script-bd.sql + datos de demo
php artisan test              # todas las pruebas
php artisan test --filter=AutenticacionTest   # solo un archivo
php artisan test --filter=test_login_panel_admin   # una sola prueba
```

- `bd:pruebas` solo acepta nombres terminados en `_pruebas`; no puede borrar la BD de desarrollo.
- Cada prueba corre en una transacción que se revierte: no deja datos.
- Si `phpunit.xml` apuntara a otra BD, `tests/TestCase.php` detiene las pruebas.
- Volver a correr `bd:pruebas` después de cambiar `script-bd.sql` o el seeder.

## Formato de respuesta

Todas las respuestas de `api/*` usan el mismo formato JSON:

```json
{ "ok": true, "mensaje": "API YelpMe funcionando", "datos": { } }
```

Errores (`ok: false`, `datos: null`), siempre en JSON y en español:

| Código | Cuándo |
|---|---|
| 401 | Sin token o token inválido |
| 403 | Sin permiso (rol o propietario) |
| 404 | Ruta o recurso inexistente |
| 405 | Método HTTP no permitido |
| 422 | Validación fallida; incluye `errores` por campo |
| 429 | Demasiados intentos (`throttle`) |
| 500 | Error interno (detalle solo con `APP_DEBUG=true`) |

En los controladores se usa el helper `$this->respuesta($datos, $mensaje, $codigo)` del `Controller` base.

Verificación rápida: `GET http://localhost:8000/api/ping`.

## Autenticación (Sanctum)

Token en el header `Authorization: Bearer <token>`. Los endpoints de acceso tienen un límite de 5 intentos por minuto (429).

| Método | Ruta | Quién | Cuerpo | Respuesta (`datos`) |
|---|---|---|---|---|
| POST | `/api/registro` | Público (app) | `nombre`, `apellido_paterno`, `apellido_materno`? (vacío → `null`), `telefono` (10 dígitos), `correo`, `password`, `password_confirmation`, `fcm_token`? | `token`, `usuario`, `contactos_vinculados` (201) |
| POST | `/api/login` | Público (app, rol usuario) | `correo` **o** `telefono`, `password`, `fcm_token`? | `token`, `usuario` |
| POST | `/api/admin/login` | Público (panel, rol admin) | `correo`, `password` | `token`, `usuario` |
| GET | `/api/sesion` | Con token | — | `usuario` |
| DELETE | `/api/sesion` | Con token | — | `null` (borra solo el token actual) |

`usuario` = `id, nombre, apellido_paterno, apellido_materno, telefono, correo, rol, foto_perfil (URL), activo, suspendido, strikes_activos, limite_strikes, created_at`. Nunca incluye `password` ni `fcm_token`.

| Situación | Código | Mensaje |
|---|---|---|
| Credenciales incorrectas (exista o no la cuenta) | 401 | Correo, teléfono o contraseña incorrectos. |
| Admin en `/api/login` | 403 | Esta cuenta es de administrador. Inicia sesión en el panel web. |
| Usuario en `/api/admin/login` | 403 | Esta cuenta no tiene acceso al panel de administración. |
| Cuenta deshabilitada (login o token previo) | 403 | Esta cuenta está deshabilitada. |
| Rol sin permiso en una ruta | 403 | No tienes permiso para realizar esta acción. |

Una cuenta **suspendida** sí inicia sesión (`suspendido: true`); la app bloquea pedir servicios.

Middlewares para rutas nuevas: `auth:sanctum` + `cuenta.activa` + `rol:usuario` (app) o `rol:admin` (panel, prefijo `/api/admin`). Los grupos ya están en `routes/api.php`.

### Recuperación de contraseña (cualquier rol)

Públicas, con `throttle:acceso` (5 por minuto por correo + IP). Lógica en `App\Support\Recuperacion`; tabla `codigos_recuperacion` (se guarda el hash del código).

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/recuperacion` | `correo` | 200 "Si el correo está registrado, te enviamos un código." (igual si no existe o está deshabilitada) |
| POST | `/api/recuperacion/verificacion` | `correo`, `codigo` | 200 `{ valido: true }` · 422 `errores.codigo` "El código no es válido o ya venció." |
| PUT | `/api/recuperacion` | `correo`, `codigo`, `password`, `password_confirmation` | 200 "Contraseña actualizada. Inicia sesión." · 422 |

- Código de 6 dígitos, vence en 15 min, 5 intentos fallidos lo invalidan. Pedir otro anula el anterior.
- Al cambiar la contraseña se borran **todos** los tokens de la cuenta.
- En desarrollo (`MAIL_MAILER=log`) el correo queda en `storage/logs/laravel.log`: buscar "Tu código de YelpMe". Para SMTP ver el comentario en `.env.example`.

## Contactos de confianza (app, rol `usuario`)

| Método | Ruta | Cuerpo | Respuesta (`datos`) |
|---|---|---|---|
| GET | `/api/contactos` | — | Lista de mi agenda, por nombre |
| POST | `/api/contactos` | `nombre`, `telefono`, `parentesco` | Contacto (201) |
| GET | `/api/contactos/{id}` | — | Contacto |
| PUT/PATCH | `/api/contactos/{id}` | Campos a cambiar | Contacto |
| DELETE | `/api/contactos/{id}` | — | `null` |
| GET | `/api/telefonos/{telefono}` | — | `{ telefono, vinculable, en_agenda }` (30/min) |
| GET | `/api/vinculos` | — | Quién me tiene: `[{ id, nombre, apellido_paterno, apellido_materno, telefono, desde }]` |
| DELETE | `/api/vinculos/{id}` | — | `null` (me borra de su agenda) |
| GET | `/api/bloqueos` | — | `[{ id, nombre, apellido_paterno, apellido_materno, telefono, desde }]` |
| POST | `/api/bloqueos` | `contacto_id` (id de un vínculo) | Bloqueo (201) |
| DELETE | `/api/bloqueos/{id}` | — | `null` |

Contacto = `id, nombre, telefono, parentesco, vinculado, created_at, updated_at`.

- **Vinculación automática** al crear y al cambiar el teléfono: si el número es de una cuenta de rol `usuario` que no me bloqueó. Si no, se guarda sin vínculo (se puede llamar, no recibe avisos).
- **Errores 422:** teléfono repetido en mi agenda ("Ya tienes un contacto con ese teléfono."), mi propio número ("No puedes agregarte a ti mismo como contacto.").
- **Propiedad:** contactos, vínculos y bloqueos ajenos responden 404 (no revela que existen).
- **Bloquear** borra el contacto de la agenda de quien me tiene e impide que me vuelva a vincular. **Desbloquear** no lo restaura, pero vincula el contacto si ya me había guardado de nuevo.
- `vinculable: false` es igual para sin cuenta, admin, número propio o cuenta que me bloqueó.

## Solicitudes de ayuda

Solicitud = columnas de `solicitudes` (`id, usuario_id, titulo, descripcion, latitud, longitud, ubicacion_nombre, estado, admin_id, cerrado_por_id, motivo, enviado_en, cerrado_en, created_at, updated_at`) + `servicios` (ids) + `usuario` `{ id, nombre, apellido_paterno, apellido_materno, telefono }` + `admin` y `cerrado_por` `{ id, nombre, apellido_paterno, apellido_materno }` o `null`. Misma forma que usa el frontend.

| Método | Ruta | Quién | Cuerpo |
|---|---|---|---|
| GET | `/api/servicios` | Cualquier rol | — (`{ id, nombre, icono, color }`) |
| GET | `/api/solicitudes?estado=` | Usuario | — |
| POST | `/api/solicitudes` | Usuario | `titulo`, `latitud`, `longitud`, `descripcion`?, `ubicacion_nombre`?, `servicios`? (ids) |
| GET | `/api/solicitudes/{id}` | Usuario | — |
| PUT/PATCH | `/api/solicitudes/{id}` | Usuario (solo Pendiente) | Campos a cambiar |
| PUT | `/api/solicitudes/{id}/estado` | Usuario | `estado` (Cancelado/Terminado), `motivo` (al cancelar) |
| GET | `/api/admin/solicitudes?estado=` | Admin | — |
| GET | `/api/admin/solicitudes/{id}` | Admin | — |
| PUT | `/api/admin/solicitudes/{id}/estado` | Admin | `estado` (Enviado/Rechazado/Terminado), `motivo` (al rechazar), `strike`? |
| PUT | `/api/admin/solicitudes/{id}/servicios` | Admin | `servicios` (mínimo 1) |

Transiciones (plan principal 7.2; tabla en `App\Support\TransicionesSolicitud`, igual al frontend):

| Rol | Con servicios | Sin servicios |
|---|---|---|
| Admin | Pendiente → Enviado/Rechazado · Enviado → Terminado | Pendiente → Rechazado |
| Usuario | Pendiente → Cancelado · Enviado → Terminado/Cancelado | Pendiente → Terminado/Cancelado |

| Situación | Código |
|---|---|
| Transición no permitida o ya cambiada por otro admin | 409 "La solicitud está en X: no se puede pasar a Y…" |
| Modificar una solicitud que no está Pendiente | 409 |
| Cuenta suspendida pide o agrega servicios | 403 "Tu cuenta está suspendida: solo puedes avisar a tus contactos." |
| Falta motivo al cancelar o rechazar | 422 |
| Solicitud ajena (rutas de la app) | 404 |

- Rechazar con `strike: true` registra el strike ligado a la solicitud; con 3 activos, la cuenta se suspende automáticamente.
- Las solicitudes no se borran (DELETE → 405).

## Gestión de usuarios (panel, rol `admin`)

| Método | Ruta | Cuerpo | Respuesta (`datos`) |
|---|---|---|---|
| GET | `/api/admin/usuarios` | — | Resúmenes de usuarios de la app |
| GET | `/api/admin/usuarios/{id}` | — | Detalle |
| PUT | `/api/admin/usuarios/{id}/cuenta` | `activo` (false deshabilita, true reactiva) | Detalle |
| POST | `/api/admin/usuarios/{id}/strikes` | `motivo` | Detalle (201) |
| PUT | `/api/admin/strikes/{id}/anulacion` | `motivo` | Detalle |
| POST | `/api/admin/usuarios/{id}/suspension` | `motivo` | Detalle (201) |
| DELETE | `/api/admin/usuarios/{id}/suspension` | — | Detalle (rehabilitar) |
| GET | `/api/admin/actividad` | — | `{ solicitudes, strikes_dados }` del admin con sesión |

- **Resumen** = usuario + `suspendido, strikes_activos, limite_strikes, estado_cuenta, veces_suspendido`. **Detalle** = resumen + `suspension_vigente, solicitudes, strikes, suspensiones` (con nombres de los admins). Misma forma que `frontend/src/services/usuariosService.js`.
- Solo usuarios de rol `usuario`: un id de admin → 404.

| Cuenta | Acciones permitidas (otra → 409) |
|---|---|
| Activo | reportar, suspender, deshabilitar |
| Suspendido | rehabilitar, reportar, deshabilitar |
| Deshabilitado | reactivar |

- Reportar suma un strike manual; al tercero activo, suspensión automática.
- Anular deja de contar el strike y guarda quién y por qué; **no** levanta la suspensión (eso es rehabilitar).
- Rehabilitar cierra la suspensión vigente y reinicia los strikes activos a 0; el historial se conserva.
- Deshabilitar cierra todas las sesiones de la cuenta.

## Perfil propio (cualquier rol)

| Método | Ruta | Cuerpo |
|---|---|---|
| GET | `/api/sesion` | — (consultar el perfil) |
| PUT/PATCH | `/api/perfil` | `nombre`, `apellido_paterno`, `apellido_materno` (vacío o `null` lo quita), `telefono`, `correo` (los que cambian) |
| POST | `/api/perfil/foto` | multipart `foto` (JPG/PNG/WEBP, máx. 1 MB) |
| DELETE | `/api/perfil/foto` | — |
| PUT | `/api/perfil/password` | `actual`, `nueva`, `nueva_confirmation` |

- Teléfono nuevo: se actualiza en las agendas que me tienen vinculado (salvo donde ese número ya estaba guardado aparte) y se vinculan contactos que ya lo tenían.
- La foto anterior se borra del disco al reemplazarla o quitarla.
- Cambiar la contraseña cierra las demás sesiones; la actual sigue.

## Convenciones

- Controladores **sin modelos Eloquent**: acceso a datos con `DB::table(...)`. Única excepción: `App\Models\User`, solo para autenticación con Sanctum.
- Controladores con `index`, `show`, `store`, `update`, `destroy`.
- Rutas en `routes/api.php`.
- Sesiones y caché en archivos (`SESSION_DRIVER=file`, `CACHE_STORE=file`).
