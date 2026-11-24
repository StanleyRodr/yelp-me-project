# 029 — Tarea: Estado por el usuario

> Plan: `005-plan-backend-solicitudes-2026-11-23.md` · Rama: `feature/backend-solicitudes` · Estado: ✅ Completada

## Objetivo
El usuario cancela (con motivo) o da por terminada su solicitud, solo en las transiciones del plan 7.2.

## Endpoint
`PUT /api/solicitudes/{id}/estado` · rol `usuario`

```json
{ "estado": "Cancelado", "motivo": "Ya se apagó" }
{ "estado": "Terminado" }
```

| Estado actual | Con servicios | Sin servicios |
|---|---|---|
| Pendiente | Cancelado | Cancelado, Terminado |
| Enviado | Cancelado, Terminado | — |
| Final | — | — |

## Pasos

### 1. `Solicitudes::aplicarEstado($solicitud, $hacia, $actorId, $motivo)` (compartido con el panel)
| Destino | Campos |
|---|---|
| Enviado | `admin_id`, `enviado_en` |
| Rechazado | `admin_id`, `cerrado_por_id`, `cerrado_en`, `motivo` |
| Cancelado / Terminado | `cerrado_por_id`, `cerrado_en`, `motivo` (si viene) |

- Al terminar, `admin_id` se conserva: el historial sigue mostrando quién la atendió.
- Solo aplica; la validación de la transición va antes.

### 2. `EstadoSolicitudController@update` (app)
1. Validar: `estado` ∈ {Cancelado, Terminado}; `motivo` `required_if:estado,Cancelado` → "Escribe el motivo de la cancelación."
2. Transacción:
   - `Solicitudes::bloquear($id, usuario)` → ajena 404.
   - Con servicios = tiene filas en `solicitud_servicio`.
   - `TransicionesSolicitud::exigir('usuario', ...)` → 409 si no se permite.
   - `aplicarEstado`.
3. Mensaje: "Solicitud cancelada" / "Solicitud terminada".

### 3. Ruta
Grupo `rol:usuario`: `Route::put('/solicitudes/{id}/estado', ...)->whereNumber('id')`.

## Verificación
Script `tinker` con transacción revertida:

| Caso | Resultado |
|---|---|
| `estado: Enviado` | 422 |
| Cancelar sin motivo | 422 "Escribe el motivo de la cancelación." |
| Terminar Pendiente con servicios | 409 |
| Cancelar ajena | 404 |
| Cancelar Pendiente con motivo (espacios) | 200, motivo limpio, `cerrado_por` = Carlos, `admin: null` |
| Cancelar otra vez | 409 |
| Terminar Enviado | 200, `admin` conservado, `cerrado_por` = Carlos |
| María termina Pendiente sin servicios | 200 |
| Cancelado → Terminado | 409 |
| Admin en esta ruta | 403 |

## Resultado
Commit: `feat(backend): agrega cancelar y terminar solicitudes desde la app`.
