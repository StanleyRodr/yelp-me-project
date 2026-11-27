<template>
  <!-- Un clic pasa al siguiente tema: Claro → Oscuro → Sistema → Claro -->
  <button
    type="button"
    class="btn selector"
    :class="sobreMarca ? 'selector-marca' : 'btn-outline-secondary'"
    :title="descripcion"
    :aria-label="descripcion"
    @click="elegir(siguiente.valor)"
  >
    <i :class="['bi', actual.icono]" aria-hidden="true"></i>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useTema } from '../../composables/useTema'

/** Botón de ícono que cicla Claro / Oscuro / Sistema. */
defineProps({
  /** true sobre la barra de marca (navbar) */
  sobreMarca: { type: Boolean, default: false },
})

const { preferencia, elegir, TEMAS } = useTema()
const indice = computed(() => Math.max(0, TEMAS.findIndex((t) => t.valor === preferencia.value)))
const actual = computed(() => TEMAS[indice.value])
const siguiente = computed(() => TEMAS[(indice.value + 1) % TEMAS.length])
const descripcion = computed(() => `Tema: ${actual.value.etiqueta}. Cambiar a ${siguiente.value.etiqueta}`)
</script>

<style scoped>
.selector {
  display: inline-grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  padding: 0;
  font-size: var(--ym-texto-lg);
}

.selector-marca {
  color: var(--ym-sobre-marca);
  border: 1px solid transparent;
}

.selector-marca:hover,
.selector-marca:focus-visible {
  color: var(--ym-sobre-marca);
  border-color: var(--ym-sobre-marca-tenue);
}
</style>
