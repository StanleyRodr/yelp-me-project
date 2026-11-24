import { onBeforeUnmount, ref } from 'vue'

/** Marca de tiempo reactiva que se actualiza cada `intervalo` ms. */
export function useAhora(intervalo = 30000) {
  const ahora = ref(Date.now())
  const id = setInterval(() => (ahora.value = Date.now()), intervalo)
  onBeforeUnmount(() => clearInterval(id))
  return ahora
}
