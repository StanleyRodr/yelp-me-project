import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usuariosService } from '../services/usuariosService'

/** Usuarios de la app: listado, perfil, strikes y suspensiones. */
export const useUsuariosStore = defineStore('usuarios', () => {
  const lista = ref([])
  const actual = ref(null)
  const cargando = ref(false)
  const cargandoActual = ref(false)
  const error = ref(null)

  async function cargar() {
    cargando.value = true
    error.value = null
    try {
      lista.value = await usuariosService.listar()
    } catch (e) {
      error.value = e.message
    } finally {
      cargando.value = false
    }
  }

  async function cargarUno(id) {
    cargandoActual.value = true
    error.value = null
    actual.value = null
    try {
      actual.value = await usuariosService.obtener(id)
    } catch (e) {
      error.value = e.message
    } finally {
      cargandoActual.value = false
    }
  }

  /** Actualiza el perfil abierto y su fila en el listado. */
  function aplicar(detalle) {
    actual.value = detalle
    const i = lista.value.findIndex((u) => u.id === detalle.id)
    if (i >= 0) {
      const { solicitudes, strikes, suspensiones, suspension_vigente, ...resumen } = detalle
      lista.value[i] = resumen
    }
    return detalle
  }

  // El admin que actúa lo toma la API del token.
  const reportar = async (id, motivo) => aplicar(await usuariosService.reportar(id, { motivo }))
  const anularStrike = async (strikeId, motivo) => aplicar(await usuariosService.anularStrike(strikeId, { motivo }))
  const suspender = async (id, motivo) => aplicar(await usuariosService.suspender(id, { motivo }))
  const rehabilitar = async (id) => aplicar(await usuariosService.rehabilitar(id))
  const deshabilitar = async (id) => aplicar(await usuariosService.deshabilitar(id))
  const reactivar = async (id) => aplicar(await usuariosService.reactivar(id))

  return {
    lista, actual, cargando, cargandoActual, error,
    cargar, cargarUno, reportar, anularStrike, suspender, rehabilitar, deshabilitar, reactivar,
  }
})
