<!-- Provisional (tarea 009). Se rehace con componentes en la tarea 015. -->
<template>
  <section>
    <h1>Mi perfil</h1>
    <p v-if="auth.admin">{{ auth.admin.nombre }} {{ auth.admin.apellidos }} · {{ auth.admin.correo }}</p>
  </section>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>
