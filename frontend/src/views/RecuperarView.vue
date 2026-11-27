<template>
  <main class="recuperar">
    <PanelMarca class="recuperar-marca" />
    <div class="recuperar-form">
      <SelectorTema class="selector-tema" />
      <div class="recuperar-contenido">
        <PasoCorreo
          v-if="paso === 'correo'"
          :inicial="correo"
          :cargando="cargando"
          :error="error"
          :error-campo="errorApi?.campo('correo') ?? ''"
          @enviar="pedirCodigo"
        />
        <PasoCodigo
          v-else-if="paso === 'codigo'"
          :correo="correo"
          :cargando="cargando"
          :aviso="aviso"
          :error="error"
          :error-campo="errorApi?.campo('codigo') ?? ''"
          @verificar="verificar"
          @reenviar="pedirCodigo(correo)"
          @cambiar-correo="irA('correo')"
        />
        <PasoPassword
          v-else
          :cargando="cargando"
          :error="error"
          :errores-api="errorApi?.errores ?? null"
          @guardar="guardar"
        />

        <RouterLink :to="{ name: 'login' }" class="volver">
          <i class="bi bi-arrow-left" aria-hidden="true"></i> Volver a iniciar sesión
        </RouterLink>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PanelMarca from '../components/auth/PanelMarca.vue'
import SelectorTema from '../components/comunes/SelectorTema.vue'
import PasoCorreo from '../components/recuperacion/PasoCorreo.vue'
import PasoCodigo from '../components/recuperacion/PasoCodigo.vue'
import PasoPassword from '../components/recuperacion/PasoPassword.vue'
import { recuperacionService } from '../services/recuperacionService'

const router = useRouter()

const paso = ref('correo')
const correo = ref('')
const codigo = ref('')
const cargando = ref(false)
const aviso = ref('')
const error = ref('')
const errorApi = ref(null)

function irA(nuevo) {
  paso.value = nuevo
  error.value = ''
  errorApi.value = null
}

/** Ejecuta una llamada; un 422 va bajo su campo y lo demás (429, red) arriba. */
async function ejecutar(accion) {
  cargando.value = true
  error.value = ''
  errorApi.value = null
  try {
    await accion()
  } catch (e) {
    errorApi.value = e
    if (e.codigo !== 422) error.value = e.message
  } finally {
    cargando.value = false
  }
}

const pedirCodigo = (valor) =>
  ejecutar(async () => {
    aviso.value = await recuperacionService.pedirCodigo(valor)
    correo.value = valor
    irA('codigo')
  })

const verificar = (valor) =>
  ejecutar(async () => {
    await recuperacionService.verificar(correo.value, valor)
    codigo.value = valor
    irA('password')
  })

const guardar = (password) =>
  ejecutar(async () => {
    try {
      await recuperacionService.cambiar({ correo: correo.value, codigo: codigo.value, password })
    } catch (e) {
      // El código venció entre pasos: se vuelve a pedirlo.
      if (e.campo?.('codigo')) {
        aviso.value = ''
        paso.value = 'codigo'
      }
      throw e
    }
    router.replace({ name: 'login', query: { recuperada: 1 } })
  })
</script>

<style scoped>
.recuperar {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.recuperar-form {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--ym-espacio-6) var(--ym-espacio-5);
  background: var(--ym-superficie);
}

.recuperar-contenido {
  width: 100%;
  max-width: 24rem;
}

.volver {
  display: inline-flex;
  gap: var(--ym-espacio-2);
  align-items: center;
  margin-top: var(--ym-espacio-5);
}

.selector-tema {
  position: absolute;
  top: var(--ym-espacio-4);
  right: var(--ym-espacio-4);
}

@media (max-width: 767.98px) {
  .recuperar {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }

  .recuperar-form {
    align-items: flex-start;
  }
}
</style>
