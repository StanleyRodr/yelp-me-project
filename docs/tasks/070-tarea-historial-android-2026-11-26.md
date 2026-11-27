# 070 — Tarea: Historial (app)

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ⬜ Pendiente

## Objetivo
`HistorialActivity` con todas las solicitudes del usuario y filtro por estado.

## Pasos

### 1. Layout `activity_historial.xml` (estructura del diseño)
- Tarjeta "Mis solicitudes": `ChipGroup` de opción única (Todas, Pendiente, Enviado, Terminado, Cancelado, Rechazado) en lugar del `Spinner`.
- `RecyclerView rvHistorial` + `SwipeRefreshLayout` + estado vacío ("No tienes solicitudes con este estado.").
- Quitar `cardDetalleHistorial` (el detalle es `DetalleSolicitudActivity`, tarea 069).

### 2. `AdaptadorHistorial extends ListAdapter<Solicitud, …>`
`item_solicitud.xml`: badge de estado, título, fecha (`Formato.fecha`), lugar, íconos de servicios (o "Solo contactos"). Fila tocable → detalle.

### 3. Datos
- Chip elegido → `listar(estado)` (`null` para Todas). Ordenadas por fecha desc (como la API).
- Recarga en `onResume`.
- Chip elegido en `onSaveInstanceState`.

### 4. Nota
Sin paginación: `GET /api/solicitudes` devuelve la lista completa. Si el volumen crece, agregar `paginate()` en la API y scroll infinito aquí (fuera del MVP).

## Verificación
Carlos (seeder): Pendiente, Enviado y Cancelado; cada filtro muestra lo correcto; tocar abre el detalle con motivo en la cancelada.

## Resultado
Commit: `feat(mobile): muestra el historial de solicitudes con filtro`.
