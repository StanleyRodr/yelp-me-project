<template>
  <SkeletonContenedor :etiqueta="etiqueta">
    <div class="detalle">
      <span class="placeholder placeholder-lg col-7"></span>
      <span class="placeholder col-3"></span>
      <div v-for="n in campos" :key="n" class="detalle-campo">
        <span class="placeholder placeholder-sm col-2"></span>
        <span class="placeholder" :class="n % 2 ? 'col-8' : 'col-5'"></span>
      </div>
    </div>
  </SkeletonContenedor>
</template>

<script setup>
import SkeletonContenedor from './SkeletonContenedor.vue'

defineProps({
  campos: { type: Number, default: 4 },
  etiqueta: { type: String, default: 'Cargando detalle…' },
})
</script>

<style scoped>
.detalle {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-3);
}

.detalle-campo {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-1);
}
</style>
