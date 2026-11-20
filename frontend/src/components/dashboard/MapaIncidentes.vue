<template>
  <div class="mapa-incidentes">
    <MapaBase
      :marcadores="marcadores"
      :seleccionado-id="seleccionadoId"
      :alto="alto"
      etiqueta="Mapa de solicitudes activas"
      @seleccionar="$emit('seleccionar', $event)"
    />
    <ul class="leyenda" aria-label="Leyenda del mapa">
      <li v-for="estado in leyenda" :key="estado.clave" :class="`leyenda-${estado.token}`">
        <span class="leyenda-punto" aria-hidden="true"></span>{{ estado.clave }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import MapaBase from '../comunes/MapaBase.vue'
import { ESTADO_POR_CLAVE, ESTADOS_ACTIVOS } from '../../constantes/estados'

const props = defineProps({
  /** Solicitudes activas (Pendiente y Enviado) */
  solicitudes: { type: Array, required: true },
  seleccionadoId: { type: Number, default: null },
  alto: { type: String, default: '520px' },
})

defineEmits(['seleccionar'])

const marcadores = computed(() =>
  props.solicitudes.map(({ id, latitud, longitud, estado, titulo }) => ({ id, latitud, longitud, estado, titulo })),
)

const leyenda = ESTADOS_ACTIVOS.map((clave) => ESTADO_POR_CLAVE[clave])
</script>

<style scoped>
.leyenda {
  display: flex;
  gap: var(--ym-espacio-4);
  list-style: none;
  margin: var(--ym-espacio-2) 0 0;
  padding: 0;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.leyenda li {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
}

.leyenda-punto {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background: var(--p);
}

.leyenda-pendiente { --p: var(--ym-estado-pendiente); }
.leyenda-enviado { --p: var(--ym-estado-enviado); }
</style>
