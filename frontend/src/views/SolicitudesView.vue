<template>
  <div class="rejilla">
    <header class="rejilla-cabecera">
      <h1>Solicitudes</h1>
      <FiltrosSolicitudes
        v-model:estado="estado"
        v-model:busqueda="busqueda"
        :conteos="store.conteoPorEstado"
        :total="store.lista.length"
      />
    </header>

    <TarjetaSeccion class="rejilla-tabla" sin-relleno>
      <SkeletonTabla v-if="mostrarSkeleton" :filas="8" :columnas="6" etiqueta="Cargando solicitudes…" />
      <EstadoVacio v-else-if="store.error" icono="error" titulo="No se pudieron cargar las solicitudes" :mensaje="store.error">
        <button type="button" class="btn btn-outline-primary" @click="store.cargar()">Reintentar</button>
      </EstadoVacio>
      <EstadoVacio
        v-else-if="hayDatos && !filtradas.length"
        icono="search_off"
        titulo="Ninguna solicitud coincide con el filtro"
        mensaje="Prueba con otro estado o con otra búsqueda."
      >
        <button type="button" class="btn btn-outline-primary" @click="quitarFiltros">Quitar filtros</button>
      </EstadoVacio>
      <TablaSolicitudes
        v-else-if="hayDatos"
        :solicitudes="filtradas"
        :catalogo="catalogos.servicioPorId"
        :seleccionado-id="seleccionadoId"
        @seleccionar="seleccionadoId = $event"
        @abrir="abrir"
      />
    </TarjetaSeccion>

    <aside class="rejilla-mapa" aria-label="Ubicación de las solicitudes filtradas">
      <SkeletonBloque v-if="mostrarSkeleton" alto="560px" etiqueta="Cargando mapa…" />
      <MapaBase
        v-else-if="hayDatos"
        :marcadores="filtradas"
        :seleccionado-id="seleccionadoId"
        alto="560px"
        etiqueta="Mapa de las solicitudes filtradas"
        @seleccionar="seleccionadoId = $event"
      />
    </aside>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FiltrosSolicitudes from '../components/solicitudes/FiltrosSolicitudes.vue'
import TablaSolicitudes from '../components/solicitudes/TablaSolicitudes.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import MapaBase from '../components/comunes/MapaBase.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonTabla from '../components/comunes/SkeletonTabla.vue'
import SkeletonBloque from '../components/comunes/SkeletonBloque.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { ESTADO_POR_CLAVE } from '../constantes/estados'
import { useSolicitudesStore } from '../stores/solicitudes'
import { useCatalogosStore } from '../stores/catalogos'
import { folio, nombreCompleto, normalizar } from '../utils/formato'

const store = useSolicitudesStore()
const catalogos = useCatalogosStore()
const route = useRoute()
const router = useRouter()

const busqueda = ref('')
const seleccionadoId = ref(null)

// El filtro de estado vive en la URL (?estado=Pendiente) para poder enlazarlo.
const estado = computed({
  get: () => (ESTADO_POR_CLAVE[route.query.estado] ? route.query.estado : null),
  set: (valor) => router.replace({ query: { ...route.query, estado: valor ?? undefined } }),
})

// Texto donde se busca: folio, título, usuario, ubicación y nombres de los servicios.
const textoBuscable = (s) =>
  normalizar([
    folio(s.id),
    s.titulo,
    nombreCompleto(s.usuario),
    s.ubicacion_nombre,
    ...s.servicios.map((id) => catalogos.servicioPorId[id]?.nombre ?? ''),
  ].join(' '))

const filtradas = computed(() => {
  const texto = normalizar(busqueda.value.trim())
  return store.lista
    .filter((s) => !estado.value || s.estado === estado.value)
    .filter((s) => !texto || textoBuscable(s).includes(texto))
    .sort((a, b) => b.created_at.localeCompare(a.created_at))
})

const hayDatos = computed(() => store.lista.length > 0)
const cargaDiferida = useCargaDiferida(() => store.cargando)
const mostrarSkeleton = computed(() => cargaDiferida.value && !hayDatos.value)

onMounted(() => {
  catalogos.cargar()
  store.cargar()
})

function quitarFiltros() {
  busqueda.value = ''
  estado.value = null
}

const abrir = (id) => router.push({ name: 'solicitud', params: { id } })
</script>

<style scoped>
.rejilla {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
  grid-template-areas:
    'cabecera cabecera'
    'tabla mapa';
  gap: var(--ym-espacio-4);
  align-items: start;
}

.rejilla-cabecera {
  grid-area: cabecera;
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-3);
}

.rejilla-cabecera h1 { margin: 0; }
.rejilla-tabla { grid-area: tabla; }

.rejilla-mapa {
  grid-area: mapa;
  position: sticky;
  top: calc(var(--ym-alto-navbar) + var(--ym-espacio-4));
}

@media (max-width: 1199.98px) {
  .rejilla {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'cabecera' 'tabla' 'mapa';
  }

  .rejilla-mapa {
    position: static;
  }
}
</style>
