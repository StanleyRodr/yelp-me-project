# 001 — Tarea: Configurar el entorno

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Entorno alineado con plan principal. Sin tablas de sesión, caché ni colas en BD. Idioma español. Zona horaria México.

## Pasos

### 1. Editar `backend/.env` (local, no se sube)
Cambiar estas claves:

```env
APP_NAME=YelpMe
APP_URL=http://localhost:8000
APP_TIMEZONE=America/Mexico_City
FRONTEND_URL=http://localhost:5173
APP_LOCALE=es
APP_FALLBACK_LOCALE=en
APP_FAKER_LOCALE=es_MX
SESSION_DRIVER=file
CACHE_STORE=file
QUEUE_CONNECTION=sync
```

- `file` en sesión y caché: BD no tiene tablas `sessions` ni `cache`. `throttle` usa caché, sigue funcionando.
- `sync`: no hay tabla `jobs`.
- Fallback `en`: Laravel trae traducciones en inglés. Regla sin traducir muestra texto legible, no la clave.

### 2. Editar `backend/.env.example` (sí se sube)
- Mismas claves del paso 1.
- Cambiar BD de `sqlite` a MySQL:

```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=YelpMe
DB_USERNAME=root
DB_PASSWORD=
```

- Dejar vacíos `APP_KEY` y `DB_PASSWORD`. Nunca poner secretos aquí.

### 3. Zona horaria configurable
En `config/app.php` reemplazar `'timezone' => 'UTC'` por:

```php
'timezone' => env('APP_TIMEZONE', 'America/Mexico_City'),
```

### 4. Limpiar caché de configuración

```bash
php artisan config:clear
```

## Verificación

```bash
php artisan config:show app      # name=YelpMe, locale=es, timezone=America/Mexico_City
php artisan config:show session  # driver=file
php artisan config:show cache    # default=file
php artisan db:show              # Connection mysql, Database YelpMe
```

## Resultado
Todo verificado. Commit: `chore(backend): configura entorno en español y drivers en archivo`.
