<template>
  <div class="usuarios-module-container py-3">
    
    <!-- ==========================================
         VISTA 1: LISTADO DE USUARIOS
         ========================================== -->
    <div v-if="!usuarioSeleccionado" class="usuarios-content-wrapper mx-auto">
      <div class="text-center mb-4">
        <h1 class="module-usuarios-title mb-1">Usuarios</h1>
      </div>

      <!-- Buscador -->
      <div class="search-box-container d-flex align-items-center mb-4">
        <input 
          type="text" 
          class="search-box-input"
          v-model="busqueda"
          placeholder="Buscar usuario por nombre, teléfono o colonia..."
        >
        <i class="bi bi-search text-secondary ms-2 fs-5"></i>
      </div>

      <!-- Tabla / Lista -->
      <div class="card border shadow-sm rounded-0 overflow-hidden bg-white">
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <tbody>
              <tr 
                v-for="user in usuariosFiltrados" 
                :key="user.id"
                class="user-row border-bottom"
                :class="{ 'user-row-banned': user.strikes >= 3 }"
              >
                <!-- Avatar Circular -->
                <td class="ps-4 py-3" style="width: 80px;">
                  <div class="position-relative d-inline-block">
                    <img 
                      :src="user.avatar" 
                      :alt="user.nombre"
                      class="user-avatar-circle"
                      :class="{ 'avatar-grayscale': user.strikes >= 3 }"
                    >
                    <span 
                      v-if="user.strikes >= 3" 
                      class="position-absolute bottom-0 start-50 translate-middle-x badge bg-danger text-white rounded-pill px-2"
                      style="font-size: 0.65rem;"
                    >
                      Baneado
                    </span>
                  </div>
                </td>

                <!-- Datos de Usuario -->
                <td class="py-3">
                  <div class="d-flex align-items-center gap-2">
                    <span class="user-name-text">{{ user.nombre }}</span>
                    <!-- Indicador visual de reportes -->
                    <span 
                      v-if="user.strikes > 0" 
                      class="badge rounded-pill fw-bold"
                      :class="user.strikes >= 3 ? 'bg-danger text-white' : 'bg-warning-subtle text-dark border border-warning'"
                      style="font-size: 0.72rem;"
                    >
                      <i class="bi bi-exclamation-triangle-fill me-1"></i>
                      {{ user.strikes }}/3 {{ user.strikes >= 3 ? 'BANEADO' : 'REPORTES' }}
                    </span>
                  </div>
                  <div class="text-muted small">
                    <span class="me-3"><i class="bi bi-telephone me-1"></i>{{ user.telefono }}</span>
                    <span><i class="bi bi-geo-alt me-1"></i>{{ user.direccion }}</span>
                  </div>
                </td>

                <!-- 3 Botones Cuadrados -->
                <td class="text-end pe-4 py-3" style="width: 220px;">
                  <div class="d-inline-flex gap-2">
                    <!-- 1. Ver Perfil e Historial -->
                    <button 
                      class="btn-user-action btn-user-view" 
                      @click="verDetalleUsuario(user)"
                      title="Ver perfil e historial"
                    >
                      <i class="bi bi-eye-fill"></i>
                    </button>

                    <!-- 2. Botón Amarillo: Abrir modal de Reportar perfil -->
                    <button 
                      class="btn-user-action btn-user-warn" 
                      :disabled="user.strikes >= 3"
                      @click="abrirModalReportar(user)"
                      title="Reportar perfil de usuario"
                    >
                      <i class="bi bi-exclamation-triangle-fill"></i>
                    </button>

                    <!-- 3. Bloquear / Desbloquear directo -->
                    <button 
                      class="btn-user-action btn-user-block" 
                      @click="toggleBloqueoDirecto(user)"
                      :title="user.strikes >= 3 ? 'Quitar suspensión' : 'Suspender directamente'"
                    >
                      <i :class="user.strikes >= 3 ? 'bi bi-check-circle-fill' : 'bi bi-slash-circle-fill'"></i>
                    </button>
                  </div>
                </td>
              </tr>

              <tr v-if="usuariosFiltrados.length === 0">
                <td colspan="3" class="text-center py-5 text-muted">
                  No se encontraron usuarios registrados con ese criterio.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- ==========================================
         VISTA 2: PERFIL E HISTORIAL (LIMPIO)
         ========================================== -->
    <div v-else class="user-detail-view-container mx-auto">
      
      <!-- Botón Volver -->
      <div class="mb-4 text-start">
        <button class="btn btn-sm btn-outline-secondary px-3" @click="usuarioSeleccionado = null">
          <i class="bi bi-arrow-left me-1"></i> Volver a Usuarios
        </button>
      </div>

      <div class="row g-5 align-items-start">
        
        <!-- Columna Izquierda: Perfil -->
        <div class="col-12 col-lg-5 text-center">
          <h2 class="section-title-gold mb-3">Perfil</h2>

          <div class="profile-photo-wrapper mb-3 mx-auto shadow-sm position-relative">
            <img 
              :src="usuarioSeleccionado.avatar" 
              :alt="usuarioSeleccionado.nombre"
              class="profile-big-avatar"
              :class="{ 'avatar-grayscale': usuarioSeleccionado.strikes >= 3 }"
            >
            <div 
              v-if="usuarioSeleccionado.strikes >= 3" 
              class="banned-overlay d-flex flex-column align-items-center justify-content-center"
            >
              <i class="bi bi-slash-circle fs-1 mb-1"></i>
              <span class="fw-bold tracking-wider">CUENTA SUSPENDIDA</span>
            </div>
          </div>

          <h3 class="profile-user-name mb-1">{{ usuarioSeleccionado.nombre }}</h3>
          <div class="profile-user-id mb-3">#{{ usuarioSeleccionado.folioUsuario }}</div>

          <!-- Pequeña etiqueta informativa del estado de reportes (sin botones) -->
          <div class="mb-4 d-inline-block">
            <span 
              class="badge rounded-pill px-3 py-2 fw-semibold"
              :class="usuarioSeleccionado.strikes >= 3 ? 'bg-danger text-white' : (usuarioSeleccionado.strikes > 0 ? 'bg-warning-subtle text-dark border border-warning' : 'bg-light text-muted border')"
            >
              <i class="bi bi-shield-exclamation me-1"></i>
              Reportes acumulados: <strong>{{ usuarioSeleccionado.strikes }}/3</strong>
              <span v-if="usuarioSeleccionado.strikes >= 3"> (Suspendido)</span>
            </span>
          </div>

          <!-- Información de contacto -->
          <div class="profile-contact-info text-start mx-auto">
            <div class="contact-item d-flex align-items-center mb-2">
              <i class="bi bi-telephone-fill me-3 contact-icon"></i>
              <span>{{ usuarioSeleccionado.telefono }}</span>
            </div>
            <div class="contact-item d-flex align-items-center mb-2">
              <i class="bi bi-envelope-fill me-3 contact-icon"></i>
              <span>{{ usuarioSeleccionado.correo }}</span>
            </div>
            <div class="contact-item d-flex align-items-start">
              <i class="bi bi-geo-alt-fill me-3 contact-icon mt-1"></i>
              <span>{{ usuarioSeleccionado.direccion }}</span>
            </div>
          </div>
        </div>

        <!-- Columna Derecha: Historial de Solicitudes -->
        <div class="col-12 col-lg-7">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="section-title-gold mb-0">Historial de solicitudes</h2>
            <span class="text-muted small">
              {{ usuarioSeleccionado.historial?.length || 0 }} solicitudes
            </span>
          </div>

          <div class="history-table-container shadow-sm">
            <table class="table table-history mb-0">
              <tbody>
                <tr 
                  v-for="(sol, idx) in usuarioSeleccionado.historial" 
                  :key="idx"
                  class="history-row"
                >
                  <td class="history-folio-cell ps-3">#{{ sol.folio }}</td>
                  <td class="history-desc-cell pe-3">{{ sol.descripcion }}</td>
                </tr>

                <tr v-if="!usuarioSeleccionado.historial || usuarioSeleccionado.historial.length === 0">
                  <td colspan="2" class="text-center py-5 text-muted fst-italic">
                    Este usuario no cuenta con solicitudes registradas.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </div>

    </div>

    <!-- ==========================================
         MODAL: REPORTAR PERFIL (DISEÑO EXACTO)
         ========================================== -->
    <div 
      v-if="modalReportarVisible" 
      class="modal-backdrop-custom d-flex align-items-center justify-content-center px-3"
      @click.self="cerrarModalReportar"
    >
      <div class="card modal-reportar-perfil-card shadow-lg w-100">
        
        <!-- Encabezado Amarillo/Mostaza -->
        <div class="modal-reportar-header">
          Reportar perfil
        </div>

        <div class="card-body p-4 bg-white text-center">
          
          <!-- Texto de Confirmación -->
          <p class="modal-reportar-question mb-3">
            ¿Estas seguro que desea reportar el perfil? al acumularse 3 reportes puede quedar permanentemente suspendido
          </p>

          <!-- Botones Sí / No -->
          <div class="d-flex justify-content-center gap-4 mb-3">
            <button 
              type="button"
              class="btn-modal-yes"
              :class="{ 'is-active': confirmadoReporte }"
              @click="confirmadoReporte = true"
            >
              Sí
            </button>
            <button 
              type="button"
              class="btn-modal-no"
              @click="cerrarModalReportar"
            >
              No
            </button>
          </div>

          <!-- Razón del reporte -->
          <div class="text-start mb-2">
            <label class="modal-reportar-label mb-0">Razón del reporte</label>
          </div>

          <!-- Cuadro de texto gris -->
          <div class="mb-4">
            <textarea 
              class="textarea-razon-reporte"
              v-model="motivoReporte"
              :disabled="!confirmadoReporte"
              placeholder="Escribe la razón del reporte..."
            ></textarea>
          </div>

          <!-- Botones inferiores Enviar reporte y Salir -->
          <div class="d-flex justify-content-between gap-3">
            <button 
              type="button"
              class="btn-modal-submit"
              :disabled="!confirmadoReporte"
              @click="ejecutarReportePerfil"
            >
              Enviar reporte
            </button>

            <button 
              type="button"
              class="btn-modal-exit"
              @click="cerrarModalReportar"
            >
              Salir
            </button>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const busqueda = ref('')
