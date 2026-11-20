# 002 — Tarea: Quitar migraciones y factories por defecto

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Esquema vive solo en `database/script-bd.sql`. Laravel no debe crear tablas.

## Por qué
`php artisan migrate` con migraciones por defecto crea `users`, `cache`, `jobs`, `migrations`. Falla en `personal_access_tokens`: tabla ya existe. Deja BD a medias.

## Pasos

### 1. Eliminar migraciones
Borrar los 4 archivos de `backend/database/migrations/`:

- `0001_01_01_000000_create_users_table.php`
- `0001_01_01_000001_create_cache_table.php`
- `0001_01_01_000002_create_jobs_table.php`
- `2026_11_20_011255_create_personal_access_tokens_table.php`

```bash
git rm backend/database/migrations/*.php
```

### 2. Eliminar factory
Borrar `backend/database/factories/UserFactory.php`. Usa columnas de `users` (`name`, `email`), no existen.

### 3. Reescribir `backend/README.md`
Reemplazar README de plantilla Laravel. Contenido mínimo:

1. Requisitos: PHP 8.2+, Composer, MySQL 8.
2. Instalación: `composer install`, copiar `.env.example`, `key:generate`, `storage:link`.
3. BD: ejecutar `mysql -u root -p < ../database/script-bd.sql`.
4. Aviso: **no ejecutar `php artisan migrate`**.
5. Ejecutar: `php artisan serve`.
6. Convenciones: sin modelos excepto `User`, métodos de recurso, rutas en `api.php`.

## Verificación

```bash
ls backend/database/migrations   # vacío
php artisan migrate:status       # "Migration table not found." → correcto, no hay migraciones
```

## Notas
- Cambio de tablas: editar `script-bd.sql`, nunca crear migración.
- `DatabaseSeeder` aún usa `User::factory()`. Se reescribe en tarea 006.

## Resultado
Commit: `chore(backend): elimina migraciones por defecto y documenta el script de BD`.
