<template>
  <ol class="linea">
    <li v-for="evento in eventos" :key="evento.clave" class="evento" :class="`evento-${evento.token ?? 'neutro'}`">
      <span class="evento-punto" aria-hidden="true"></span>
      <div class="evento-contenido">
        <p class="evento-titulo">{{ evento.titulo }}</p>
        <p class="evento-meta">
          <time class="ym-cifras" :datetime="evento.fecha">{{ fechaHora(evento.fecha) }}</time>
          <span v-if="evento.autor"> por {{ evento.autor }}</span>
        </p>
        <p v-if="evento.detalle" class="evento-detalle">{{ evento.detalle }}</p>
      </div>
    </li>
  </ol>
</template>

<script setup>
import { fechaHora } from '../../utils/formato'

defineProps({
  /** [{ clave, titulo, fecha, autor?, detalle?, token? }] token = estado (pendiente, enviado...) */
  eventos: { type: Array, required: true },
})
</script>

<style scoped>
.linea {
  list-style: none;
  margin: 0;
  padding: 0;
}

.evento {
  position: relative;
  display: grid;
  grid-template-columns: 1rem 1fr;
  gap: var(--ym-espacio-3);
  padding-bottom: var(--ym-espacio-4);
}

/* Línea vertical entre puntos */
.evento:not(:last-child)::before {
  content: '';
  position: absolute;
  left: calc(0.5rem - 1px);
  top: 1.1rem;
  bottom: 0;
  width: 2px;
  background: var(--ym-borde);
}

.evento-punto {
  width: 1rem;
  height: 1rem;
  margin-top: 0.2rem;
  border-radius: 50%;
  border: 3px solid var(--p, var(--ym-borde-fuerte));
  background: var(--ym-superficie);
}

.evento-pendiente { --p: var(--ym-estado-pendiente); }
.evento-enviado { --p: var(--ym-estado-enviado); }
.evento-terminado { --p: var(--ym-estado-terminado); }
.evento-rechazado { --p: var(--ym-estado-rechazado); }
.evento-cancelado { --p: var(--ym-estado-cancelado); }

.evento-titulo {
  margin: 0;
  font-weight: var(--ym-peso-medio);
}

.evento-meta,
.evento-detalle {
  margin: 0;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.evento-detalle {
  margin-top: var(--ym-espacio-1);
  color: var(--ym-texto);
  max-width: 60ch;
}
</style>
