# 030 — Tarea: Solicitudes del panel

> Plan: `005-plan-backend-solicitudes-2026-11-23.md` · Rama: `feature/backend-solicitudes` · Estado: ✅ Completada

## Objetivo
El admin consulta todas las solicitudes, envía ayuda, rechaza (con strike opcional), termina y corrige servicios, sin que dos admins atiendan la misma.

## Endpoints (rol `admin`, prefijo `/api/admin`)

| Método | Ruta | Controlador | Éxito |
|---|---|---|---|
| GET | `/solicitudes?estado=` | `Admin\SolicitudController@index` | 200 |
| GET | `/solicitudes/{id}` | `Admin\SolicitudController@show` | 200 |
| PUT | `/solicitudes/{id}/estado` | `Admin\EstadoSolicitudController@update` | "Ayuda enviada" · "Solicitud rechazada [y strike registrado]" · "Solicitud terminada" |
| PUT | `/solicitudes/{id}/servicios` | `Admin\ServiciosSolicitudController@update` | "Servicios actualizados" |

## Pasos

### 1. `Admin\SolicitudController`
- `index`: todas, filtro `estado` validado, recientes primero.
- `show`: `PresentadorSolicitud::porId` o 404.
- Nombres de ruta `admin.solicitudes.*` (`->names()`) para no chocar con las de la app.

### 2. `Admin\EstadoSolicitudController@update`
Cuerpo: `{ estado: Enviado|Rechazado|Terminado, motivo?, strike? }`.
1. `motivo` `required_if:estado,Rechazado` → "Escribe el motivo del rechazo."
2. Transacción:
   - `Solicitudes::bloquear($id)`: `FOR UPDATE`. Segundo admin espera y luego ve el estado nuevo.
   - `TransicionesSolicitud::exigir('admin', ...)`. Ya cambiado por otro admin → 409.
   - `Solicitudes::aplicarEstado` (Enviado → `admin_id`, `enviado_en`; Rechazado → `admin_id` + cierre).
   - `strike` solo cuenta con `Rechazado`: `ReglasCuenta::darStrike(usuario, admin, 'Broma / mal uso del servicio.', solicitud)` en la **misma** transacción. Tercer strike → suspensión automática.
3. Terminar conserva `admin_id` (quien la despachó); `cerrado_por` = quien la cerró.

### 3. `Admin\ServiciosSolicitudController@update`
- `servicios` required, array, `min:1` ("Selecciona al menos un servicio."), ids activos sin repetir.
- **Error corregido:** se armó con `reglasServicios() + [...]`. En PHP `+` **no sobrescribe** claves existentes: `min:1` se perdía. Solución: `array_merge`.
- Transacción + `bloquear`; `adminPuedeEditarServicios` (con servicios, Pendiente o Enviado). Si no → 409.

### 4. Rutas
Grupo `prefix('admin')->middleware('rol:admin')` en `routes/api.php`.

## Verificación
Script `tinker` con transacción revertida:

| Caso | Resultado |
|---|---|
| Listado / `?estado=Rechazado` | 11 / 5 |
| Detalle / inexistente | 200 / 404 |
| Usuario en el panel | 403 |
| Servicios vacíos / inexistente | 422 "Selecciona al menos un servicio." / 422 |
| Agregar Ambulancia a Pendiente | `[2, 3, 5]` |
| Servicios a solicitud sin servicios | 409 |
| Rechazar sin motivo / estado Cancelado | 422 / 422 |
| Admin 1 envía | 200, `admin` y `enviado_en` |
| Laura envía o rechaza la misma | 409 (concurrencia) |
| Corregir servicios en Enviado | 200 |
| Laura termina | `admin` conservado, `cerrado_por` = Laura |
| Servicios en estado final | 409 |
| Rechazar con strike a Ana (2 → 3) | "…y strike registrado", suspendida, strike ligado a la solicitud |
| Rechazar sin servicios, sin strike | 200 |

Revertido: Ana sigue con 2 strikes. El autoincremento avanza (los `ROLLBACK` no lo regresan): sin efecto.

## Resultado
Commit: `feat(backend): agrega atención de solicitudes desde el panel`.
