<template>
  <SkeletonContenedor :etiqueta="etiqueta">
    <span class="placeholder w-100 d-block" :style="{ height: alto }"></span>
  </SkeletonContenedor>
</template>

<script setup>
import SkeletonContenedor from './SkeletonContenedor.vue'

/** Bloque del tamaño final (p. ej. el mapa) para que la página no salte. */
defineProps({
  alto: { type: String, default: '420px' },
  etiqueta: { type: String, default: 'Cargando…' },
})
</script>
