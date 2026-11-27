<template>
  <div :class="['aviso', `aviso-${tipo}`]" :role="tipo === 'error' ? 'alert' : 'status'">
    <i :class="['bi', tipo === 'error' ? 'bi-exclamation-circle' : 'bi-info-circle']" aria-hidden="true"></i>
    <span><slot /></span>
  </div>
</template>

<script setup>
/** Aviso informativo o de error arriba de un formulario de recuperación. */
defineProps({
  tipo: { type: String, default: 'info' },
})
</script>

<style scoped>
.aviso {
  display: flex;
  gap: var(--ym-espacio-2);
  align-items: flex-start;
  padding: var(--ym-espacio-3);
  margin-bottom: var(--ym-espacio-4);
  border: 1px solid var(--ym-borde-fuerte);
  border-radius: var(--ym-radio);
  background: var(--ym-marca-suave);
  color: var(--ym-texto);
}

.aviso-error {
  border-color: var(--ym-estado-pendiente);
  background: var(--ym-estado-pendiente-fondo);
  color: var(--ym-estado-pendiente-texto);
}
</style>
