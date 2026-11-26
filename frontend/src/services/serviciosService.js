import { pedir } from './http'

/** Catálogo de servicios de emergencia activos. GET /api/servicios */
export const serviciosService = {
  listar() {
    return pedir({ url: '/servicios' })
  },
}
