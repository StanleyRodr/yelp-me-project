import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { serviciosService } from '../services/serviciosService'

/** Catálogos fijos (servicios de emergencia). Se cargan una vez. */
export const useCatalogosStore = defineStore('catalogos', () => {
  const servicios = ref([])
  const cargando = ref(false)
  const cargado = ref(false)

  const servicioPorId = computed(() => Object.fromEntries(servicios.value.map((s) => [s.id, s])))

  async function cargar() {
    if (cargado.value || cargando.value) return
    cargando.value = true
    try {
      servicios.value = await serviciosService.listar()
      cargado.value = true
    } finally {
      cargando.value = false
    }
  }

  return { servicios, cargando, cargado, servicioPorId, cargar }
})
