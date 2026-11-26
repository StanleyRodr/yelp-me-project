import { onBeforeUnmount, onMounted } from 'vue'

/**
 * Ejecuta `fn` cada `intervalo` ms mientras el componente esté montado.
 * Se pausa con la pestaña oculta y, al volver, consulta de inmediato.
 */
export function useConsultaPeriodica(fn, intervalo = 15000) {
  let id = null

  const detener = () => {
    clearInterval(id)
    id = null
  }

  const iniciar = () => {
    detener()
    id = setInterval(fn, intervalo)
  }

  const alCambiarVisibilidad = () => {
    if (document.hidden) {
      detener()
    } else {
      fn()
      iniciar()
    }
  }

  onMounted(() => {
    iniciar()
    document.addEventListener('visibilitychange', alCambiarVisibilidad)
  })

  onBeforeUnmount(() => {
    detener()
    document.removeEventListener('visibilitychange', alCambiarVisibilidad)
  })
}
