/** Formatos de presentación en español de México. */

const fmtFechaHora = new Intl.DateTimeFormat('es-MX', {
  day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
})
const fmtFecha = new Intl.DateTimeFormat('es-MX', { day: 'numeric', month: 'short', year: 'numeric' })

export const fechaHora = (iso) => (iso ? fmtFechaHora.format(new Date(iso)) : '')
export const fecha = (iso) => (iso ? fmtFecha.format(new Date(iso)) : '')

export const minutosDesde = (iso, ahora = Date.now()) =>
  Math.max(0, Math.floor((ahora - new Date(iso).getTime()) / 60000))

/** Duración compacta: "14 min", "2 h 5 min", "3 d". */
export function duracion(minutos) {
  if (minutos < 60) return `${minutos} min`
  if (minutos < 24 * 60) {
    const h = Math.floor(minutos / 60)
    const m = minutos % 60
    return m ? `${h} h ${m} min` : `${h} h`
  }
  return `${Math.floor(minutos / (24 * 60))} d`
}

export const haceTiempo = (iso, ahora) => `hace ${duracion(minutosDesde(iso, ahora))}`

export const nombreCompleto = (p) => (p ? [p.nombre, p.apellidos].filter(Boolean).join(' ') : '')

/** Texto para búsqueda: minúsculas y sin acentos ("Policía" ≈ "policia"). */
export const normalizar = (texto) =>
  String(texto ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()

/** Folio visible: #0007 */
export const folio = (id) => `#${String(id).padStart(4, '0')}`
