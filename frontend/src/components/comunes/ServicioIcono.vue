<template>
  <span class="servicio" :title="servicio.nombre">
    <span class="material-symbols-outlined" aria-hidden="true">{{ servicio.icono }}</span>
    <span :class="{ 'visually-hidden': !conNombre }">{{ servicio.nombre }}</span>
  </span>
</template>

<script setup>
defineProps({
  servicio: { type: Object, required: true },
  conNombre: { type: Boolean, default: false },
})
</script>

<style scoped>
.servicio {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
  color: var(--ym-texto);
  white-space: nowrap;
}

.servicio .material-symbols-outlined {
  color: var(--ym-marca-texto);
}
</style>
