<template>
  <div class="shell">
    <a class="saltar visually-hidden-focusable" href="#contenido">Saltar al contenido</a>
    <AppNavbar
      :enlaces="enlaces"
      :admin="auth.admin"
      @cerrar-sesion="cerrarSesion"
    />
    <main id="contenido" class="shell-contenido" tabindex="-1">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppNavbar from './AppNavbar.vue'
import { useAuthStore } from '../../stores/auth'
import { useSolicitudesStore } from '../../stores/solicitudes'
import { exito } from '../../plugins/alertas'
import { useConsultaPeriodica } from '../../composables/useConsultaPeriodica'

const auth = useAuthStore()
const solicitudes = useSolicitudesStore()
const router = useRouter()

const enlaces = computed(() => [
  { nombre: 'inicio', etiqueta: 'Inicio', icono: 'bi-grid-1x2' },
  { nombre: 'solicitudes', etiqueta: 'Solicitudes', icono: 'bi-list-ul', contador: solicitudes.conteoPorEstado.Pendiente || 0 },
  { nombre: 'usuarios', etiqueta: 'Usuarios', icono: 'bi-people' },
])

onMounted(() => {
  if (!solicitudes.lista.length) solicitudes.cargar()
})

// Plan principal 9: el panel no recibe push; consulta cada 15 s. Una sola
// consulta aquí mantiene al día navbar, dashboard y listado (mismo store).
useConsultaPeriodica(() => solicitudes.refrescar(), 15000)

async function cerrarSesion() {
  await auth.cerrarSesion()
  await router.replace({ name: 'login' })
  exito('Sesión cerrada')
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.shell-contenido {
  flex: 1;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--ym-espacio-5) var(--ym-espacio-5) var(--ym-espacio-6);
  outline: none;
}

.saltar {
  position: absolute;
  z-index: 1100;
  top: var(--ym-espacio-2);
  left: var(--ym-espacio-2);
  padding: var(--ym-espacio-2) var(--ym-espacio-3);
  background: var(--ym-superficie);
  color: var(--ym-marca-texto);
  border-radius: var(--ym-radio);
}

@media (max-width: 575.98px) {
  .shell-contenido {
    padding: var(--ym-espacio-4) var(--ym-espacio-3) var(--ym-espacio-5);
  }
}
</style>
