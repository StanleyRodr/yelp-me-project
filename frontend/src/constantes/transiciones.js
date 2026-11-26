/**
 * Transiciones de estado que puede hacer el ADMIN (plan principal 7.2).
 * Solo decide qué botones mostrar: la validación real la hace la API.
 * Espejo de backend/app/Support/TransicionesSolicitud.php: si cambia una, cambiar la otra.
 */
export const ACCIONES = {
  enviar: { clave: 'enviar', etiqueta: 'Enviar ayuda', icono: 'bi-send', variante: 'primary' },
  terminar: { clave: 'terminar', etiqueta: 'Marcar como terminada', icono: 'bi-check2-circle', variante: 'success' },
  editarServicios: { clave: 'editarServicios', etiqueta: 'Editar servicios', icono: 'bi-pencil', variante: 'outline-secondary' },
  rechazar: { clave: 'rechazar', etiqueta: 'Rechazar', icono: 'bi-slash-circle', variante: 'outline-danger' },
}

export const tieneServicios = (solicitud) => (solicitud?.servicios?.length ?? 0) > 0

/**
 * Acciones permitidas al admin según estado y si la solicitud tiene servicios.
 * - Con servicios:  Pendiente → enviar, rechazar, editar servicios
 *                   Enviado   → terminar, editar servicios
 * - Sin servicios (solo contactos): Pendiente → rechazar (solo por mal uso)
 * - Estados finales: ninguna.
 */
export function accionesPermitidas(solicitud) {
  if (!solicitud) return []
  const conServicios = tieneServicios(solicitud)
  switch (solicitud.estado) {
    case 'Pendiente':
      return conServicios
        ? [ACCIONES.enviar, ACCIONES.editarServicios, ACCIONES.rechazar]
        : [ACCIONES.rechazar]
    case 'Enviado':
      return conServicios ? [ACCIONES.terminar, ACCIONES.editarServicios] : []
    default:
      return []
  }
}

export function puede(solicitud, clave) {
  return accionesPermitidas(solicitud).some((a) => a.clave === clave)
}
