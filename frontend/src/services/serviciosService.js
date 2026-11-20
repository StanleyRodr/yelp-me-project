import { bd } from '../mocks/bd'
import { responder } from './simulacion'

/** Catálogo de servicios de emergencia. Futuro: GET /api/servicios. */
export const serviciosService = {
  async listar() {
    return responder(bd.servicios.filter((s) => s.activo))
  },
}
