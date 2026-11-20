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
import 'material-symbols/outlined.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Sistema de diseño (después de Bootstrap para que sus variables ganen)
import './styles/tokens.css'
import './styles/tema-bootstrap.css'
import './styles/base.css'
import './styles/sweetalert.css'

createApp(App).use(createPinia()).use(router).mount('#app')
