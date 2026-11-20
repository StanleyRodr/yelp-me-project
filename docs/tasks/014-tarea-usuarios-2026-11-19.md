# 014 — Tarea: Usuarios

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Gestión de usuarios de la app: estado de cuenta, strikes y suspensiones (plan principal sección 8).

## Pasos
1. `components/usuarios/TablaUsuarios`: nombre, teléfono, correo, estado de cuenta (Activo / Suspendido / Deshabilitado), strikes activos (x/3). Búsqueda.
2. `views/UsuariosView.vue`: búsqueda + tabla; fila abre `/usuarios/:id`.
3. `components/usuarios/CabeceraUsuario`: datos, foto, estado, veces suspendido; acciones: reportar (strike), suspender/rehabilitar, deshabilitar/reactivar.
4. `components/usuarios/HistorialSolicitudes`, `HistorialStrikes` (con "Anular" y motivo), `HistorialSuspensiones`.
5. `views/UsuarioDetalleView.vue`: composición.
6. Reglas en el store: 3 strikes activos → suspensión automática; rehabilitar → strikes activos a 0; anular requiere motivo.

## Verificación
- Reportar 3 veces suspende. Rehabilitar reinicia. Historial conserva todo.

## Resultado
Pendiente.
