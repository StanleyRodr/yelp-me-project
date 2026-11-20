<template>
  <div class="filtros">
    <div class="filtros-estados" role="group" aria-label="Filtrar por estado">
      <button
        v-for="opcion in opciones"
        :key="opcion.clave ?? 'todas'"
        type="button"
        class="chip"
        :class="opcion.token && `chip-${opcion.token}`"
        :aria-pressed="estado === opcion.clave"
        @click="estado = opcion.clave"
      >
        <span v-if="opcion.icono" class="material-symbols-outlined" aria-hidden="true">{{ opcion.icono }}</span>
        {{ opcion.etiqueta }}
        <span class="chip-conteo ym-cifras">{{ opcion.conteo }}</span>
      </button>
    </div>
    <CampoBusqueda
      v-model="busqueda"
      class="filtros-busqueda"
      etiqueta="Buscar solicitudes"
      placeholder="Folio, título, usuario, ubicación o servicio"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CampoBusqueda from '../comunes/CampoBusqueda.vue'
import { ESTADOS } from '../../constantes/estados'

const props = defineProps({
  conteos: { type: Object, required: true },
  total: { type: Number, required: true },
})

/** null = todas */
const estado = defineModel('estado', { type: String, default: null })
const busqueda = defineModel('busqueda', { type: String, default: '' })

const opciones = computed(() => [
  { clave: null, etiqueta: 'Todas', conteo: props.total },
  ...ESTADOS.map((e) => ({ clave: e.clave, etiqueta: e.clave, icono: e.icono, token: e.token, conteo: props.conteos[e.clave] ?? 0 })),
])
</script>

<style scoped>
.filtros {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--ym-espacio-3);
}

.filtros-estados {
  display: flex;
  flex-wrap: wrap;
  gap: var(--ym-espacio-2);
}

.filtros-busqueda {
  flex: 1 1 16rem;
  max-width: 24rem;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
  padding: var(--ym-espacio-1) var(--ym-espacio-3);
  border: 1px solid var(--ym-borde-fuerte);
  border-radius: var(--ym-radio);
  background: var(--ym-superficie);
  color: var(--ym-texto);
  font-weight: var(--ym-peso-medio);
  font-size: var(--ym-texto-sm);
}

.chip .material-symbols-outlined {
  color: var(--e-texto, var(--ym-texto-secundario));
}

.chip-conteo {
  color: var(--ym-texto-secundario);
}

.chip[aria-pressed='true'] {
  border-color: var(--e, var(--ym-marca));
  background: var(--e-fondo, var(--ym-marca-suave));
  color: var(--e-texto, var(--ym-marca));
  box-shadow: inset 0 -3px 0 var(--e, var(--ym-marca));
}

.chip[aria-pressed='true'] .chip-conteo {
  color: inherit;
}

.chip-pendiente { --e: var(--ym-estado-pendiente); --e-texto: var(--ym-estado-pendiente-texto); --e-fondo: var(--ym-estado-pendiente-fondo); }
.chip-enviado { --e: var(--ym-estado-enviado); --e-texto: var(--ym-estado-enviado-texto); --e-fondo: var(--ym-estado-enviado-fondo); }
.chip-terminado { --e: var(--ym-estado-terminado); --e-texto: var(--ym-estado-terminado-texto); --e-fondo: var(--ym-estado-terminado-fondo); }
.chip-rechazado { --e: var(--ym-estado-rechazado); --e-texto: var(--ym-estado-rechazado-texto); --e-fondo: var(--ym-estado-rechazado-fondo); }
.chip-cancelado { --e: var(--ym-estado-cancelado); --e-texto: var(--ym-estado-cancelado-texto); --e-fondo: var(--ym-estado-cancelado-fondo); }
</style>
