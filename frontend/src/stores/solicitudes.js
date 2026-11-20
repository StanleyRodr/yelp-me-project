import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { ESTADOS, ESTADOS_ACTIVOS } from '../constantes/estados'
import { solicitudesService } from '../services/solicitudesService'
import { useAuthStore } from './auth'

/** Solicitudes de ayuda: listado, detalle y acciones del admin. */
export const useSolicitudesStore = defineStore('solicitudes', () => {
  const lista = ref([])
  const actual = ref(null)
  const cargando = ref(false)
  const cargandoActual = ref(false)
  const error = ref(null)

  const conteoPorEstado = computed(() =>
    Object.fromEntries(ESTADOS.map((e) => [e.clave, lista.value.filter((s) => s.estado === e.clave).length])),
  )

  /** Pendientes de la más antigua a la más reciente (prioridad de atención). */
  const pendientesPorAntiguedad = computed(() =>
    lista.value
      .filter((s) => s.estado === 'Pendiente')
      .sort((a, b) => a.created_at.localeCompare(b.created_at)),
  )

  const activas = computed(() => lista.value.filter((s) => ESTADOS_ACTIVOS.includes(s.estado)))

  // Si ya hay una carga en curso (p. ej. AppShell y la vista a la vez), se reutiliza.
  let cargaEnCurso = null

  function cargar() {
    if (cargaEnCurso) return cargaEnCurso
    cargando.value = true
    error.value = null
    cargaEnCurso = solicitudesService
      .listar()
      .then((datos) => (lista.value = datos))
      .catch((e) => (error.value = e.message))
      .finally(() => {
        cargando.value = false
        cargaEnCurso = null
      })
    return cargaEnCurso
  }

  async function cargarUna(id) {
    cargandoActual.value = true
    error.value = null
    actual.value = null
    try {
      actual.value = await solicitudesService.obtener(id)
    } catch (e) {
      error.value = e.message
    } finally {
      cargandoActual.value = false
    }
  }

  /** Reemplaza la solicitud actualizada en el detalle y en el listado. */
  function aplicar(solicitud) {
    if (actual.value?.id === solicitud.id) actual.value = solicitud
    const i = lista.value.findIndex((s) => s.id === solicitud.id)
    if (i >= 0) lista.value[i] = solicitud
    return solicitud
  }

  const adminId = () => useAuthStore().admin?.id

  const enviar = async (id) => aplicar(await solicitudesService.enviar(id, { adminId: adminId() }))
  const terminar = async (id) => aplicar(await solicitudesService.terminar(id, { adminId: adminId() }))
  const rechazar = async (id, { motivo, strike }) =>
    aplicar(await solicitudesService.rechazar(id, { adminId: adminId(), motivo, strike }))
  const actualizarServicios = async (id, servicios) =>
    aplicar(await solicitudesService.actualizarServicios(id, { servicios }))

  return {
    lista, actual, cargando, cargandoActual, error,
    conteoPorEstado, pendientesPorAntiguedad, activas,
    cargar, cargarUna, enviar, terminar, rechazar, actualizarServicios,
  }
})
