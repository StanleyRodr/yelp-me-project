<template>
  <div>
    <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center mb-4">
      <div>
        <h1 class="h3 fw-bold text-dark mb-1">Buenos días, administración</h1>
        <p class="text-secondary small mb-0">Monitoreo de solicitudes en Misantla.</p>
      </div>
      <div class="mt-2 mt-sm-0">
        <div class="badge-simulated d-flex align-items-center shadow-sm">
          <span class="status-dot me-2"></span>
          <span>Datos simulados</span>
        </div>
      </div>
    </div>

    <!-- Métricas -->
    <div class="row g-3 mb-4">
      <div class="col-12 col-md-4">
        <div class="card metric-card shadow-sm border-0 p-3 h-100 bg-white">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <span class="text-secondary small fw-medium">Total alertas</span>
            <div class="icon-box icon-blue"><i class="bi bi-broadcast-pin"></i></div>
          </div>
          <h2 class="display-6 fw-bold text-dark mb-0">{{ totalAlertas }}</h2>
        </div>
      </div>

      <div class="col-12 col-md-4">
        <div class="card metric-card shadow-sm border-0 p-3 h-100 bg-white">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <span class="text-secondary small fw-medium">Alertas activas</span>
            <div class="icon-box icon-red"><i class="bi bi-exclamation-triangle"></i></div>
          </div>
          <h2 class="display-6 fw-bold text-dark mb-0">{{ alertasActivasCount }}</h2>
        </div>
      </div>

      <div class="col-12 col-md-4">
        <div class="card metric-card shadow-sm border-0 p-3 h-100 bg-white">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <span class="text-secondary small fw-medium">Atendidas</span>
            <div class="icon-box icon-green"><i class="bi bi-check2-circle"></i></div>
          </div>
          <h2 class="display-6 fw-bold text-dark mb-0">{{ atendidasCount }}</h2>
        </div>
      </div>
    </div>

    <!-- Mapa y Carrusel -->
    <div class="row g-4 align-items-stretch">
      <div class="col-12 col-lg-7">
        <div class="card shadow-sm border-0 rounded-4 p-3 bg-white h-100">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <h2 class="h5 fw-bold text-dark mb-0">Mapa de incidentes</h2>
            <span class="text-muted small">Misantla, Veracruz</span>
          </div>
          <p class="text-secondary small mb-3">Ubicación reportada por solicitud (10 registradas).</p>
          
          <!-- Contenedor del Mapa Principal -->
          <div id="map-misantla" style="height: 480px; width: 100%; border-radius: 12px; z-index: 1;"></div>

          <!-- Leyenda de colores -->
          <div class="d-flex flex-wrap gap-2 mt-3 pt-2 border-top small text-secondary">
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#ef4444">&nbsp;</span> Pendiente ({{ conteoPorEstado('Pendiente') }})</span>
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#0284c7">&nbsp;</span> Asignada ({{ conteoPorEstado('Asignada') }})</span>
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#f59e0b">&nbsp;</span> En Camino ({{ conteoPorEstado('En Camino') }})</span>
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#15803d">&nbsp;</span> Atendida ({{ conteoPorEstado('Atendida') }})</span>
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#1e293b">&nbsp;</span> Rechazada ({{ conteoPorEstado('Rechazada') }})</span>
            <span class="d-flex align-items-center"><span class="badge rounded-circle p-1 me-1" style="background:#64748b">&nbsp;</span> Cancelada ({{ conteoPorEstado('Cancelada') }})</span>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-5">
        <div class="card shadow-sm border-0 rounded-4 p-4 bg-white h-100 d-flex flex-column">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div>
              <h2 class="h5 fw-bold text-dark mb-0">Pendientes recientes</h2>
              <span class="text-secondary small">Alerta {{ indiceActual + 1 }} de {{ alertasPendientes.length }}</span>
            </div>
            <div class="d-flex gap-2">
              <button class="carousel-control-btn" @click="anteriorAlerta"><i class="bi bi-chevron-left"></i></button>
              <button class="carousel-control-btn" @click="siguienteAlerta"><i class="bi bi-chevron-right"></i></button>
            </div>
          </div>

          <div 
            class="emergency-card-wrapper mt-2 flex-grow-1 cursor-pointer"
            v-if="alertaActiva"
            @click="$emit('ir-a-atencion', alertaActiva)"
          >
            <div 
              class="badge-banner-unattended mb-3 shadow-sm"
              :style="{ backgroundColor: ESTILOS_ESTADO[alertaActiva.estado]?.color || '#ef4444' }"
            >
              <div class="status-title">{{ textoEstado(alertaActiva.estado) }}</div>
              <div class="status-folio">#{{ alertaActiva.id }}</div>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-3">
              <div class="pe-2">
                <span class="text-muted small d-block">Emergencia</span>
                <h5 class="fw-bold text-dark mb-0 fs-5 lh-sm">{{ alertaActiva.titulo }}</h5>
              </div>
              <div class="d-flex gap-2 flex-shrink-0">
                <div class="badge-round-agency bg-secondary-subtle text-danger"><i class="bi bi-truck-front-fill"></i></div>
                <div class="badge-round-agency bg-warning-subtle text-warning-emphasis"><i class="bi bi-shield-fill"></i></div>
              </div>
            </div>

            <!-- Mapa pequeño de la tarjeta -->
            <div class="mini-map-preview mb-3">
              <div class="mini-map-overlay">
                <div class="fw-bold text-truncate">{{ alertaActiva.ubicacionNombre }}</div>
                <div class="text-muted" style="font-size: 0.65rem;">Misantla, Ver. (Clic para atender)</div>
              </div>
              <div id="mini-map" style="height: 100%; width: 100%;"></div>
            </div>

            <div class="mt-auto">
              <div class="fw-bold text-dark small mb-1">Comentario</div>
              <p class="text-secondary small mb-2 lh-base">{{ alertaActiva.comentario }}</p>
              <div class="text-danger small fw-bold text-end">
                Pasar a Atención <i class="bi bi-arrow-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import L from 'leaflet'
