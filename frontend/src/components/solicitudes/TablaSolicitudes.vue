<template>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <caption class="visually-hidden">Solicitudes de ayuda. Selecciona una fila para ubicarla en el mapa.</caption>
      <thead>
        <tr>
          <th scope="col">Folio</th>
          <th scope="col">Solicitud</th>
          <th scope="col">Usuario</th>
          <th scope="col">Servicios</th>
          <th scope="col">Estado</th>
          <th scope="col" class="text-end">Fecha</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="s in solicitudes"
          :key="s.id"
          :class="{ 'fila-seleccionada': s.id === seleccionadoId }"
          @click="$emit('seleccionar', s.id)"
          @dblclick="$emit('abrir', s.id)"
        >
          <td class="ym-cifras">
            <RouterLink :to="{ name: 'solicitud', params: { id: s.id } }" class="folio" @focus="$emit('seleccionar', s.id)">
              {{ folio(s.id) }}
            </RouterLink>
          </td>
          <td>
            <span class="titulo">{{ s.titulo }}</span>
            <span class="secundario"><i class="bi bi-geo-alt" aria-hidden="true"></i> {{ s.ubicacion_nombre }}</span>
          </td>
          <td>{{ nombreCompleto(s.usuario) }}</td>
          <td><ListaServicios :ids="s.servicios" :catalogo="catalogo" /></td>
          <td><EstadoBadge :estado="s.estado" tamano="sm" /></td>
          <td class="text-end secundario ym-cifras">
            <time :datetime="s.created_at">{{ fechaHora(s.created_at) }}</time>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import EstadoBadge from '../comunes/EstadoBadge.vue'
import ListaServicios from '../comunes/ListaServicios.vue'
import { fechaHora, folio, nombreCompleto } from '../../utils/formato'

defineProps({
  solicitudes: { type: Array, required: true },
  catalogo: { type: Object, required: true },
  seleccionadoId: { type: Number, default: null },
})

defineEmits(['seleccionar', 'abrir'])
</script>

<style scoped>
tbody tr {
  cursor: pointer;
}

.fila-seleccionada > td {
  background: var(--ym-marca-suave);
  box-shadow: none;
}

.fila-seleccionada > td:first-child {
  box-shadow: inset 3px 0 0 var(--ym-marca-texto);
}

.folio {
  font-weight: var(--ym-peso-fuerte);
  color: var(--ym-marca-texto);
}

.titulo {
  display: block;
  font-weight: var(--ym-peso-medio);
}

.secundario {
  display: block;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
  white-space: nowrap;
}

th:first-child,
td:first-child {
  padding-left: var(--ym-espacio-4);
}

th:last-child,
td:last-child {
  padding-right: var(--ym-espacio-4);
}
</style>
