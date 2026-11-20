<template>
  <div class="mapa" :style="{ height: alto }">
    <div ref="contenedor" class="mapa-lienzo" role="region" :aria-label="etiqueta"></div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L from 'leaflet'
import { ESTADO_POR_CLAVE } from '../../constantes/estados'

const props = defineProps({
  /** [{ id, latitud, longitud, estado, titulo }] */
  marcadores: { type: Array, default: () => [] },
  seleccionadoId: { type: Number, default: null },
  /** Centro inicial: Misantla, Ver. */
  centro: { type: Array, default: () => [19.93, -96.852] },
  zoom: { type: Number, default: 15 },
  alto: { type: String, default: '420px' },
  etiqueta: { type: String, default: 'Mapa de solicitudes' },
  /** Ajustar la vista para que se vean todos los marcadores */
  ajustar: { type: Boolean, default: true },
})

const emit = defineEmits(['seleccionar'])

const contenedor = ref(null)
// Instancias de Leaflet fuera de la reactividad de Vue
let mapa = null
let capa = null
const porId = new Map()

function icono(marcador, seleccionado) {
  const estado = ESTADO_POR_CLAVE[marcador.estado] ?? { token: 'cancelado', icono: 'place' }
  return L.divIcon({
    className: '',
    html: `<span class="marcador marcador-${estado.token}${seleccionado ? ' marcador-seleccionado' : ''}">`
      + `<span class="material-symbols-outlined" aria-hidden="true">${estado.icono}</span></span>`,
    iconSize: seleccionado ? [40, 40] : [32, 32],
    iconAnchor: seleccionado ? [20, 20] : [16, 16],
  })
}

function pintar() {
  if (!mapa) return
  capa.clearLayers()
  porId.clear()
  for (const m of props.marcadores) {
    const marcador = L.marker([m.latitud, m.longitud], {
      icon: icono(m, m.id === props.seleccionadoId),
      title: `${m.titulo} (${m.estado})`,
      keyboard: true,
      riseOnHover: true,
    })
    marcador.on('click', () => emit('seleccionar', m.id))
    marcador.addTo(capa)
    porId.set(m.id, marcador)
  }
  if (props.ajustar && props.marcadores.length > 1 && !props.seleccionadoId) {
    mapa.fitBounds(L.latLngBounds(props.marcadores.map((m) => [m.latitud, m.longitud])), { padding: [32, 32], maxZoom: 16 })
  } else if (props.marcadores.length === 1) {
    mapa.setView([props.marcadores[0].latitud, props.marcadores[0].longitud], 16)
  }
}

function enfocarSeleccionado() {
  const m = props.marcadores.find((x) => x.id === props.seleccionadoId)
  if (!m || !mapa) return
  porId.forEach((marcador, id) => {
    const datos = props.marcadores.find((x) => x.id === id)
    marcador.setIcon(icono(datos, id === props.seleccionadoId))
    marcador.setZIndexOffset(id === props.seleccionadoId ? 1000 : 0)
  })
  mapa.panTo([m.latitud, m.longitud])
}

onMounted(() => {
  mapa = L.map(contenedor.value, { zoomControl: true, attributionControl: true }).setView(props.centro, props.zoom)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap',
  }).addTo(mapa)
  capa = L.layerGroup().addTo(mapa)
  pintar()
  // El contenedor puede cambiar de tamaño al montarse dentro de la rejilla
  requestAnimationFrame(() => mapa?.invalidateSize())
})

watch(() => props.marcadores, pintar, { deep: true })
watch(() => props.seleccionadoId, enfocarSeleccionado)

onBeforeUnmount(() => {
  mapa?.remove()
  mapa = null
})
</script>

<style scoped>
.mapa {
  position: relative;
  border: 1px solid var(--ym-borde);
  border-radius: var(--ym-radio);
  overflow: hidden;
  background: var(--ym-skeleton);
}

.mapa-lienzo {
  width: 100%;
  height: 100%;
  font-family: var(--ym-fuente);
}

/* Los marcadores los crea Leaflet fuera del template: requieren :deep */
.mapa :deep(.marcador) {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--m);
  color: var(--ym-sobre-marca);
  border: 2px solid var(--ym-superficie);
  outline: 1px solid var(--m);
}

.mapa :deep(.marcador .material-symbols-outlined) {
  font-size: 1.125rem;
  vertical-align: 0;
}

.mapa :deep(.marcador-seleccionado) {
  outline-width: 3px;
  outline-offset: 2px;
}

.mapa :deep(.marcador-pendiente) { --m: var(--ym-estado-pendiente); }
.mapa :deep(.marcador-enviado) { --m: var(--ym-estado-enviado); }
.mapa :deep(.marcador-terminado) { --m: var(--ym-estado-terminado); }
.mapa :deep(.marcador-rechazado) { --m: var(--ym-estado-rechazado); }
.mapa :deep(.marcador-cancelado) { --m: var(--ym-estado-cancelado); }

.mapa :deep(.leaflet-marker-icon:focus-visible) {
  outline: 2px solid var(--ym-foco);
  outline-offset: 4px;
  border-radius: 50%;
}
</style>
