import * as datos from './datos'

/**
 * "Base de datos" en memoria para los services mientras no exista la API.
 * Se reinicia al recargar la página.
 */
export const bd = {
  servicios: structuredClone(datos.servicios),
  usuarios: structuredClone(datos.usuarios),
  solicitudes: structuredClone(datos.solicitudes),
  strikes: structuredClone(datos.strikes),
  suspensiones: structuredClone(datos.suspensiones),
}

export function siguienteId(tabla) {
  return Math.max(0, ...bd[tabla].map((r) => r.id)) + 1
}

/** Contraseña de todos los admins de demo (igual que el seeder). */
export const CONTRASENA_DEMO_ADMIN = 'Admin123*'
