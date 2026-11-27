import { http, pedir } from './http'

/** Recuperar la contraseña en 3 pasos (plan 008, rutas públicas). */
export const recuperacionService = {
  /** POST /api/recuperacion → mensaje neutro (exista o no el correo) */
  async pedirCodigo(correo) {
    const { data } = await http.post('/recuperacion', { correo })
    return data?.mensaje ?? ''
  },

  /** POST /api/recuperacion/verificacion → { valido: true } o 422 en `codigo` */
  verificar(correo, codigo) {
    return pedir({ method: 'post', url: '/recuperacion/verificacion', data: { correo, codigo } })
  },

  /** PUT /api/recuperacion → cierra todas las sesiones de la cuenta */
  cambiar({ correo, codigo, password }) {
    return pedir({
      method: 'put',
      url: '/recuperacion',
      data: { correo, codigo, password, password_confirmation: password },
    })
  },
}
