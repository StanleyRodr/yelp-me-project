import { bd, CONTRASENA_DEMO_ADMIN } from '../mocks/bd'
import { ahora } from '../mocks/tiempo'
import {
  darStrike,
  estadoCuenta,
  LIMITE_STRIKES,
  strikesActivos,
  suspender,
  suspensionVigente,
} from './reglasUsuarios'
import { ErrorApi, responder } from './simulacion'

/*
 * Usuarios de la app (rol "usuario") y perfil del admin.
 * Futuro: GET /api/admin/usuarios, GET /api/admin/usuarios/{id}, POST .../strikes,
 *         PUT /api/admin/strikes/{id}/anular, POST .../suspender, POST .../rehabilitar,
 *         PUT .../deshabilitar, PUT .../reactivar, PUT /api/perfil, PUT /api/perfil/password
 */

const nombreDe = (id) => {
  const u = bd.usuarios.find((x) => x.id === id)
  return u ? `${u.nombre} ${u.apellidos}` : null
}

function buscar(id) {
  const usuario = bd.usuarios.find((u) => u.id === Number(id))
  if (!usuario) throw new ErrorApi('Recurso no encontrado.', 404)
  return usuario
}

const resumen = (u) => ({
  ...u,
  estado_cuenta: estadoCuenta(u),
  strikes_activos: strikesActivos(u.id),
  limite_strikes: LIMITE_STRIKES,
  veces_suspendido: bd.suspensiones.filter((s) => s.usuario_id === u.id).length,
})

function detalle(u) {
  return {
    ...resumen(u),
    suspension_vigente: suspensionVigente(u.id),
    solicitudes: bd.solicitudes
      .filter((s) => s.usuario_id === u.id)
      .map((s) => ({ id: s.id, titulo: s.titulo, estado: s.estado, servicios: s.servicios, created_at: s.created_at })),
    strikes: bd.strikes
      .filter((s) => s.usuario_id === u.id)
      .map((s) => ({ ...s, admin_nombre: nombreDe(s.admin_id), anulado_por_nombre: nombreDe(s.anulado_por_id) })),
    suspensiones: bd.suspensiones
      .filter((s) => s.usuario_id === u.id)
      .map((s) => ({
        ...s,
        suspendido_por_nombre: nombreDe(s.suspendido_por_id),
        rehabilitado_por_nombre: nombreDe(s.rehabilitado_por_id),
      })),
  }
}

const exigirMotivo = (motivo) => {
  if (!motivo?.trim()) throw new ErrorApi('El campo motivo es obligatorio.')
  return motivo.trim()
}

export const usuariosService = {
  async listar() {
    return responder(bd.usuarios.filter((u) => u.rol === 'usuario').map(resumen))
  },

  async obtener(id) {
    return responder(detalle(buscar(id)))
  },

  async reportar(id, { motivo, adminId }) {
    const u = buscar(id)
    darStrike(u.id, { motivo: exigirMotivo(motivo), adminId })
    return responder(detalle(u))
  },

  async anularStrike(strikeId, { motivo, adminId }) {
    const strike = bd.strikes.find((s) => s.id === Number(strikeId))
    if (!strike) throw new ErrorApi('Recurso no encontrado.', 404)
    if (!strike.activo) throw new ErrorApi('Este strike ya no está activo.')
    Object.assign(strike, {
      activo: false,
      anulado_por_id: adminId,
      motivo_anulacion: exigirMotivo(motivo),
      anulado_en: ahora(),
    })
    return responder(detalle(buscar(strike.usuario_id)))
  },

  async suspender(id, { motivo, adminId }) {
    const u = buscar(id)
    if (suspensionVigente(u.id)) throw new ErrorApi('El usuario ya está suspendido.')
    suspender(u.id, { motivo: exigirMotivo(motivo), adminId })
    return responder(detalle(u))
  },

  /** Rehabilitar: cierra la suspensión vigente y reinicia los strikes activos a 0. */
  async rehabilitar(id, { adminId }) {
    const u = buscar(id)
    const vigente = suspensionVigente(u.id)
    if (!vigente) throw new ErrorApi('El usuario no está suspendido.')
    Object.assign(vigente, { rehabilitado_por_id: adminId, fecha_rehabilitacion: ahora() })
    bd.strikes.filter((s) => s.usuario_id === u.id && s.activo).forEach((s) => (s.activo = false))
    return responder(detalle(u))
  },

  async deshabilitar(id) {
    const u = buscar(id)
    Object.assign(u, { activo: false, deshabilitado_en: ahora() })
    return responder(detalle(u))
  },

  async reactivar(id) {
    const u = buscar(id)
    Object.assign(u, { activo: true, deshabilitado_en: null })
    return responder(detalle(u))
  },

  async actualizarPerfil(id, cambios) {
    const u = buscar(id)
    const { nombre, apellidos, telefono, correo, foto_perfil } = cambios
    const duplicado = bd.usuarios.find(
      (x) => x.id !== u.id && (x.correo === correo || x.telefono === telefono),
    )
    if (duplicado) throw new ErrorApi('El correo o el teléfono ya está registrado.')
    Object.assign(u, { nombre, apellidos, telefono, correo, foto_perfil: foto_perfil ?? u.foto_perfil })
    return responder(u)
  },

  async cambiarPassword(_id, { actual, nueva }) {
    // Simulación: la contraseña de demo no cambia en memoria.
    if (actual !== CONTRASENA_DEMO_ADMIN) throw new ErrorApi('La contraseña actual no es correcta.')
    if (!nueva || nueva.length < 8) throw new ErrorApi('La nueva contraseña debe tener al menos 8 caracteres.')
    return responder(true)
  },
}
