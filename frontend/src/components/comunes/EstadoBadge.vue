<template>
  <span class="estado" :class="[`estado-${info.token}`, `estado-${tamano}`]">
    <span class="material-symbols-outlined" aria-hidden="true">{{ info.icono }}</span>
    <span>{{ estado }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { ESTADO_POR_CLAVE } from '../../constantes/estados'

const props = defineProps({
  estado: { type: String, required: true },
  tamano: { type: String, default: 'md', validator: (v) => ['sm', 'md', 'lg'].includes(v) },
})

const info = computed(() => ESTADO_POR_CLAVE[props.estado] ?? { icono: 'help', token: 'cancelado' })
</script>

<style scoped>
.estado {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
  padding: 0.125rem var(--ym-espacio-2);
  border: 1px solid var(--e);
  border-radius: var(--ym-radio);
  background: var(--e-fondo);
  color: var(--e-texto);
  font-weight: var(--ym-peso-medio);
  font-size: var(--ym-texto-sm);
  line-height: 1.5;
  white-space: nowrap;
}

.estado-sm { padding: 0 var(--ym-espacio-1); }
.estado-lg { font-size: var(--ym-texto-base); padding: var(--ym-espacio-1) var(--ym-espacio-3); }

.estado-pendiente { --e: var(--ym-estado-pendiente); --e-texto: var(--ym-estado-pendiente-texto); --e-fondo: var(--ym-estado-pendiente-fondo); }
.estado-enviado { --e: var(--ym-estado-enviado); --e-texto: var(--ym-estado-enviado-texto); --e-fondo: var(--ym-estado-enviado-fondo); }
.estado-terminado { --e: var(--ym-estado-terminado); --e-texto: var(--ym-estado-terminado-texto); --e-fondo: var(--ym-estado-terminado-fondo); }
.estado-rechazado { --e: var(--ym-estado-rechazado); --e-texto: var(--ym-estado-rechazado-texto); --e-fondo: var(--ym-estado-rechazado-fondo); }
.estado-cancelado { --e: var(--ym-estado-cancelado); --e-texto: var(--ym-estado-cancelado-texto); --e-fondo: var(--ym-estado-cancelado-fondo); }
</style>
