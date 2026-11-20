# 005 — Tarea: Base de la API (rutas, JSON y CORS)

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Estructura común de la API: formato JSON único, errores en español, validación traducida, CORS para frontend, ruta de prueba.

## Formato estándar

```json
{ "ok": true, "mensaje": "...", "datos": { } }
```

Error: `ok: false`, `datos: null`. En 422 se agrega `errores` por campo.

## Pasos

### 1. Helper en `Controller` base
`app/Http/Controllers/Controller.php`:

```php
protected function respuesta(mixed $datos = null, string $mensaje = 'OK', int $codigo = 200): JsonResponse
{
    return response()->json([
        'ok' => $codigo < 400,
        'mensaje' => $mensaje,
        'datos' => $datos,
    ], $codigo);
}
```

Todo controlador responde con `$this->respuesta(...)`. No armar JSON a mano.

### 2. Carpeta y controlador de prueba
- Crear `app/Http/Controllers/Api/`. Controladores de la API viven aquí.
- `Api/PingController.php` con `index()`: devuelve nombre de la app y hora.

### 3. Rutas
`routes/api.php`: borrar ruta de ejemplo `/user`. Dejar:

```php
Route::get('/ping', [PingController::class, 'index']);
```

### 4. Errores JSON en `bootstrap/app.php`
En `withExceptions`:

1. `shouldRenderJsonWhen`: JSON si ruta es `api/*` o cliente pide JSON.
2. `render`: para `api/*`, mapear excepción a código y mensaje en español:

| Excepción | Código | Mensaje |
|---|---|---|
| `ValidationException` | 422 | Los datos enviados no son válidos. (+ `errores`) |
| `AuthenticationException` | 401 | No autenticado. Inicia sesión. |
| `AuthorizationException` / `AccessDeniedHttpException` | 403 | No tienes permiso para realizar esta acción. |
| `ModelNotFoundException` / `NotFoundHttpException` | 404 | Recurso no encontrado. |
| `MethodNotAllowedHttpException` | 405 | Método no permitido. |
| `ThrottleRequestsException` | 429 | Demasiados intentos. Intenta más tarde. |
| Otro `HttpExceptionInterface` | su código | su mensaje |
| Cualquier otro | 500 | Detalle solo si `APP_DEBUG=true` |

### 5. 401 sin redirección a `login`
Problema: invitado sin `Accept: application/json` (Retrofit por defecto) daba 500 `Route [login] not defined.`
Fix en `withMiddleware`:

```php
$middleware->redirectGuestsTo(fn () => null);
```

### 6. Validación en español
- Crear `lang/es/validation.php` con reglas usadas: `required`, `email`, `unique`, `min`, `max`, `in`, `exists`, `image`, `mimes`, `numeric`, `between`, etc.
- Bloque `attributes`: nombres legibles (`telefono` → teléfono, `password` → contraseña).
- Regla nueva no traducida: agregarla aquí. Mientras, cae a inglés (fallback `en`).
- Paquete `laravel-lang/common` no se instaló (no autorizado). Archivo manual basta para MVP.

### 7. CORS

```bash
php artisan config:publish cors
```

En `config/cors.php`:

```php
'allowed_origins' => [env('FRONTEND_URL', 'http://localhost:5173')],
```

- Solo el frontend. Android no usa CORS.
- Producción: cambiar `FRONTEND_URL` en `.env`.

### 8. Documentar
README del backend: sección "Formato de respuesta" con tabla de códigos.

## Verificación
Con kernel HTTP en `tinker` (sin levantar servidor), header `Accept: text/html` para simular Retrofit:

| Petición | Esperado |
|---|---|
| `GET /api/ping` | 200, `ok: true`, `Access-Control-Allow-Origin: http://localhost:5173` |
| `GET /api/no-existe` | 404 JSON |
| `POST /api/ping` | 405 JSON |
| Ruta `auth:sanctum` sin token | 401 JSON |
| Validación fallida | 422 con `errores` en español |

## Resultado
Todo verificado. Commit: `feat(backend): agrega base de la API con respuestas JSON en español`.
