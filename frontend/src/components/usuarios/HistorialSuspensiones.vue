<template>
  <EstadoVacio v-if="!suspensiones.length" icono="event_available" titulo="Sin suspensiones" mensaje="La cuenta nunca ha sido suspendida." />
  <ul v-else class="lista">
    <li v-for="s in ordenadas" :key="s.id" class="suspension" :class="{ 'suspension-vigente': !s.fecha_rehabilitacion }">
      <p class="titulo">
        {{ s.fecha_rehabilitacion ? 'Rehabilitada' : 'Vigente' }}
        <span class="secundario">({{ s.suspendido_por_nombre ? `por ${s.suspendido_por_nombre}` : 'automática por 3 strikes' }})</span>
      </p>
      <p class="motivo">{{ s.motivo }}</p>
      <p class="secundario ym-cifras">
        Desde {{ fechaHora(s.fecha_suspension) }}
        <template v-if="s.fecha_rehabilitacion">
          hasta {{ fechaHora(s.fecha_rehabilitacion) }} ({{ s.rehabilitado_por_nombre }})
        </template>
      </p>
    </li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'
import EstadoVacio from '../comunes/EstadoVacio.vue'
import { fechaHora } from '../../utils/formato'

const props = defineProps({
  suspensiones: { type: Array, required: true },
})

const ordenadas = computed(() =>
  [...props.suspensiones].sort((a, b) => b.fecha_suspension.localeCompare(a.fecha_suspension)),
)
</script>

<style scoped>
.lista {
  list-style: none;
  margin: 0;
  padding: 0;
}

.suspension {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-1);
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
  border-left: 3px solid var(--ym-estado-cancelado);
}

.suspension + .suspension {
  border-top: 1px solid var(--ym-borde);
}

.suspension-vigente {
  border-left-color: var(--ym-estado-pendiente);
  background: var(--ym-estado-pendiente-fondo);
}

.titulo {
  margin: 0;
  font-weight: var(--ym-peso-fuerte);
}

.suspension-vigente .titulo {
  color: var(--ym-estado-pendiente-texto);
}

.motivo,
.secundario {
  margin: 0;
}

.secundario {
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-normal);
  color: var(--ym-texto-secundario);
}
</style>
