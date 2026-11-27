<template>
  <TarjetaSeccion :titulo="titulo">
    <template v-if="domicilio">
      <div class="domicilio-datos">
        <p class="domicilio-texto">{{ domicilio.texto }}</p>
        <p v-if="domicilio.referencia" class="domicilio-referencia">Referencia: {{ domicilio.referencia }}</p>
      </div>
      <MapaBase
        :marcadores="marcadores"
        :centro="[domicilio.latitud, domicilio.longitud]"
        :zoom="16"
        alto="180px"
        :etiqueta="`Mapa del domicilio: ${domicilio.texto}`"
      />
    </template>
    <EstadoVacio v-else titulo="No registró domicilio." icono="home" />
  </TarjetaSeccion>
</template>

<script setup>
import { computed } from 'vue'
import TarjetaSeccion from './TarjetaSeccion.vue'
import MapaBase from './MapaBase.vue'
import EstadoVacio from './EstadoVacio.vue'

/** Domicilio de un usuario con mini mapa (plan 009). */
const props = defineProps({
  /** { texto, referencia, latitud, longitud } o null */
  domicilio: { type: Object, default: null },
  titulo: { type: String, default: 'Domicilio' },
})

const marcadores = computed(() => [{
  id: 0,
  latitud: props.domicilio.latitud,
  longitud: props.domicilio.longitud,
  icono: 'home',
  titulo: 'Domicilio',
}])
</script>

<style scoped>
.domicilio-datos {
  margin-bottom: var(--ym-espacio-3);
}

.domicilio-texto {
  margin: 0;
  color: var(--ym-texto);
}

.domicilio-referencia {
  margin: var(--ym-espacio-1) 0 0;
  color: var(--ym-texto-secundario);
  font-size: var(--ym-texto-sm);
}
</style>
