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

## Ejecutar

```bash
php artisan serve             # http://localhost:8000
```

## Convenciones

- Controladores **sin modelos Eloquent**: acceso a datos con `DB::table(...)`. Única excepción: `App\Models\User`, solo para autenticación con Sanctum.
- Controladores con `index`, `show`, `store`, `update`, `destroy`.
- Rutas en `routes/api.php`.
- Sesiones y caché en archivos (`SESSION_DRIVER=file`, `CACHE_STORE=file`).
