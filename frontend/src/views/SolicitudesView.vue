<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 013. -->
<template>
  <section>
    <h1>Solicitudes</h1>
    <p v-if="store.cargando" aria-busy="true">Cargando solicitudes…</p>
    <ul v-else>
      <li v-for="s in store.lista" :key="s.id">
        <RouterLink :to="{ name: 'solicitud', params: { id: s.id } }">#{{ s.id }} {{ s.titulo }}</RouterLink>
        ({{ s.estado }})
      </li>
    </ul>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useSolicitudesStore } from '../stores/solicitudes'

const store = useSolicitudesStore()
onMounted(() => store.cargar())
</script>
