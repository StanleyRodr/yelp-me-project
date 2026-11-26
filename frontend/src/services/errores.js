/**
 * Error uniforme de la API. Todos los services rechazan con esta clase:
 *  - mensaje: el "mensaje" de la API (ya en español) o uno local si no hubo respuesta
 *  - codigo:  código HTTP (0 = sin conexión)
 *  - errores: errores por campo de un 422 ({ campo: ['mensaje'] }) o null
 */
export class ErrorApi extends Error {
  constructor(mensaje, codigo = 0, errores = null) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.codigo = codigo
    this.errores = errores
  }

  /** Primer mensaje de un campo (para mostrarlo bajo el input). */
  campo(nombre) {
    return this.errores?.[nombre]?.[0] ?? ''
  }
}

export const MENSAJE_SIN_CONEXION = 'No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo.'
