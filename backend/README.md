# Yelp Me — Backend (API)

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

## Formato de respuesta

Todas las respuestas de `api/*` usan el mismo formato JSON:

```json
{ "ok": true, "mensaje": "API Yelp Me funcionando", "datos": { } }
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

## Convenciones

- Controladores **sin modelos Eloquent**: acceso a datos con `DB::table(...)`. Única excepción: `App\Models\User`, solo para autenticación con Sanctum.
- Controladores con `index`, `show`, `store`, `update`, `destroy`.
- Rutas en `routes/api.php`.
- Sesiones y caché en archivos (`SESSION_DRIVER=file`, `CACHE_STORE=file`).
