import { onBeforeUnmount, ref, watch } from 'vue'

/**
 * Devuelve `mostrar` = true solo si `cargando` dura más de `retraso` ms.
 * Evita que el skeleton destelle en cargas muy rápidas.
 * @param {() => boolean} cargando getter reactivo
 */
export function useCargaDiferida(cargando, retraso = 150) {
  const mostrar = ref(false)
  let temporizador = null

  const limpiar = () => {
    clearTimeout(temporizador)
    temporizador = null
  }

  watch(
    cargando,
    (activo) => {
      limpiar()
      if (activo) temporizador = setTimeout(() => (mostrar.value = true), retraso)
      else mostrar.value = false
    },
    { immediate: true },
  )

  onBeforeUnmount(limpiar)
  return mostrar
}
