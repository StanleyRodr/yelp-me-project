# 004 — Tarea: Disco de fotos de perfil

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Disco `perfiles` para fotos de perfil, servido por URL pública.

## Pasos

### 1. Agregar disco
En `backend/config/filesystems.php`, dentro de `'disks'`, antes de `'s3'`:

```php
'perfiles' => [
    'driver' => 'local',
    'root' => storage_path('app/public/perfiles'),
    'url' => rtrim(env('APP_URL', 'http://localhost'), '/').'/storage/perfiles',
    'visibility' => 'public',
    'throw' => false,
    'report' => false,
],
```

- Raíz dentro de `storage/app/public`: el enlace público la expone.
- URL depende de `APP_URL`. Cambiar `APP_URL` si cambia host o puerto.

### 2. Crear enlace público

```bash
php artisan storage:link
```

Crea `public/storage` → `storage/app/public`. Cada máquina nueva debe correrlo (está en README).

### 3. Probar en `tinker`

```php
$d = Storage::disk('perfiles');
$d->put('prueba.txt', 'ok');
$d->url('prueba.txt');   // http://localhost:8000/storage/perfiles/prueba.txt
$d->delete('prueba.txt');
```

## Uso en controladores (plan futuro)

```php
$ruta = $request->file('foto_perfil')->store('', 'perfiles');   // guarda nombre en usuarios.foto_perfil
$url  = Storage::disk('perfiles')->url($ruta);                    // URL para Glide / web
```

## Verificación
- Archivo de prueba accesible en `public/storage/perfiles/`.
- `git check-ignore backend/storage/app/public/perfiles/x.jpg` → ignorado. Fotos nunca se suben.
- `public/storage` también ignorado.
- `php artisan serve`: archivo existente → 200. Archivo inexistente → 403, no 404. Cae en ruta firmada `/storage/{path}` que Laravel registra para disco `local` (`serve => true`). Esperado, no es error.

## Resultado
Commit: `feat(backend): agrega disco perfiles para fotos de perfil`.
