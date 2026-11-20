<template>
  <span class="cuenta" :class="`cuenta-${info.token}`">
    <span class="material-symbols-outlined" aria-hidden="true">{{ info.icono }}</span>
    {{ estado }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { CUENTA_POR_CLAVE } from '../../constantes/cuentas'

const props = defineProps({
  estado: { type: String, required: true },
})

const info = computed(() => CUENTA_POR_CLAVE[props.estado] ?? CUENTA_POR_CLAVE.Activo)
</script>

<style scoped>
.cuenta {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
  padding: 0 var(--ym-espacio-2);
  border: 1px solid var(--e);
  border-radius: var(--ym-radio);
  background: var(--e-fondo);
  color: var(--e-texto);
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-medio);
  line-height: 1.6;
  white-space: nowrap;
}

.cuenta-terminado { --e: var(--ym-estado-terminado); --e-texto: var(--ym-estado-terminado-texto); --e-fondo: var(--ym-estado-terminado-fondo); }
.cuenta-pendiente { --e: var(--ym-estado-pendiente); --e-texto: var(--ym-estado-pendiente-texto); --e-fondo: var(--ym-estado-pendiente-fondo); }
.cuenta-cancelado { --e: var(--ym-estado-cancelado); --e-texto: var(--ym-estado-cancelado-texto); --e-fondo: var(--ym-estado-cancelado-fondo); }
</style>
