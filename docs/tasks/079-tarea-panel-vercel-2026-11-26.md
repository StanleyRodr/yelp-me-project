# 079 — Tarea: Panel en Vercel

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ⬜ Pendiente

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

## Verificación
Consola del navegador sin errores de CORS ni *mixed content*.

## Resultado
Commit: `chore(frontend): agrega configuración de Vercel`.
