# 001 — Plan: Backend base

> Fecha: 2026-11-19 · Rama: `feature/backend-base`
> Referencia: `docs/plans/principal-plan.md` (secciones 5, 10.1, 10.1.1 y 10.2)

## Objetivo

Dejar el proyecto Laravel listo para construir la API sobre el esquema de `database/script-bd.sql`: configuración alineada con el plan principal, autenticación preparada con Sanctum sobre la tabla `usuarios`, almacenamiento de fotos de perfil, respuestas JSON consistentes y datos de prueba para la demo.

**No incluye** endpoints de negocio (auth, contactos, solicitudes); esos van en los planes 002 en adelante.

## Punto de partida

- Laravel 12 + Sanctum 4 instalados (`php artisan install:api` ya ejecutado).
- `routes/api.php` solo tiene la ruta de ejemplo `/user`.
- La conexión a la base de datos `YelpMe` funciona y el esquema del script ya está creado. **No se ha corrido ninguna migración.**

### Diferencias con el plan principal

| Elemento | Actual | Esperado |
|---|---|---|
| `SESSION_DRIVER` / `CACHE_STORE` | `database` | `file` (no hay tablas de sesiones ni de caché) |
| Migraciones por defecto | `users`, `cache`, `jobs`, `personal_access_tokens` | Ninguna: el esquema vive en el script |
| `App\Models\User` | Tabla `users`, campos `name`/`email`, sin `HasApiTokens` | Tabla `usuarios`, campos propios, `HasApiTokens` |
| `UserFactory` / `DatabaseSeeder` | Usan la tabla `users` | Seeders con `DB::table` sobre `usuarios` |
| `APP_NAME` | `Laravel` | `YelpMe` |
| Disco de fotos de perfil | No existe | Disco `perfiles` + `storage:link` |

> ⚠️ Si se ejecuta `php artisan migrate` por error, se crearán las tablas `users`, `cache`, `jobs` y `migrations`, y la migración de `personal_access_tokens` fallará porque esa tabla ya existe. Por eso la tarea 002 elimina las migraciones.

---

## Tareas

### 001 — Configurar el entorno
Archivo: `docs/tasks/001-tarea-configurar-entorno-2026-11-19.md`

- [ ] En `.env` y `.env.example`: `APP_NAME=YelpMe`, `SESSION_DRIVER=file`, `CACHE_STORE=file`, `QUEUE_CONNECTION=sync`.
- [ ] Localización: `APP_LOCALE=es`, `APP_FAKER_LOCALE=es_MX` y zona horaria `America/Mexico_City` en `config/app.php`.
- [ ] `.env.example` con `DB_DATABASE=YelpMe` y **sin** contraseñas ni llaves.
- [ ] Verificar con `php artisan config:show database` y `php artisan db:show`.

### 002 — Quitar migraciones y factories por defecto
Archivo: `docs/tasks/002-tarea-limpiar-migraciones-2026-11-19.md`

- [ ] Eliminar las 4 migraciones de `database/migrations/`.
- [ ] Eliminar `database/factories/UserFactory.php`.
- [ ] Documentar en `backend/README.md` que el esquema se crea con `database/script-bd.sql` y que **no** se usa `php artisan migrate`.
- [ ] Reemplazar el `README.md` de la plantilla de Laravel por uno del proyecto (instalación, `.env`, script de BD, `serve`).

### 003 — Adaptar el modelo `User` a `usuarios`
Archivo: `docs/tasks/003-tarea-modelo-user-2026-11-19.md`

- [ ] `protected $table = 'usuarios'`.
- [ ] Agregar `HasApiTokens` (Sanctum); quitar `HasFactory`.
- [ ] `$fillable`: `nombre`, `apellidos`, `telefono`, `correo`, `password`, `rol`, `foto_perfil`, `fcm_token`, `activo`.
- [ ] `$hidden`: `password`, `fcm_token`. Sin `remember_token`.
- [ ] `casts`: `password => hashed`, `activo => boolean`, `deshabilitado_en => datetime`.
- [ ] **Es el único modelo del proyecto**; el resto del acceso a datos se hace con `DB::table`.
- [ ] Prueba rápida en `php artisan tinker`: buscar al admin inicial y generar un token con `createToken()`.

### 004 — Disco de fotos de perfil
Archivo: `docs/tasks/004-tarea-disco-perfiles-2026-11-19.md`

- [ ] Agregar el disco `perfiles` en `config/filesystems.php` (raíz `storage/app/public/perfiles`, visibilidad pública, URL `/storage/perfiles`).
- [ ] Ejecutar `php artisan storage:link`.
- [ ] Verificar que un archivo de prueba se sirve desde `http://localhost:8000/storage/perfiles/...`.

### 005 — Base de la API: rutas, JSON y CORS
Archivo: `docs/tasks/005-tarea-base-api-2026-11-19.md`

- [ ] Quitar la ruta de ejemplo `/user` de `routes/api.php`.
- [ ] Agregar `GET /api/ping` (pública) para verificar que la API responde.
- [ ] En `bootstrap/app.php`: errores en JSON para `api/*` (401, 403, 404, 422, 500) con mensajes en español.
- [ ] Definir el formato estándar de respuesta (p. ej. `{ "ok": true, "mensaje": "...", "datos": ... }`) y documentarlo en el README.
- [ ] Publicar y configurar CORS (`php artisan config:publish cors`) para permitir el origen del frontend en desarrollo (`http://localhost:5173`).
- [ ] Crear la carpeta `app/Http/Controllers/Api/` para los controladores de la API.

### 006 — Datos de prueba
Archivo: `docs/tasks/006-tarea-datos-prueba-2026-11-19.md`

- [ ] `DatabaseSeeder` con `DB::table(...)`, sin modelos ni factories.
- [ ] No duplicar los datos iniciales del script (admin y servicios); solo agregar datos de demo:
  - [ ] 1 administrador más.
  - [ ] 4–5 usuarios, con contraseña conocida y cifrada con `Hash::make`.
  - [ ] Contactos vinculados y no vinculados.
  - [ ] Solicitudes en los 5 estados, con y sin servicios, con coordenadas reales de la zona.
  - [ ] Un usuario con 2 strikes activos y otro suspendido (para la demo de strikes).
- [ ] Que se pueda ejecutar varias veces sin duplicar (validar por teléfono o correo antes de insertar).
- [ ] Ejecutar con `php artisan db:seed` y documentar las credenciales de prueba en el README.

---

## Criterios de terminado

- [ ] `php artisan serve` levanta y `GET /api/ping` responde JSON.
- [ ] `php artisan migrate:status` no lista migraciones pendientes que puedan romper el esquema.
- [ ] Desde `tinker`, el admin inicial genera un token de Sanctum y el token queda en `personal_access_tokens`.
- [ ] Las imágenes del disco `perfiles` se sirven por URL pública.
- [ ] `php artisan db:seed` carga los datos de demo sin errores y sin duplicar.
- [ ] Ningún archivo con secretos (`.env`, credenciales) entra en el repositorio.
- [ ] Commits por tarea, en español, sin coautoría.
