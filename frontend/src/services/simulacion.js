/**
 * Utilidades para simular la API mientras se usan mocks.
 */
export const LATENCIA_MS = 400

export function esperar(ms = LATENCIA_MS) {
  return new Promise((resolver) => setTimeout(resolver, ms))
}

/** Responde una copia para que la UI nunca mute la "BD" en memoria. */
export async function responder(valor, ms) {
  await esperar(ms)
  return structuredClone(valor)
}

/** Error con el mismo mensaje que daría la API. */
export class ErrorApi extends Error {
  constructor(mensaje, codigo = 422) {
    super(mensaje)
    this.codigo = codigo
  }
}
