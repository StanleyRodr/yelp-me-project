import { pedir } from './http'

/*
 * Solicitudes de ayuda desde el panel (plan 005). El admin que actúa sale del
 * token: el frontend no lo envía. Errores: ErrorApi (409 = la solicitud ya
 * cambió de estado; 422 = validación).
 */
const estado = (id, datos) => pedir({ method: 'put', url: `/admin/solicitudes/${id}/estado`, data: datos })

export const solicitudesService = {
  /** GET /api/admin/solicitudes */
  listar() {
    return pedir({ url: '/admin/solicitudes' })
  },

  /** GET /api/admin/solicitudes/{id} */
  obtener(id) {
    return pedir({ url: `/admin/solicitudes/${id}` })
  },

  enviar(id) {
    return estado(id, { estado: 'Enviado' })
  },

  terminar(id) {
    return estado(id, { estado: 'Terminado' })
  },

  rechazar(id, { motivo, strike = false }) {
    return estado(id, { estado: 'Rechazado', motivo, strike })
  },

  /** PUT /api/admin/solicitudes/{id}/servicios */
  actualizarServicios(id, { servicios }) {
    return pedir({ method: 'put', url: `/admin/solicitudes/${id}/servicios`, data: { servicios } })
  },
}
