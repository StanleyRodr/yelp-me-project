/**
 * Niveles del indicador de espera de la cola de pendientes.
 * El color se acompaña siempre de una etiqueta (no depender solo del color).
 */
export const NIVELES_ESPERA = [
  { clave: 'reciente', hasta: 10, etiqueta: 'Reciente' },
  { clave: 'espera', hasta: 30, etiqueta: 'En espera' },
  { clave: 'prolongada', hasta: Infinity, etiqueta: 'Espera prolongada' },
]

export const nivelEspera = (minutos) => NIVELES_ESPERA.find((n) => minutos < n.hasta)
