<template>
  <div class="rejilla">
    <header class="rejilla-cabecera">
      <h1>Inicio</h1>
    </header>

    <div class="rejilla-franja">
      <SkeletonBloque v-if="mostrarSkeleton" alto="64px" etiqueta="Cargando conteo por estado…" />
      <FranjaEstados v-else-if="hayDatos" :conteos="store.conteoPorEstado" />
    </div>

    <TarjetaSeccion
      class="rejilla-cola"
      titulo="Por atender"
      descripcion="De la más antigua a la más reciente."
      sin-relleno
    >
      <SkeletonLista v-if="mostrarSkeleton" :elementos="4" etiqueta="Cargando solicitudes pendientes…" />
      <EstadoVacio v-else-if="store.error" icono="error" titulo="No se pudieron cargar las solicitudes" :mensaje="store.error">
        <button type="button" class="btn btn-outline-primary" @click="store.cargar()">Reintentar</button>
      </EstadoVacio>
      <EstadoVacio
        v-else-if="hayDatos && !store.pendientesPorAntiguedad.length"
        icono="task_alt"
        titulo="No hay solicitudes pendientes"
        mensaje="Las nuevas solicitudes aparecerán aquí en orden de llegada."
      />
      <ColaPendientes
        v-else-if="hayDatos"
        :solicitudes="store.pendientesPorAntiguedad"
        :catalogo="catalogos.servicioPorId"
        :ahora="ahora"
        @resaltar="seleccionadoId = $event"
      />
    </TarjetaSeccion>

    <TarjetaSeccion class="rejilla-mapa" titulo="Mapa de solicitudes activas">
      <SkeletonBloque v-if="mostrarSkeleton" alto="520px" etiqueta="Cargando mapa…" />
      <MapaIncidentes
        v-else-if="hayDatos"
        :solicitudes="store.activas"
        :seleccionado-id="seleccionadoId"
        @seleccionar="abrir"
      />
    </TarjetaSeccion>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import FranjaEstados from '../components/dashboard/FranjaEstados.vue'
import ColaPendientes from '../components/dashboard/ColaPendientes.vue'
import MapaIncidentes from '../components/dashboard/MapaIncidentes.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonBloque from '../components/comunes/SkeletonBloque.vue'
import SkeletonLista from '../components/comunes/SkeletonLista.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { useAhora } from '../composables/useAhora'
import { useSolicitudesStore } from '../stores/solicitudes'
import { useCatalogosStore } from '../stores/catalogos'

const store = useSolicitudesStore()
const catalogos = useCatalogosStore()
const router = useRouter()
const ahora = useAhora()
const seleccionadoId = ref(null)

// Con datos ya cargados se conservan (sin skeleton) mientras se refrescan.
const hayDatos = computed(() => store.lista.length > 0)
const cargaDiferida = useCargaDiferida(() => store.cargando)
const mostrarSkeleton = computed(() => cargaDiferida.value && !hayDatos.value)

onMounted(() => {
  catalogos.cargar()
  store.cargar()
})

const abrir = (id) => router.push({ name: 'solicitud', params: { id } })
</script>

<style scoped>
.rejilla {
  display: grid;
  grid-template-columns: minmax(0, 5fr) minmax(0, 7fr);
  grid-template-areas:
    'cabecera cabecera'
    'franja franja'
    'cola mapa';
  gap: var(--ym-espacio-4);
  align-items: start;
}

.rejilla-cabecera { grid-area: cabecera; }
.rejilla-cabecera h1 { margin: 0; }
.rejilla-franja { grid-area: franja; }
.rejilla-cola { grid-area: cola; }
.rejilla-mapa { grid-area: mapa; }

@media (max-width: 991.98px) {
  .rejilla {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'cabecera' 'franja' 'cola' 'mapa';
  }
}
</style>
