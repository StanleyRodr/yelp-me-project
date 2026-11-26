import { pedir } from './http'

/** Perfil propio del admin con sesión (plan 006). */
export const perfilService = {
  /** GET /api/admin/actividad → { solicitudes, strikes_dados } */
  actividad() {
    return pedir({ url: '/admin/actividad' })
  },

  /** PUT /api/perfil → usuario actualizado */
  actualizar({ nombre, apellidos, telefono, correo }) {
    return pedir({ method: 'put', url: '/perfil', data: { nombre, apellidos, telefono, correo } })
  },

  /** POST /api/perfil/foto (multipart, campo `foto`) → usuario con la URL nueva */
  subirFoto(archivo) {
    const datos = new FormData()
    datos.append('foto', archivo)
    return pedir({ method: 'post', url: '/perfil/foto', data: datos })
  },

  /** DELETE /api/perfil/foto → usuario sin foto */
  quitarFoto() {
    return pedir({ method: 'delete', url: '/perfil/foto' })
  },

  /** PUT /api/perfil/password. El formulario ya validó que la confirmación coincida. */
  cambiarPassword({ actual, nueva }) {
    return pedir({ method: 'put', url: '/perfil/password', data: { actual, nueva, nueva_confirmation: nueva } })
  },
}
