import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

// Dependencias globales
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'leaflet/dist/leaflet.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

createApp(App).use(createPinia()).mount('#app')