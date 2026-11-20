<template>
  <div class="vacio" role="status">
    <span class="material-symbols-outlined vacio-icono" aria-hidden="true">{{ icono }}</span>
    <p class="vacio-titulo">{{ titulo }}</p>
    <p v-if="mensaje" class="vacio-mensaje">{{ mensaje }}</p>
    <div v-if="$slots.default" class="vacio-acciones">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  titulo: { type: String, required: true },
  mensaje: { type: String, default: '' },
  icono: { type: String, default: 'inbox' },
})
</script>

<style scoped>
.vacio {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--ym-espacio-1);
  padding: var(--ym-espacio-5) var(--ym-espacio-4);
}

.vacio-icono {
  font-size: 2rem;
  color: var(--ym-texto-secundario);
}

.vacio-titulo {
  margin: var(--ym-espacio-2) 0 0;
  font-weight: var(--ym-peso-medio);
}

.vacio-mensaje {
  margin: 0;
  color: var(--ym-texto-secundario);
  max-width: 60ch;
}

.vacio-acciones {
  margin-top: var(--ym-espacio-3);
}
</style>
