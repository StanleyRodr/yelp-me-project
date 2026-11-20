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

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 001 | [Configurar el entorno](../tasks/001-tarea-configurar-entorno-2026-11-19.md) | `.env` y `.env.example` en español, zona horaria de México, sesión/caché en archivos, BD `YelpMe`. | ✅ |
| 002 | [Quitar migraciones](../tasks/002-tarea-limpiar-migraciones-2026-11-19.md) | Eliminar migraciones y factory por defecto; README del backend con el script de BD como fuente. | ✅ |
| 003 | [Modelo `User`](../tasks/003-tarea-modelo-user-2026-11-19.md) | Adaptar el único modelo a la tabla `usuarios` con `HasApiTokens` para emitir tokens. | ✅ |
| 004 | [Disco de perfiles](../tasks/004-tarea-disco-perfiles-2026-11-19.md) | Disco `perfiles` público para fotos de perfil y `storage:link`. | ✅ |
| 005 | [Base de la API](../tasks/005-tarea-base-api-2026-11-19.md) | `/api/ping`, formato JSON estándar, errores en español, validación traducida y CORS. | ✅ |
| 006 | [Datos de prueba](../tasks/006-tarea-datos-prueba-2026-11-19.md) | Seeder con `DB::table` e idempotente que cubre estados, contactos, strikes y suspensiones. | Pendiente |

---

## Criterios de terminado

- [x] `php artisan serve` levanta y `GET /api/ping` responde JSON.
- [x] `php artisan migrate:status` no lista migraciones pendientes que puedan romper el esquema.
- [x] Desde `tinker`, el admin inicial genera un token de Sanctum y el token queda en `personal_access_tokens`.
- [x] Las imágenes del disco `perfiles` se sirven por URL pública.
- [ ] `php artisan db:seed` carga los datos de demo sin errores y sin duplicar.
- [x] Ningún archivo con secretos (`.env`, credenciales) entra en el repositorio.
- [x] Commits por tarea, en español, sin coautoría.
