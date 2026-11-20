<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 011. -->
<template>
  <main class="container py-5" style="max-width: 420px">
    <h1>Iniciar sesión</h1>
    <form novalidate @submit.prevent="entrar">
      <div class="mb-3">
        <label for="correo" class="form-label">Correo</label>
        <input id="correo" v-model="correo" type="email" class="form-control" autocomplete="username" required />
      </div>
      <div class="mb-3">
        <label for="password" class="form-label">Contraseña</label>
        <input id="password" v-model="password" type="password" class="form-control" autocomplete="current-password" required />
      </div>
      <p v-if="auth.error" class="text-danger" role="alert">{{ auth.error }}</p>
      <button class="btn btn-primary w-100" :disabled="auth.cargando">Iniciar sesión</button>
    </form>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const correo = ref('')
const password = ref('')

async function entrar() {
  try {
    await auth.iniciarSesion(correo.value, password.value)
    router.replace(route.query.redirigir || { name: 'inicio' })
  } catch {
    // El mensaje queda en auth.error
  }
}
</script>
