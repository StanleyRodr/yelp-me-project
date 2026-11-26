# 038 — Tarea: Sesión contra la API

> Plan: `007-plan-frontend-api-2026-11-23.md` · Rama: `feature/frontend-api` · Estado: ✅ Completada

## Objetivo
Login, cierre y validación de sesión del panel contra la API real; salida automática si el token deja de valer.

## Pasos

### 1. `services/authService.js` (sin mocks)
| Función | Endpoint | Devuelve |
|---|---|---|
| `login(correo, password)` | `POST /api/admin/login` | `{ token, usuario }` |
| `sesion()` | `GET /api/sesion` | usuario |
| `logout()` | `DELETE /api/sesion` | `null` |

Todas con `pedir()`: errores ya llegan como `ErrorApi` con el mensaje de Laravel.

### 2. Store `auth`
- `limpiarSesion()`: olvida la sesión local sin llamar a la API.
- `cerrarSesion()`: intenta `logout`; si el token ya no valía, igual limpia.
- `validarSesion()`: con token guardado, `GET /api/sesion` y refresca `admin`. Sin conexión conserva la sesión local.

### 3. `main.js`
- `registrarSesionExpirada(...)`: único lugar que conoce router y store. Ante un 401: `limpiarSesion()` + `/login?expirada=1`.
- Tras montar: `auth.validarSesion()` (no bloquea la carga).

### 4. Aviso en el login
- `FormLogin`: prop `aviso` (estilo informativo, `role="status"`), se oculta si hay error.
- `LoginView`: con `?expirada=1` → "Tu sesión terminó. Inicia sesión de nuevo para continuar."

## Verificación contra la API real
Servidor de prueba: `DB_DATABASE=YelpMe_pruebas php artisan serve --port=8010` (las variables de entorno ganan al `.env`). Comprobado: un login crea el token en `YelpMe_pruebas` y `YelpMe` queda en 0.

Script fuera del repo (`vite.ssrLoadModule` + `localStorage` simulado + `VITE_API_URL` al 8010):

| Caso | Resultado |
|---|---|
| Contraseña mala | `ErrorApi` 401, mensaje de Laravel; **no** dispara sesión expirada |
| Usuario de la app | 403 "…no tiene acceso al panel…" |
| Vacío | 422, `campo('correo')` con mensaje |
| Login admin (correo con espacios y mayúsculas) | token + usuario |
| `GET /sesion` con token guardado | admin |
| Tras `logout` | 401 + aviso de sesión expirada |
| Token inventado en ruta protegida | 401 + aviso |
| Sin servidor | código 0, "No se pudo conectar…" |

`npm run build` sin errores.

## Resultado
Commit: `feat(frontend): conecta la sesión del panel a la API`.
