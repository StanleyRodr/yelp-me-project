# 039 — Tarea: Solicitudes contra la API

> Plan: `007-plan-frontend-api-2026-11-23.md` · Rama: `feature/frontend-api` · Estado: ✅ Completada

## Objetivo
Catálogo, listado, detalle y acciones del admin contra la API; el panel se actualiza solo cada 15 s.

## Pasos

### 1. Services (sin mocks)
| Función | Endpoint |
|---|---|
| `serviciosService.listar()` | `GET /api/servicios` |
| `solicitudesService.listar()` | `GET /api/admin/solicitudes` |
| `obtener(id)` | `GET /api/admin/solicitudes/{id}` |
| `enviar(id)` / `terminar(id)` | `PUT …/{id}/estado` `{ estado }` |
| `rechazar(id, { motivo, strike })` | `PUT …/{id}/estado` `{ estado: 'Rechazado', motivo, strike }` |
| `actualizarServicios(id, { servicios })` | `PUT …/{id}/servicios` |

- `adminId` ya no se envía: la API lo toma del token.

### 2. Store `solicitudes`
- `cargar({ silenciosa })`:
  - normal: `cargando = true` (skeleton si no hay datos), error visible.
  - silenciosa: sin `cargando`; si falla, conserva datos y no muestra error.
- `refrescar()` = carga silenciosa.
- `actualizadoEn`: hora de la última lista recibida.
- Acciones sin `adminId`; sin import de `auth`.

### 3. Recarga periódica
- `composables/useConsultaPeriodica(fn, 15000)`: intervalo; se pausa con la pestaña oculta (`visibilitychange`) y al volver consulta de inmediato; se limpia al desmontar.
- En `AppShell` (no en el dashboard): siempre montado con sesión → una sola consulta mantiene navbar (contador), dashboard y listado, que comparten el store.
- Dashboard: "Se actualiza solo cada 15 segundos. Última actualización: hh:mm:ss" (`utils/formato.hora`).

### 4. Sin cambios necesarios
- Vistas y modales ya usaban `e.message` y `e.codigo === 409` (plan 002): con `ErrorApi` funcionan igual.

## Verificación contra la API real
`YelpMe_pruebas` recreada con `bd:pruebas`; servidor en 8010. Script fuera del repo:

| Caso | Resultado |
|---|---|
| Catálogo | 6 servicios, íconos |
| Listado | 11; forma del frontend; `latitud` numérica |
| Botones de `accionesPermitidas` en Pendiente con servicios | enviar, editar servicios, rechazar (lo que acepta la API) |
| Servicios vacíos | 422 "Selecciona al menos un servicio." |
| Agregar Ambulancia | `[2, 3, 5]` |
| Enviar / reenviar | Enviado por Administrador / 409 con "Recarga…" |
| Terminar | Terminado |
| Rechazar sin motivo / con strike | 422 "Escribe el motivo del rechazo." / Rechazado |
| Detalle tras cierre / inexistente | `cerrado_por` / 404 |
| `store.cargar` | 11 + `actualizadoEn` |
| `store.refrescar` | sin `cargando`; con token inválido conserva datos y sin error |

`npm run build` sin errores.

## Resultado
Commit: `feat(frontend): conecta solicitudes a la API con recarga periódica`.
