# 010 — Tarea: Componentes comunes, skeletons y modales

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Piezas reutilizables que todas las vistas usan. Presentacionales: props + emits, sin store (excepto modales).

## Pasos

### 1. `components/comunes/`
- `EstadoBadge`: prop `estado`; ícono + etiqueta; colores de tokens.
- `ServicioIcono`: prop `servicio`; ícono Material Symbols + nombre accesible; variante seleccionable para formularios.
- `MapaBase`: Leaflet; props `marcadores`, `centro`, `zoom`; emite `seleccionar`. Marcadores con color de estado (`L.divIcon`). Destruir mapa en `onBeforeUnmount`.
- `LineaTiempo`: prop `eventos` (`v-for`).
- `CampoBusqueda`: `v-model` con etiqueta accesible.
- `EstadoVacio`: mensaje + acción opcional.
- `TarjetaSeccion`: contenedor con título y slot de acciones.

### 2. Skeletons (`components/comunes/`)
- `SkeletonTabla` (props `filas`, `columnas`), `SkeletonLista`, `SkeletonDetalle`, `SkeletonBloque` (alto configurable).
- Placeholders de Bootstrap (`.placeholder`, `.placeholder-glow`).
- Contenedor `aria-busy="true"` + texto `visually-hidden`; bloques `aria-hidden`.
- Composable `useCargaDiferida(ref)`: muestra skeleton solo si carga dura > 150 ms.

### 3. Modales (`components/modals/`)
- `ModalBase`: modal de Bootstrap controlado por `v-model`, foco atrapado, cierre con Esc.
- `ModalEditarServicios`: selección múltiple de servicios; guarda con store.
- `ModalRechazar`: motivo obligatorio + casilla "Es broma o mal uso (suma strike)".
- `ModalReportarUsuario`: motivo obligatorio; suma strike manual.
- Botón principal con círculo pequeño mientras guarda.

## Verificación
- Build sin errores.
- Modales: Tab no sale del modal; Esc cierra; foco vuelve al botón que lo abrió.

## Resultado
Pendiente.