const usuarioSeleccionado = ref(null)

// Estados para el Modal de Reportar Perfil
const modalReportarVisible = ref(false)
const usuarioParaReportar = ref(null)
const confirmadoReporte = ref(false)
const motivoReporte = ref('')

const listaUsuarios = ref([
  {
    id: 1,
    nombre: 'Kimberly Heredia',
    folioUsuario: '00000325',
    telefono: '+52 235 114 3616',
    correo: 'kimberher2205@gmail.com',
    direccion: 'Miguel Hidalgo, Carlos R. Smith, sn, 93827, Misantla, Ver.',
    avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80',
    strikes: 1,
    historial: [
      { folio: '00001', descripcion: 'Solicitud a protección civil por conato de incendio en pastizal' },
      { folio: '00014', descripcion: 'Reporte de luminaria y cableado expuesto en vía pública' },
      { folio: '00021', descripcion: 'Solicitud de auxilio vial por bache en crucero Smith' }
    ]
  },
  {
    id: 2,
    nombre: 'Carlos Rodríguez Pérez',
    folioUsuario: '00000118',
    telefono: '+52 235 104 8892',
    correo: 'carlos.rodriguez@gmail.com',
    direccion: 'Col. Buenos Aires / Periferia, Misantla, Ver.',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80',
    strikes: 0,
    historial: [
      { folio: '00002', descripcion: 'Solicitud a bomberos por rescate de mascota en árbol' }
    ]
  },
  {
    id: 3,
    nombre: 'Bryan Stanley Rodríguez',
    folioUsuario: '00000089',
    telefono: '+52 235 105 7711',
    correo: 'bryan.stanley@gmail.com',
    direccion: 'Av. Ignacio Zaragoza esq. Doblado, Misantla, Ver.',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80',
    strikes: 2,
    historial: [
      { folio: '00003', descripcion: 'Solicitud a tránsito por alcance vehicular en semáforo' },
      { folio: '00009', descripcion: 'Reporte de semáforo averiado en esquina céntrica' }
    ]
  },
  {
    id: 4,
    nombre: 'Nubia Guadalupe Juárez',
    folioUsuario: '00000204',
    telefono: '+52 235 112 4590',
    correo: 'nubia.juarez@gmail.com',
    direccion: 'Calle 5 de Mayo, Centro, Misantla, Ver.',
    avatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400&auto=format&fit=crop&q=80',
    strikes: 3,
    historial: [
      { folio: '00004', descripcion: 'Solicitud a bomberos por fuga de gas en establecimiento' }
    ]
  }
])

const usuariosFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return listaUsuarios.value
  return listaUsuarios.value.filter(u => 
    u.nombre.toLowerCase().includes(q) ||
    u.telefono.toLowerCase().includes(q) ||
    u.direccion.toLowerCase().includes(q)
  )
})

const verDetalleUsuario = (user) => {
  usuarioSeleccionado.value = user
}

// Abrir el modal de reportar con el botón amarillo
const abrirModalReportar = (user) => {
  usuarioParaReportar.value = user
  confirmadoReporte.value = false
  motivoReporte.value = ''
  modalReportarVisible.value = true
}

const cerrarModalReportar = () => {
  modalReportarVisible.value = false
  usuarioParaReportar.value = null
  confirmadoReporte.value = false
  motivoReporte.value = ''
}

// Sumar strike y suspender si llega a 3
const ejecutarReportePerfil = () => {
  if (usuarioParaReportar.value && usuarioParaReportar.value.strikes < 3) {
    usuarioParaReportar.value.strikes++
  }
  cerrarModalReportar()
}

const toggleBloqueoDirecto = (user) => {
  if (user.strikes >= 3) {
    user.strikes = 0
  } else {
    user.strikes = 3
  }
}
</script>

<style scoped>
.usuarios-content-wrapper {
  max-width: 920px;
  width: 100%;
}

