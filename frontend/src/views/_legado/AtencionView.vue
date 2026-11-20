<template>
  <div>
    <!-- Encabezado con botón de volver -->
    <div class="mb-3 d-flex justify-content-between align-items-center">
      <button class="btn btn-sm btn-outline-secondary px-3" @click="$emit('volver')">
        <i class="bi bi-arrow-left me-1"></i> Volver
      </button>
      <span class="badge badge-atencion-module px-3 py-2 fs-6">
        <i class="bi bi-shield-check me-1"></i> Módulo de Atención de Incidentes
      </span>
    </div>

    <div class="detail-container-box">
      <div class="row g-4 align-items-center">
        
        <!-- Panel Izquierdo Gris -->
        <div class="col-12 col-lg-8">
          <div class="detail-left-panel">
            
            <!-- Título de Solicitud en Azul -->
            <h1 class="detail-title-blue mb-3">
              Solicitud #{{ solicitud.id }}
            </h1>

            <div class="mb-4">
              <span class="text-secondary d-block mb-1 fs-5">Emergencia:</span>
              <div class="fw-bold text-dark fs-4">{{ solicitud.titulo }}</div>
            </div>

            <!-- Columnas interiores -->
            <div class="row g-4 align-items-start">
              
              <!-- Iconos y Mapa sincronizados -->
              <div class="col-12 col-md-6 d-flex flex-column align-items-center align-items-md-start">
                <div class="service-column-block">
                  <div class="d-flex justify-content-between align-items-center mb-3 w-100">
                    <div 
                      v-for="s in catalogoServicios" 
                      :key="s.key"
                      class="service-icon-circle shadow-sm"
                      :class="[s.clase, { 'opacity-25': !solicitud.ayudas?.includes(s.key) }]"
                      :title="s.nombre"
                    >
                      <i :class="s.icono"></i>
                    </div>
                  </div>

                  <!-- Contenedor del Mapa -->
                  <div id="detail-map-atencion"></div>
                </div>
              </div>

              <!-- Comentarios y Ubicación -->
              <div class="col-12 col-md-6">
                <div class="info-right-block d-flex flex-column h-100 justify-content-between">
                  <div class="mb-4">
                    <h6 class="fw-bold text-dark fs-5 mb-2">Comentarios:</h6>
                    <p class="text-secondary lh-base mb-0" style="font-size: 1rem;">
                      {{ solicitud.comentario }}
                    </p>
                  </div>

                  <div>
                    <h6 class="fw-bold text-dark small text-uppercase mb-2 text-muted">
                      <i class="bi bi-geo-alt-fill text-danger me-1"></i> Ubicación reportada:
                    </h6>
                    <div class="location-text-box shadow-sm d-flex align-items-center">
                      <i class="bi bi-pin-map text-danger fs-5 me-2 flex-shrink-0"></i>
                      <span>{{ solicitud.ubicacionNombre }}, Misantla, Ver.</span>
                    </div>
                  </div>
                </div>
              </div>

            </div>

          </div>
        </div>

        <!-- Panel Derecho: Estado y Acciones -->
        <div class="col-12 col-lg-4">
          <div class="actions-column-mockup text-center d-flex flex-column align-items-center">
            
            <!-- Título "Estado" y Badge dinámicos con el color del estado -->
            <div class="w-100 header-status-block">
              <h2 class="title-estado-mockup mb-2" :style="{ color: colorEstadoActual }">
                Estado
              </h2>
              <div 
                class="badge-status-mockup" 
                :style="{ backgroundColor: colorEstadoActual }"
              >
                {{ textoEstado(solicitud.estado) }}
              </div>
            </div>

            <!-- Botones compactos -->
            <div class="buttons-mockup-group d-flex flex-column gap-2 align-items-center">
              
              <!-- 1. Editar solicitud -->
              <button class="btn-pill-action btn-pill-edit" @click="$emit('abrir-modal', 'editar')">
                <i class="bi bi-pencil-square me-2 fs-6"></i>
                <span>Editar solicitud</span>
              </button>

              <!-- 2. Rechazar y reportar -->
              <button class="btn-pill-action btn-pill-reject" @click="$emit('abrir-modal', 'rechazar')">
                <i class="bi bi-x-circle me-2 fs-6"></i>
                <span>Rechazar y reportar</span>
              </button>

              <!-- 3. Enviar ayuda / Cancelar envío de ayuda -->
              <button 
                v-if="solicitud.estado === 'En Camino'"
                class="btn-pill-action btn-pill-revoke" 
                @click="$emit('abrir-modal', 'confirmar-cancelar-ayuda')"
              >
                <i class="bi bi-x-circle me-2 fs-6"></i>
                <span>Cancelar envio de ayuda</span>
              </button>

              <button 
                v-else
                class="btn-pill-action btn-pill-help" 
                @click="$emit('abrir-modal', 'ayuda')"
              >
                <i class="bi bi-send-check me-2 fs-6"></i>
                <span>Enviar ayuda</span>
              </button>

            </div>

          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import L from 'leaflet'
