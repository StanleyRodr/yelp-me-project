import { defineStore } from 'pinia'
import { ref } from 'vue'
import { perfilService } from '../services/perfilService'
import { useAuthStore } from './auth'

/** Perfil del admin con sesión: datos propios, foto, contraseña y actividad. */
export const usePerfilStore = defineStore('perfil', () => {
  const actividad = ref(null)
  const cargandoActividad = ref(false)
  const error = ref(null)

  async function cargarActividad() {
    cargandoActividad.value = true
    error.value = null
    try {
      actividad.value = await perfilService.actividad()
    } catch (e) {
      error.value = e.message
    } finally {
      cargandoActividad.value = false
    }
  }

  /**
   * Guarda datos y, si cambió, la foto (subir o quitar). Cada respuesta trae el
   * usuario actualizado: se refleja en la sesión para que la navbar cambie.
   */
  async function guardarDatos({ datos, foto = null, quitarFoto = false }) {
    const auth = useAuthStore()
    let usuario = await perfilService.actualizar(datos)
    if (foto) usuario = await perfilService.subirFoto(foto)
    else if (quitarFoto) usuario = await perfilService.quitarFoto()
    auth.actualizarAdmin(usuario)
    return usuario
  }

  const cambiarPassword = (datos) => perfilService.cambiarPassword(datos)

  return { actividad, cargandoActividad, error, cargarActividad, guardarDatos, cambiarPassword }
})
