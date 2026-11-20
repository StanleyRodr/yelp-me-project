<template>
  <span class="medidor" :class="{ 'medidor-lleno': activos >= limite }">
    <span class="medidor-segmentos" aria-hidden="true">
      <span v-for="n in limite" :key="n" class="segmento" :class="{ 'segmento-activo': n <= activos }"></span>
    </span>
    <span class="medidor-texto ym-cifras">{{ activos }} de {{ limite }}</span>
    <span class="visually-hidden"> strikes activos</span>
  </span>
</template>

<script setup>
defineProps({
  activos: { type: Number, required: true },
  limite: { type: Number, default: 3 },
})
</script>

<style scoped>
.medidor {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  white-space: nowrap;
}

.medidor-segmentos {
  display: inline-flex;
  gap: 2px;
}

.segmento {
  width: 0.875rem;
  height: 0.5rem;
  border-radius: 2px;
  background: var(--ym-skeleton);
  border: 1px solid var(--ym-borde-fuerte);
}

.segmento-activo {
  background: var(--ym-estado-enviado);
  border-color: var(--ym-estado-enviado);
}

.medidor-lleno .segmento-activo {
  background: var(--ym-estado-pendiente);
  border-color: var(--ym-estado-pendiente);
}

.medidor-texto {
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.medidor-lleno .medidor-texto {
  color: var(--ym-estado-pendiente-texto);
  font-weight: var(--ym-peso-medio);
}
</style>
