<template>
  <ModalBase v-model:abierto="abierto" titulo="Rechazar solicitud" :bloqueado="guardando">
    <form :id="id" novalidate @submit.prevent="rechazar">
      <div class="mb-3">
        <label :for="`${id}-motivo`" class="form-label">Motivo del rechazo</label>
        <textarea
          :id="`${id}-motivo`"
          v-model="motivo"
          class="form-control"
          :class="{ 'is-invalid': intentado && !motivoValido }"
          rows="4"
          maxlength="500"
          required
          :aria-describedby="`${id}-ayuda`"
          :disabled="guardando"
        ></textarea>
        <div v-if="intentado && !motivoValido" class="invalid-feedback">El motivo es obligatorio.</div>
        <div :id="`${id}-ayuda`" class="form-text">El usuario verá este motivo en su historial.</div>
      </div>

      <div class="form-check">
        <input :id="`${id}-strike`" v-model="strike" class="form-check-input" type="checkbox" :disabled="guardando" />
        <label :for="`${id}-strike`" class="form-check-label">
          Es broma o mal uso: sumar un strike a {{ nombreUsuario }}
        </label>
        <div class="form-text">Con 3 strikes activos la cuenta se suspende para pedir servicios.</div>
      </div>

      <p v-if="error" class="mensaje-error" role="alert">{{ error }}</p>
    </form>

    <template #pie>
      <button type="button" class="btn btn-outline-secondary" :disabled="guardando" @click="abierto = false">
        Cancelar
      </button>
      <BotonCarga tipo="submit" :form="id" variante="danger" icono="bi-slash-circle" :cargando="guardando">
        Rechazar solicitud
      </BotonCarga>
    </template>
  </ModalBase>
</template>

<script setup>
import { computed, ref, useId, watch } from 'vue'
import ModalBase from './ModalBase.vue'
import BotonCarga from '../comunes/BotonCarga.vue'
import { useSolicitudesStore } from '../../stores/solicitudes'
import { tieneServicios } from '../../constantes/transiciones'
import { exito } from '../../plugins/alertas'

const props = defineProps({
  solicitud: { type: Object, default: null },
})

const abierto = defineModel('abierto', { type: Boolean, default: false })
const solicitudes = useSolicitudesStore()
const id = useId()

const motivo = ref('')
const strike = ref(false)
const intentado = ref(false)
const guardando = ref(false)
const error = ref('')

const motivoValido = computed(() => motivo.value.trim().length > 0)
const nombreUsuario = computed(() => props.solicitud?.usuario?.nombre ?? 'el usuario')

watch(abierto, (valor) => {
  if (!valor) return
  motivo.value = ''
  // Sin servicios solo se rechaza por mal uso: el strike viene marcado.
  strike.value = !tieneServicios(props.solicitud)
  intentado.value = false
  error.value = ''
})

async function rechazar() {
  intentado.value = true
  if (!motivoValido.value) return
  guardando.value = true
  error.value = ''
  try {
    await solicitudes.rechazar(props.solicitud.id, { motivo: motivo.value, strike: strike.value })
    exito(strike.value ? 'Solicitud rechazada y strike registrado' : 'Solicitud rechazada')
    abierto.value = false
  } catch (e) {
    error.value = e.message
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
.mensaje-error {
  margin: var(--ym-espacio-3) 0 0;
  color: var(--ym-estado-pendiente-texto);
}
</style>
