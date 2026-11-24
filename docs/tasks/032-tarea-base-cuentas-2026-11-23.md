# 032 — Tarea: Base de cuentas

> Plan: `006-plan-backend-usuarios-perfil-2026-11-23.md` · Rama: `feature/backend-usuarios` · Estado: ✅ Completada

## Objetivo
Forma de los usuarios en el panel (resumen y detalle, sin N+1), estado de cuenta y acciones permitidas por estado.

## Pasos

### 1. `PresentadorUsuario` dividido
- `base($u)`: solo columnas de `usuarios` (+ `deshabilitado_en`). Sin consultas.
- `datos($u)`: `base` + `suspendido`, `strikes_activos`, `limite_strikes` (2 consultas). Lo usan sesión y registro, igual que antes.

### 2. `ReglasCuenta` (ampliada)
- `estadoCuenta($u, $suspendido = null)`: Deshabilitado > Suspendido > Activo. `$suspendido` precalculado evita consulta en listados.
- `ACCIONES` por estado, igual a `frontend/src/constantes/cuentas.js`:

| Cuenta | Acciones |
|---|---|
| Activo | reportar, suspender, deshabilitar |
| Suspendido | rehabilitar, reportar, deshabilitar |
| Deshabilitado | reactivar |

- `exigirAccion($u, $accion)` → 409 "La cuenta está X: no se puede Y. Recarga para ver su estado actual."
- `rehabilitar($usuarioId, $adminId)`: cierra la suspensión vigente (`rehabilitado_por_id`, `fecha_rehabilitacion`) y strikes activos → `activo = 0` **sin** datos de anulación (situación "Reiniciado" en el frontend).

### 3. `app/Support/PresentadorCuenta.php`
- `resumenes(Collection)`: 2 consultas para toda la lista (`COUNT` de strikes activos agrupado; `COUNT` y `SUM(fecha_rehabilitacion IS NULL)` de suspensiones agrupado).
  - Campos: `base` + `suspendido`, `strikes_activos`, `limite_strikes`, `estado_cuenta`, `veces_suspendido`.
- `detalle($id)`: solo rol `usuario` (admin → null → 404 en el controlador).
  - `suspension_vigente`, `solicitudes` (`id, titulo, estado, servicios, created_at`), `strikes` (con `admin_nombre`, `anulado_por_nombre`), `suspensiones` (con `suspendido_por_nombre`, `rehabilitado_por_nombre`).
  - Nombres de admins en una sola consulta. Más recientes primero.
- Misma forma que `frontend/src/services/usuariosService.js`.

## Verificación (`tinker`)
- Listado de 5 usuarios de la app: **2 consultas**.

| Usuario | Cuenta | Strikes | Veces suspendido |
|---|---|---|---|
| Ana | Activo | 2 | 0 |
| Luis | Suspendido | 3 | 2 |
| Carlos, José, María | Activo | 0 | 0 |

- Detalle de Luis: vigente "Suspensión automática por 3 strikes.", 3 solicitudes, 6 strikes, 2 suspensiones; la anterior rehabilitada por "Administrador General".
- Detalle de un admin → `null`. Sin `password` ni `fcm`.
- `php artisan test`: 58/58.

## Resultado
Commit: `feat(backend): agrega presentador de cuentas y acciones por estado`.
