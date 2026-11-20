<template>
  <ModalBase v-model:abierto="abierto" :titulo="titulo" :bloqueado="guardando">
    <form :id="id" novalidate @submit.prevent="confirmar">
      <p v-if="descripcion" class="texto-ayuda">{{ descripcion }}</p>
      <label :for="`${id}-motivo`" class="form-label">{{ etiqueta }}</label>
      <textarea
        :id="`${id}-motivo`"
        v-model="motivo"
        class="form-control"
        :class="{ 'is-invalid': intentado && !motivoValido }"
        rows="3"
        maxlength="500"
        required
        :disabled="guardando"
      ></textarea>
      <div v-if="intentado && !motivoValido" class="invalid-feedback">El motivo es obligatorio.</div>
      <p v-if="error" class="mensaje-error" role="alert">{{ error }}</p>
    </form>

    <template #pie>
      <button type="button" class="btn btn-outline-secondary" :disabled="guardando" @click="abierto = false">
        Cancelar
      </button>
      <BotonCarga tipo="submit" :form="id" :variante="variante" :cargando="guardando">{{ textoBoton }}</BotonCarga>
    </template>
  </ModalBase>
</template>

<script setup>
import { computed, ref, useId, watch } from 'vue'
import ModalBase from './ModalBase.vue'
import BotonCarga from '../comunes/BotonCarga.vue'
import { exito } from '../../plugins/alertas'

/**
 * Acción que requiere un motivo obligatorio (reportar usuario, suspender,
 * anular strike). La vista pasa `accion(motivo)`, que llama a su store.
 */
const props = defineProps({
  titulo: { type: String, required: true },
  descripcion: { type: String, default: '' },
  etiqueta: { type: String, default: 'Motivo' },
  textoBoton: { type: String, required: true },
  variante: { type: String, default: 'primary' },
  mensajeExito: { type: String, default: '' },
  accion: { type: Function, required: true },
})

const abierto = defineModel('abierto', { type: Boolean, default: false })
const id = useId()

const motivo = ref('')
const intentado = ref(false)
const guardando = ref(false)
const error = ref('')
const motivoValido = computed(() => motivo.value.trim().length > 0)

watch(abierto, (valor) => {
  if (!valor) return
  motivo.value = ''
  intentado.value = false
  error.value = ''
})

async function confirmar() {
  intentado.value = true
  if (!motivoValido.value) return
  guardando.value = true
  error.value = ''
  try {
    await props.accion(motivo.value.trim())
    if (props.mensajeExito) exito(props.mensajeExito)
    abierto.value = false
  } catch (e) {
    error.value = e.message
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
.texto-ayuda {
  color: var(--ym-texto-secundario);
}

.mensaje-error {
  margin: var(--ym-espacio-3) 0 0;
  color: var(--ym-estado-pendiente-texto);
}
</style>
