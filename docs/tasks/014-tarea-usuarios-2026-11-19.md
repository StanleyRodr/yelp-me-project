# 014 — Tarea: Usuarios

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Gestión de usuarios de la app: estado de cuenta, strikes y suspensiones (plan principal sección 8).

## Pasos

### 1. `constantes/cuentas.js`
- `ESTADOS_CUENTA`: Activo (`check_circle`, verde), Suspendido (`pause_circle`, rojo), Deshabilitado (`person_off`, gris). Reusa tokens de estado.
- `accionesCuenta(usuario)`:

| Cuenta | Acciones |
|---|---|
| Activo | Reportar, Suspender, Deshabilitar |
| Suspendido | Rehabilitar, Reportar, Deshabilitar |
| Deshabilitado | Reactivar |

- `situacionStrike(strike)`: Activo · Anulado (tiene `anulado_por_id`) · Reiniciado (inactivo por rehabilitación).

### 2. Componentes `components/usuarios/`

| Componente | Función |
|---|---|
| `EstadoCuenta` | Badge de cuenta con ícono + texto. |
| `MedidorStrikes` | 3 segmentos + "2 de 3". Ámbar mientras < 3; rojo al llegar al límite. Texto oculto "strikes activos". |
| `FiltrosUsuarios` | Chips Todos / Activo / Suspendido / Deshabilitado con conteo + búsqueda. |
| `TablaUsuarios` | Usuario + correo, teléfono, cuenta, strikes, registro. Nombre = `RouterLink`; fila clic abre perfil. |
| `CabeceraUsuario` | Avatar (foto o iniciales), nombre `h1`, `tel:` y `mailto:`, resumen (`dl`) y botones de `accionesCuenta()` con `v-for`. |
| `HistorialSolicitudes` | Solicitudes del usuario, más recientes primero, enlace al detalle. |
| `HistorialStrikes` | Situación, fecha, motivo, quién y origen (rechazo con folio o reporte manual). Anulado: motivo tachado + quién anuló. Botón "Anular strike" solo en activos. |
| `HistorialSuspensiones` | Vigente (tinte rojo) o rehabilitada, origen (automática por 3 strikes o admin), rango de fechas. |

Todos presentacionales. Vacíos con `EstadoVacio` y mensaje concreto.

### 3. `views/UsuariosView.vue`
- Filtro por estado + búsqueda sin acentos (nombre, teléfono, correo).
- Skeleton → error con "Reintentar" → sin coincidencias con "Quitar filtros" → tabla.

### 4. `views/UsuarioDetalleView.vue`
- Rejilla: volver · cabecera · solicitudes (7/12) + strikes y suspensiones (5/12).
- Usuario inexistente → `EstadoVacio` + "Ver usuarios".
- Un solo `ModalMotivo` con configuración por acción (`v-bind`):

| Acción | Interfaz | Texto clave |
|---|---|---|
| Reportar | `ModalMotivo` danger | Suma strike; 3 suspenden |
| Suspender | `ModalMotivo` danger | No pide servicios; sí avisa a contactos |
| Anular strike | `ModalMotivo` | Deja de contar, queda en historial |
| Rehabilitar | `confirmar()` | Strikes a 0, historial se conserva |
| Deshabilitar | `confirmar()` peligro, foco en Cancelar | No inicia sesión; reversible |
| Reactivar | `confirmar()` | Vuelve a iniciar sesión |

- Textos explican la consecuencia de la acción antes de confirmar.
- Separadores "·" evitados (paréntesis o íconos).

## Verificación
- `npm run build` sin errores.
- Sin hex, sin `v-if` + `v-for` juntos, vistas sin `<table>` ni `<form>`.
- Reglas (3 strikes suspenden, rehabilitar reinicia, anular exige motivo, deshabilitado prevalece): 23/23 OK en tarea 009.
- **Pendiente en navegador:** extensión sin conexión. Tarea 016.

## Resultado
Commit: `feat(frontend): agrega gestión de usuarios con strikes y suspensiones`.
