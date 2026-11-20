<template>
  <EstadoVacio v-if="!solicitudes.length" icono="inbox" titulo="Sin solicitudes" mensaje="Este usuario no ha creado solicitudes de ayuda." />
  <ul v-else class="lista">
    <li v-for="s in ordenadas" :key="s.id" class="lista-item">
      <RouterLink :to="{ name: 'solicitud', params: { id: s.id } }" class="lista-enlace">
        <span class="folio ym-cifras">{{ folio(s.id) }}</span>
        <span class="titulo">{{ s.titulo }}</span>
        <EstadoBadge :estado="s.estado" tamano="sm" />
        <time class="fecha ym-cifras" :datetime="s.created_at">{{ fecha(s.created_at) }}</time>
      </RouterLink>
    </li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'
import EstadoBadge from '../comunes/EstadoBadge.vue'
import EstadoVacio from '../comunes/EstadoVacio.vue'
import { fecha, folio } from '../../utils/formato'

const props = defineProps({
  solicitudes: { type: Array, required: true },
})

const ordenadas = computed(() => [...props.solicitudes].sort((a, b) => b.created_at.localeCompare(a.created_at)))
</script>

<style scoped>
.lista {
  list-style: none;
  margin: 0;
  padding: 0;
}

.lista-item + .lista-item {
  border-top: 1px solid var(--ym-borde);
}

.lista-enlace {
  display: grid;
  grid-template-columns: 4rem minmax(0, 1fr) auto 6.5rem;
  align-items: center;
  gap: var(--ym-espacio-3);
  padding: var(--ym-espacio-2) var(--ym-espacio-4);
  color: var(--ym-texto);
  text-decoration: none;
}

.lista-enlace:hover {
  background: var(--ym-marca-suave);
}

.folio {
  color: var(--ym-marca);
  font-weight: var(--ym-peso-fuerte);
}

.titulo {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fecha {
  text-align: right;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

@media (max-width: 575.98px) {
  .lista-enlace {
    grid-template-columns: 4rem minmax(0, 1fr);
  }
}
</style>
