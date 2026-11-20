<template>
  <div>
    <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
      <div class="row g-4">
        <!-- Mitad Izquierda: Tabla de Solicitudes -->
        <div class="col-12 col-lg-6 d-flex flex-column">
          <div class="search-box-container d-flex align-items-center mb-3">
            <input 
              type="text" 
              class="search-box-input"
              v-model="busqueda"
              placeholder="Buscar por folio, reportante, corporación o lugar..."
            >
            <i class="bi bi-search text-secondary ms-2 fs-5"></i>
          </div>

          <div class="table-container-scroll flex-grow-1">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-top">
                <tr class="small text-secondary">
                  <th class="ps-3">Folio</th>
                  <th>Solicitud / Detalle</th>
                  <th>Estado</th>
                  <th class="text-end pe-3">Fecha y Hora</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="item in filtradas" 
                  :key="item.id"
                  class="solicitud-row"
                  :id="'row-' + item.id"
                  :class="{ 'is-selected': seleccionadaId === item.id }"
                  @click="seleccionarFila(item)"
                  @dblclick="$emit('abrir-atencion', item)"
                >
                  <td class="ps-3 fw-bold text-dark font-monospace">#{{ item.id }}</td>
                  <td>
                    <div class="fw-semibold text-dark fs-6">{{ item.titulo }}</div>
                    <div class="text-muted small">
                      <i class="bi bi-geo-alt text-danger me-1"></i>{{ item.ubicacionNombre }}
                    </div>
                  </td>
                  <td>
                    <span 
                      class="badge rounded-pill fw-semibold"
                      :style="{ 
                        backgroundColor: ESTILOS_ESTADO[item.estado]?.color || '#64748b',
                        color: '#fff' 
                      }"
                    >
                      {{ textoEstado(item.estado) }}
                    </span>
                  </td>
                  <td class="text-end pe-3 fst-italic text-secondary small">
                    <div>{{ item.fecha }}</div>
                    <div class="text-muted" style="font-size: 0.75rem;">{{ item.hora }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Mitad Derecha: Mapa de Solicitudes con selector de capas -->
        <div class="col-12 col-lg-6">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="fw-bold text-dark small text-uppercase">
              <i class="bi bi-pin-map-fill text-danger me-1"></i> MAPA DE SOLICITUDES
            </span>
            <span class="badge bg-light text-secondary border">Misantla, Veracruz</span>
          </div>

          <!-- Contenedor del Mapa -->
          <div 
            id="map-solicitudes-view" 
            style="height: 575px; width: 100%; border-radius: 14px; border: 1px solid #e2e8f0; z-index: 1;"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import L from 'leaflet'
import { useAlertas, ESTILOS_ESTADO } from './useAlertas'

const emit = defineEmits(['abrir-atencion'])
const { todasLasAlertas, textoEstado } = useAlertas()

const busqueda = ref('')
const seleccionadaId = ref(null)
let moduleMap = null
let markersMap = {}

const filtradas = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return todasLasAlertas.value
  return todasLasAlertas.value.filter(s => 
    s.id.toLowerCase().includes(q) ||
    s.titulo.toLowerCase().includes(q) ||
    s.ubicacionNombre.toLowerCase().includes(q) ||
    s.estado.toLowerCase().includes(q)
  )
})

const seleccionarFila = (item) => {
  seleccionadaId.value = item.id
  if (moduleMap) {
    moduleMap.setView([item.lat, item.lng], 16)
    markersMap[item.id]?.openPopup()
  }
}

onMounted(() => {
  // 1. Capas disponibles (Calles, Satelital, Terreno y OSM)
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

  // 2. Inicializar mapa con la capa calles por defecto
  moduleMap = L.map('map-solicitudes-view', {
    center: [19.9298, -96.8525],
    zoom: 14,
    layers: [capaCalles]
  })

  // 3. Menú selector de capas en la esquina superior derecha
  const capasBase = {
    '🗺️ Calles': capaCalles,
    '🛰️ Satelital': capaSatelite,
    '⛰️ Terreno': capaTerreno,
    '🌐 OpenStreetMap': capaOSM
  }

  L.control.layers(capasBase, null, { position: 'topright' }).addTo(moduleMap)

  // 4. Marcadores de las solicitudes
  todasLasAlertas.value.forEach(alerta => {
    const color = ESTILOS_ESTADO[alerta.estado]?.color || '#64748b'
    const marker = L.circleMarker([alerta.lat, alerta.lng], { 
      radius: 9, 
      fillColor: color, 
      color: '#fff', 
      weight: 2, 
      fillOpacity: 0.95 
    }).addTo(moduleMap)

    marker.bindPopup(`<b>#${alerta.id} · ${alerta.titulo}</b><br><small>${alerta.ubicacionNombre}</small>`)
    
    marker.on('click', () => {
      seleccionadaId.value = alerta.id
      nextTick(() => {
        document.getElementById('row-' + alerta.id)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
      })
    })

    marker.on('dblclick', () => {
      emit('abrir-atencion', alerta)
    })

    markersMap[alerta.id] = marker
  })
})
</script>

<style scoped>
.search-box-container { 
  background-color: #f1f5f9; 
  border-radius: 8px; 
  padding: 6px 14px; 
  border: 1px solid #e2e8f0; 
}
.search-box-input { 
  background: transparent; 
  border: none; 
  outline: none; 
  width: 100%; 
}
.table-container-scroll { 
  max-height: 520px; 
  overflow-y: auto; 
  border-radius: 12px; 
  border: 1px solid #e2e8f0; 
}
.solicitud-row { 
  cursor: pointer; 
  user-select: none; 
}
.solicitud-row:hover { 
  background-color: #f8fafc; 
}
.solicitud-row.is-selected { 
  background-color: #eff6ff !important; 
  border-left: 4px solid #0284c7 !important; 
}
</style>