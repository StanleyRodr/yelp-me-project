import axios from 'axios'
import { ErrorApi, MENSAJE_SIN_CONEXION } from './errores'

export const CLAVE_SESION = 'ym_sesion'

/**
 * Cliente HTTP de la API Laravel (formato { ok, mensaje, datos, errores? }).
 * Los services usan `pedir()`: devuelve `datos` o rechaza con ErrorApi.
 */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8000/api',
  headers: { Accept: 'application/json' },
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  try {
    const sesion = JSON.parse(localStorage.getItem(CLAVE_SESION) || 'null')
    if (sesion?.token) config.headers.Authorization = `Bearer ${sesion.token}`
  } catch {
    // Sin sesión guardada o almacenamiento bloqueado: petición sin token.
  }
  return config
})

// Lo registra el store de sesión: limpia la sesión y manda a /login.
// Así http.js no importa router ni stores (sin dependencias circulares).
let alExpirarSesion = null
export function registrarSesionExpirada(fn) {
  alExpirarSesion = fn
}

const ES_LOGIN = /\/(admin\/)?login$/

http.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const respuesta = error.response

    if (!respuesta) {
      return Promise.reject(new ErrorApi(MENSAJE_SIN_CONEXION, 0))
    }

    const { status, data } = respuesta
    // 401 fuera del login = token inválido o cuenta deshabilitada.
    if (status === 401 && !ES_LOGIN.test(error.config?.url ?? '')) {
      alExpirarSesion?.()
    }

    const mensaje = data?.mensaje || `Error ${status} al comunicarse con el servidor.`
    return Promise.reject(new ErrorApi(mensaje, status, data?.errores ?? null))
  },
)

/** Atajo: ejecuta la petición y devuelve solo `datos`. */
export async function pedir(config) {
  const { data } = await http.request(config)
  return data?.datos
}
