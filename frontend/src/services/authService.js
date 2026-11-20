import { bd, CONTRASENA_DEMO_ADMIN } from '../mocks/bd'
import { ErrorApi, responder } from './simulacion'

/**
 * Autenticación del panel (simulada). Futuro: POST /api/admin/login y POST /api/logout.
 */
export const authService = {
  async login(correo, password) {
    const usuario = bd.usuarios.find((u) => u.correo.toLowerCase() === correo.trim().toLowerCase())
    if (!usuario || password !== CONTRASENA_DEMO_ADMIN) {
      await responder(null)
      throw new ErrorApi('Correo o contraseña incorrectos.', 401)
    }
    if (usuario.rol !== 'admin') {
      await responder(null)
      throw new ErrorApi('Esta cuenta no tiene acceso al panel de administración.', 403)
    }
    if (!usuario.activo) {
      await responder(null)
      throw new ErrorApi('Esta cuenta está deshabilitada.', 403)
    }
    return responder({ token: `demo-${usuario.id}-${Date.now()}`, usuario })
  },

  async logout() {
    return responder(true, 150)
  },
}
