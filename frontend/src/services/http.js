import axios from 'axios'

export const CLAVE_SESION = 'ym_sesion'

/**
 * Cliente HTTP para la API Laravel. Aún no se usa: los services leen mocks.
 * Al conectar la API, cada service cambia su implementación por llamadas a `http`.
 */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8000/api',
  headers: { Accept: 'application/json' },
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
