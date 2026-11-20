<template>
  <div class="placeholder-glow" role="status" aria-busy="true" aria-live="polite">
    <span class="visually-hidden">{{ etiqueta }}</span>
    <div aria-hidden="true">
      <slot />
    </div>
  </div>
</template>

<script setup>
/** Envoltura accesible de todo skeleton: anuncia la carga, oculta los bloques. */
defineProps({
  etiqueta: { type: String, default: 'Cargando…' },
})
</script>
