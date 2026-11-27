import { readonly, ref } from 'vue'

/**
 * Tema del panel: 'claro' | 'oscuro' | 'sistema' (por defecto).
 * Se guarda en localStorage['ym_tema'] y se aplica en <html> con
 * data-theme (tokens.css) y data-bs-theme (Bootstrap 5.3).
 * index.html repite la lectura inicial para evitar el parpadeo al cargar.
 */
export const CLAVE_TEMA = 'ym_tema'
export const TEMAS = [
  { valor: 'claro', etiqueta: 'Claro', icono: 'bi-sun' },
  { valor: 'oscuro', etiqueta: 'Oscuro', icono: 'bi-moon' },
  { valor: 'sistema', etiqueta: 'Sistema', icono: 'bi-circle-half' },
]

const preferencia = ref('sistema')
const consulta = typeof window !== 'undefined' && window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null

function leer() {
  try {
    const guardado = localStorage.getItem(CLAVE_TEMA)
    return TEMAS.some((t) => t.valor === guardado) ? guardado : 'sistema'
  } catch {
    return 'sistema'
  }
}

function aplicar() {
  const oscuro = preferencia.value === 'oscuro' || (preferencia.value === 'sistema' && Boolean(consulta?.matches))
  const tema = oscuro ? 'dark' : 'light'
  document.documentElement.dataset.theme = tema
  document.documentElement.dataset.bsTheme = tema
}

/** Llamar una vez en main.js antes de montar. */
export function iniciarTema() {
  preferencia.value = leer()
  aplicar()
  // Con "Sistema", el panel sigue el cambio del sistema sin recargar.
  consulta?.addEventListener('change', aplicar)
}

export function useTema() {
  function elegir(valor) {
    preferencia.value = valor
    try {
      localStorage.setItem(CLAVE_TEMA, valor)
    } catch {
      // Almacenamiento bloqueado: el tema dura hasta recargar.
    }
    aplicar()
  }

  return { preferencia: readonly(preferencia), elegir, TEMAS }
}
