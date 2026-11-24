import { nombreCompleto } from './formato'

/**
 * Línea de tiempo derivada de las fechas de la solicitud (sin tabla de eventos):
 * created_at → enviado_en → cerrado_en (con quién y motivo).
 */
export function eventosDe(s) {
  if (!s) return []
  const eventos = [
    {
      clave: 'creada',
      titulo: 'Solicitud recibida',
      fecha: s.created_at,
      autor: nombreCompleto(s.usuario),
      token: 'pendiente',
    },
  ]

  if (s.enviado_en) {
    eventos.push({
      clave: 'enviada',
      titulo: 'Ayuda enviada',
      fecha: s.enviado_en,
      autor: nombreCompleto(s.admin),
      token: 'enviado',
    })
  }

  if (s.cerrado_en) {
    const titulos = {
      Terminado: 'Atención terminada',
      Rechazado: 'Solicitud rechazada',
      Cancelado: 'Cancelada por el usuario',
    }
    eventos.push({
      clave: 'cerrada',
      titulo: titulos[s.estado] ?? 'Solicitud cerrada',
      fecha: s.cerrado_en,
      autor: nombreCompleto(s.cerrado_por),
      detalle: s.motivo ? `Motivo: ${s.motivo}` : '',
      token: s.estado.toLowerCase(),
    })
  }

  return eventos
}
