<template>
  <form novalidate @submit.prevent="enviar">
    <h2 class="titulo">Nueva contraseña</h2>
    <p class="descripcion">Al guardarla se cerrarán todas las sesiones abiertas de tu cuenta.</p>

    <AvisoFormulario v-if="error" tipo="error">{{ error }}</AvisoFormulario>

    <div v-for="c in CAMPOS" :key="c.clave" class="mb-3">
      <label :for="`${id}-${c.clave}`" class="form-label">{{ c.etiqueta }}</label>
      <input
        :id="`${id}-${c.clave}`"
        v-model="datos[c.clave]"
        type="password"
        class="form-control"
        :class="{ 'is-invalid': mensajes[c.clave] }"
        autocomplete="new-password"
        :aria-invalid="Boolean(mensajes[c.clave])"
        :aria-describedby="`${id}-${c.clave}-ayuda`"
        :disabled="cargando"
      />
      <div :id="`${id}-${c.clave}-ayuda`" :class="mensajes[c.clave] ? 'invalid-feedback' : 'form-text'">
        {{ mensajes[c.clave] || c.ayuda }}
      </div>
    </div>

    <BotonCarga tipo="submit" class="w-100 mt-2" icono="bi-key" :cargando="cargando">Guardar contraseña</BotonCarga>
  </form>
</template>

<script setup>
import { computed, reactive, useId } from 'vue'
import AvisoFormulario from './AvisoFormulario.vue'
import BotonCarga from '../comunes/BotonCarga.vue'

const props = defineProps({
  cargando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  /** Errores por campo de la API (`password`) */
  erroresApi: { type: Object, default: null },
})

const emit = defineEmits(['guardar'])

// Mismas reglas que FormPassword del perfil.
const MINIMO = 8
const CAMPOS = [
  { clave: 'nueva', etiqueta: 'Nueva contraseña', ayuda: `Al menos ${MINIMO} caracteres.` },
  { clave: 'confirmar', etiqueta: 'Confirmar nueva contraseña', ayuda: '' },
]

const id = useId()
const datos = reactive({ nueva: '', confirmar: '' })
const locales = reactive({ nueva: '', confirmar: '' })
const mensajes = computed(() => ({
  nueva: locales.nueva || props.erroresApi?.password?.[0] || '',
  confirmar: locales.confirmar,
}))

function enviar() {
  locales.nueva = datos.nueva.length >= MINIMO ? '' : `La nueva contraseña necesita al menos ${MINIMO} caracteres.`
  locales.confirmar = datos.confirmar === datos.nueva ? '' : 'Las contraseñas no coinciden.'
  if (locales.nueva || locales.confirmar) return
  emit('guardar', datos.nueva)
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