import { useAlertas, ESTILOS_ESTADO } from './useAlertas'

defineEmits(['ir-a-atencion'])

const { todasLasAlertas, totalAlertas, alertasActivasCount, atendidasCount, alertasPendientes, textoEstado, conteoPorEstado } = useAlertas()

let mainMap = null
let miniMap = null
let miniMapMarker = null
let miniMapTileLayer = null

// Guardamos la configuración de la capa activa para sincronizarla
const currentTileConfig = ref({
  url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
  options: { attribution: 'Esri Street', maxZoom: 19 }
})

const indiceActual = ref(0)

const alertaActiva = computed(() => alertasPendientes.value[indiceActual.value] || todasLasAlertas.value[0])

const siguienteAlerta = () => {
  if (alertasPendientes.value.length === 0) return
  indiceActual.value = (indiceActual.value + 1) % alertasPendientes.value.length
  nextTick(() => updateMiniMap())
}

const anteriorAlerta = () => {
  if (alertasPendientes.value.length === 0) return
  indiceActual.value = (indiceActual.value - 1 + alertasPendientes.value.length) % alertasPendientes.value.length
  nextTick(() => updateMiniMap())
}

// Función para actualizar la capa del mini mapa cuando cambia la selección en el mapa principal
const sincronizarCapaMiniMap = (url, options) => {
  currentTileConfig.value = { url, options }
  if (miniMap) {
    if (miniMapTileLayer) {
      miniMap.removeLayer(miniMapTileLayer)
    }
    miniMapTileLayer = L.tileLayer(url, options).addTo(miniMap)
  }
}

const updateMiniMap = () => {
  const item = alertaActiva.value
  if (!item) return
  const color = ESTILOS_ESTADO[item.estado]?.color || '#ef4444'

  if (!miniMap) {
    miniMap = L.map('mini-map', { zoomControl: false, attributionControl: false }).setView([item.lat, item.lng], 15)
    miniMapTileLayer = L.tileLayer(currentTileConfig.value.url, currentTileConfig.value.options).addTo(miniMap)
    miniMapMarker = L.circleMarker([item.lat, item.lng], { radius: 8, fillColor: color, color: '#fff', weight: 2, fillOpacity: 1 }).addTo(miniMap)
  } else {
    miniMap.setView([item.lat, item.lng], 15)
    miniMapMarker.setLatLng([item.lat, item.lng])
    miniMapMarker.setStyle({ fillColor: color })
    miniMap.invalidateSize()
  }
}

onMounted(() => {
  // 1. Capas base disponibles
  const capaCalles = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Esri Street',
    maxZoom: 19
  })

  const capaSatelite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Esri Satellite',
    maxZoom: 19
  })

  const capaTerreno = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Esri Topo',
    maxZoom: 19
  })

  const capaOSM = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap',
    maxZoom: 19
  })

  // 2. Mapa principal
  mainMap = L.map('map-misantla', {
    center: [19.9298, -96.8525],
    zoom: 14,
    layers: [capaCalles]
  })

  // 3. Menú de capas
  const capasBase = {
    '🗺️ Calles': capaCalles,
    '🛰️ Satelital': capaSatelite,
    '⛰️ Terreno': capaTerreno,
    '🌐 OpenStreetMap': capaOSM
  }

  L.control.layers(capasBase, null, { position: 'topright' }).addTo(mainMap)

  // 4. EVENTO CLAVE: Al cambiar la capa en el menú superior, actualiza también el mapa de la tarjeta
  mainMap.on('baselayerchange', (e) => {
    sincronizarCapaMiniMap(e.layer._url, e.layer.options)
  })

  // 5. Marcadores de incidentes
  todasLasAlertas.value.forEach(alerta => {
    const color = ESTILOS_ESTADO[alerta.estado]?.color || '#64748b'
    L.circleMarker([alerta.lat, alerta.lng], {
      radius: 9,
      fillColor: color,
      color: '#fff',
      weight: 2,
      opacity: 1,
      fillOpacity: 0.95
    }).addTo(mainMap)
  })

  nextTick(() => updateMiniMap())
})
</script>

<style scoped>
.metric-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); }
.icon-box { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; }
.icon-blue { background-color: #e0f2fe; color: #0284c7; }
.icon-red { background-color: #fee2e2; color: #ef4444; }
.icon-green { background-color: #dcfce7; color: #22c55e; }
.badge-simulated { background-color: #f1f5f9; color: #334155; font-size: 0.8rem; font-weight: 600; border: 1px solid #e2e8f0; padding: 6px 12px; border-radius: 30px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background-color: #22c55e; display: inline-block; }
.emergency-card-wrapper { background: #fff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 1.25rem; min-height: 480px; display: flex; flex-direction: column; }
.badge-banner-unattended { color: #fff; font-weight: 800; text-align: center; padding: 10px 14px; border-radius: 14px; }
.badge-round-agency { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.35rem; }
.mini-map-preview { height: 180px; width: 100%; border-radius: 10px; border: 1px solid #cbd5e1; overflow: hidden; position: relative; }
.mini-map-overlay { position: absolute; top: 8px; left: 8px; background: rgba(255,255,255,0.95); padding: 4px 8px; border-radius: 6px; font-size: 0.72rem; z-index: 400; }
.carousel-control-btn { width: 34px; height: 34px; border-radius: 50%; border: 1px solid #cbd5e1; background: #fff; display: flex; align-items: center; justify-content: center; }
.cursor-pointer { cursor: pointer; }
</style>