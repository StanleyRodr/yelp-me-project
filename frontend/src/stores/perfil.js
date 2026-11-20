import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usuariosService } from '../services/usuariosService'
import { useAuthStore } from './auth'

/** Perfil del admin con sesión: datos propios, contraseña y actividad. */
export const usePerfilStore = defineStore('perfil', () => {
  const actividad = ref(null)
  const cargandoActividad = ref(false)
  const error = ref(null)

  const adminId = () => useAuthStore().admin?.id

  async function cargarActividad() {
    cargandoActividad.value = true
    error.value = null
    try {
      actividad.value = await usuariosService.actividadAdmin(adminId())
    } catch (e) {
      error.value = e.message
    } finally {
      cargandoActividad.value = false
    }
  }

  /** Guarda y actualiza la sesión para que la navbar muestre el nombre nuevo. */
  async function guardarDatos(datos) {
    const usuario = await usuariosService.actualizarPerfil(adminId(), datos)
    useAuthStore().actualizarAdmin(usuario)
    return usuario
  }

  const cambiarPassword = (datos) => usuariosService.cambiarPassword(adminId(), datos)

  return { actividad, cargandoActividad, error, cargarActividad, guardarDatos, cambiarPassword }
})
