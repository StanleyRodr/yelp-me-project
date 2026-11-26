# 037 — Tarea: Cliente HTTP

> Plan: `007-plan-frontend-api-2026-11-23.md` · Rama: `feature/frontend-api` · Estado: ✅ Completada

## Objetivo
Un solo cliente para la API: token automático, errores uniformes en español y aviso global de sesión expirada.

## Pasos

### 1. `src/services/errores.js`
- `ErrorApi(mensaje, codigo, errores)`:
  - `mensaje`: el de la API (ya en español).
  - `codigo`: HTTP; `0` = sin conexión.
  - `errores`: por campo en un 422 (`{ campo: ['…'] }`) o `null`.
  - `campo(nombre)`: primer mensaje de un campo.
- `MENSAJE_SIN_CONEXION`: "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."

### 2. `src/services/http.js`
- `axios.create`: `baseURL = VITE_API_URL` (por defecto `http://localhost:8000/api`), `Accept: application/json`, `timeout` 15 s.
- Interceptor de petición: `Authorization: Bearer <token>` desde `localStorage['ym_sesion']` (try/catch).
- Interceptor de respuesta:
  - Sin respuesta → `ErrorApi(MENSAJE_SIN_CONEXION, 0)`.
  - 401 fuera de `/login` y `/admin/login` → `alExpirarSesion()`.
  - Siempre rechaza con `ErrorApi(data.mensaje, status, data.errores)`.
- `registrarSesionExpirada(fn)`: el store de sesión registra qué hacer. `http.js` no importa router ni stores: sin dependencias circulares.
- `pedir(config)`: devuelve solo `datos` de `{ ok, mensaje, datos }`.

### 3. `frontend/.env.example`

```env
VITE_API_URL=http://localhost:8000/api
```

- `.env` real ignorado por git; `.env.example` sí se sube.
- Sin `.env`, se usa el valor por defecto.

### 4. Mocks
`simulacion.js` conserva su `ErrorApi` hasta la tarea 041 (se borra entero). Reemplazo automático no aplicó por saltos de línea CRLF; no se forzó.

## Verificación
- `npm run build` sin errores.
- Comportamiento contra la API real: tarea 041.

## Resultado
Commit: `feat(frontend): agrega cliente HTTP con errores uniformes y sesión expirada`.
