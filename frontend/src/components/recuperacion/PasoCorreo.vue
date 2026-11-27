<template>
  <form novalidate @submit.prevent="enviar">
    <h2 class="titulo">Recuperar contraseña</h2>
    <p class="descripcion">Escribe el correo de tu cuenta y te enviaremos un código de 6 dígitos.</p>

    <AvisoFormulario v-if="error" tipo="error">{{ error }}</AvisoFormulario>

    <div class="mb-4">
      <label :for="`${id}-correo`" class="form-label">Correo</label>
      <input
        :id="`${id}-correo`"
        ref="campo"
        v-model.trim="correo"
        type="email"
        class="form-control"
        :class="{ 'is-invalid': mensaje }"
        autocomplete="username"
        required
        :aria-invalid="Boolean(mensaje)"
        :aria-describedby="mensaje ? `${id}-correo-error` : undefined"
        :disabled="cargando"
      />
      <div v-if="mensaje" :id="`${id}-correo-error`" class="invalid-feedback">{{ mensaje }}</div>
    </div>

    <BotonCarga tipo="submit" class="w-100" icono="bi-envelope" :cargando="cargando">Enviar código</BotonCarga>
  </form>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, useId } from 'vue'
import AvisoFormulario from './AvisoFormulario.vue'
import BotonCarga from '../comunes/BotonCarga.vue'

const props = defineProps({
  inicial: { type: String, default: '' },
  cargando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  /** Error del campo devuelto por la API */
  errorCampo: { type: String, default: '' },
})

const emit = defineEmits(['enviar'])

const id = useId()
const correo = ref(props.inicial)
const local = ref('')
const campo = ref(null)
const mensaje = computed(() => local.value || props.errorCampo)

onMounted(() => campo.value?.focus())

async function enviar() {
  local.value = !correo.value
    ? 'Escribe tu correo.'
    : /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo.value) ? '' : 'Escribe un correo válido, por ejemplo nombre@dominio.com.'
  if (local.value) {
    await nextTick()
    campo.value?.focus()
    return
  }
  emit('enviar', correo.value)
}
</script>

<style scoped>
.titulo {
  font-size: var(--ym-texto-xl);
  margin-bottom: var(--ym-espacio-1);
}

.descripcion {
  color: var(--ym-texto-secundario);
  margin-bottom: var(--ym-espacio-5);
}
</style>
