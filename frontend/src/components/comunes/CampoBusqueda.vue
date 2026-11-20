<template>
  <div class="busqueda">
    <label :for="id" class="visually-hidden">{{ etiqueta }}</label>
    <i class="bi bi-search busqueda-icono" aria-hidden="true"></i>
    <input
      :id="id"
      v-model="texto"
      type="search"
      class="form-control busqueda-input"
      :placeholder="placeholder"
      autocomplete="off"
    />
    <button
      v-show="texto"
      type="button"
      class="btn busqueda-limpiar"
      :aria-label="`Limpiar ${etiqueta.toLowerCase()}`"
      @click="texto = ''"
    >
      <i class="bi bi-x-lg" aria-hidden="true"></i>
    </button>
  </div>
</template>

<script setup>
import { useId } from 'vue'

defineProps({
  etiqueta: { type: String, default: 'Buscar' },
  placeholder: { type: String, default: 'Buscar' },
})

const texto = defineModel({ type: String, default: '' })
const id = useId()
</script>

<style scoped>
.busqueda {
  position: relative;
  min-width: 0;
}

.busqueda-icono {
  position: absolute;
  left: var(--ym-espacio-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--ym-texto-secundario);
  pointer-events: none;
}

.busqueda-input {
  padding-left: 2.25rem;
  padding-right: 2.25rem;
}

/* Ocultar la X nativa del input search: usamos la propia */
.busqueda-input::-webkit-search-cancel-button {
  display: none;
}

.busqueda-limpiar {
  position: absolute;
  right: var(--ym-espacio-1);
  top: 50%;
  transform: translateY(-50%);
  padding: var(--ym-espacio-1) var(--ym-espacio-2);
  color: var(--ym-texto-secundario);
  border: 0;
}
</style>
