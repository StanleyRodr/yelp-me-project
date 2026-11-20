/**
 * Fechas relativas a "ahora" para que la demo siempre se vea reciente.
 * Devuelve ISO 8601, igual que la API.
 */
export function hace({ dias = 0, horas = 0, minutos = 0 } = {}) {
  const ms = ((dias * 24 + horas) * 60 + minutos) * 60 * 1000
  return new Date(Date.now() - ms).toISOString()
}

export function ahora() {
  return new Date().toISOString()
}
