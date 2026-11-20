<template>
  <main class="login">
    <PanelMarca class="login-marca" />
    <div class="login-form">
      <FormLogin
        :cargando="auth.cargando"
        :error="auth.error ?? ''"
        :ayuda-demo="esDesarrollo"
        @enviar="entrar"
      />
    </div>
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PanelMarca from '../components/auth/PanelMarca.vue'
import FormLogin from '../components/auth/FormLogin.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const esDesarrollo = import.meta.env.DEV

// Un error de un intento anterior no debe aparecer al volver al login.
onMounted(() => auth.limpiarError())

// Solo rutas internas: evita redirigir a otro sitio con ?redirigir=//dominio
const destino = () => {
  const r = route.query.redirigir
  return typeof r === 'string' && r.startsWith('/') && !r.startsWith('//') ? r : { name: 'inicio' }
}

async function entrar({ correo, password }) {
  try {
    await auth.iniciarSesion(correo, password)
    router.replace(destino())
  } catch {
    // El mensaje queda en auth.error y FormLogin lo muestra.
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 5fr) minmax(0, 7fr);
}

.login-form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--ym-espacio-6) var(--ym-espacio-5);
  background: var(--ym-superficie);
}

@media (max-width: 767.98px) {
  .login {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }

  .login-form {
    align-items: flex-start;
  }
}
</style>
