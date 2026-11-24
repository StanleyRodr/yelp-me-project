<template>
  <button
    :type="tipo"
    class="btn"
    :class="`btn-${variante}`"
    :disabled="cargando || deshabilitado"
    :aria-busy="cargando || undefined"
  >
    <span v-if="cargando" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
    <i v-else-if="icono" :class="['bi', icono, 'me-2']" aria-hidden="true"></i>
    <slot />
    <span v-if="cargando" class="visually-hidden">, en proceso</span>
  </button>
</template>

<script setup>
/** Botón de acción con círculo de carga pequeño (regla: círculo solo en acciones). */
defineProps({
  cargando: { type: Boolean, default: false },
  deshabilitado: { type: Boolean, default: false },
  variante: { type: String, default: 'primary' },
  icono: { type: String, default: '' },
  tipo: { type: String, default: 'button' },
})
</script>
