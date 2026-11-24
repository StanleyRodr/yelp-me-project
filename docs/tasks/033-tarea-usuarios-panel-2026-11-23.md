# 033 — Tarea: Usuarios del panel

> Plan: `006-plan-backend-usuarios-perfil-2026-11-23.md` · Rama: `feature/backend-usuarios` · Estado: ✅ Completada

## Objetivo
El admin ve los usuarios de la app con su estado de cuenta e historial, y deshabilita o reactiva cuentas.

## Endpoints (rol `admin`)

| Método | Ruta | Controlador | Éxito |
|---|---|---|---|
| GET | `/api/admin/usuarios` | `Admin\UsuarioController@index` | 200, por nombre |
| GET | `/api/admin/usuarios/{id}` | `Admin\UsuarioController@show` | 200 detalle |
| PUT | `/api/admin/usuarios/{id}/cuenta` | `Admin\CuentaController@update` | "Cuenta deshabilitada" / "Cuenta reactivada" |

## Pasos

### 1. `ReglasCuenta` (ampliada)
- `bloquearUsuarioApp($id)`: `FOR UPDATE`, solo rol `usuario`; admin o inexistente → 404.
- `cerrarSesiones($usuarioId, $exceptoTokenId = null)`: borra sus tokens de Sanctum. Lo reutiliza el cambio de contraseña (tarea 035).
- Mensaje 409 con concordancia: "La cuenta está activa/suspendida/deshabilitada: no se puede …". Primera versión decía "está Activo".

### 2. `Admin\UsuarioController`
- `index`: solo rol `usuario`, por nombre y apellidos; `PresentadorCuenta::resumenes` (2 consultas).
- `show`: `PresentadorCuenta::detalle` o 404.
- Nombres de ruta `admin.usuarios.*`.

### 3. `Admin\CuentaController@update`
1. `activo` required, boolean (acepta `true/false/1/0/"0"`).
2. Transacción: `bloquearUsuarioApp` → `exigirAccion(reactivar | deshabilitar)`.
3. Deshabilitar: `activo = 0`, `deshabilitado_en = now()`, `cerrarSesiones`. Reactivar: `activo = 1`, `deshabilitado_en = null`.
4. Responde el detalle.
- Deshabilitar un suspendido está permitido (queda Deshabilitado; la suspensión sigue en su historial).

## Verificación
Script `tinker` con transacción revertida:

| Caso | Resultado |
|---|---|
| Listado | Ana, Carlos, José, Luis (Suspendido), María |
| Detalle de Luis | todas las claves del frontend |
| Detalle / deshabilitar de un admin | 404 |
| Usuario de la app en el panel | 403 |
| Sin `activo` | 422 |
| Reactivar cuenta activa | 409 "La cuenta está activa: no se puede reactivar…" |
| Deshabilitar a José | Deshabilitado, `deshabilitado_en`, 0 tokens; su token previo → 401 |
| Deshabilitar otra vez | 409 |
| Reactivar a José | Activo |
| Deshabilitar a Luis (suspendido, `"0"`) | Deshabilitado |

## Resultado
Commit: `feat(backend): agrega listado y gestión de cuentas de usuarios`.
