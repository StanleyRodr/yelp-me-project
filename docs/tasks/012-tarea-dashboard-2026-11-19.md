# 012 — Tarea: Dashboard

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Inicio enfocado en lo que hay que atender: conteo por estado, cola de pendientes y mapa.

## Distribución

```
┌ Inicio ───────────────────────────────────────────────────┐
│ Pendiente 2 │ Enviado 1 │ Terminado 2 │ Rechazado 5 │ Canc. 1 │  ← enlaces a /solicitudes?estado=
├──────────────────────┬────────────────────────────────────┤
│ Por atender (5/12)   │ Mapa de solicitudes activas (7/12) │
│ ▌38 min  #0001 …     │                                    │
│ ▌14 min  #0002 …     │   marcadores Pendiente / Enviado   │
└──────────────────────┴────────────────────────────────────┘
```

< 992 px: una columna.

## Pasos

### 1. Apoyos
- `composables/useAhora.js`: marca de tiempo reactiva cada 30 s. Limpia el intervalo al desmontar.
- `constantes/espera.js`: niveles de espera.

| Nivel | Minutos | Etiqueta | Barra | Tono |
|---|---|---|---|---|
| reciente | < 10 | Reciente | 3 px | neutro |
| espera | 10–30 | En espera | 6 px | ámbar (Enviado) |
| prolongada | > 30 | Espera prolongada | 10 px | rojo (Pendiente) |

Etiqueta textual siempre presente: no depender solo del color.

### 2. `components/dashboard/FranjaEstados`
- `nav` con `v-for` de los 5 estados. Cada uno es `RouterLink` a `/solicitudes?estado=X`.
- Una sola franja con divisores, no 5 tarjetas. Borde superior del color del estado; conteo con cifras tabulares.
- < 992 px: rejilla `auto-fit`.

### 3. `components/dashboard/ColaPendientes` (elemento memorable)
- Props: `solicitudes` (ya ordenadas), `catalogo`, `ahora`. Emite `resaltar(id)` en hover y foco.
- `<ol>` con `aria-label` que explica el orden.
- Cada fila = `RouterLink` al detalle. Columnas: indicador de espera | folio, título, usuario, ubicación | servicios.
- Indicador: columna izquierda con tiempo grande y etiqueta; barra y fondo se intensifican por nivel.
- Usuario y ubicación con íconos, no con separador "·" (recurso de plantilla).
- Sin servicios → "Solo contactos" (desde `ListaServicios`).
- `TransitionGroup`: único movimiento del panel, la entrada de una solicitud nueva. Se anula con `prefers-reduced-motion`.

### 4. `components/dashboard/MapaIncidentes`
- `MapaBase` con solicitudes activas + leyenda (`v-for` de estados activos).
- Resaltar en la cola → marcador seleccionado en el mapa. Clic en marcador → detalle.

### 5. `views/InicioView.vue`
- Solo rejilla (`grid-template-areas`) + secciones + store.
- Skeleton: `useCargaDiferida` y solo si no hay datos. Con datos se conservan al refrescar.
- Estados de la cola con `v-if` / `v-else-if`: skeleton → error con "Reintentar" → vacío ("No hay solicitudes pendientes") → cola.

### 6. Store `solicitudes`: carga sin duplicar
- Problema: `AppShell` y la vista llamaban `cargar()` a la vez con la lista vacía. Dos peticiones.
- Fix: `cargar()` guarda la promesa en curso y la reutiliza.

## Verificación
- `npm run build` sin errores.
- Sin hex, sin `v-if` + `v-for` juntos, sin `<table>` ni `<form>` en la vista.
- Datos de demo: #0001 con 38 min (Espera prolongada), #0002 con 14 min (En espera, solo contactos).
- **Pendiente en navegador:** extensión de Chrome sin conexión. Revisión visual en tarea 016.

## Resultado
Commit: `feat(frontend): agrega dashboard con cola de pendientes y mapa`.
