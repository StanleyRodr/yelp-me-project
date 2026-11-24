# 005 — Plan: API de solicitudes y servicios

> Fecha: 2026-11-23 · Rama: `feature/backend-solicitudes` (desde `main`)
> Referencia: `docs/plans/principal-plan.md` (secciones 4, 7 y 8)

## Objetivo

Implementar en la API el ciclo de vida completo de una solicitud de ayuda: el usuario la crea con su ubicación y los servicios que necesita, la consulta, la modifica mientras está pendiente, la cancela o la da por terminada; el admin la consulta, corrige servicios, la envía, la rechaza (con strike opcional) o la termina. El backend valida cada transición del plan principal 7.2 y evita que dos admins atiendan la misma solicitud.

**No incluye:** notificaciones push a contactos y al solicitante (plan FCM), conectar el frontend o Android (planes posteriores) ni la gestión de usuarios y strikes manuales del panel (plan 006).

## Punto de partida
- Tablas `solicitudes`, `solicitud_servicio`, `servicios`, `strikes`, `suspensiones` en `script-bd.sql`.
- Datos de demo: solicitudes en los 5 estados, con y sin servicios; Ana con 2 strikes; Luis suspendido.
- Grupos de rutas `rol:usuario` y `rol:admin` listos (plan 003).
- El frontend ya usa estas reglas con datos de ejemplo (`constantes/transiciones.js`, `services/solicitudesService.js`); la API debe responder con la misma forma.

## Decisiones

### Endpoints

| Método | Ruta | Quién | Controlador | Uso |
|---|---|---|---|---|
| GET | `/api/servicios` | Cualquier rol | `ServicioController@index` | Catálogo activo |
| GET | `/api/solicitudes` | Usuario | `SolicitudController@index` | Mis solicitudes (`?estado=`) |
| POST | `/api/solicitudes` | Usuario | `SolicitudController@store` | Crear |
| GET | `/api/solicitudes/{id}` | Usuario | `SolicitudController@show` | Ver una mía |
| PUT/PATCH | `/api/solicitudes/{id}` | Usuario | `SolicitudController@update` | Modificar (solo `Pendiente`) |
| PUT | `/api/solicitudes/{id}/estado` | Usuario | `EstadoSolicitudController@update` | Cancelar o terminar |
| GET | `/api/admin/solicitudes` | Admin | `Admin\SolicitudController@index` | Todas (`?estado=`) |
| GET | `/api/admin/solicitudes/{id}` | Admin | `Admin\SolicitudController@show` | Detalle |
| PUT | `/api/admin/solicitudes/{id}/estado` | Admin | `Admin\EstadoSolicitudController@update` | Enviar, rechazar o terminar |
| PUT | `/api/admin/solicitudes/{id}/servicios` | Admin | `Admin\ServiciosSolicitudController@update` | Corregir servicios |

- Los cambios de estado son un subrecurso (`/estado`) con `update`: siguen la convención de controladores de recurso y no hay `destroy` (las solicitudes nunca se borran).

### Reglas
- **Transiciones:** una sola tabla en `App\Support\TransicionesSolicitud`, igual al plan principal 7.2 y a `frontend/src/constantes/transiciones.js`. Lo no permitido → 409 con el estado actual.
- **Concurrencia (7.4):** cada cambio se hace en una transacción con `lockForUpdate` sobre la solicitud; si el estado cambió entre que el admin la vio y actuó, 409 "La solicitud ya cambió de estado (Enviado)…".
- **Creación:** `titulo` (máx. 150), `descripcion` opcional, `latitud` (−90 a 90), `longitud` (−180 a 180), `ubicacion_nombre` opcional, `servicios` opcional (ids de servicios activos, sin repetir). Siempre nace `Pendiente`.
- **Cuenta suspendida (8):** puede crear solicitudes **sin** servicios; con servicios → 403 "Tu cuenta está suspendida: solo puedes avisar a tus contactos." Tampoco puede agregar servicios al modificar.
- **Modificar (usuario):** solo en `Pendiente`; título, descripción, ubicación y servicios. En otro estado → 409.
- **Motivo obligatorio** al rechazar y al cancelar (422).
- **Rechazar con strike:** registra el strike ligado a la solicitud; con 3 activos crea la suspensión automática.
- **Corregir servicios (admin):** solo en solicitudes con servicios en `Pendiente` o `Enviado`; mínimo uno.
- **Trazabilidad:** `admin_id` al enviar o rechazar; `cerrado_por_id`, `cerrado_en` y `motivo` al llegar a un estado final; `enviado_en` al enviar.
- **Propiedad:** solicitudes ajenas → 404 en las rutas del usuario.
- **Forma de respuesta** (igual al frontend): columnas de `solicitudes` + `servicios` (ids) + `usuario` `{ id, nombre, apellidos, telefono }` + `admin` y `cerrado_por` `{ id, nombre, apellidos }` o `null`. Listados sin consultas por fila.

### Pruebas
Archivos por actor sobre `YelpMe_pruebas`, buscando solicitudes del demo por título (no por id).

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 027 | [Base de solicitudes](../tasks/027-tarea-base-solicitudes-2026-11-23.md) | Tabla de transiciones, presentador, strikes con suspensión automática y catálogo de servicios. | ✅ |
| 028 | [Solicitudes del usuario](../tasks/028-tarea-solicitudes-usuario-2026-11-23.md) | Crear, listar, ver y modificar mientras está pendiente; regla de suspensión. | ✅ |
| 029 | [Estado por el usuario](../tasks/029-tarea-estado-usuario-2026-11-23.md) | Cancelar con motivo y dar por terminada según la tabla. | Pendiente |
| 030 | [Solicitudes del panel](../tasks/030-tarea-solicitudes-panel-2026-11-23.md) | Listado y detalle, enviar, rechazar con strike, terminar y corregir servicios con control de concurrencia. | Pendiente |
| 031 | [Pruebas y documentación](../tasks/031-tarea-pruebas-solicitudes-2026-11-23.md) | Pruebas de todas las transiciones y endpoints en el README. | Pendiente |

---

## Criterios de terminado

- [ ] Todas las transiciones de la tabla 7.2 funcionan y las demás responden 409.
- [ ] Dos admins no pueden atender la misma solicitud (409 al segundo).
- [ ] Un usuario suspendido no puede pedir ni agregar servicios.
- [ ] Rechazar con strike suma el strike y suspende al tercero.
- [ ] Un usuario no ve ni modifica solicitudes ajenas; un admin no usa las rutas del usuario ni al revés.
- [ ] La forma de respuesta coincide con la que usa el frontend.
- [ ] `php artisan test` pasa sobre `YelpMe_pruebas`.
- [ ] Endpoints documentados en `backend/README.md`.
- [ ] Commits por tarea, en español, sin coautoría; PR a `main` al terminar.
