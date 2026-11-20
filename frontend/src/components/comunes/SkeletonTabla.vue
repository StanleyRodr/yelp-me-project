<template>
  <SkeletonContenedor :etiqueta="etiqueta">
    <table class="table mb-0">
      <thead>
        <tr>
          <th v-for="c in columnas" :key="c" scope="col"><span class="placeholder col-6"></span></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="f in filas" :key="f">
          <td v-for="c in columnas" :key="c">
            <span class="placeholder" :class="ancho(f, c)"></span>
          </td>
        </tr>
      </tbody>
    </table>
  </SkeletonContenedor>
</template>

<script setup>
import SkeletonContenedor from './SkeletonContenedor.vue'

defineProps({
  filas: { type: Number, default: 6 },
  columnas: { type: Number, default: 4 },
  etiqueta: { type: String, default: 'Cargando tabla…' },
})

// Anchos variados pero estables: parece texto real y no "baila" al re-renderizar.
const ANCHOS = ['col-4', 'col-9', 'col-6', 'col-7', 'col-5', 'col-10', 'col-3']
const ancho = (f, c) => ANCHOS[(f * 3 + c) % ANCHOS.length]
</script>
