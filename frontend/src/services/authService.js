import { pedir } from './http'

/**
 * Sesión del panel contra la API (plan 003).
 * Errores: ErrorApi con el mensaje de la API (401 credenciales, 403 sin acceso o deshabilitada).
 */
export const authService = {
  /** POST /api/admin/login → { token, usuario } */
  login(correo, password) {
    return pedir({ method: 'post', url: '/admin/login', data: { correo, password } })
  },

  /** GET /api/sesion → usuario del token (confirma que sigue válido). */
  sesion() {
    return pedir({ method: 'get', url: '/sesion' })
  },

  /** DELETE /api/sesion → borra el token actual. */
  logout() {
    return pedir({ method: 'delete', url: '/sesion' })
  },
}
