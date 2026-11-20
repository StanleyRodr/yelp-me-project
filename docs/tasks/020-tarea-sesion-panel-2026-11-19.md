# 020 — Tarea: Sesión del panel

> Plan: `003-plan-backend-autenticacion-2026-11-19.md` · Rama: `feature/backend-auth` · Estado: ✅ Completada

## Objetivo
Login del panel web solo para admins y grupos de rutas protegidas por rol, listos para los planes siguientes.

## Pasos

### 1. `app/Http/Controllers/Api/Admin/SesionController@store` — `POST /api/admin/login`
1. Correo a minúsculas sin espacios.
2. Validar `correo` (required, email) y `password`. Solo correo: el panel no usa teléfono.
3. `Credenciales::verificar` + `exigirAcceso(..., 'admin', 'Esta cuenta no tiene acceso al panel de administración.')`.
4. Token `createToken('panel')`. Nombre distinto a `app`: se distingue el origen en `personal_access_tokens`.
5. Consultar y cerrar sesión: mismo `/api/sesion` que la app.

### 2. Rutas (`routes/api.php`)

```
throttle:acceso
  POST /registro, /login, /admin/login
auth:sanctum + cuenta.activa
  GET|DELETE /sesion
  rol:usuario            → contactos y solicitudes propias (planes 004, 005)
  prefix admin, rol:admin → solicitudes y usuarios (planes 005, 006)
```

Grupos vacíos con comentario: los planes siguientes solo agregan rutas adentro.

### 3. Documentación
`backend/README.md`, sección "Autenticación": endpoints, cuerpos, forma de `usuario`, tabla de errores y middlewares para rutas nuevas.

## Verificación
Script `php artisan tinker <archivo>` con rutas temporales dentro de cada zona:

| Caso | Resultado |
|---|---|
| Panel sin datos | 422 |
| Usuario de la app en el panel | 403 "Esta cuenta no tiene acceso al panel…" |
| Contraseña mala | 401 |
| Admin (correo con espacios y mayúsculas) | 200, token `panel` |
| Admin → zona admin / usuario → zona admin | 200 / 403 |
| Admin → zona app / usuario → zona app | 403 / 200 |
| Admin en `GET /api/sesion` | 200 |
| Laura deshabilitada | 403 |
| Admin cierra sesión | 200 |

Tokens borrados; `activo` restaurado. `route:list` muestra los 6 endpoints.

## Resultado
Commit: `feat(backend): agrega inicio de sesión del panel y rutas por rol`.
