# 001 — Tarea: Configurar el entorno

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Completada

## Objetivo
Alinear `.env`, `.env.example` y `config/app.php` con el plan principal: sin tablas de sesiones, caché ni colas en BD, idioma español y zona horaria de México.

## Subtareas
- [x] `.env`: `APP_NAME=YelpMe`, `SESSION_DRIVER=file`, `CACHE_STORE=file`, `QUEUE_CONNECTION=sync`, `APP_LOCALE=es`, `APP_FALLBACK_LOCALE=es`, `APP_FAKER_LOCALE=es_MX`, `APP_URL=http://localhost:8000`.
- [x] `.env.example`: los mismos valores, `DB_CONNECTION=mysql` con `DB_DATABASE=YelpMe`, sin contraseñas ni llaves.
- [x] `config/app.php`: `timezone` configurable con `APP_TIMEZONE`, por defecto `America/Mexico_City`.
- [x] Limpiar la caché de configuración (`php artisan config:clear`).

## Criterios de aceptación
- `php artisan config:show app` muestra `name=YelpMe`, `locale=es`, `timezone=America/Mexico_City`.
- `php artisan config:show session` y `config:show cache` muestran el driver `file`.
- `php artisan db:show` conecta con `YelpMe`.
