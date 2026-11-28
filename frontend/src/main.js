import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Dependencias de terceros
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'leaflet/dist/leaflet.css'
import 'sweetalert2/dist/sweetalert2.min.css'
import '@fontsource-variable/atkinson-hyperlegible-next'
import './styles/iconos.css'
import './plugins/bootstrap'

// Sistema de diseño (después de Bootstrap para que sus variables ganen)
import './styles/tokens.css'
import './styles/tema-bootstrap.css'
import './styles/base.css'
import './styles/sweetalert.css'

import { iniciarTema } from './composables/useTema'
import { registrarSesionExpirada } from './services/http'
import { useAuthStore } from './stores/auth'

iniciarTema()

const app = createApp(App).use(createPinia()).use(router)
const auth = useAuthStore()

// Cualquier 401 de la API (token borrado, cuenta deshabilitada): fuera del panel.
registrarSesionExpirada(() => {
  if (!auth.autenticado) return
  auth.limpiarSesion()
  router.replace({ name: 'login', query: { expirada: '1' } })
})

app.mount('#app')

// Con un token guardado, confirmar que sigue válido (no bloquea la carga).
auth.validarSesion()
