<template>
  <EstadoVacio v-if="!strikes.length" icono="verified_user" titulo="Sin strikes" mensaje="Este usuario no ha recibido sanciones." />
  <ul v-else class="lista">
    <li v-for="s in items" :key="s.id" class="strike" :class="`strike-${s.situacion.toLowerCase()}`">
      <div class="strike-cabecera">
        <span class="situacion">{{ s.situacion }}</span>
        <time class="ym-cifras secundario" :datetime="s.created_at">{{ fechaHora(s.created_at) }}</time>
      </div>
      <p class="motivo">{{ s.motivo }}</p>
      <p class="secundario">
        Dado por {{ s.admin_nombre }}
        <template v-if="s.solicitud_id">
          al rechazar
          <RouterLink :to="{ name: 'solicitud', params: { id: s.solicitud_id } }" class="ym-cifras">{{ folio(s.solicitud_id) }}</RouterLink>
        </template>
        <template v-else>por reporte manual</template>
      </p>
      <p v-if="s.situacion === 'Anulado'" class="secundario">
        Anulado por {{ s.anulado_por_nombre }}: {{ s.motivo_anulacion }}
      </p>
      <p v-else-if="s.situacion === 'Reiniciado'" class="secundario">Dejó de contar al rehabilitar la cuenta.</p>
      <button
        v-if="s.situacion === 'Activo'"
        type="button"
        class="btn btn-sm btn-outline-secondary"
        @click="$emit('anular', s)"
      >
        Anular strike
      </button>
    </li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'
import EstadoVacio from '../comunes/EstadoVacio.vue'
import { situacionStrike } from '../../constantes/cuentas'
import { fechaHora, folio } from '../../utils/formato'

const props = defineProps({
  strikes: { type: Array, required: true },
})

defineEmits(['anular'])

const items = computed(() =>
  [...props.strikes]
    .sort((a, b) => b.created_at.localeCompare(a.created_at))
    .map((s) => ({ ...s, situacion: situacionStrike(s) })),
)
</script>

<style scoped>
.lista {
  list-style: none;
  margin: 0;
  padding: 0;
}

.strike {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--ym-espacio-1);
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
  border-left: 3px solid var(--s);
}

.strike + .strike {
  border-top: 1px solid var(--ym-borde);
}

.strike-activo { --s: var(--ym-estado-pendiente); --s-texto: var(--ym-estado-pendiente-texto); }
.strike-anulado { --s: var(--ym-borde-fuerte); --s-texto: var(--ym-texto-secundario); }
.strike-reiniciado { --s: var(--ym-estado-cancelado); --s-texto: var(--ym-estado-cancelado-texto); }

.strike-cabecera {
  display: flex;
  gap: var(--ym-espacio-3);
  align-items: baseline;
}

.situacion {
  font-weight: var(--ym-peso-fuerte);
  color: var(--s-texto);
}

.motivo {
  margin: 0;
}

.strike-anulado .motivo {
  text-decoration: line-through;
  color: var(--ym-texto-secundario);
}

.secundario {
  margin: 0;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

.btn {
  margin-top: var(--ym-espacio-1);
}
</style>
