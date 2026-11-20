<template>
  <div class="rejilla">
    <header class="rejilla-cabecera">
      <h1>Usuarios</h1>
      <FiltrosUsuarios
        v-model:estado="estado"
        v-model:busqueda="busqueda"
        :conteos="conteos"
        :total="store.lista.length"
      />
    </header>

    <TarjetaSeccion sin-relleno>
      <SkeletonTabla v-if="mostrarSkeleton" :filas="6" :columnas="5" etiqueta="Cargando usuarios…" />
      <EstadoVacio v-else-if="store.error" icono="error" titulo="No se pudieron cargar los usuarios" :mensaje="store.error">
        <button type="button" class="btn btn-outline-primary" @click="store.cargar()">Reintentar</button>
      </EstadoVacio>
      <EstadoVacio
        v-else-if="hayDatos && !filtrados.length"
        icono="person_search"
        titulo="Ningún usuario coincide con el filtro"
        mensaje="Prueba con otro estado de cuenta o con otra búsqueda."
      >
        <button type="button" class="btn btn-outline-primary" @click="quitarFiltros">Quitar filtros</button>
      </EstadoVacio>
      <TablaUsuarios v-else-if="hayDatos" :usuarios="filtrados" @abrir="abrir" />
    </TarjetaSeccion>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import FiltrosUsuarios from '../components/usuarios/FiltrosUsuarios.vue'
import TablaUsuarios from '../components/usuarios/TablaUsuarios.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonTabla from '../components/comunes/SkeletonTabla.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { ESTADOS_CUENTA } from '../constantes/cuentas'
import { useUsuariosStore } from '../stores/usuarios'
import { nombreCompleto, normalizar } from '../utils/formato'

const store = useUsuariosStore()
const router = useRouter()
const estado = ref(null)
const busqueda = ref('')

const conteos = computed(() =>
  Object.fromEntries(ESTADOS_CUENTA.map((e) => [e.clave, store.lista.filter((u) => u.estado_cuenta === e.clave).length])),
)

const filtrados = computed(() => {
  const texto = normalizar(busqueda.value.trim())
  return store.lista
    .filter((u) => !estado.value || u.estado_cuenta === estado.value)
    .filter((u) => !texto || normalizar(`${nombreCompleto(u)} ${u.telefono} ${u.correo}`).includes(texto))
})

const hayDatos = computed(() => store.lista.length > 0)
const cargaDiferida = useCargaDiferida(() => store.cargando)
const mostrarSkeleton = computed(() => cargaDiferida.value && !hayDatos.value)

onMounted(() => store.cargar())

function quitarFiltros() {
  estado.value = null
  busqueda.value = ''
}

const abrir = (id) => router.push({ name: 'usuario', params: { id } })
</script>

<style scoped>
.rejilla {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-4);
}

.rejilla-cabecera {
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-3);
}

.rejilla-cabecera h1 {
  margin: 0;
}
</style>
