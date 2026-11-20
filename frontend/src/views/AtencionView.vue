<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 013. -->
<template>
  <section>
    <RouterLink :to="{ name: 'solicitudes' }">Volver a solicitudes</RouterLink>
    <p v-if="store.cargandoActual" aria-busy="true">Cargando solicitud…</p>
    <p v-else-if="!store.actual">{{ store.error || 'No se encontró la solicitud.' }}</p>
    <template v-else>
      <h1>Solicitud #{{ store.actual.id }}: {{ store.actual.titulo }}</h1>
      <p>Estado: {{ store.actual.estado }} · Usuario: {{ store.actual.usuario?.nombre }}</p>
      <p>Acciones permitidas: {{ accionesPermitidas(store.actual).map((a) => a.etiqueta).join(', ') || 'ninguna' }}</p>
    </template>
  </section>
</template>

<script setup>
import { watch } from 'vue'
import { accionesPermitidas } from '../constantes/transiciones'
import { useSolicitudesStore } from '../stores/solicitudes'

const props = defineProps({ id: { type: String, required: true } })
const store = useSolicitudesStore()
watch(() => props.id, (id) => store.cargarUna(id), { immediate: true })
</script>
