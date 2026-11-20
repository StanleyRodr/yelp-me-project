<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 014. -->
<template>
  <section>
    <RouterLink :to="{ name: 'usuarios' }">Volver a usuarios</RouterLink>
    <p v-if="store.cargandoActual" aria-busy="true">Cargando usuario…</p>
    <p v-else-if="!store.actual">{{ store.error || 'No se encontró el usuario.' }}</p>
    <template v-else>
      <h1>{{ store.actual.nombre }} {{ store.actual.apellidos }}</h1>
      <p>
        {{ store.actual.estado_cuenta }} · strikes {{ store.actual.strikes_activos }}/{{ store.actual.limite_strikes }}
        · suspendido {{ store.actual.veces_suspendido }} veces
      </p>
    </template>
  </section>
</template>

<script setup>
import { watch } from 'vue'
import { useUsuariosStore } from '../stores/usuarios'

const props = defineProps({ id: { type: String, required: true } })
const store = useUsuariosStore()
watch(() => props.id, (id) => store.cargarUno(id), { immediate: true })
</script>
