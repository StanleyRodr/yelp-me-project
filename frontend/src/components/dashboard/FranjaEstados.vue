<template>
  <nav class="franja" aria-label="Solicitudes por estado">
    <RouterLink
      v-for="estado in ESTADOS"
      :key="estado.clave"
      :to="{ name: 'solicitudes', query: { estado: estado.clave } }"
      class="franja-item"
      :class="`franja-${estado.token}`"
    >
      <span class="material-symbols-outlined franja-icono" aria-hidden="true">{{ estado.icono }}</span>
      <span class="franja-etiqueta">{{ estado.clave }}</span>
      <span class="franja-conteo ym-cifras">{{ conteos[estado.clave] ?? 0 }}</span>
    </RouterLink>
  </nav>
</template>

<script setup>
import { ESTADOS } from '../../constantes/estados'

defineProps({
  /** { Pendiente: 2, Enviado: 1, ... } */
  conteos: { type: Object, required: true },
})
</script>

<style scoped>
.franja {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  background: var(--ym-superficie);
  border: 1px solid var(--ym-borde);
  border-radius: var(--ym-radio);
  overflow: hidden;
}

.franja-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: var(--ym-espacio-2);
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
  color: var(--ym-texto);
  text-decoration: none;
  border-top: 3px solid var(--e);
}

.franja-item + .franja-item {
  border-left: 1px solid var(--ym-borde);
}

.franja-item:hover {
  background: var(--e-fondo);
}

.franja-icono {
  color: var(--e-texto);
}

.franja-etiqueta {
  font-weight: var(--ym-peso-medio);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.franja-conteo {
  font-size: var(--ym-texto-xl);
  font-weight: var(--ym-peso-fuerte);
  line-height: 1;
  color: var(--e-texto);
}

.franja-pendiente { --e: var(--ym-estado-pendiente); --e-texto: var(--ym-estado-pendiente-texto); --e-fondo: var(--ym-estado-pendiente-fondo); }
.franja-enviado { --e: var(--ym-estado-enviado); --e-texto: var(--ym-estado-enviado-texto); --e-fondo: var(--ym-estado-enviado-fondo); }
.franja-terminado { --e: var(--ym-estado-terminado); --e-texto: var(--ym-estado-terminado-texto); --e-fondo: var(--ym-estado-terminado-fondo); }
.franja-rechazado { --e: var(--ym-estado-rechazado); --e-texto: var(--ym-estado-rechazado-texto); --e-fondo: var(--ym-estado-rechazado-fondo); }
.franja-cancelado { --e: var(--ym-estado-cancelado); --e-texto: var(--ym-estado-cancelado-texto); --e-fondo: var(--ym-estado-cancelado-fondo); }

@media (max-width: 991.98px) {
  .franja {
    grid-template-columns: repeat(auto-fit, minmax(9rem, 1fr));
  }

  .franja-item + .franja-item {
    border-left: 0;
  }

  .franja-item {
    border-right: 1px solid var(--ym-borde);
    border-bottom: 1px solid var(--ym-borde);
  }
}
</style>
