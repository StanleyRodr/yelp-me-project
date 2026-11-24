# 007 — Tarea: Limpieza del backend

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Quitar restos de plantilla Laravel que no aplican a un backend solo API sobre `script-bd.sql`.

## Pasos

### 1. Borrar `database/database.sqlite`
Sobra del instalador (BD era SQLite). Proyecto usa MySQL. Archivo ignorado por git: solo borrar local.

```bash
rm backend/database/database.sqlite
```

### 2. Desactivar servicio del disco `local`
`config/filesystems.php`, disco `local`:

```php
'serve' => false,
```

- Con `true`, Laravel registra ruta firmada `/storage/{path}` para disco privado.
- Efecto: foto inexistente en `/storage/perfiles/...` daba 403, no 404.
- Disco `local` no se sirve por URL en este proyecto. Fotos usan disco `perfiles`.

### 3. Quitar plantilla web (Blade + Vite)
Backend solo expone API. Borrar:

- `resources/views/welcome.blade.php`
- `resources/css/`, `resources/js/`
- `package.json`, `vite.config.js`
- `VITE_APP_NAME` de `.env` y `.env.example`

Ajustes por dependencias:

- `routes/web.php`: `/` deja de usar vista `welcome`. Responde JSON con formato estándar y enlace a `/api/ping`.
- `tests/Feature/ExampleTest.php`: probaba `/` con la vista. Reescrito con pruebas reales:
  - `/api/ping` → 200, `ok: true`.
  - `/api/no-existe` → 404, `ok: false`.
  - `/` → 200 JSON.

No borrar `lang/`: tiene validación en español.

### 4. Quitar broker de recuperación de contraseña
`config/auth.php` apuntaba a tabla `password_reset_tokens`. Tabla no existe. Recuperación fuera del MVP.

```php
'passwords' => [],
```

- Comentario en el archivo explica por qué.
- Si un día se implementa: crear tabla en `script-bd.sql` y restaurar broker.

## Verificación

```bash
php artisan config:clear
php artisan test          # 4 passed
php artisan route:list    # /, api/ping, sanctum/csrf-cookie, up. Sin /storage/{path}
```

Con `php artisan serve`:

| Petición | Esperado |
|---|---|
| `GET /storage/perfiles/no-existe.jpg` | 404 |
| `GET /` | 200 JSON |
| `GET /algo` | 404 |

## Resultado
Todo verificado. Commit: `chore(backend): elimina restos de plantilla web y configuración sin uso`.
