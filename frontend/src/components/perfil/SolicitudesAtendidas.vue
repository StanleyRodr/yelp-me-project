<template>
  <div>
    <dl class="resumen">
      <div v-for="dato in resumen" :key="dato.etiqueta">
        <dt>{{ dato.etiqueta }}</dt>
        <dd class="ym-cifras">{{ dato.valor }}</dd>
      </div>
    </dl>
    <HistorialSolicitudes
      :solicitudes="solicitudes"
      vacio-titulo="Aún no has atendido solicitudes"
      vacio-mensaje="Las solicitudes que envíes, termines o rechaces aparecerán aquí."
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import HistorialSolicitudes from '../usuarios/HistorialSolicitudes.vue'

const props = defineProps({
  solicitudes: { type: Array, required: true },
  strikesDados: { type: Number, required: true },
})

const contar = (estado) => props.solicitudes.filter((s) => s.estado === estado).length

const resumen = computed(() => [
  { etiqueta: 'Atendidas', valor: props.solicitudes.length },
  { etiqueta: 'En curso', valor: contar('Enviado') },
  { etiqueta: 'Terminadas', valor: contar('Terminado') },
  { etiqueta: 'Rechazadas', valor: contar('Rechazado') },
  { etiqueta: 'Strikes dados', valor: props.strikesDados },
])
</script>

<style scoped>
.resumen {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  margin: 0;
  border-bottom: 1px solid var(--ym-borde);
}

.resumen > div {
  padding: var(--ym-espacio-3) var(--ym-espacio-4);
}

.resumen > div + div {
  border-left: 1px solid var(--ym-borde);
}

dt {
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-medio);
  color: var(--ym-texto-secundario);
}

dd {
  margin: 0;
  font-size: var(--ym-texto-xl);
  font-weight: var(--ym-peso-fuerte);
  line-height: 1.2;
}

@media (max-width: 575.98px) {
  .resumen {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .resumen > div:nth-child(4) {
    border-left: 0;
  }
}
</style>
