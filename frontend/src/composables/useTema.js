import { computed, readonly, ref } from 'vue'

/**
 * Tema del panel: 'claro' | 'oscuro'.
 * Sin elección guardada sigue al sistema (prefers-color-scheme); al elegir
 * se guarda en localStorage['ym_tema'] y se aplica en <html> con
 * data-theme (tokens.css) y data-bs-theme (Bootstrap 5.3).
 * index.html repite la lectura inicial para evitar el parpadeo al cargar.
 */
export const CLAVE_TEMA = 'ym_tema'
export const TEMAS = [
  { valor: 'claro', etiqueta: 'Claro', icono: 'bi-sun' },
  { valor: 'oscuro', etiqueta: 'Oscuro', icono: 'bi-moon' },
]

/** null = sin elección: sigue al sistema */
const preferencia = ref(null)
const sistemaOscuro = ref(false)
const consulta = typeof window !== 'undefined' && window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null

const tema = computed(() => preferencia.value ?? (sistemaOscuro.value ? 'oscuro' : 'claro'))

function leer() {
  try {
    const guardado = localStorage.getItem(CLAVE_TEMA)
    return TEMAS.some((t) => t.valor === guardado) ? guardado : null
  } catch {
    return null
  }
}

function aplicar() {
  sistemaOscuro.value = Boolean(consulta?.matches)
  const valor = tema.value === 'oscuro' ? 'dark' : 'light'
  document.documentElement.dataset.theme = valor
  document.documentElement.dataset.bsTheme = valor
}

/** Llamar una vez en main.js antes de montar. */
export function iniciarTema() {
  preferencia.value = leer()
  aplicar()
  // Sin elección guardada, el panel sigue el cambio del sistema sin recargar.
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

  return { tema: readonly(tema), elegir, TEMAS }
}
