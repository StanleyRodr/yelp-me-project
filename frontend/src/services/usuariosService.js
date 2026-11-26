import { pedir } from './http'

/*
 * Gestión de usuarios de la app desde el panel (plan 006). Todas las acciones
 * responden el detalle actualizado de la cuenta. El admin que actúa sale del
 * token. Errores: ErrorApi (409 = la acción no corresponde al estado actual).
 */
const base = (id) => `/admin/usuarios/${id}`

export const usuariosService = {
  /** GET /api/admin/usuarios */
  listar() {
    return pedir({ url: '/admin/usuarios' })
  },

  /** GET /api/admin/usuarios/{id} */
  obtener(id) {
    return pedir({ url: base(id) })
  },

  /** POST /api/admin/usuarios/{id}/strikes */
  reportar(id, { motivo }) {
    return pedir({ method: 'post', url: `${base(id)}/strikes`, data: { motivo } })
  },

  /** PUT /api/admin/strikes/{id}/anulacion */
  anularStrike(strikeId, { motivo }) {
    return pedir({ method: 'put', url: `/admin/strikes/${strikeId}/anulacion`, data: { motivo } })
  },

  /** POST /api/admin/usuarios/{id}/suspension */
  suspender(id, { motivo }) {
    return pedir({ method: 'post', url: `${base(id)}/suspension`, data: { motivo } })
  },

  /** DELETE /api/admin/usuarios/{id}/suspension — rehabilitar */
  rehabilitar(id) {
    return pedir({ method: 'delete', url: `${base(id)}/suspension` })
  },

  /** PUT /api/admin/usuarios/{id}/cuenta */
  deshabilitar(id) {
    return pedir({ method: 'put', url: `${base(id)}/cuenta`, data: { activo: false } })
  },

  reactivar(id) {
    return pedir({ method: 'put', url: `${base(id)}/cuenta`, data: { activo: true } })
  },
}
