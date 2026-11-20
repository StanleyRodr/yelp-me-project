import { bd } from '../mocks/bd'
import { ahora } from '../mocks/tiempo'
import { puede } from '../constantes/transiciones'
import { darStrike } from './reglasUsuarios'
import { ErrorApi, responder } from './simulacion'

/*
 * Solicitudes de ayuda. Forma de respuesta = la que devolverá la API:
 * columnas de `solicitudes` + `servicios` (ids) + datos básicos del usuario y del admin.
 * Futuro: GET /api/admin/solicitudes, GET /api/admin/solicitudes/{id},
 *         PUT /api/admin/solicitudes/{id}/estado, PUT /api/admin/solicitudes/{id}/servicios
 */

const persona = (id) => {
  const u = bd.usuarios.find((x) => x.id === id)
  return u ? { id: u.id, nombre: u.nombre, apellidos: u.apellidos, telefono: u.telefono } : null
}

const expandir = (s) => ({
  ...s,
  usuario: persona(s.usuario_id),
  admin: s.admin_id ? persona(s.admin_id) : null,
  cerrado_por: s.cerrado_por_id ? persona(s.cerrado_por_id) : null,
})

function buscar(id) {
  const solicitud = bd.solicitudes.find((s) => s.id === Number(id))
  if (!solicitud) throw new ErrorApi('Recurso no encontrado.', 404)
  return solicitud
}

function exigir(solicitud, accion) {
  if (!puede(solicitud, accion)) {
    throw new ErrorApi(`La solicitud ya cambió de estado (${solicitud.estado}). Recarga para ver su estado actual.`, 409)
  }
}

export const solicitudesService = {
  async listar() {
    return responder(bd.solicitudes.map(expandir))
  },

  async obtener(id) {
    return responder(expandir(buscar(id)))
  },

  async enviar(id, { adminId }) {
    const s = buscar(id)
    exigir(s, 'enviar')
    Object.assign(s, { estado: 'Enviado', admin_id: adminId, enviado_en: ahora(), updated_at: ahora() })
    return responder(expandir(s))
  },

  async terminar(id, { adminId }) {
    const s = buscar(id)
    exigir(s, 'terminar')
    Object.assign(s, { estado: 'Terminado', cerrado_por_id: adminId, cerrado_en: ahora(), updated_at: ahora() })
    return responder(expandir(s))
  },

  async rechazar(id, { adminId, motivo, strike = false }) {
    const s = buscar(id)
    exigir(s, 'rechazar')
    if (!motivo?.trim()) throw new ErrorApi('El campo motivo es obligatorio.')
    Object.assign(s, {
      estado: 'Rechazado',
      admin_id: adminId,
      cerrado_por_id: adminId,
      motivo: motivo.trim(),
      cerrado_en: ahora(),
      updated_at: ahora(),
    })
    if (strike) {
      darStrike(s.usuario_id, { motivo: 'Broma / mal uso del servicio.', adminId, solicitudId: s.id })
    }
    return responder(expandir(s))
  },

  async actualizarServicios(id, { servicios }) {
    const s = buscar(id)
    exigir(s, 'editarServicios')
    if (!servicios?.length) throw new ErrorApi('Selecciona al menos un servicio.')
    Object.assign(s, { servicios: [...new Set(servicios)], updated_at: ahora() })
    return responder(expandir(s))
  },
}
