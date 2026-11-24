# 017 — Tarea: Base de autenticación

> Plan: `003-plan-backend-autenticacion-2026-11-19.md` · Rama: `feature/backend-auth` · Estado: ✅ Completada

## Objetivo
Piezas comunes de autenticación: middlewares de rol y cuenta activa, límite de intentos, forma pública del usuario y traducciones.

## Pasos

### 1. `app/Support/ReglasCuenta.php`
- `LIMITE_STRIKES = 3`.
- `strikesActivos($id)`: `COUNT` de `strikes` con `activo = 1`.
- `estaSuspendido($id)`: existe suspensión con `fecha_rehabilitacion IS NULL`.
- Solo `DB::table`. La reutiliza la gestión de usuarios en planes siguientes.

### 2. `app/Support/PresentadorUsuario.php`
Forma pública del usuario en toda la API. Nunca `password` ni `fcm_token`.

```json
{ "id", "nombre", "apellidos", "telefono", "correo", "rol",
  "foto_perfil": "URL del disco perfiles o null",
  "activo", "suspendido", "strikes_activos", "limite_strikes",
  "created_at": "ISO 8601 con zona" }
```

- `created_at`: MySQL lo da como `2026-11-19 20:47:45` (sin zona). Se convierte a ISO 8601 (`2026-11-19T20:47:45-06:00`), igual que los mocks del frontend.

### 3. Middlewares (`app/Http/Middleware/`)
- `VerificarRol`: `->middleware('rol:admin')` o `rol:usuario`. Rol del usuario del token. Falla → `abort(403, 'No tienes permiso para realizar esta acción.')`.
- `VerificarCuentaActiva`: `activo = 0` → 403 "Esta cuenta está deshabilitada." Bloquea también tokens emitidos antes de deshabilitar.
- Alias en `bootstrap/app.php` (`withMiddleware`): `rol`, `cuenta.activa`.
- `abort(403, mensaje)` lanza `HttpException`: el render de errores (plan 001) usa ese mensaje.

### 4. Límite de intentos
`AppServiceProvider::boot()`:

```php
RateLimiter::for('acceso', fn (Request $r) =>
    Limit::perMinute(5)->by(mb_strtolower($r->input('correo') ?? $r->input('telefono') ?? '').'|'.$r->ip()));
```

- Clave = identificador + IP: un atacante no bloquea a otros usuarios de la misma red con correos distintos.
- Se usa con `->middleware('throttle:acceso')`. Excedido → 429 "Demasiados intentos…".
- Caché en archivos (`CACHE_STORE=file`).

### 5. Traducciones (`lang/es/validation.php`)
- Regla `required_without`: "Escribe el correo o el teléfono."
- Atributos: `password_confirmation`, `fcm_token`.

## Verificación
Con kernel HTTP en `tinker` y rutas temporales:

| Caso | Resultado |
|---|---|
| Token admin en ruta `rol:admin` | 200 |
| Token usuario en ruta `rol:admin` | 403 "No tienes permiso…" |
| Sin token | 401 |
| Cuenta con `activo = 0` en ruta `cuenta.activa` | 403 "Esta cuenta está deshabilitada." |
| `PresentadorUsuario::porId(7)` (Luis) | `suspendido: true`, `strikes_activos: 3`, sin password |

Tokens de prueba borrados y `activo` restaurado. `php artisan test`: 4/4.

## Resultado
Commit: `feat(backend): agrega middlewares de rol y cuenta activa y límite de intentos`.
