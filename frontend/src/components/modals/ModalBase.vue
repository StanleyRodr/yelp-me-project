<template>
  <Teleport to="body">
    <div
      ref="elemento"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="idTitulo"
    >
      <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable" :class="tamano && `modal-${tamano}`">
        <div class="modal-content">
          <div class="modal-header">
            <h2 :id="idTitulo" class="modal-title">{{ titulo }}</h2>
            <button type="button" class="btn-close" aria-label="Cerrar" :disabled="bloqueado" @click="abierto = false"></button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.pie" class="modal-footer">
            <slot name="pie" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'
import { Modal } from '../../plugins/bootstrap'

/**
 * Modal controlado con v-model:abierto sobre el Modal de Bootstrap
 * (atrapa el foco, cierra con Esc y con el fondo).
 * `bloqueado` impide cerrarlo mientras se guarda.
 */
const props = defineProps({
  titulo: { type: String, required: true },
  tamano: { type: String, default: '' },
  bloqueado: { type: Boolean, default: false },
})

const emit = defineEmits(['cerrado'])
const abierto = defineModel('abierto', { type: Boolean, default: false })

const elemento = ref(null)
const idTitulo = useId()
let instancia = null
let disparador = null

function alOcultar(evento) {
  if (props.bloqueado) evento.preventDefault()
}

function alOculto() {
  abierto.value = false
  // Devolver el foco al botón que abrió el modal
  disparador?.focus?.()
  disparador = null
  emit('cerrado')
}

function alMostrado() {
  elemento.value.querySelector('[autofocus], input, textarea, select')?.focus()
}

function abrir() {
  disparador = document.activeElement
  instancia.show()
}

onMounted(() => {
  instancia = new Modal(elemento.value)
  elemento.value.addEventListener('hide.bs.modal', alOcultar)
  elemento.value.addEventListener('hidden.bs.modal', alOculto)
  elemento.value.addEventListener('shown.bs.modal', alMostrado)
  if (abierto.value) abrir()
})

watch(abierto, (valor) => {
  if (!instancia) return
  if (valor) abrir()
  else instancia.hide()
})

onBeforeUnmount(() => {
  instancia?.hide()
  instancia?.dispose()
  instancia = null
})
</script>

<style scoped>
.modal-title {
  font-size: var(--ym-texto-lg);
  margin: 0;
}

.modal-footer {
  gap: var(--ym-espacio-2);
}

.modal-footer > :deep(*) {
  margin: 0;
}
</style>
