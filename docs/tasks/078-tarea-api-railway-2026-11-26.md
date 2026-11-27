# 078 — Tarea: API en Railway

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ✅ Hecha

## Objetivo
Servicio `api` de Railway desde GitHub, con variables, volumen para fotos y dominio público.

## Pasos

### 1. (Tú) Servicio
- En el proyecto YelpMe: Add → GitHub Repo → este repositorio.
- Settings → Root Directory: `backend`. Branch: `main`. Nombre: `api`.
- Primero desplegar la rama `feature/despliegue` (Settings → branch) para probar; volver a `main` tras fusionar.

### 2. Variables (Tú, en Variables → Raw Editor)
| Variable | Valor |
|---|---|
| `APP_NAME` | YelpMe |
| `APP_ENV` / `APP_DEBUG` | production / false |
| `APP_KEY` | `php artisan key:generate --show` (local) |
| `APP_URL` | `https://<dominio de api>` (paso 4) |
| `FRONTEND_URL` | `https://<panel>.vercel.app` (tarea 079) |
| `DB_CONNECTION` | mysql |
| `DB_HOST` / `DB_PORT` / `DB_USERNAME` / `DB_PASSWORD` | referencias `${{MySQL.MYSQLHOST}}`, `${{MySQL.MYSQLPORT}}`, `${{MySQL.MYSQLUSER}}`, `${{MySQL.MYSQLPASSWORD}}` |
| `DB_DATABASE` | YelpMe |
| `SESSION_DRIVER` / `CACHE_STORE` | file / file |
| `FILESYSTEM_DISK` | local |
| `LOG_CHANNEL` | stderr |
| `MAIL_*` | SMTP elegido (host, puerto 587, usuario, contraseña de aplicación, `MAIL_FROM_ADDRESS`) |
| `ADMIN_PASSWORD` | contraseña fuerte (no `Admin123*`) |
| `FIREBASE_CREDENTIALS_BASE64` | `base64 -w0 credenciales.json` |

### 3. Volumen
Add Volume al servicio `api`, mount path `/app/storage/app/public` (confirmar la ruta de trabajo del contenedor en los logs de build; ajustar si no es `/app`).

### 4. Dominio
Settings → Networking → Generate Domain → p. ej. `yelpme-api.up.railway.app`. Actualizar `APP_URL`.

### 5. Datos de demo
```bash
railway run --service api php artisan db:seed
```
(`ADMIN_PASSWORD` cambia la contraseña de los admins.)

### 6. Comprobar
```bash
curl https://<api>/api/ping
curl -X POST https://<api>/api/login -H "Accept: application/json" -d "correo=carlos@correo.com&password=Usuario123*"
```
Subir una foto (curl multipart o desde el panel) → la URL `https://…/storage/perfiles/…` responde 200. Redesplegar → la foto sigue.

## Cómo se hizo (2026-11-27)
- `railway add --service api --repo StanleyRodr/yelp-me-project --branch feature/despliegue` con las variables no secretas (`APP_KEY` generada con `key:generate --show`, `DB_*` como referencias `${{MySQL.*}}`, `LOG_CHANNEL=stderr`, `RAILPACK_SKIP_MIGRATIONS=true`, `MAIL_MAILER=brevo`).
- `railway domain --service api` → `https://api-production-9782e676.up.railway.app` (en `APP_URL`).
- **(Tú) en el panel:** Root Directory `/backend`, Healthcheck `/api/ping` (el CLI `environment edit` no los aplicó) y secretos: `BREVO_API_KEY`, `MAIL_FROM_ADDRESS`, `ADMIN_PASSWORD`, `DEMO_PASSWORD`, `FIREBASE_CREDENTIALS_BASE64` (PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("<ruta absoluta>"))`, con ruta **absoluta**).
- Railpack: PHP 8.2, FrankenPHP, `sodium` instalada, config en caché.
- Demo: `railway ssh --service api -- php artisan db:seed --force`.
- **Volumen:** el plan Free da un solo volumen (0.5 GB) y lo usa MySQL → sin volumen en `api`, las fotos de perfil se pierden al redesplegar. Con Hobby: volumen en `/app/storage/app/public`.

## Verificación
- `/api/ping` 200 con el dominio de Railway; 7 usuarios y 11 solicitudes de demo.
- `Notificador` = `NotificadorFcm` (credenciales base64 válidas); mailer `brevo`.
- `admin/login` con `Admin123*` → 401 (`ADMIN_PASSWORD` aplicada).
- Pendiente: foto persistente (depende del volumen) y correo real (tarea 081).

## Resultado
Commit: `docs(despliegue): documenta la API en Railway`.
