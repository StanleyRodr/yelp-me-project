# 010 — Tarea: Componentes comunes, skeletons y modales

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Piezas reutilizables para todas las vistas. Comunes = presentacionales (props + emits, sin store). Modales sí usan store.

## Pasos

### 1. Un solo JavaScript de Bootstrap
- Problema: importar `bootstrap/js/dist/modal.js` además de `bootstrap.bundle` duplica registro de eventos. Botones de la data-API crearían instancias repetidas.
- Fix: `src/plugins/bootstrap.js` exporta `Modal` desde el build ESM (`import { Modal } from 'bootstrap'`). `main.js` importa ese plugin en lugar del bundle.
- ESM registra data-API (collapse, dropdown) al importarse. `@popperjs/core` ya estaba instalado. Nada nuevo.

### 2. Utilidades
- `utils/formato.js`: `fechaHora`, `fecha` (Intl `es-MX`), `minutosDesde`, `duracion` ("14 min", "2 h 5 min", "3 d"), `haceTiempo`, `nombreCompleto`, `folio` (`#0007`).
- `composables/useCargaDiferida.js`: `mostrar` = true solo si la carga dura > 150 ms. Evita destello de skeleton.

### 3. `components/comunes/`

| Componente | Props clave | Notas |
|---|---|---|
| `EstadoBadge` | `estado`, `tamano` (sm/md/lg) | Ícono + texto. Variables `--e`, `--e-texto`, `--e-fondo` por estado. Siempre con borde. |
| `ServicioIcono` | `servicio`, `conNombre` | Ícono en color de marca. Sin nombre visible → nombre `visually-hidden` + `title`. |
| `ListaServicios` | `ids`, `catalogo`, `conNombre` | `v-for` de `ServicioIcono`. Sin servicios → "Solo contactos". |
| `TarjetaSeccion` | `titulo`, `descripcion`, `sinRelleno` | `<section aria-labelledby>`, slot `acciones`. Contenedor estándar de toda sección. |
| `CampoBusqueda` | `v-model`, `etiqueta`, `placeholder` | Label oculto, botón limpiar con `v-show`. |
| `EstadoVacio` | `titulo`, `mensaje`, `icono` | Slot para acción. Dice qué hacer, no se disculpa. |
| `LineaTiempo` | `eventos` | `<ol>` con punto de color por estado y `<time datetime>`. |
| `BotonCarga` | `cargando`, `variante`, `icono`, `tipo` | Círculo pequeño + disabled + texto oculto ", en proceso". Atributos extra (`form`) pasan al `<button>`. |
| `MapaBase` | `marcadores`, `seleccionadoId`, `alto`, `ajustar` | Ver paso 4. |

### 4. `MapaBase` (Leaflet encapsulado)
- Instancia de Leaflet fuera de reactividad (`let mapa`). Destruir en `onBeforeUnmount`.
- Marcadores `L.divIcon`: círculo con color e ícono del estado. Seleccionado: más grande + anillo.
- Leaflet crea marcadores fuera del template: estilos con `:deep()`.
- `keyboard: true` + `title`: marcadores enfocables con Tab, Enter dispara clic.
- `fitBounds` si hay varios; `setView` si hay uno; `panTo` al cambiar selección.
- `invalidateSize` tras montar: la rejilla cambia tamaño del contenedor.
- Tiles OpenStreetMap: requieren internet.

### 5. Skeletons
- `SkeletonContenedor`: `role="status"`, `aria-busy`, texto `visually-hidden`, bloques `aria-hidden`. Base de los demás.
- `SkeletonTabla` (`filas`, `columnas`), `SkeletonLista` (`elementos`), `SkeletonDetalle` (`campos`), `SkeletonBloque` (`alto`).
- Placeholders de Bootstrap (`.placeholder-glow`). Anchos variados con patrón fijo: parece texto y no cambia al re-renderizar.
- `prefers-reduced-motion`: sin animación (regla en `base.css`).

Uso en vistas:

```html
<SkeletonTabla v-if="mostrarSkeleton && !store.lista.length" :filas="8" :columnas="5" />
<TablaSolicitudes v-else-if="store.lista.length" ... />
```

Con datos ya cargados no se muestra skeleton: se conservan los datos (regla de recarga).

### 6. `components/modals/`
- `ModalBase`: `Teleport` a `body` + `Modal` de Bootstrap. `v-model:abierto`. Atrapa foco, Esc, fondo. `bloqueado` cancela `hide.bs.modal` mientras guarda. Enfoca primer campo al abrir; devuelve foco al botón que lo abrió.
- `ModalEditarServicios`: casillas `btn-check` (checkbox real, accesible) con `v-for` del catálogo. Mínimo 1. Store `solicitudes.actualizarServicios`.
- `ModalRechazar`: motivo obligatorio + casilla de strike. Sin servicios → casilla marcada por defecto (solo se rechaza por mal uso). Botón `danger` "Rechazar solicitud".
- `ModalMotivo` (genérico): reemplaza `ModalReportarUsuario`. Reportar, suspender y anular strike piden lo mismo: motivo obligatorio. La vista pasa `accion(motivo)` que llama a su store. Evita 3 modales casi iguales.
- Todos: error de la API en `role="alert"`, toast de éxito con verbo concreto, reinician formulario al abrir.

## Verificación
- Componentes sin uso todavía: el build no los procesa. Se compilan uno por uno con `vite.transformRequest` (script fuera del repo): **20/20 OK**.
- `npm run build` sin errores tras cambiar a Bootstrap ESM.
- Hex fuera de `tokens.css`: ninguno (única coincidencia: comentario `#0007`).
- `v-if` + `v-for` en el mismo elemento: ninguno. Todo `v-for` con `:key`.
- Comportamiento en navegador (modales, mapa, foco): se prueba al usarlos en 012–015 y en 016.

## Resultado
Commit: `feat(frontend): agrega componentes comunes, skeletons y modales`.
