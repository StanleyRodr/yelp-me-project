<template>
  <div class="acciones">
    <div v-if="acciones.length" class="acciones-botones">
      <BotonCarga
        v-for="accion in acciones"
        :key="accion.clave"
        :variante="accion.variante"
        :icono="accion.icono"
        :cargando="enCurso === accion.clave"
        :deshabilitado="Boolean(enCurso)"
        @click="$emit('accion', accion.clave)"
      >
        {{ accion.etiqueta }}
      </BotonCarga>
    </div>
    <p v-if="nota" class="acciones-nota">{{ nota }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import BotonCarga from '../comunes/BotonCarga.vue'
import { tieneServicios } from '../../constantes/transiciones'
import { ESTADO_POR_CLAVE } from '../../constantes/estados'

const props = defineProps({
  solicitud: { type: Object, required: true },
  /** Resultado de accionesPermitidas(solicitud) */
  acciones: { type: Array, required: true },
  /** Clave de la acción que se está ejecutando */
  enCurso: { type: String, default: '' },
})

defineEmits(['accion'])

const nota = computed(() => {
  const s = props.solicitud
  if (ESTADO_POR_CLAVE[s.estado]?.final) {
    return 'La solicitud está cerrada y ya no admite acciones.'
  }
  if (s.estado === 'Pendiente' && !tieneServicios(s)) {
    return 'Solo avisa a los contactos de confianza; quien la creó la da por terminada. Recházala únicamente si es mal uso.'
  }
  return ''
})
</script>

<style scoped>
.acciones-botones {
  display: flex;
  flex-wrap: wrap;
  gap: var(--ym-espacio-2);
}

.acciones-nota {
  margin: var(--ym-espacio-3) 0 0;
  color: var(--ym-texto-secundario);
  max-width: 60ch;
}

.acciones-botones + .acciones-nota {
  margin-top: var(--ym-espacio-3);
}

.acciones-nota:first-child {
  margin-top: 0;
}
</style>
