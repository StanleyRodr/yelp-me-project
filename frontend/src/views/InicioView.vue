<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 012. -->
<template>
  <section>
    <h1>Inicio</h1>
    <p v-if="store.cargando" aria-busy="true">Cargando solicitudes…</p>
    <ul v-else>
      <li v-for="estado in ESTADOS" :key="estado.clave">
        {{ estado.clave }}: <span class="ym-cifras">{{ store.conteoPorEstado[estado.clave] }}</span>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { ESTADOS } from '../constantes/estados'
import { useSolicitudesStore } from '../stores/solicitudes'

const store = useSolicitudesStore()
onMounted(() => store.cargar())
</script>
