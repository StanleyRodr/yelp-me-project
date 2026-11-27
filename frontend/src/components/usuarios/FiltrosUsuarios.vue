<template>
  <div class="filtros">
    <div class="filtros-estados" role="group" aria-label="Filtrar por estado de cuenta">
      <button
        v-for="opcion in opciones"
        :key="opcion.clave ?? 'todos'"
        type="button"
        class="chip"
        :aria-pressed="estado === opcion.clave"
        @click="estado = opcion.clave"
      >
        {{ opcion.etiqueta }}
        <span class="chip-conteo ym-cifras">{{ opcion.conteo }}</span>
      </button>
    </div>
    <CampoBusqueda
      v-model="busqueda"
      class="filtros-busqueda"
      etiqueta="Buscar usuarios"
      placeholder="Nombre, teléfono o correo"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CampoBusqueda from '../comunes/CampoBusqueda.vue'
import { ESTADOS_CUENTA } from '../../constantes/cuentas'

const props = defineProps({
  conteos: { type: Object, required: true },
  total: { type: Number, required: true },
})

const estado = defineModel('estado', { type: String, default: null })
const busqueda = defineModel('busqueda', { type: String, default: '' })

const opciones = computed(() => [
  { clave: null, etiqueta: 'Todos', conteo: props.total },
  ...ESTADOS_CUENTA.map((e) => ({ clave: e.clave, etiqueta: e.clave, conteo: props.conteos[e.clave] ?? 0 })),
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

.chip-conteo {
  color: var(--ym-texto-secundario);
}

.chip[aria-pressed='true'] {
  border-color: var(--ym-marca-texto);
  background: var(--ym-marca-suave);
  color: var(--ym-marca-texto);
  box-shadow: inset 0 -3px 0 var(--ym-marca-texto);
}
</style>
