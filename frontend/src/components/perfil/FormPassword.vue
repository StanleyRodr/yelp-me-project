<template>
  <form novalidate @submit.prevent="enviar">
    <div v-for="campo in CAMPOS" :key="campo.clave" class="mb-3">
      <label :for="`${id}-${campo.clave}`" class="form-label">{{ campo.etiqueta }}</label>
      <input
        :id="`${id}-${campo.clave}`"
        v-model="datos[campo.clave]"
        type="password"
        class="form-control"
        :class="{ 'is-invalid': errores[campo.clave] }"
        :autocomplete="campo.autocompletar"
        :aria-invalid="Boolean(errores[campo.clave])"
        :aria-describedby="`${id}-${campo.clave}-ayuda`"
        :disabled="guardando"
      />
      <div :id="`${id}-${campo.clave}-ayuda`" :class="errores[campo.clave] ? 'invalid-feedback' : 'form-text'">
        {{ errores[campo.clave] || campo.ayuda }}
      </div>
    </div>

    <p v-if="error" class="text-error" role="alert">{{ error }}</p>

    <div class="pie">
      <BotonCarga tipo="submit" variante="outline-primary" icono="bi-key" :cargando="guardando">Cambiar contraseña</BotonCarga>
    </div>
  </form>
</template>

<script setup>
import { reactive, useId, watch } from 'vue'
import BotonCarga from '../comunes/BotonCarga.vue'

const props = defineProps({
  guardando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  /** Errores por campo de la API (p. ej. contraseña actual incorrecta) */
  erroresApi: { type: Object, default: null },
})

const emit = defineEmits(['cambiar'])

const MINIMO = 8
const CAMPOS = [
  { clave: 'actual', etiqueta: 'Contraseña actual', autocompletar: 'current-password', ayuda: '' },
  { clave: 'nueva', etiqueta: 'Nueva contraseña', autocompletar: 'new-password', ayuda: `Al menos ${MINIMO} caracteres.` },
  { clave: 'confirmar', etiqueta: 'Confirmar nueva contraseña', autocompletar: 'new-password', ayuda: '' },
]

const id = useId()
const datos = reactive({ actual: '', nueva: '', confirmar: '' })
const errores = reactive({ actual: '', nueva: '', confirmar: '' })

watch(
  () => props.erroresApi,
  (lista) => {
    for (const [campo, mensajes] of Object.entries(lista ?? {})) {
      if (campo in errores) errores[campo] = mensajes[0]
    }
  },
)

function validar() {
  errores.actual = datos.actual ? '' : 'Escribe tu contraseña actual.'
  errores.nueva = datos.nueva.length >= MINIMO ? '' : `La nueva contraseña necesita al menos ${MINIMO} caracteres.`
  errores.confirmar = datos.confirmar === datos.nueva ? '' : 'Las contraseñas no coinciden.'
  return !errores.actual && !errores.nueva && !errores.confirmar
}

function enviar() {
  if (!validar()) return
  emit('cambiar', { actual: datos.actual, nueva: datos.nueva })
}

/** La vista lo llama tras un cambio exitoso. */
function reiniciar() {
  Object.assign(datos, { actual: '', nueva: '', confirmar: '' })
}

defineExpose({ reiniciar })
</script>

<style scoped>
.text-error {
  color: var(--ym-estado-pendiente-texto);
}

.pie {
  display: flex;
  justify-content: flex-end;
}
</style>
