<template>
  <div class="rejilla">
    <header class="rejilla-cabecera">
      <h1>Mi perfil</h1>
    </header>

    <div class="rejilla-formularios">
      <TarjetaSeccion titulo="Datos personales">
        <FormPerfil v-if="auth.admin" :admin="auth.admin" :guardando="guardandoDatos" :error="errorDatos" :errores-api="erroresDatos" @guardar="guardarDatos" />
      </TarjetaSeccion>
      <TarjetaSeccion titulo="Contraseña">
        <FormPassword ref="formPassword" :guardando="guardandoPassword" :error="errorPassword" :errores-api="erroresPassword" @cambiar="cambiarPassword" />
      </TarjetaSeccion>
    </div>

    <TarjetaSeccion class="rejilla-actividad" titulo="Solicitudes que atendiste" sin-relleno>
      <SkeletonLista v-if="mostrarSkeleton" :elementos="4" etiqueta="Cargando tu actividad…" />
      <EstadoVacio v-else-if="perfil.error" icono="error" titulo="No se pudo cargar tu actividad" :mensaje="perfil.error">
        <button type="button" class="btn btn-outline-primary" @click="perfil.cargarActividad()">Reintentar</button>
      </EstadoVacio>
      <SolicitudesAtendidas
        v-else-if="perfil.actividad"
        :solicitudes="perfil.actividad.solicitudes"
        :strikes-dados="perfil.actividad.strikes_dados"
      />
    </TarjetaSeccion>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import FormPerfil from '../components/perfil/FormPerfil.vue'
import FormPassword from '../components/perfil/FormPassword.vue'
import SolicitudesAtendidas from '../components/perfil/SolicitudesAtendidas.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonLista from '../components/comunes/SkeletonLista.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { useAuthStore } from '../stores/auth'
import { usePerfilStore } from '../stores/perfil'
import { exito } from '../plugins/alertas'

const auth = useAuthStore()
const perfil = usePerfilStore()

const guardandoDatos = ref(false)
const errorDatos = ref('')
const erroresDatos = ref(null)
const guardandoPassword = ref(false)
const errorPassword = ref('')
const erroresPassword = ref(null)
const formPassword = ref(null)

const cargaDiferida = useCargaDiferida(() => perfil.cargandoActividad)
const mostrarSkeleton = computed(() => cargaDiferida.value && !perfil.actividad)

onMounted(() => perfil.cargarActividad())

async function guardarDatos(datos) {
  guardandoDatos.value = true
  errorDatos.value = ''
  erroresDatos.value = null
  try {
    await perfil.guardarDatos(datos)
    exito('Datos actualizados')
  } catch (e) {
    // Con errores por campo se muestran bajo cada input; si no, arriba.
    erroresDatos.value = e.errores
    errorDatos.value = e.errores ? 'Revisa los campos marcados.' : e.message
  } finally {
    guardandoDatos.value = false
  }
}

async function cambiarPassword(datos) {
  guardandoPassword.value = true
  errorPassword.value = ''
  erroresPassword.value = null
  try {
    await perfil.cambiarPassword(datos)
    formPassword.value?.reiniciar()
    exito('Contraseña actualizada')
  } catch (e) {
    erroresPassword.value = e.errores
    errorPassword.value = e.errores ? 'Revisa los campos marcados.' : e.message
  } finally {
    guardandoPassword.value = false
  }
}
</script>

<style scoped>
.rejilla {
  display: grid;
  grid-template-columns: minmax(0, 5fr) minmax(0, 7fr);
  grid-template-areas:
    'cabecera cabecera'
    'formularios actividad';
  gap: var(--ym-espacio-4);
  align-items: start;
}

.rejilla-cabecera { grid-area: cabecera; }
.rejilla-cabecera h1 { margin: 0; }

.rejilla-formularios {
  grid-area: formularios;
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-4);
  min-width: 0;
}

.rejilla-actividad { grid-area: actividad; }

@media (max-width: 991.98px) {
  .rejilla {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'cabecera' 'formularios' 'actividad';
  }
}
</style>
