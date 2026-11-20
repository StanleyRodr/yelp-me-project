# 005 — Tarea: Base de la API (rutas, JSON y CORS)

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: Pendiente

## Objetivo
Dejar la estructura común de la API: formato de respuesta estándar, errores en JSON y en español, CORS para el frontend y una ruta de verificación.

## Subtareas
- [ ] Quitar la ruta de ejemplo `/user` de `routes/api.php`.
- [ ] Crear `app/Http/Controllers/Api/` y un helper de respuesta en el `Controller` base (`respuesta($datos, $mensaje, $codigo)`), con formato `{ "ok", "mensaje", "datos" }`.
- [ ] `GET /api/ping` (pública) que responda con ese formato.
- [ ] En `bootstrap/app.php`, para `api/*`, responder siempre JSON con el mismo formato: 401 (no autenticado), 403, 404, 405, 422 (con `errores` por campo) y 500, con mensajes en español.
- [ ] Publicar `config/cors.php` y permitir el origen del frontend (`FRONTEND_URL`, por defecto `http://localhost:5173`).
- [ ] Documentar el formato de respuesta en el README del backend.

## Criterios de aceptación
- `GET /api/ping` → 200 `{ "ok": true, "mensaje": "API Yelp Me funcionando", "datos": {...} }`.
- `GET /api/no-existe` → 404 JSON con el formato estándar.
- Una ruta protegida sin token → 401 JSON (no redirección a `login`).
