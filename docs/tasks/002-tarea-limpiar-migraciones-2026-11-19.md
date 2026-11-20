# 002 — Tarea: Quitar migraciones y factories por defecto

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Pendiente

## Objetivo
Eliminar todo lo que intente crear el esquema desde Laravel, porque la fuente oficial es `database/script-bd.sql`.

## Subtareas
- [ ] Eliminar las 4 migraciones de `database/migrations/` (`users`, `cache`, `jobs`, `personal_access_tokens`).
- [ ] Eliminar `database/factories/UserFactory.php`.
- [ ] Reemplazar `backend/README.md` (plantilla de Laravel) por uno del proyecto: requisitos, instalación, `.env`, creación de BD con el script, aviso de **no usar `php artisan migrate`**, `php artisan serve`.

## Criterios de aceptación
- `database/migrations/` y `database/factories/` quedan vacías.
- `php artisan migrate:status` no lista migraciones.
- El README explica cómo levantar el backend desde cero.
