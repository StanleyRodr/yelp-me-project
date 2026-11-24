# 027 — Tarea: Base de solicitudes

> Plan: `005-plan-backend-solicitudes-2026-11-23.md` · Rama: `feature/backend-solicitudes` · Estado: ✅ Completada

## Objetivo
Piezas compartidas del plan: tabla de transiciones, forma pública de la solicitud, strikes con suspensión automática y catálogo de servicios.

## Pasos

### 1. `app/Support/TransicionesSolicitud.php`
Tabla `[rol][conServicios][desde] => destinos`, idéntica a plan 7.2 y a `frontend/src/constantes/transiciones.js`:

| Rol | Servicios | Desde | Destinos |
|---|---|---|---|
| admin | con | Pendiente | Enviado, Rechazado |
| admin | con | Enviado | Terminado |
| admin | sin | Pendiente | Rechazado |
| usuario | con | Pendiente | Cancelado |
| usuario | con | Enviado | Terminado, Cancelado |
| usuario | sin | Pendiente | Terminado, Cancelado |

- `permitidas()`, `permite()`, `exigir()` → 409 "La solicitud está en X: no se puede pasar a Y. Recarga para ver su estado actual."
- Mismo 409 cubre concurrencia: si otro admin ya la cambió, el estado actual no permite la acción.
- `adminPuedeEditarServicios($estado, $conServicios)`: con servicios y en Pendiente o Enviado.
- Si cambia una tabla, cambiar la otra (comentario en ambas).

### 2. `app/Support/PresentadorSolicitud.php`
- `lista(Collection)`: 1 consulta de `solicitud_servicio` + 1 de `usuarios` para toda la lista. Sin consultas por fila.
- `porId($id)`.
- Forma = la del frontend: columnas + `servicios` (ids enteros) + `usuario` `{ id, nombre, apellidos, telefono }` + `admin` / `cerrado_por` `{ id, nombre, apellidos }`.
  - Teléfono solo del solicitante (el admin lo llama). De admins, solo nombre.
- `latitud` / `longitud` como número (MySQL DECIMAL llega como texto). Fechas ISO con `Normalizar::fecha`.

### 3. `ReglasCuenta` (ampliada)
- `suspender($usuarioId, $motivo, $adminId = null)`: no duplica si ya hay una vigente. `null` = automática.
- `darStrike($usuarioId, $adminId, $motivo, $solicitudId = null)`: inserta strike activo; con ≥ 3 activos → `suspender` automática ("Suspensión automática por 3 strikes.").
- Llamar dentro de la transacción de la acción que lo origina. Plan 006 lo reutiliza para reportes manuales.

### 4. `GET /api/servicios` — `ServicioController@index`
- Cualquier rol con token (grupo `auth:sanctum` + `cuenta.activa`).
- Solo activos, por id: `{ id, nombre, icono, color }`. `icono` = Material Symbols.

## Verificación (`tinker`)
- Tabla de transiciones: 8 combinaciones, coinciden con 7.2 (estado final → ninguna).
- Presentador: 11 solicitudes del demo; #3 con `servicios: [1, 2]`, `usuario` con teléfono, `admin` sin teléfono, `enviado_en` ISO. **2 consultas** para toda la lista.
- `darStrike` en transacción revertida: Ana 2 → 3 strikes, suspendida automáticamente (`suspendido_por_id = NULL`); 4º strike no crea otra suspensión.
- `php artisan test`: 37/37.

## Resultado
Commit: `feat(backend): agrega transiciones, presentador de solicitudes y strikes`.
