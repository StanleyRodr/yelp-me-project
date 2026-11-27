<template>
  <form novalidate @submit.prevent="enviar">
    <h2 class="titulo">Escribe el código</h2>
    <p class="descripcion">
      Si <span class="ym-cifras">{{ correo }}</span> está registrado, te llegó un código de 6 dígitos. Vence en 15 minutos.
    </p>

    <AvisoFormulario v-if="aviso && !error">{{ aviso }}</AvisoFormulario>
    <AvisoFormulario v-if="error" tipo="error">{{ error }}</AvisoFormulario>

    <div class="mb-4">
      <label :for="`${id}-codigo`" class="form-label">Código</label>
      <input
        :id="`${id}-codigo`"
        ref="campo"
        v-model.trim="codigo"
        type="text"
        class="form-control codigo ym-cifras"
        :class="{ 'is-invalid': mensaje }"
        inputmode="numeric"
        autocomplete="one-time-code"
        maxlength="6"
        required
        :aria-invalid="Boolean(mensaje)"
        :aria-describedby="mensaje ? `${id}-codigo-error` : undefined"
        :disabled="cargando"
      />
      <div v-if="mensaje" :id="`${id}-codigo-error`" class="invalid-feedback">{{ mensaje }}</div>
    </div>

    <BotonCarga tipo="submit" class="w-100" icono="bi-check2" :cargando="cargando">Verificar</BotonCarga>

    <div class="acciones">
      <button type="button" class="btn btn-link" :disabled="cargando" @click="emit('reenviar')">Enviar otro código</button>
      <button type="button" class="btn btn-link" :disabled="cargando" @click="emit('cambiarCorreo')">Usar otro correo</button>
    </div>
  </form>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, useId, watch } from 'vue'
import AvisoFormulario from './AvisoFormulario.vue'
import BotonCarga from '../comunes/BotonCarga.vue'

const props = defineProps({
  correo: { type: String, required: true },
  cargando: { type: Boolean, default: false },
  aviso: { type: String, default: '' },
  error: { type: String, default: '' },
  errorCampo: { type: String, default: '' },
})

const emit = defineEmits(['verificar', 'reenviar', 'cambiarCorreo'])

const id = useId()
const codigo = ref('')
const local = ref('')
const campo = ref(null)
const mensaje = computed(() => local.value || props.errorCampo)

onMounted(() => campo.value?.focus())

// Error de la API: se vuelve a enfocar el campo para corregirlo.
watch(
  () => props.errorCampo,
  async (valor) => {
    if (!valor) return
    await nextTick()
    campo.value?.select()
  },
)

async function enviar() {
  local.value = /^\d{6}$/.test(codigo.value) ? '' : 'Escribe los 6 dígitos del código.'
  if (local.value) {
    await nextTick()
    campo.value?.focus()
    return
  }
  emit('verificar', codigo.value)
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

.codigo {
  font-size: var(--ym-texto-lg);
  letter-spacing: 0.3em;
}

.acciones {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-top: var(--ym-espacio-3);
}

.acciones .btn {
  padding-inline: 0;
}
</style>
