# 013 — Plan: Despliegue

> Fecha: 2026-11-26 · Rama: `feature/despliegue` (desde `main`, tras fusionar el plan 012)
> Referencia: `docs/plans/principal-plan.md` (secciones 10.6 y 13) · planes 001–012

## Objetivo

Publicar el MVP completo: panel en Vercel, API y MySQL en Railway, y la app Android como APK firmado en GitHub Releases apuntando a la API en la nube. Al terminar, los 11 criterios de aceptación se pueden demostrar sin la computadora de desarrollo.

## Punto de partida
- MVP completo y probado en local (planes 001–012).
- `VITE_API_URL` (panel) y `BuildConfig.API_URL` (app) ya existen; CORS por `FRONTEND_URL`.
- Fotos en el disco `perfiles` (`storage/app/public`), esquema en `database/script-bd.sql`, seeder de demo.
- La persona dueña del repo ya usó Vercel y Railway antes.

## Decisiones
- **Railway:** un proyecto con dos servicios: `mysql` (plantilla MySQL) y `api` (repo de GitHub, raíz `backend/`). Red privada entre ellos (`MYSQL*` como variables de referencia). Volumen montado en `storage/app/public` del servicio `api`.
- **Vercel:** proyecto con raíz `frontend/`, preset Vite, `VITE_API_URL` de producción. `vercel.json` con *rewrite* a `index.html` (rutas del router en modo history).
- **Despliegue automático** al fusionar en `main` (integración GitHub de ambos). Ramas de PR: Vercel crea *preview*; Railway solo `main`.
- **Esquema:** `script-bd.sql` se aplica a mano **una vez** con el cliente `mysql` vía `railway connect`. Luego `php artisan db:seed` (demo) con `ADMIN_PASSWORD` de variable de entorno.
- **Subdominios gratuitos:** `*.vercel.app` y `*.up.railway.app` (nombres exactos al crearlos; se anotan en la tarea 081).
- **Correo:** SMTP real (cuenta elegida por la persona; p. ej. Gmail con contraseña de aplicación) en variables de Railway.
- **Firebase:** `FIREBASE_CREDENTIALS_BASE64` en Railway.
- **APK:** keystore creado por la persona, guardado fuera del repo; `mobile/keystore.properties` ignorado; release firmado `v1.0.0` publicado con `gh release create`.
- Pasos marcados **(Tú)** requieren la cuenta de la persona (Railway, Vercel, Firebase, SMTP, keystore). Claude prepara archivos y comandos; no introduce contraseñas.

---

## Tareas

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 076 | [Backend listo para producción](../tasks/076-tarea-backend-produccion-2026-11-26.md) | Proxies/HTTPS, CORS, contraseñas por variable, correo con Brevo (API), extensiones para Railpack. | ✅ |
| 077 | [MySQL en Railway](../tasks/077-tarea-mysql-railway-2026-11-26.md) | Servicio MySQL (plan Free), esquema por túnel SSH; demo en la 078. | ✅ |
| 078 | [API en Railway](../tasks/078-tarea-api-railway-2026-11-26.md) | Servicio desde GitHub, variables, volumen, dominio. | ⬜ |
| 079 | [Panel en Vercel](../tasks/079-tarea-panel-vercel-2026-11-26.md) | Proyecto, variables, rewrites y CORS. | ⬜ |
| 080 | [APK de release](../tasks/080-tarea-apk-release-2026-11-26.md) | Firma, URL de producción, versión y GitHub Release. | ⬜ |
| 081 | [Verificación en la nube](../tasks/081-tarea-verificacion-nube-2026-11-26.md) | Los 11 criterios en producción; documentación final. | ⬜ |

---

## Criterios de terminado

- [ ] `https://<api>.up.railway.app/api/ping` responde; panel en `https://<panel>.vercel.app` inicia sesión.
- [ ] Fotos sobreviven a un redespliegue (volumen).
- [ ] APK `v1.0.0` instalado en un teléfono físico usa la API en la nube (datos móviles, sin la PC).
- [ ] Recuperación de contraseña llega por correo real; push funciona en producción.
- [ ] Ningún secreto en git; variables documentadas en `README` (sin valores).
- [ ] Commits por tarea, sin atribución; PR a `main`.
