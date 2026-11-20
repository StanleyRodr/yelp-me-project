<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 014. -->
<template>
  <section>
    <h1>Usuarios</h1>
    <p v-if="store.cargando" aria-busy="true">Cargando usuarios…</p>
    <ul v-else>
      <li v-for="u in store.lista" :key="u.id">
        <RouterLink :to="{ name: 'usuario', params: { id: u.id } }">{{ u.nombre }} {{ u.apellidos }}</RouterLink>
        ({{ u.estado_cuenta }}, strikes {{ u.strikes_activos }}/{{ u.limite_strikes }})
      </li>
    </ul>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUsuariosStore } from '../stores/usuarios'

const store = useUsuariosStore()
onMounted(() => store.cargar())
</script>
