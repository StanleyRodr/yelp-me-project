<template>
  <TransitionGroup tag="ol" name="entrada" class="cola" aria-label="Solicitudes pendientes, de la más antigua a la más reciente">
    <li v-for="item in items" :key="item.id" class="cola-item" :class="`espera-${item.nivel.clave}`">
      <RouterLink
        :to="{ name: 'solicitud', params: { id: item.id } }"
        class="cola-enlace"
        @mouseenter="$emit('resaltar', item.id)"
        @focus="$emit('resaltar', item.id)"
      >
        <span class="espera">
          <span class="espera-tiempo ym-cifras">{{ item.tiempo }}</span>
          <span class="espera-etiqueta">{{ item.nivel.etiqueta }}</span>
        </span>

        <span class="cola-cuerpo">
          <span class="cola-folio ym-cifras">{{ folio(item.id) }}</span>
          <span class="cola-titulo">{{ item.titulo }}</span>
          <span class="cola-meta">
            <span><i class="bi bi-person" aria-hidden="true"></i> {{ nombreCompleto(item.usuario) }}</span>
            <span><i class="bi bi-geo-alt" aria-hidden="true"></i> {{ item.ubicacion_nombre }}</span>
          </span>
        </span>

        <span class="cola-servicios">
          <ListaServicios :ids="item.servicios" :catalogo="catalogo" />
        </span>
      </RouterLink>
    </li>
  </TransitionGroup>
</template>

<script setup>
import { computed } from 'vue'
import ListaServicios from '../comunes/ListaServicios.vue'
import { nivelEspera } from '../../constantes/espera'
import { duracion, folio, minutosDesde, nombreCompleto } from '../../utils/formato'

const props = defineProps({
  /** Pendientes ya ordenadas de la más antigua a la más reciente */
  solicitudes: { type: Array, required: true },
  catalogo: { type: Object, required: true },
  /** Marca de tiempo reactiva para recalcular la espera */
  ahora: { type: Number, required: true },
})

defineEmits(['resaltar'])

const items = computed(() =>
  props.solicitudes.map((s) => {
    const minutos = minutosDesde(s.created_at, props.ahora)
    return { ...s, tiempo: duracion(minutos), nivel: nivelEspera(minutos) }
  }),
)
</script>

<style scoped>
.cola {
  list-style: none;
  margin: 0;
  padding: 0;
}

.cola-item {
  border-top: 1px solid var(--ym-borde);
}

/*
 * Indicador de espera: el elemento memorable del panel.
 * La columna izquierda gana peso e intensidad con los minutos.
 */
.cola-enlace {
  display: grid;
  grid-template-columns: 7.5rem minmax(0, 1fr) auto;
  align-items: stretch;
  color: var(--ym-texto);
  text-decoration: none;
}

.cola-enlace:hover .cola-titulo {
  text-decoration: underline;
}

.cola-enlace:focus-visible {
  outline-offset: -2px;
}

.espera {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.125rem;
  padding: var(--ym-espacio-3) var(--ym-espacio-3) var(--ym-espacio-3) var(--ym-espacio-4);
  border-left: var(--barra) solid var(--tono);
  background: var(--fondo);
  color: var(--texto);
}

.espera-tiempo {
  font-size: var(--ym-texto-lg);
  font-weight: var(--ym-peso-fuerte);
  line-height: 1.1;
}

.espera-etiqueta {
  font-size: var(--ym-texto-sm);
}

.espera-reciente {
  --barra: 3px;
  --tono: var(--ym-borde-fuerte);
  --fondo: transparent;
  --texto: var(--ym-texto-secundario);
}

.espera-espera {
  --barra: 6px;
  --tono: var(--ym-estado-enviado);
  --fondo: var(--ym-estado-enviado-fondo);
  --texto: var(--ym-estado-enviado-texto);
}

.espera-prolongada {
  --barra: 10px;
  --tono: var(--ym-estado-pendiente);
  --fondo: var(--ym-estado-pendiente-fondo);
  --texto: var(--ym-estado-pendiente-texto);
}

.cola-cuerpo {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  min-width: 0;
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
}

.cola-folio {
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.cola-titulo {
  font-weight: var(--ym-peso-medio);
}

.cola-meta {
  display: flex;
  flex-wrap: wrap;
  column-gap: var(--ym-espacio-3);
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.cola-servicios {
  display: flex;
  align-items: center;
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
}

/* Único movimiento del panel: la llegada de una solicitud nueva */
.entrada-enter-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.entrada-enter-from {
  opacity: 0;
  transform: translateY(-0.5rem);
}

@media (max-width: 575.98px) {
  .cola-enlace {
    grid-template-columns: 6rem minmax(0, 1fr);
  }

  .cola-servicios {
    grid-column: 2;
    padding-top: 0;
  }
}
</style>
