/**
 * Estados de cuenta de un usuario (derivados en el backend/service):
 * Deshabilitado > Suspendido > Activo.
 * `token` apunta a --ym-estado-<token>[-texto|-fondo].
 */
export const ESTADOS_CUENTA = [
  { clave: 'Activo', icono: 'check_circle', token: 'terminado' },
  { clave: 'Suspendido', icono: 'pause_circle', token: 'pendiente' },
  { clave: 'Deshabilitado', icono: 'person_off', token: 'cancelado' },
]

export const CUENTA_POR_CLAVE = Object.fromEntries(ESTADOS_CUENTA.map((e) => [e.clave, e]))

/**
 * Acciones del admin sobre una cuenta según su estado (plan principal sección 8).
 * Espejo de ReglasCuenta::ACCIONES en el backend: si cambia una, cambiar la otra.
 */
export const ACCIONES_CUENTA = {
  reportar: { clave: 'reportar', etiqueta: 'Reportar (sumar strike)', icono: 'bi-flag', variante: 'outline-danger' },
  suspender: { clave: 'suspender', etiqueta: 'Suspender', icono: 'bi-pause-circle', variante: 'outline-secondary' },
  rehabilitar: { clave: 'rehabilitar', etiqueta: 'Rehabilitar', icono: 'bi-play-circle', variante: 'primary' },
  deshabilitar: { clave: 'deshabilitar', etiqueta: 'Deshabilitar cuenta', icono: 'bi-person-x', variante: 'outline-secondary' },
  reactivar: { clave: 'reactivar', etiqueta: 'Reactivar cuenta', icono: 'bi-person-check', variante: 'primary' },
}

export function accionesCuenta(usuario) {
  if (!usuario) return []
  const a = ACCIONES_CUENTA
  switch (usuario.estado_cuenta) {
    case 'Activo':
      return [a.reportar, a.suspender, a.deshabilitar]
    case 'Suspendido':
      return [a.rehabilitar, a.reportar, a.deshabilitar]
    case 'Deshabilitado':
      return [a.reactivar]
    default:
      return []
  }
}

/** Situación de un strike para el historial. */
export function situacionStrike(strike) {
  if (strike.activo) return 'Activo'
  if (strike.anulado_por_id) return 'Anulado'
  return 'Reiniciado'
}
