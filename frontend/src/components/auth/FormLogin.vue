<template>
  <form class="form" novalidate @submit.prevent="enviar">
    <h2 class="form-titulo">Iniciar sesión</h2>
    <p class="form-descripcion">Acceso solo para administradores.</p>

    <div v-if="aviso && !error" class="aviso" role="status">
      <i class="bi bi-info-circle" aria-hidden="true"></i>
      {{ aviso }}
    </div>

    <div v-if="error" class="alerta" role="alert">
      <i class="bi bi-exclamation-circle" aria-hidden="true"></i>
      {{ error }}
    </div>

    <div class="mb-3">
      <label :for="`${id}-correo`" class="form-label">Correo</label>
      <input
        :id="`${id}-correo`"
        ref="campoCorreo"
        v-model.trim="correo"
        type="email"
        class="form-control"
        :class="{ 'is-invalid': errores.correo }"
        autocomplete="username"
        required
        :aria-invalid="Boolean(errores.correo)"
        :aria-describedby="errores.correo ? `${id}-correo-error` : undefined"
        :disabled="cargando"
      />
      <div v-if="errores.correo" :id="`${id}-correo-error`" class="invalid-feedback">{{ errores.correo }}</div>
    </div>

    <div class="mb-4">
      <label :for="`${id}-password`" class="form-label">Contraseña</label>
      <div class="input-group" :class="{ 'has-validation': errores.password }">
        <input
          :id="`${id}-password`"
          ref="campoPassword"
          v-model="password"
          :type="verPassword ? 'text' : 'password'"
          class="form-control"
          :class="{ 'is-invalid': errores.password }"
          autocomplete="current-password"
          required
          :aria-invalid="Boolean(errores.password)"
          :aria-describedby="errores.password ? `${id}-password-error` : undefined"
          :disabled="cargando"
        />
        <button
          type="button"
          class="btn btn-outline-secondary"
          :aria-pressed="verPassword"
          :aria-controls="`${id}-password`"
          :disabled="cargando"
          @click="verPassword = !verPassword"
        >
          <i :class="['bi', verPassword ? 'bi-eye-slash' : 'bi-eye']" aria-hidden="true"></i>
          <span class="visually-hidden">{{ verPassword ? 'Ocultar contraseña' : 'Mostrar contraseña' }}</span>
        </button>
        <div v-if="errores.password" :id="`${id}-password-error`" class="invalid-feedback">{{ errores.password }}</div>
      </div>
    </div>

    <BotonCarga tipo="submit" class="w-100" icono="bi-box-arrow-in-right" :cargando="cargando">
      Iniciar sesión
    </BotonCarga>

    <p v-if="ayudaDemo" class="demo">
      Demo: <span class="ym-cifras">admin@yelpme.com</span> / <span class="ym-cifras">Admin123*</span>
    </p>
  </form>
</template>

<script setup>
import { nextTick, reactive, ref, useId } from 'vue'
import BotonCarga from '../comunes/BotonCarga.vue'

/** Formulario presentacional: valida campos y emite credenciales. */
defineProps({
  cargando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  /** Mensaje informativo (p. ej. sesión expirada); se oculta si hay error. */
  aviso: { type: String, default: '' },
  ayudaDemo: { type: Boolean, default: false },
})

const emit = defineEmits(['enviar'])

const id = useId()
const correo = ref('')
const password = ref('')
const verPassword = ref(false)
const errores = reactive({ correo: '', password: '' })
const campoCorreo = ref(null)
const campoPassword = ref(null)

const CORREO_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validar() {
  errores.correo = !correo.value
    ? 'Escribe tu correo.'
    : CORREO_VALIDO.test(correo.value) ? '' : 'Escribe un correo válido, por ejemplo nombre@dominio.com.'
  errores.password = password.value ? '' : 'Escribe tu contraseña.'
  return !errores.correo && !errores.password
}

async function enviar() {
  if (!validar()) {
    await nextTick()
    ;(errores.correo ? campoCorreo : campoPassword).value?.focus()
    return
  }
  emit('enviar', { correo: correo.value, password: password.value })
}
</script>

<style scoped>
.form {
  width: 100%;
  max-width: 24rem;
}

.form-titulo {
  font-size: var(--ym-texto-xl);
  margin-bottom: var(--ym-espacio-1);
}

.form-descripcion {
  color: var(--ym-texto-secundario);
  margin-bottom: var(--ym-espacio-5);
}

.aviso {
  display: flex;
  gap: var(--ym-espacio-2);
  align-items: flex-start;
  padding: var(--ym-espacio-3);
  margin-bottom: var(--ym-espacio-4);
  border: 1px solid var(--ym-borde-fuerte);
  border-radius: var(--ym-radio);
  background: var(--ym-marca-suave);
  color: var(--ym-texto);
}

.alerta {
  display: flex;
  gap: var(--ym-espacio-2);
  align-items: flex-start;
  padding: var(--ym-espacio-3);
  margin-bottom: var(--ym-espacio-4);
  border: 1px solid var(--ym-estado-pendiente);
  border-radius: var(--ym-radio);
  background: var(--ym-estado-pendiente-fondo);
  color: var(--ym-estado-pendiente-texto);
}

.demo {
  margin: var(--ym-espacio-4) 0 0;
  color: var(--ym-texto-secundario);
  font-size: var(--ym-texto-sm);
}
</style>
