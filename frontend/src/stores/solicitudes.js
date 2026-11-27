import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { ESTADOS, ESTADOS_ACTIVOS } from '../constantes/estados'
import { solicitudesService } from '../services/solicitudesService'

/** Solicitudes de ayuda: listado, detalle y acciones del admin. */
export const useSolicitudesStore = defineStore('solicitudes', () => {
  const lista = ref([])
  const actual = ref(null)
  const cargando = ref(false)
  const cargandoActual = ref(false)
  const error = ref(null)
  /** Momento de la última lista recibida (para "Actualizado hh:mm"). */
  const actualizadoEn = ref(null)

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

  /**
   * `silenciosa`: recarga periódica. No marca `cargando` (sin skeleton) y, si
   * falla, conserva los datos que ya había sin mostrar error.
   */
  function cargar({ silenciosa = false } = {}) {
    if (cargaEnCurso) return cargaEnCurso
    if (!silenciosa) {
      cargando.value = true
      error.value = null
    }
    cargaEnCurso = solicitudesService
      .listar()
      .then((datos) => {
        lista.value = datos
        actualizadoEn.value = new Date().toISOString()
        error.value = null
      })
      .catch((e) => {
        if (!silenciosa) error.value = e.message
      })
      .finally(() => {
        cargando.value = false
        cargaEnCurso = null
      })
    return cargaEnCurso
  }

  const refrescar = () => cargar({ silenciosa: true })

  /** `conservar`: refresca sin vaciar la solicitud actual (sin skeleton). */
  async function cargarUna(id, { conservar = false } = {}) {
    cargandoActual.value = true
    error.value = null
    if (!conservar) actual.value = null
    try {
      aplicar((actual.value = await solicitudesService.obtener(id)))
    } catch (e) {
      error.value = e.message
    } finally {
      cargandoActual.value = false
    }
  }

  /**
   * Recarga periódica del detalle (Atención): silenciosa, sin skeleton ni error;
   * si falla se conserva lo que ya se veía. Solo aplica si sigue abierta la misma.
   */
  async function refrescarUna(id) {
    try {
      const solicitud = await solicitudesService.obtener(id)
      if (String(actual.value?.id) === String(id)) aplicar(solicitud)
    } catch {
      // Silenciosa: la siguiente consulta lo vuelve a intentar.
    }
  }

  /** Reemplaza la solicitud actualizada en el detalle y en el listado. */
  function aplicar(solicitud) {
    if (actual.value?.id === solicitud.id) actual.value = solicitud
    const i = lista.value.findIndex((s) => s.id === solicitud.id)
    if (i >= 0) lista.value[i] = solicitud
    return solicitud
  }

  // El admin que actúa lo toma la API del token.
  const enviar = async (id) => aplicar(await solicitudesService.enviar(id))
  const terminar = async (id) => aplicar(await solicitudesService.terminar(id))
  const rechazar = async (id, { motivo, strike }) => aplicar(await solicitudesService.rechazar(id, { motivo, strike }))
  const actualizarServicios = async (id, servicios) =>
    aplicar(await solicitudesService.actualizarServicios(id, { servicios }))

  return {
    lista, actual, cargando, cargandoActual, error, actualizadoEn,
    conteoPorEstado, pendientesPorAntiguedad, activas,
    cargar, refrescar, cargarUna, refrescarUna, enviar, terminar, rechazar, actualizarServicios,
  }
})