import { catalogoServicios, ESTILOS_ESTADO, useAlertas } from './useAlertas'

const props = defineProps({
  solicitud: { type: Object, required: true }
})

defineEmits(['volver', 'abrir-modal'])
const { textoEstado } = useAlertas()

let detailMap = null
let marker = null

const colorEstadoActual = computed(() => {
  return ESTILOS_ESTADO[props.solicitud.estado]?.color || '#ef4444'
})

const renderMap = () => {
  const color = colorEstadoActual.value

  if (!detailMap) {
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

    // 2. Inicializar mapa
    detailMap = L.map('detail-map-atencion', { 
      center: [props.solicitud.lat, props.solicitud.lng],
      zoom: 15,
      layers: [capaCalles],
      zoomControl: false, 
      attributionControl: false 
    })

    // 3. Menú de capas en la esquina superior derecha
    const capasBase = {
      '🗺️ Calles': capaCalles,
      '🛰️ Satelital': capaSatelite,
      '⛰️ Terreno': capaTerreno,
      '🌐 OpenStreetMap': capaOSM
    }

    L.control.layers(capasBase, null, { position: 'topright' }).addTo(detailMap)

    // 4. Marcador circular
    marker = L.circleMarker([props.solicitud.lat, props.solicitud.lng], { 
      radius: 9, 
      fillColor: color, 
      color: '#fff', 
      weight: 2, 
      fillOpacity: 1 
    }).addTo(detailMap)
  } else {
    detailMap.setView([props.solicitud.lat, props.solicitud.lng], 15)
    marker.setLatLng([props.solicitud.lat, props.solicitud.lng])
    marker.setStyle({ fillColor: color })
    detailMap.invalidateSize()
  }
}

watch(() => props.solicitud.estado, () => renderMap())
onMounted(() => renderMap())
</script>

<style scoped>
.detail-container-box {
  background: #ffffff;
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.06);
}

.detail-left-panel {
  background-color: #e5e7eb;
  border-radius: 16px;
  padding: 2.2rem;
}

.detail-title-blue {
  color: #0284c7;
  font-weight: 900;
  font-size: 2.2rem;
  text-align: center;
  letter-spacing: -0.5px;
}

.badge-atencion-module {
  background-color: #e0f2fe;
  color: #0284c7;
  font-weight: 700;
  border-radius: 8px;
}

.service-column-block {
  max-width: 380px;
  width: 100%;
}

.service-icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  transition: transform 0.15s ease;
}

.icon-police   { background-color: #243c5a; color: #fbbf24; }
.icon-redcross { background-color: #ff0000; color: #ffffff; }
.icon-traffic  { background-color: #f59e0b; color: #ffffff; }
.icon-pcivil   { background-color: #ea580c; color: #ffffff; }
.icon-fire     { background-color: #94a3b8; color: #dc2626; }
.icon-citizen  { background-color: #10b981; color: #ffffff; }

#detail-map-atencion {
  height: 220px;
  width: 100%;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  z-index: 1;
}

.info-right-block {
  background: transparent;
  padding: 0.5rem 0.5rem 0.5rem 1rem;
}

.location-text-box {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 14px;
  color: #334155;
  font-size: 0.9rem;
  font-weight: 500;
}

/* ===================================================
   PANEL DERECHO
   =================================================== */
.actions-column-mockup {
  width: 90%;
  margin: 0 auto;
}

.header-status-block {
  margin-bottom: 3.4rem;
}

.title-estado-mockup {
  font-weight: 900;
  font-size: 2.1rem;
  letter-spacing: -0.5px;
  transition: color 0.3s ease;
}

.badge-status-mockup {
  width: 100%;
  padding: 11px 16px;
  border-radius: 8px;
  font-weight: 900;
  font-size: 1.25rem;
  color: #ffffff;
  letter-spacing: 0.5px;
  user-select: none;
  transition: background-color 0.3s ease;
}

.buttons-mockup-group {
  width: 100%;
}

/* Botones píldora */
.btn-pill-action {
  width: 100%;
  max-width: 250px;
  border: none;
  border-radius: 50px;
  padding: 7px 18px;
  font-size: 0.92rem;
  font-weight: 600;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
  user-select: none;
}

.btn-pill-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.16);
  opacity: 0.95;
}

.btn-pill-action:active {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.12);
}

.btn-pill-edit {
  background-color: #f39200;
}

.btn-pill-reject {
  background-color: #17253a;
}

.btn-pill-help {
  background-color: #64a318;
}

.btn-pill-revoke {
  background-color: #64748b;
}
</style>