<template>
  <header class="cabecera">
    <p class="folio ym-cifras">{{ folio(solicitud.id) }}</p>
    <h1 class="titulo">{{ solicitud.titulo }}</h1>
    <EstadoBadge :estado="solicitud.estado" tamano="lg" />
  </header>
</template>

<script setup>
import EstadoBadge from '../comunes/EstadoBadge.vue'
import { folio } from '../../utils/formato'

defineProps({
  solicitud: { type: Object, required: true },
})
</script>

<style scoped>
.folio {
  margin: 0;
  color: var(--ym-texto-secundario);
  font-weight: var(--ym-peso-medio);
}

.titulo {
  margin: 0 0 var(--ym-espacio-2);
}
</style>
