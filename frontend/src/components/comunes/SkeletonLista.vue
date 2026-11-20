<template>
  <SkeletonContenedor :etiqueta="etiqueta">
    <ul class="lista">
      <li v-for="n in elementos" :key="n" class="lista-elemento">
        <span class="placeholder col-2"></span>
        <span class="placeholder" :class="n % 2 ? 'col-9' : 'col-7'"></span>
        <span class="placeholder placeholder-sm col-5"></span>
      </li>
    </ul>
  </SkeletonContenedor>
</template>

<script setup>
import SkeletonContenedor from './SkeletonContenedor.vue'

defineProps({
  elementos: { type: Number, default: 4 },
  etiqueta: { type: String, default: 'Cargando lista…' },
})
</script>

<style scoped>
.lista {
  list-style: none;
  margin: 0;
  padding: 0;
}

.lista-elemento {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-2);
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
  border-bottom: 1px solid var(--ym-borde);
}
</style>
