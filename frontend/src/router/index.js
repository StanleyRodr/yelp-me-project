import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppShell from '../components/layout/AppShell.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { titulo: 'Iniciar sesión', publica: true },
  },
  {
    path: '/',
    component: AppShell,
    children: [
      { path: '', name: 'inicio', component: () => import('../views/InicioView.vue'), meta: { titulo: 'Inicio' } },
      { path: 'solicitudes', name: 'solicitudes', component: () => import('../views/SolicitudesView.vue'), meta: { titulo: 'Solicitudes' } },
      { path: 'solicitudes/:id(\\d+)', name: 'solicitud', component: () => import('../views/AtencionView.vue'), props: true, meta: { titulo: 'Atención de solicitud' } },
      { path: 'usuarios', name: 'usuarios', component: () => import('../views/UsuariosView.vue'), meta: { titulo: 'Usuarios' } },
      { path: 'usuarios/:id(\\d+)', name: 'usuario', component: () => import('../views/UsuarioDetalleView.vue'), props: true, meta: { titulo: 'Perfil de usuario' } },
      { path: 'perfil', name: 'perfil', component: () => import('../views/PerfilView.vue'), meta: { titulo: 'Mi perfil' } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: { name: 'inicio' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.publica && !auth.autenticado) {
    return { name: 'login', query: to.fullPath !== '/' ? { redirigir: to.fullPath } : {} }
  }
  if (to.name === 'login' && auth.autenticado) return { name: 'inicio' }
  return true
})

/*
 * Si el módulo de una vista no se puede descargar (dependencias de Vite
 * re-optimizadas o un despliegue nuevo con archivos renombrados), se recarga
 * la página una sola vez hacia la ruta destino en lugar de quedar en blanco.
 */
const CLAVE_RECARGA = 'ym_recarga_modulo'
router.onError((error, to) => {
  const esModulo = /dynamically imported module|Importing a module script failed|Outdated Optimize Dep/i.test(error?.message ?? '')
  if (!esModulo) throw error
  try {
    if (sessionStorage.getItem(CLAVE_RECARGA) === to.fullPath) return
    sessionStorage.setItem(CLAVE_RECARGA, to.fullPath)
  } catch {
    // Sin sessionStorage: se intenta una vez sin poder recordar el intento.
  }
  window.location.assign(to.fullPath)
})

router.afterEach((to) => {
  try {
    sessionStorage.removeItem(CLAVE_RECARGA)
  } catch {
    // Almacenamiento bloqueado: nada que limpiar.
  }
  document.title = to.meta.titulo ? `${to.meta.titulo} | Yelp Me` : 'Yelp Me'
})

export default router
