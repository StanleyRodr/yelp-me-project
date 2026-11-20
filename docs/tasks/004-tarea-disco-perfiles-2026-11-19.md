# 004 — Tarea: Disco de fotos de perfil

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Pendiente

## Objetivo
Tener un disco dedicado para las fotos de perfil, accesible por URL pública.

## Subtareas
- [ ] Agregar el disco `perfiles` en `config/filesystems.php`: driver `local`, raíz `storage/app/public/perfiles`, URL `APP_URL/storage/perfiles`, visibilidad pública.
- [ ] Ejecutar `php artisan storage:link`.
- [ ] Probar guardando un archivo con `Storage::disk('perfiles')` y obteniendo su URL; borrar el archivo de prueba.

## Criterios de aceptación
- `public/storage` apunta a `storage/app/public`.
- `Storage::disk('perfiles')->url('x.jpg')` devuelve `http://localhost:8000/storage/perfiles/x.jpg`.
- Las fotos subidas no entran al repositorio (`storage/app/public` ignorado).
