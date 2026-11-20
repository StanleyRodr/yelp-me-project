/**
 * Estados oficiales de una solicitud (plan principal 7.1).
 * `token` apunta a las variables --ym-estado-<token>[-texto|-fondo] de tokens.css.
 */
export const ESTADOS = [
  { clave: 'Pendiente', icono: 'emergency', token: 'pendiente', final: false },
  { clave: 'Enviado', icono: 'local_shipping', token: 'enviado', final: false },
  { clave: 'Terminado', icono: 'check_circle', token: 'terminado', final: true },
  { clave: 'Rechazado', icono: 'block', token: 'rechazado', final: true },
  { clave: 'Cancelado', icono: 'cancel', token: 'cancelado', final: true },
]

export const ESTADO_POR_CLAVE = Object.fromEntries(ESTADOS.map((e) => [e.clave, e]))

export const ESTADOS_ACTIVOS = ['Pendiente', 'Enviado']