.user-detail-view-container {
  max-width: 1060px;
  width: 100%;
}

.module-usuarios-title,
.section-title-gold {
  color: #df9e26;
  font-weight: 900;
  font-size: 2.2rem;
  letter-spacing: -0.5px;
}

.search-box-container {
  background-color: #e9ecef;
  border-radius: 4px;
  padding: 8px 16px;
  border: 1px solid #dee2e6;
}

.search-box-input {
  background: transparent;
  border: none;
  outline: none;
  width: 100%;
  font-size: 1rem;
  color: #212529;
}

.user-avatar-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e9ecef;
}

.avatar-grayscale {
  filter: grayscale(100%);
  opacity: 0.7;
}

.user-name-text {
  font-size: 1.15rem;
  font-weight: 500;
  color: #212529;
}

.user-row {
  transition: background-color 0.15s ease;
}
.user-row:hover {
  background-color: #f8fafc;
}
.user-row-banned {
  background-color: #fef2f2;
}

.btn-user-action {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.35rem;
  color: #ffffff;
  transition: transform 0.15s ease, opacity 0.15s ease;
  cursor: pointer;
}
.btn-user-action:hover:not(:disabled) {
  transform: translateY(-2px);
  opacity: 0.92;
}
.btn-user-action:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-user-view  { background-color: #5ba344; }
.btn-user-warn  { background-color: #f3a824; }
.btn-user-block { background-color: #d60000; }

/* Perfil */
.profile-photo-wrapper {
  width: 210px;
  height: 210px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f1f5f9;
}

.profile-big-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banned-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(220, 38, 38, 0.75);
  color: #ffffff;
}

.profile-user-name {
  color: #17253a;
  font-weight: 700;
  font-size: 1.6rem;
}

.profile-user-id {
  color: #334155;
  font-weight: 600;
  font-size: 1.05rem;
  letter-spacing: 0.5px;
}

.profile-contact-info {
  max-width: 330px;
  font-size: 0.95rem;
  color: #334155;
}

.contact-icon {
  color: #94a3b8;
  font-size: 1.15rem;
  flex-shrink: 0;
}

/* Tabla beige */
.history-table-container {
  border: 1.5px solid #eab355;
  background-color: #fff4d9;
}

.table-history {
  width: 100%;
  margin-bottom: 0;
}

.history-row {
  border-bottom: 1px solid #eab355;
}
.history-row:last-child {
  border-bottom: none;
}

.history-folio-cell {
  font-weight: 800;
  color: #000000;
  font-size: 1.05rem;
  width: 110px;
  padding: 16px 12px;
  vertical-align: middle;
}

.history-desc-cell {
  font-weight: 400;
  color: #17253a;
  font-size: 1.05rem;
  padding: 16px 12px;
  vertical-align: middle;
}

/* ===================================================
   ESTILO EXACTO: MODAL REPORTAR PERFIL (CAPTURA)
   =================================================== */
.modal-backdrop-custom {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.55);
  z-index: 1050;
}

.modal-reportar-perfil-card {
  max-width: 480px;
  border-radius: 4px;
  overflow: hidden;
  border: none;
}

.modal-reportar-header {
  background-color: #fab446; /* Amarillo cálido de la barra del modal */
  color: #ffffff;
  padding: 14px 20px;
  text-align: center;
  font-weight: 800;
  font-size: 1.35rem;
  letter-spacing: -0.3px;
}

.modal-reportar-question {
  color: #1f2937;
  font-size: 0.98rem;
  line-height: 1.35;
  font-weight: 400;
}

.btn-modal-yes {
  background-color: #68a341;
  color: #ffffff;
  font-weight: 600;
  border: none;
  border-radius: 2px;
  padding: 7px 32px;
  font-size: 0.95rem;
  transition: all 0.15s ease;
}
.btn-modal-yes:hover { background-color: #558734; }
.btn-modal-yes.is-active {
  box-shadow: 0 0 0 3px rgba(104, 163, 65, 0.4);
}

.btn-modal-no {
  background-color: #d0021b;
  color: #ffffff;
  font-weight: 600;
  border: none;
  border-radius: 2px;
  padding: 7px 32px;
  font-size: 0.95rem;
  transition: all 0.15s ease;
}
.btn-modal-no:hover { background-color: #b00115; }

.modal-reportar-label {
  font-size: 0.98rem;
  color: #1f2937;
  font-weight: 400;
}

.textarea-razon-reporte {
  background-color: #e5e7eb; /* Fondo gris plano del mockup */
  border: none;
  border-radius: 2px;
  padding: 12px;
  width: 100%;
  min-height: 110px;
  outline: none;
  color: #1f2937;
  font-size: 0.95rem;
  resize: vertical;
}

.textarea-razon-reporte:disabled {
  background-color: #e5e7eb;
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-modal-submit {
  background-color: #68a341;
  color: #ffffff;
  font-weight: 600;
  border: none;
  border-radius: 2px;
  padding: 9px 24px;
  font-size: 0.95rem;
  min-width: 140px;
  transition: all 0.15s ease;
}
.btn-modal-submit:hover:not(:disabled) { background-color: #558734; }
.btn-modal-submit:disabled {
  background-color: #9cb885;
  cursor: not-allowed;
  opacity: 0.8;
}

.btn-modal-exit {
  background-color: #d0021b;
  color: #ffffff;
  font-weight: 600;
  border: none;
  border-radius: 2px;
  padding: 9px 28px;
  font-size: 0.95rem;
  min-width: 120px;
  transition: all 0.15s ease;
}
.btn-modal-exit:hover { background-color: #b00115; }
</style>