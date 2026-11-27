<template>
  <div class="rejilla">
    <nav class="rejilla-volver" aria-label="Ruta">
      <RouterLink :to="{ name: 'solicitudes' }"><i class="bi bi-arrow-left" aria-hidden="true"></i> Solicitudes</RouterLink>
    </nav>

    <template v-if="mostrarSkeleton">
      <TarjetaSeccion class="rejilla-principal"><SkeletonDetalle :campos="6" etiqueta="Cargando solicitud…" /></TarjetaSeccion>
      <div class="rejilla-lateral"><SkeletonBloque alto="320px" etiqueta="Cargando mapa…" /></div>
    </template>

    <EstadoVacio
      v-else-if="!store.cargandoActual && !s"
      class="rejilla-principal"
      icono="search_off"
      :titulo="`No existe la solicitud ${folio(id)}`"
      mensaje="Revisa el folio o vuelve al listado de solicitudes."
    >
      <RouterLink :to="{ name: 'solicitudes' }" class="btn btn-outline-primary">Ver solicitudes</RouterLink>
    </EstadoVacio>

    <template v-else-if="s">
      <CabeceraSolicitud class="rejilla-cabecera" :solicitud="s" />

      <div class="rejilla-principal">
        <TarjetaSeccion titulo="Acciones">
          <AccionesSolicitud :solicitud="s" :acciones="acciones" :en-curso="enCurso" @accion="ejecutar" />
        </TarjetaSeccion>
        <TarjetaSeccion titulo="Detalle">
          <DetalleSolicitud :solicitud="s" :catalogo="catalogos.servicioPorId" />
        </TarjetaSeccion>
        <TarjetaDomicilio :domicilio="s.usuario?.domicilio ?? null" titulo="Domicilio del solicitante" />
      </div>

      <div class="rejilla-lateral">
        <TarjetaSeccion titulo="Ubicación">
          <MapaBase :marcadores="[s]" :seleccionado-id="s.id" alto="320px" :etiqueta="`Ubicación de ${s.titulo}`" />
        </TarjetaSeccion>
        <TarjetaSeccion titulo="Historial">
          <LineaTiempo :eventos="eventosDe(s)" />
        </TarjetaSeccion>
      </div>

      <ModalEditarServicios v-model:abierto="modal.servicios" :solicitud="s" />
      <ModalRechazar v-model:abierto="modal.rechazar" :solicitud="s" />
    </template>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import DetalleSolicitud from '../components/solicitudes/DetalleSolicitud.vue'
import TarjetaDomicilio from '../components/comunes/TarjetaDomicilio.vue'
import AccionesSolicitud from '../components/solicitudes/AccionesSolicitud.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import CabeceraSolicitud from '../components/solicitudes/CabeceraSolicitud.vue'
import MapaBase from '../components/comunes/MapaBase.vue'
import LineaTiempo from '../components/comunes/LineaTiempo.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonDetalle from '../components/comunes/SkeletonDetalle.vue'
import SkeletonBloque from '../components/comunes/SkeletonBloque.vue'
import ModalEditarServicios from '../components/modals/ModalEditarServicios.vue'
import ModalRechazar from '../components/modals/ModalRechazar.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { accionesPermitidas } from '../constantes/transiciones'
import { useSolicitudesStore } from '../stores/solicitudes'
import { useCatalogosStore } from '../stores/catalogos'
import { confirmar, exito } from '../plugins/alertas'
import { eventosDe } from '../utils/eventosSolicitud'
import { folio } from '../utils/formato'

const props = defineProps({ id: { type: String, required: true } })

const store = useSolicitudesStore()
const catalogos = useCatalogosStore()
const s = computed(() => store.actual)
const acciones = computed(() => accionesPermitidas(s.value))
const modal = reactive({ servicios: false, rechazar: false })
const enCurso = ref('')

const cargaDiferida = useCargaDiferida(() => store.cargandoActual)
const mostrarSkeleton = computed(() => cargaDiferida.value && !s.value)

catalogos.cargar()
watch(() => props.id, (id) => store.cargarUna(id), { immediate: true })

const nombresServicios = () =>
  s.value.servicios.map((id) => catalogos.servicioPorId[id]?.nombre).filter(Boolean).join(', ')

async function ejecutar(clave) {
  if (clave === 'editarServicios') return (modal.servicios = true)
  if (clave === 'rechazar') return (modal.rechazar = true)

  const dialogos = {
    enviar: {
      titulo: 'Enviar ayuda',
      // "¿Dónde?" es opcional en la app: sin referencia escrita se usa el punto del mapa.
      texto: `Se despachará ${nombresServicios()} a ${s.value.ubicacion_nombre || 'la ubicación marcada en el mapa'}.`,
      confirmar: 'Enviar ayuda',
      accion: () => store.enviar(s.value.id),
      exito: 'Ayuda enviada',
    },
    terminar: {
      titulo: 'Marcar como terminada',
      texto: 'Confirma que la atención concluyó. La solicitud quedará cerrada.',
      confirmar: 'Marcar como terminada',
      accion: () => store.terminar(s.value.id),
      exito: 'Solicitud terminada',
    },
  }

  const d = dialogos[clave]
  const accion = d.accion
  // Si otro admin ya la atendió (409), recargar para mostrar su estado real.
  d.accion = async () => {
    try {
      await accion()
    } catch (e) {
      if (e.codigo === 409) store.cargarUna(s.value.id, { conservar: true })
      throw e
    }
  }
  enCurso.value = clave
  try {
    if (await confirmar(d)) exito(d.exito)
  } finally {
    enCurso.value = ''
  }
}
</script>

<style scoped>
.rejilla {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
  grid-template-areas:
    'volver volver'
    'cabecera cabecera'
    'principal lateral';
  gap: var(--ym-espacio-4);
  align-items: start;
}

.rejilla-volver { grid-area: volver; }
.rejilla-cabecera { grid-area: cabecera; }

.rejilla-principal,
.rejilla-lateral {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-4);
  min-width: 0;
}

.rejilla-principal { grid-area: principal; }
.rejilla-lateral { grid-area: lateral; }

@media (max-width: 991.98px) {
  .rejilla {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'volver' 'cabecera' 'principal' 'lateral';
  }
}
</style>
