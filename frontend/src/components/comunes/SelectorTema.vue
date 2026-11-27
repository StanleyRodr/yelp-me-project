<template>
  <!-- Dentro de un dropdown existente (menú del usuario) -->
  <template v-if="modo === 'items'">
    <li><h6 class="dropdown-header">Tema</h6></li>
    <li v-for="t in TEMAS" :key="t.valor">
      <button
        type="button"
        class="dropdown-item"
        role="menuitemradio"
        :aria-checked="preferencia === t.valor"
        :class="{ active: preferencia === t.valor }"
        @click="elegir(t.valor)"
      >
        <i :class="['bi', t.icono, 'me-2']" aria-hidden="true"></i>{{ t.etiqueta }}
      </button>
    </li>
  </template>

  <!-- Botón propio con su menú (login y recuperación) -->
  <div v-else class="dropdown">
    <button
      type="button"
      class="btn btn-outline-secondary btn-sm"
      data-bs-toggle="dropdown"
      aria-expanded="false"
    >
      <i :class="['bi', actual.icono]" aria-hidden="true"></i>
      <span class="visually-hidden">Tema: {{ actual.etiqueta }}</span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <li v-for="t in TEMAS" :key="t.valor">
        <button
          type="button"
          class="dropdown-item"
          role="menuitemradio"
          :aria-checked="preferencia === t.valor"
          :class="{ active: preferencia === t.valor }"
          @click="elegir(t.valor)"
        >
          <i :class="['bi', t.icono, 'me-2']" aria-hidden="true"></i>{{ t.etiqueta }}
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTema } from '../../composables/useTema'

/** Selector Claro / Oscuro / Sistema. */
defineProps({
  /** 'items': opciones sueltas para un dropdown existente; 'boton': botón con menú propio */
  modo: { type: String, default: 'boton' },
})

const { preferencia, elegir, TEMAS } = useTema()
const actual = computed(() => TEMAS.find((t) => t.valor === preferencia.value) ?? TEMAS[2])
</script>
