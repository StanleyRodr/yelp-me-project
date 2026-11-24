<template>
  <span v-if="lista.length" class="servicios" :class="{ 'servicios-columna': conNombre }">
    <ServicioIcono v-for="s in lista" :key="s.id" :servicio="s" :con-nombre="conNombre" />
  </span>
  <span v-else class="solo-contactos">
    <i class="bi bi-people" aria-hidden="true"></i> Solo contactos
  </span>
</template>

<script setup>
import { computed } from 'vue'
import ServicioIcono from './ServicioIcono.vue'

const props = defineProps({
  /** ids de servicio de la solicitud */
  ids: { type: Array, default: () => [] },
  /** mapa id → servicio (store catalogos.servicioPorId) */
  catalogo: { type: Object, required: true },
  conNombre: { type: Boolean, default: false },
})

const lista = computed(() => props.ids.map((id) => props.catalogo[id]).filter(Boolean))
</script>

<style scoped>
.servicios {
  display: inline-flex;
  flex-wrap: wrap;
  gap: var(--ym-espacio-2);
}

.servicios-columna {
  flex-direction: column;
  gap: var(--ym-espacio-1);
}

.solo-contactos {
  color: var(--ym-texto-secundario);
  font-size: var(--ym-texto-sm);
  white-space: nowrap;
}
</style>
