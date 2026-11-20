# 019 — Tarea: Sesión de la app

> Plan: `003-plan-backend-autenticacion-2026-11-19.md` · Rama: `feature/backend-auth` · Estado: ✅ Completada

## Objetivo
Login de usuarios de la app por correo o teléfono, consultar la sesión y cerrarla.

## Endpoints

| Método | Ruta | Middleware | Acción |
|---|---|---|---|
| POST | `/api/login` | `throttle:acceso` | `SesionController@store` |
| GET | `/api/sesion` | `auth:sanctum`, `cuenta.activa` | `SesionController@show` |
| DELETE | `/api/sesion` | `auth:sanctum`, `cuenta.activa` | `SesionController@destroy` |

`show` y `destroy` sirven a cualquier rol (app y panel).

## Pasos

### 1. `app/Support/Credenciales.php` (compartido con el panel)
- `verificar(['correo'|'telefono'], $password)`: busca con `DB::table`, compara con `Hash::check`. Falla → `abort(401, 'Correo, teléfono o contraseña incorrectos.')`.
- Mismo mensaje exista o no la cuenta. Sin cuenta se compara contra `HASH_FICTICIO` (bcrypt cost 12 real): tiempo similar, no revela qué correos existen.
- `HASH_FICTICIO` debe ser bcrypt válido: Laravel lanza excepción con hashes de otro algoritmo. Se generó con `password_hash` y se pegó con el editor (el shell expande `$`).
- `exigirAcceso($usuario, $rol, $mensajeRol)`: rol distinto → 403 con mensaje que dice dónde entrar; `activo = 0` → 403 "Esta cuenta está deshabilitada."
- Orden: primero contraseña, luego rol y estado. Sin contraseña correcta no se revela nada de la cuenta.

### 2. `SesionController@store` — `POST /api/login`
1. Normalizar si vienen: correo a minúsculas sin espacios; teléfono solo dígitos (`235-100-0001` → `2351000001`).
2. Validar: `correo` (`required_without:telefono`, email) · `telefono` (`required_without:correo`, digits:10) · `password` · `fcm_token` opcional.
3. `Credenciales::verificar` + `exigirAcceso(..., 'usuario', 'Esta cuenta es de administrador. Inicia sesión en el panel web.')`.
4. Si viene `fcm_token`, guardarlo (`DB::table`). Preparado para notificaciones (plan FCM).
5. Token `createToken('app')`. Respuesta: `{ token, usuario }`.
- Suspendido sí entra: `usuario.suspendido = true`; la app bloquea pedir servicios.

### 3. `SesionController@show` — `GET /api/sesion`
`PresentadorUsuario::porId(user()->id)`. La app lo usa al abrir para saber si el token sigue vivo y si la cuenta está suspendida.

### 4. `SesionController@destroy` — `DELETE /api/sesion`
- Borra solo `currentAccessToken()`: otros dispositivos siguen con sesión.
- Rol `usuario`: `fcm_token = NULL`. El dispositivo deja de recibir avisos.

## Verificación
Script `php artisan tinker <archivo>` (fuera del repo; con `--execute` las comillas del shell rompían el código):

| Caso | Resultado |
|---|---|
| Sin datos | 422 |
| Contraseña mala / cuenta inexistente | 401, mismo mensaje |
| Admin en `/api/login` | 403 "Esta cuenta es de administrador…" |
| Correo con espacio y mayúsculas + `fcm_token` | 200, FCM guardado |
| Teléfono con guiones | 200 |
| `GET /api/sesion` | 200, `suspendido: false` |
| Luis (suspendido) | 200, `suspendido: true` |
| José deshabilitado | 403 |
| `DELETE /api/sesion` | 200, FCM en NULL |
| Token cerrado | 401 |
| Otro token del mismo usuario | 200 (sigue válido) |
| 6º intento en un minuto | 429 |

Tokens de prueba borrados; `activo` restaurado.

## Resultado
Commit: `feat(backend): agrega inicio, consulta y cierre de sesión de la app`.
