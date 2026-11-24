# 007 — Plan: Conectar el panel a la API

> Fecha: 2026-11-23 · Rama: `feature/frontend-api` (desde `main`)
> Referencia: `docs/plans/principal-plan.md` (secciones 4.2, 9 y 10.3) · planes 002 (frontend) y 003–006 (API)

## Objetivo

Reemplazar los datos de ejemplo del panel por la API real: cada *service* del frontend llama a su endpoint con axios, los errores de la API se muestran en la interfaz con sus mensajes en español, la sesión se valida contra el backend y el dashboard se actualiza solo cada 15 segundos. Al terminar, el panel funciona de punta a punta contra Laravel.

**No incluye:** notificaciones push (plan FCM) ni la app Android.

## Punto de partida
- Services del frontend con la misma firma y forma de respuesta que la API (plan 002); cada uno documenta su endpoint.
- Cliente `services/http.js` listo con `VITE_API_URL` y token Bearer, aún sin uso.
- API completa del panel (planes 003, 005 y 006).
- CORS del backend permite `http://localhost:5173`.

## Decisiones
- **Se eliminan los mocks** (`src/mocks/`, `services/simulacion.js`, `services/reglasUsuarios.js`): el panel solo usa la API. Las reglas viven en el backend; en el frontend quedan solo las que deciden qué botones mostrar (`constantes/`).
- **Errores uniformes:** un interceptor de axios convierte toda respuesta de error en `ErrorApi { mensaje, codigo, errores }` con el `mensaje` de la API. Sin conexión → "No se pudo conectar con el servidor…".
- **Sesión:**
  - Login: `POST /api/admin/login`; cierre: `DELETE /api/sesion`.
  - Al abrir el panel con un token guardado, `GET /api/sesion` confirma que sigue válido y refresca los datos del admin.
  - Cualquier **401** (token borrado, cuenta deshabilitada) limpia la sesión y manda a `/login` con aviso.
- **El `adminId` ya no viaja desde el frontend:** el backend lo toma del token.
- **Foto de perfil:** el formulario envía el archivo y el store lo sube por `multipart` a `POST /api/perfil/foto`; la vista previa sigue siendo local.
- **Contraseña:** se envía `nueva_confirmation` (el formulario ya valida que coincida).
- **Dashboard:** recarga cada 15 s sin skeleton (conserva los datos mientras llegan los nuevos); se pausa si la pestaña está oculta. El contador de pendientes de la navbar usa los mismos datos.
- **Concurrencia:** el 409 de la API (otro admin ya actuó) se muestra en el diálogo y recarga la solicitud (ya implementado en el plan 002).

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 037 | [Cliente HTTP](../tasks/037-tarea-cliente-http-2026-11-23.md) | Interceptor de errores, `ErrorApi`, manejo global de 401 y `.env.example`. | ✅ |
| 038 | [Sesión contra la API](../tasks/038-tarea-sesion-api-2026-11-23.md) | Login, cierre y validación de sesión al iniciar. | Pendiente |
| 039 | [Solicitudes contra la API](../tasks/039-tarea-solicitudes-api-2026-11-23.md) | Catálogo, listado, detalle y acciones; recarga periódica del dashboard. | Pendiente |
| 040 | [Usuarios y perfil contra la API](../tasks/040-tarea-usuarios-perfil-api-2026-11-23.md) | Gestión de cuentas, strikes, suspensiones, perfil, foto y contraseña. | Pendiente |
| 041 | [Quitar mocks y verificar](../tasks/041-tarea-quitar-mocks-verificar-2026-11-23.md) | Borrar datos de ejemplo y probar todos los services contra la API real. | Pendiente |

---

## Criterios de terminado

- [ ] Ningún service usa datos de ejemplo; `src/mocks/` no existe.
- [ ] Todos los services funcionan contra la API (probado contra `YelpMe_pruebas`).
- [ ] Los mensajes de error de la API llegan a la interfaz.
- [ ] Un 401 cierra la sesión y lleva a `/login`.
- [ ] La foto de perfil se sube y se ve con la URL del backend.
- [ ] El dashboard se actualiza solo sin mostrar skeleton.
- [ ] `npm run build` sin errores.
- [ ] Commits por tarea, en español, sin coautoría; PR a `main` al terminar.
