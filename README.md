# YelpMe

Sistema de contactos de confianza y solicitudes de ayuda. La persona usuaria pide ayuda desde Android (con GPS, domicilio o un punto en el mapa) y sus contactos reciben un aviso; un administrador atiende las solicitudes desde un panel web.

> **Prototipo académico (MVP).** No se conecta con servicios reales de emergencia: las "llamadas" a policía, ambulancia, etc. son simuladas.

## En línea

| Parte | Dónde |
|---|---|
| Panel del administrador | https://yelp-me-project.vercel.app |
| API REST | https://api-production-9782e676.up.railway.app/api/ping |
| App Android (APK) | [Release v1.0.0](https://github.com/StanleyRodr/yelp-me-project/releases/tag/v1.0.0) |

Las contraseñas de producción (admin y cuentas de demo) no están en el repositorio.

## Arquitectura

```
 App Android (Java)  ──┐
                       ├──>  API Laravel (Railway)  ──>  MySQL (Railway)
 Panel Vue (Vercel)  ──┘          │
                                  ├──>  Firebase Cloud Messaging (push)
                                  └──>  Brevo (correo de recuperación)
```

| Carpeta | Stack | Rol |
|---|---|---|
| `frontend/` | Vue 3, Vite, Pinia, Bootstrap 5, Leaflet | Panel web del administrador |
| `backend/` | PHP Laravel 12, Sanctum, MySQL | API REST compartida |
| `mobile/` | Android (Java), Retrofit, osmdroid, FCM | App de la persona usuaria |
| `database/` | `script-bd.sql` | Esquema oficial (sin migraciones) |
| `docs/` | Planes y tareas | Reglas de negocio en `docs/plans/principal-plan.md` |

## Correr en local

Requisitos: PHP 8.2+ con `pdo_mysql` y `sodium`, Composer, MySQL 8 (p. ej. XAMPP), Node 20+, Android Studio.

```bash
# Base de datos
mysql -u root < database/script-bd.sql

# API (http://localhost:8000)
cd backend
cp .env.example .env && composer install && php artisan key:generate
php artisan db:seed            # datos de demo (Admin123* / Usuario123*)
php artisan storage:link
php artisan serve

# Panel (http://localhost:5173)
cd frontend
cp .env.example .env && npm install && npm run dev

# App: abrir mobile/ en Android Studio y ejecutar "app" en el emulador.
# Si el emulador no alcanza 10.0.2.2: adb reverse tcp:8000 tcp:8000
# y api.url=http://127.0.0.1:8000/api/ en mobile/local.properties.
```

Detalle de cada parte: `backend/README.md`, `mobile/README.md` y `CLAUDE.md`.

## Despliegue

| Pieza | Servicio | Cómo |
|---|---|---|
| MySQL | Railway, servicio `MySQL` | Esquema aplicado una vez por túnel SSH (`railway connect MySQL --tunnel-only`). |
| API | Railway, servicio `api` | Repo de GitHub, raíz `/backend`, Railpack (PHP + FrankenPHP), healthcheck `/api/ping`. Demo: `railway ssh --service api -- php artisan db:seed --force`. |
| Panel | Vercel, raíz `frontend` | Preset Vite; `frontend/vercel.json` reescribe las rutas a `index.html`. |
| APK | GitHub Releases | `./gradlew assembleRelease` firmado con `mobile/keystore.properties` (fuera de git). |

Railway y Vercel despliegan solos al hacer push a la rama de producción (`main`).

### Variables (sin valores)

**API (Railway):** `APP_NAME`, `APP_ENV=production`, `APP_DEBUG=false`, `APP_KEY`, `APP_URL`, `APP_TIMEZONE`, `APP_LOCALE`, `FRONTEND_URL`, `DB_CONNECTION`, `DB_HOST`, `DB_PORT`, `DB_DATABASE=YelpMe`, `DB_USERNAME`, `DB_PASSWORD` (referencias `${{MySQL.*}}`), `SESSION_DRIVER=file`, `CACHE_STORE=file`, `QUEUE_CONNECTION=sync`, `LOG_CHANNEL=stderr`, `RAILPACK_SKIP_MIGRATIONS=true`, `MAIL_MAILER=brevo`, `BREVO_API_KEY`, `MAIL_FROM_ADDRESS`, `MAIL_FROM_NAME`, `ADMIN_PASSWORD`, `DEMO_PASSWORD`, `FIREBASE_CREDENTIALS_BASE64`.

**Panel (Vercel):** `VITE_API_URL`.

**App:** `mobile/keystore.properties` (firma) y `mobile/app/google-services.json` (push), ambos fuera de git.

### Respaldo de la base

Con el túnel abierto (`railway connect MySQL --tunnel-only -P 3407`), usar `mysqldump` de MySQL 8 o MySQL Workbench contra `127.0.0.1:3407`. El cliente de XAMPP (MariaDB) no soporta la autenticación `caching_sha2_password` de MySQL 8.
