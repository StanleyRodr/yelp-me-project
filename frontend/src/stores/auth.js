import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authService } from '../services/authService'
import { CLAVE_SESION } from '../services/http'

function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_SESION) || 'null')
  } catch {
    return null
  }
}

function guardarSesion(sesion) {
  try {
    if (sesion) localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
    else localStorage.removeItem(CLAVE_SESION)
  } catch {
    // Almacenamiento bloqueado: la sesión dura hasta recargar.
  }
}

/** Sesión del administrador. */
export const useAuthStore = defineStore('auth', () => {
  const sesion = ref(leerSesion())
  const cargando = ref(false)
  const error = ref(null)

  const autenticado = computed(() => Boolean(sesion.value?.token))
  const admin = computed(() => sesion.value?.usuario ?? null)

  async function iniciarSesion(correo, password) {
    cargando.value = true
    error.value = null
    try {
      sesion.value = await authService.login(correo, password)
      guardarSesion(sesion.value)
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      cargando.value = false
    }
  }

  async function cerrarSesion() {
    try {
      await authService.logout()
    } finally {
      sesion.value = null
      guardarSesion(null)
    }
  }

  function actualizarAdmin(usuario) {
    sesion.value = { ...sesion.value, usuario }
    guardarSesion(sesion.value)
  }

  return { sesion, cargando, error, autenticado, admin, iniciarSesion, cerrarSesion, actualizarAdmin }
})
