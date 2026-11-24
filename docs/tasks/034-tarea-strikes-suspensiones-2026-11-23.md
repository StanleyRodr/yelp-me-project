# 034 — Tarea: Strikes y suspensiones

> Plan: `006-plan-backend-usuarios-perfil-2026-11-23.md` · Rama: `feature/backend-usuarios` · Estado: ✅ Completada

## Objetivo
Reportar, anular strikes, suspender y rehabilitar desde el panel, conservando todo el historial (plan principal 8).

## Endpoints (rol `admin`)

| Método | Ruta | Controlador | Éxito |
|---|---|---|---|
| POST | `/api/admin/usuarios/{id}/strikes` | `Admin\StrikeController@store` | 201 "Strike registrado [y cuenta suspendida]" |
| PUT | `/api/admin/strikes/{id}/anulacion` | `Admin\AnulacionStrikeController@update` | "Strike anulado" |
| POST | `/api/admin/usuarios/{id}/suspension` | `Admin\SuspensionController@store` | 201 "Cuenta suspendida" |
| DELETE | `/api/admin/usuarios/{id}/suspension` | `Admin\SuspensionController@destroy` | "Cuenta rehabilitada" |

Todos con `motivo` obligatorio (máx. 500), salvo rehabilitar. Responden el **detalle** de la cuenta (`PresentadorCuenta::detalle`): el panel refresca sin otra petición.

## Pasos

### 1. Reportar (`StrikeController@store`)
1. Transacción: `bloquearUsuarioApp` → `exigirAccion('reportar')` (Activo o Suspendido).
2. `ReglasCuenta::darStrike($id, $admin, $motivo)` sin solicitud (manual). Al 3.º activo: suspensión automática.
3. Mensaje dice si la cuenta quedó suspendida en esta acción.
- Ya suspendida: suma strike, no crea otra suspensión.

### 2. Anular (`AnulacionStrikeController@update`)
- Strike con `FOR UPDATE`; inexistente → 404; ya inactivo → 409 "Este strike ya no está activo: no se puede anular."
- `activo = 0` + `anulado_por_id`, `motivo_anulacion`, `anulado_en`.
- **No** levanta una suspensión aplicada: solo rehabilitar lo hace (decisión del plan 006).
- Permitido en cualquier estado de cuenta.

### 3. Suspender (`SuspensionController@store`)
`exigirAccion('suspender')` (solo Activo) → `ReglasCuenta::suspender($id, $motivo, $admin)`: `suspendido_por_id` = admin (manual).

### 4. Rehabilitar (`SuspensionController@destroy`)
- `destroy` = quitar la suspensión **vigente**; la fila queda en el historial.
- `exigirAccion('rehabilitar')` (solo Suspendido) → `ReglasCuenta::rehabilitar`: cierra vigente + strikes activos a 0 **sin** datos de anulación (situación "Reiniciado").

## Verificación
Script `tinker` con transacción revertida:

| Caso | Resultado |
|---|---|
| Reportar sin motivo | 422 |
| Reportar a Ana (2 → 3) | 201 "…y cuenta suspendida", Suspendido, strike sin solicitud |
| Suspender a Ana ya suspendida | 409 "La cuenta está suspendida…" |
| 4.º strike | Suspendido, 4 strikes, sigue 1 suspensión |
| Anular sin motivo / con motivo | 422 / 200, 3 strikes, quién y motivo guardados, sigue Suspendido |
| Anular otra vez / inexistente | 409 / 404 |
| Rehabilitar a Ana | Activo, 0 strikes, 3 reiniciados sin anulación, rehabilitó guardado |
| Rehabilitar otra vez | 409 "La cuenta está activa…" |
| Suspender a Carlos | Suspendido, `suspendido_por` = admin; pedir servicios → 403 |
| Reportar a José deshabilitado | 409 |
| Reportar a un admin | 404 |
| Usuario de la app reporta | 403 |

## Resultado
Commit: `feat(backend): agrega reportes, anulación de strikes, suspensión y rehabilitación`.
