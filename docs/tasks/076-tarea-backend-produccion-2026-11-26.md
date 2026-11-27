# 076 — Tarea: Backend listo para producción

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ✅ Hecha

## Objetivo
Cambios de código y configuración para que Laravel corra bien detrás del proxy HTTPS de Railway.

## Pasos

### 1. Proxy y HTTPS (`bootstrap/app.php` y `AppServiceProvider`)
- `->withMiddleware(fn ($m) => $m->trustProxies(at: '*'))` (Railway termina TLS).
- `if (app()->isProduction()) URL::forceScheme('https');` → URLs de fotos con `https`.

### 2. CORS (`config/cors.php`)
`allowed_origins` acepta lista separada por comas:
```php
'allowed_origins' => array_map('trim', explode(',', env('FRONTEND_URL', 'http://localhost:5173'))),
```
(producción: dominio de Vercel; opcional `allowed_origins_patterns` para previews `#^https://.*-<equipo>\.vercel\.app$#`).

### 3. Admin de producción
`DatabaseSeeder`: si existe `ADMIN_PASSWORD`, actualiza la contraseña de `admin@yelpme.com` y `laura.admin@yelpme.com` con `Hash::make(env('ADMIN_PASSWORD'))`. Sin variable → deja `Admin123*` (local).
Usuarios de demo: `DEMO_PASSWORD` opcional con la misma idea.

### 4. Firebase
Confirmar que `FIREBASE_CREDENTIALS_BASE64` (tarea 072) funciona sin archivo en disco.

### 5. Config de Railway en el repo
`backend/railway.json` (o el formato vigente de Railway al hacerlo; revisar docs):
- Build: detección automática de PHP/Laravel (Railpack/Nixpacks).
- Pre-deploy: `php artisan storage:link && php artisan config:cache && php artisan route:cache`.
- Healthcheck: `/api/ping`.
Verificar en la documentación actual de Railway qué servidor usa para Laravel (FrankenPHP/Nginx); no usar `php artisan serve` en producción si hay alternativa.

### 6. `.env.example` — sección producción (sin valores)
```
APP_ENV=production
APP_DEBUG=false
APP_URL=https://<api>.up.railway.app
FRONTEND_URL=https://<panel>.vercel.app
DB_HOST=${{MySQL.MYSQLHOST}}  # referencias de Railway
...
MAIL_MAILER=smtp
ADMIN_PASSWORD=
FIREBASE_CREDENTIALS_BASE64=
LOG_CHANNEL=stderr
```

### 7. Comprobar en local con `APP_ENV=production`
```bash
APP_ENV=production APP_DEBUG=false php artisan config:cache && php artisan serve
```
Errores 500 sin detalle; `php artisan config:clear` al terminar.

## Cambios respecto al plan (al hacerla, 2026-11-27)
- **Correo:** Railway bloquea SMTP en Trial/Free/Hobby (solo Pro). Se usa **Brevo por API HTTPS**: `symfony/brevo-mailer` + `symfony/http-client`, mailer `brevo` en `config/mail.php` (`BREVO_API_KEY`), transporte registrado en `AppServiceProvider` (`Mail::extend`).
- **Sin `railway.json`:** Config as Code está obsoleto y los servicios nuevos ya no lo leen. Railpack (Laravel) ya hace `storage:link` y `optimize` al arrancar y sirve con FrankenPHP desde `/app/public`. Healthcheck `/api/ping` se configura en el panel.
- **`RAILPACK_SKIP_MIGRATIONS=true`:** no hay migraciones (esquema en `script-bd.sql`).
- **Extensiones:** `ext-pdo_mysql` y `ext-sodium` en `composer.json` → Railpack las instala.
- **Contraseñas:** `config('app.admin_password')` / `config('app.demo_password')` (funcionan con caché de config). `ADMIN_PASSWORD` también reemplaza la del admin inicial del script aunque la demo ya esté cargada.
- `.env.example`: sección de producción sin valores.

## Verificación
`php artisan test` en verde (108); `git diff` sin secretos.
Local con `APP_ENV=production APP_DEBUG=false`: `/api/ping` 200, CORS acepta el origen de `FRONTEND_URL` (lista con comas), 404 en JSON sin detalles, transporte `BrevoApiTransport` resuelto.

## Resultado
Commit: `chore(backend): prepara la API para producción en Railway` (ver tabla del plan).
