import { bd, siguienteId } from '../mocks/bd'
import { ahora } from '../mocks/tiempo'

/*
 * Reglas de strikes y suspensiones (plan principal sección 8) aplicadas
 * sobre la BD en memoria. Las comparten usuariosService y solicitudesService.
 * Cuando exista la API, esta lógica vive en el backend.
 */

export const LIMITE_STRIKES = 3

export const strikesActivos = (usuarioId) =>
  bd.strikes.filter((s) => s.usuario_id === usuarioId && s.activo).length

export const suspensionVigente = (usuarioId) =>
  bd.suspensiones.find((s) => s.usuario_id === usuarioId && s.fecha_rehabilitacion === null) ?? null

/** Estado de cuenta derivado: Deshabilitado > Suspendido > Activo. */
export function estadoCuenta(usuario) {
  if (!usuario.activo) return 'Deshabilitado'
  if (suspensionVigente(usuario.id)) return 'Suspendido'
  return 'Activo'
}

export function suspender(usuarioId, { motivo, adminId = null }) {
  if (suspensionVigente(usuarioId)) return null
  const suspension = {
    id: siguienteId('suspensiones'),
    usuario_id: usuarioId,
    suspendido_por_id: adminId,
    motivo,
    fecha_suspension: ahora(),
    rehabilitado_por_id: null,
    fecha_rehabilitacion: null,
  }
  bd.suspensiones.push(suspension)
  return suspension
}

/** Agrega un strike; al llegar a 3 activos suspende automáticamente. */
export function darStrike(usuarioId, { motivo, adminId, solicitudId = null }) {
  bd.strikes.push({
    id: siguienteId('strikes'),
    usuario_id: usuarioId,
    admin_id: adminId,
    solicitud_id: solicitudId,
    motivo,
    activo: true,
    anulado_por_id: null,
    motivo_anulacion: null,
    anulado_en: null,
    created_at: ahora(),
  })
  if (strikesActivos(usuarioId) >= LIMITE_STRIKES) {
    suspender(usuarioId, { motivo: 'Suspensión automática por 3 strikes.' })
  }
}
