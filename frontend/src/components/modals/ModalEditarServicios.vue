<template>
  <ModalBase v-model:abierto="abierto" titulo="Editar servicios" :bloqueado="guardando">
    <p class="texto-ayuda">
      Ajusta los servicios según la situación real. Debe quedar al menos uno.
    </p>

    <fieldset>
      <legend class="visually-hidden">Servicios requeridos</legend>
      <div class="opciones">
        <template v-for="s in catalogos.servicios" :key="s.id">
          <input
            :id="`${id}-${s.id}`"
            v-model="seleccion"
            class="btn-check"
            type="checkbox"
            :value="s.id"
            :disabled="guardando"
          />
          <label class="btn opcion" :for="`${id}-${s.id}`">
            <span class="material-symbols-outlined" aria-hidden="true">{{ s.icono }}</span>
            {{ s.nombre }}
          </label>
        </template>
      </div>
    </fieldset>

    <p v-if="error" class="mensaje-error" role="alert">{{ error }}</p>

    <template #pie>
      <button type="button" class="btn btn-outline-secondary" :disabled="guardando" @click="abierto = false">
        Cancelar
      </button>
      <BotonCarga :cargando="guardando" icono="bi-check2" @click="guardar">Guardar servicios</BotonCarga>
    </template>
  </ModalBase>
</template>

<script setup>
import { ref, useId, watch } from 'vue'
import ModalBase from './ModalBase.vue'
import BotonCarga from '../comunes/BotonCarga.vue'
import { useCatalogosStore } from '../../stores/catalogos'
import { useSolicitudesStore } from '../../stores/solicitudes'
import { exito } from '../../plugins/alertas'

const props = defineProps({
  solicitud: { type: Object, default: null },
})

const abierto = defineModel('abierto', { type: Boolean, default: false })
const catalogos = useCatalogosStore()
const solicitudes = useSolicitudesStore()
const id = useId()

const seleccion = ref([])
const guardando = ref(false)
const error = ref('')

watch(abierto, (valor) => {
  if (!valor) return
  catalogos.cargar()
  seleccion.value = [...(props.solicitud?.servicios ?? [])]
  error.value = ''
})

async function guardar() {
  if (!seleccion.value.length) {
    error.value = 'Selecciona al menos un servicio.'
    return
  }
  guardando.value = true
  error.value = ''
  try {
    await solicitudes.actualizarServicios(props.solicitud.id, seleccion.value)
    exito('Servicios actualizados')
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
  margin-bottom: var(--ym-espacio-4);
}

fieldset {
  border: 0;
  margin: 0;
  padding: 0;
}

.opciones {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(10rem, 1fr));
  gap: var(--ym-espacio-2);
}

.opcion {
  display: flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  text-align: left;
  font-weight: var(--ym-peso-normal);
  border: 1px solid var(--ym-borde-fuerte);
  color: var(--ym-texto);
  background: var(--ym-superficie);
}

.opcion .material-symbols-outlined {
  color: var(--ym-marca-texto);
}

.btn-check:checked + .opcion {
  background: var(--ym-marca);
  border-color: var(--ym-marca-contorno);
  color: var(--ym-sobre-marca);
  font-weight: var(--ym-peso-medio);
}

.btn-check:checked + .opcion .material-symbols-outlined {
  color: var(--ym-sobre-marca);
}

.btn-check:focus-visible + .opcion {
  outline: 2px solid var(--ym-foco);
  outline-offset: 2px;
}

.mensaje-error {
  margin: var(--ym-espacio-3) 0 0;
  color: var(--ym-estado-pendiente-texto);
}
</style>
