# 079 — Tarea: Panel en Vercel

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ✅ Hecha

## Objetivo
Panel Vue en Vercel contra la API de Railway.

## Pasos

### 1. `frontend/vercel.json`
```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```
(rutas del router en modo history; los assets estáticos se sirven antes del rewrite.)

### 2. (Tú) Proyecto en Vercel
- Add New → Project → importar el repositorio.
- Root Directory: `frontend`. Framework: Vite. Build `npm run build`, output `dist`.
- Environment Variables (Production y Preview): `VITE_API_URL=https://<api>.up.railway.app/api`.
- Deploy. Anotar el dominio (`https://<panel>.vercel.app`).

### 3. CORS
En Railway (`api`): `FRONTEND_URL=https://<panel>.vercel.app` (si se quieren previews, agregar patrón de la tarea 076). Redesplegar `api`.

### 4. Comprobar
- Entrar con `admin@yelpme.com` y `ADMIN_PASSWORD`.
- Recargar en `/solicitudes/…` (rewrite) → no da 404.
- Fotos de usuarios con `https`. Mapa carga teselas (sin contenido mixto).
- Recuperar contraseña → el correo llega.
- Tema oscuro.

## Cómo se hizo (2026-11-27)
- `frontend/vercel.json` con el rewrite a `index.html`.
- **(Tú)** Proyecto Vercel `yelp-me-project`, raíz `frontend`, `VITE_API_URL=https://api-production-9782e676.up.railway.app/api`. Production Branch temporal `feature/despliegue` (la de `main` no tenía `vercel.json` → `/login` daba 404); volver a `main` al fusionar.
- Dominio: `https://yelp-me-project.vercel.app` → `FRONTEND_URL` en Railway (`railway variables --service api --set ...`).
- La pista "Demo: admin… / Admin123*" del login solo sale en desarrollo (`import.meta.env.DEV`).

## Verificación
- `/login`, `/solicitudes/3`, `/usuarios`, `/recuperar` responden 200 directo (rewrite).
- Preflight CORS desde el dominio de Vercel → `access-control-allow-origin: https://yelp-me-project.vercel.app`.
- En Chrome: login con datos falsos muestra el mensaje de la API ("Correo, teléfono o contraseña incorrectos."); consola sin errores de CORS ni *mixed content*.
- Pendiente para la 081: login real del admin (con `ADMIN_PASSWORD`), mapa y correo de recuperación.

## Resultado
Commit: `chore(frontend): agrega configuración de Vercel` (`1b95895`) + documentación.
