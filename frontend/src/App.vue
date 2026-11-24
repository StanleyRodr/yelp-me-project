<template>
  <div class="min-vh-100 d-flex flex-column">
    <AppNavbar :current-view="currentView" @cambiar-vista="navegarA" />

    <main class="container-fluid px-lg-5 py-4 flex-grow-1">
      <InicioView 
        v-if="currentView === 'inicio'" 
        @ir-a-atencion="abrirAtencionCon" 
      />

      <SolicitudesView 
        v-else-if="currentView === 'solicitudes'" 
        @abrir-atencion="abrirAtencionCon" 
      />

      <AtencionView 
        v-else-if="currentView === 'atencion'" 
        :solicitud="solicitudEnAtencion"
        @volver="volverAtras"
        @abrir-modal="abrirModal"
      />

      <UsuariosView 
        v-else-if="currentView === 'usuarios'" 
      />

      <!-- VISTA PERFIL CONECTADA -->
      <PerfilView 
        v-else-if="currentView === 'perfil'" 
      />
    </main>

    <!-- Modales de emergencias -->
    <div v-if="modalVisible" class="modal-backdrop-custom d-flex align-items-center justify-content-center px-3" @click.self="cerrarModal">
      <!-- Modal Edición -->
      <div v-if="modalTipo === 'editar'" class="card modal-edit-card shadow-lg w-100">
        <div class="modal-edit-header-yellow">Editar solicitud</div>
        <div class="card-body p-4 bg-white">
          <div class="mb-4 text-start">
            <label class="form-label small fw-bold text-secondary mb-1">Título de la Emergencia</label>
            <input type="text" class="form-control form-control-lg fs-6" v-model="formEdicion.titulo">
          </div>
          <div class="mb-4">
            <div class="text-secondary small fw-bold text-start mb-2">Seleccionar ayudas requeridas:</div>
            <div class="d-flex flex-wrap justify-content-center gap-3">
              <div 
                v-for="s in catalogoServicios" 
                :key="s.key"
                class="service-select-btn"
                :class="[s.btnClass, formEdicion.ayudas.includes(s.key) ? 'is-selected' : 'is-unselected']"
                @click="toggleAyuda(s.key)"
              >
                <i :class="s.icono"></i>
              </div>
            </div>
          </div>
          <div class="d-flex justify-content-center gap-3 pt-2">
            <button class="btn btn-success px-4" @click="guardarCambios">Realizar cambios</button>
            <button class="btn btn-danger px-4" @click="cerrarModal">Salir</button>
          </div>
        </div>
      </div>

      <!-- Modal Rechazo -->
      <div v-else-if="modalTipo === 'rechazar'" class="card modal-reject-card shadow-lg w-100">
        <div class="modal-reject-header-black">Rechazar y reportar</div>
        <div class="card-body p-4 bg-white text-center">
          <p class="text-dark fw-normal mb-3 modal-confirm-title">¿Estas seguro de rechazar la solicitud?</p>
          <div class="d-flex justify-content-center gap-4 mb-3">
            <button type="button" class="btn-choice-yes shadow-sm" :class="{ 'is-active': confirmacionRechazar }" @click="confirmacionRechazar = true">Sí</button>
            <button type="button" class="btn-choice-no shadow-sm" @click="cerrarModal">No</button>
          </div>
          <p v-if="!confirmacionRechazar" class="text-muted small mb-3 fst-italic">Presiona "Sí" para habilitar la redacción del reporte.</p>
          <p v-else class="text-success small mb-3 fw-semibold">Rechazo autorizado. Procede a redactar el reporte.</p>
          <div class="text-start mb-2">
            <label class="form-label text-dark fw-normal mb-0 label-reporte-rechazo">Reporte de rechazo</label>
          </div>
          <div class="mb-4">
            <textarea class="textarea-report-reject" v-model="formRechazo.motivo" :disabled="!confirmacionRechazar" placeholder="Escribe el motivo del reporte de rechazo..."></textarea>
          </div>
          <div class="d-flex justify-content-between gap-3">
            <button type="button" class="btn-submit-reject" :disabled="!confirmacionRechazar" @click="confirmarRechazo">Enviar reporte</button>
            <button type="button" class="btn-exit-reject" @click="cerrarModal">Salir</button>
          </div>
        </div>
      </div>

      <!-- Modal Despacho -->
      <div v-else-if="modalTipo === 'ayuda'" class="card border-0 shadow-lg rounded-4 modal-dialog-box w-100 p-4 text-center">
        <i class="bi bi-send-check text-warning fs-1 mb-2"></i>
        <h4 class="fw-bold text-dark">Confirmar despacho</h4>
        <p class="text-muted small">¿Deseas despachar auxilio a {{ solicitudEnAtencion.ubicacionNombre }}?</p>
        <div class="d-flex justify-content-center gap-2 mt-3">
          <button class="btn btn-light px-4" @click="cerrarModal">No</button>
          <button class="btn btn-success px-4" @click="confirmarEnvio">Sí, enviar ayuda</button>
        </div>
      </div>

      <!-- Modal Procesada -->
      <div v-else-if="modalTipo === 'procesada'" class="modal-processed-box" @click="cerrarModal">
        <div class="fs-2 fw-bold">Solicitud procesada</div>
        <div class="fs-4">Ayuda en camino</div>
      </div>

      <!-- Modal Cancelación -->
      <div v-else-if="modalTipo === 'confirmar-cancelar-ayuda'" class="card border-0 shadow-lg rounded-4 modal-dialog-box w-100 p-4 text-center">
        <i class="bi bi-x-circle text-secondary fs-1 mb-2"></i>
        <h4 class="fw-bold text-dark">Cancelar envío de ayuda</h4>
        <p class="text-muted small">La solicitud cambiará su estado a <strong>Cancelada</strong>.</p>
        <div class="d-flex justify-content-center gap-2 mt-3">
          <button class="btn btn-light px-4" @click="cerrarModal">No</button>
          <button class="btn btn-secondary px-4" @click="ejecutarCancelacion">Sí, cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppNavbar from './components/layout/AppNavbar.vue'
import InicioView from './views/InicioView.vue'
import SolicitudesView from './views/SolicitudesView.vue'
import AtencionView from './views/AtencionView.vue'
import UsuariosView from './views/UsuariosView.vue'
import PerfilView from './views/PerfilView.vue'
import { useAlertas, catalogoServicios } from './composables/useAlertas'

const currentView = ref('inicio')
const vistaPrevia = ref('inicio')
const { solicitudEnAtencion } = useAlertas()

const modalVisible = ref(false)
const modalTipo = ref('')
const confirmacionRechazar = ref(false)
const formEdicion = ref({ titulo: '', ayudas: [] })
const formRechazo = ref({ motivo: '' })

const navegarA = (vista) => {
  vistaPrevia.value = currentView.value
  currentView.value = vista
}

const abrirAtencionCon = (solicitud) => {
  solicitudEnAtencion.value = solicitud
  vistaPrevia.value = currentView.value
  currentView.value = 'atencion'
}

const volverAtras = () => {
  currentView.value = vistaPrevia.value || 'inicio'
}

const abrirModal = (tipo) => {
  modalTipo.value = tipo
  if (tipo === 'editar') {
    formEdicion.value = { titulo: solicitudEnAtencion.value.titulo, ayudas: [...(solicitudEnAtencion.value.ayudas || [])] }
  } else if (tipo === 'rechazar') {
    formRechazo.value = { motivo: '' }
    confirmacionRechazar.value = false
  }
  modalVisible.value = true
}

const cerrarModal = () => {
  modalVisible.value = false
  modalTipo.value = ''
  confirmacionRechazar.value = false
}

const toggleAyuda = (key) => {
  const idx = formEdicion.value.ayudas.indexOf(key)
  if (idx > -1) formEdicion.value.ayudas.splice(idx, 1)
  else formEdicion.value.ayudas.push(key)
}

const guardarCambios = () => {
  solicitudEnAtencion.value.titulo = formEdicion.value.titulo
  solicitudEnAtencion.value.ayudas = [...formEdicion.value.ayudas]
  cerrarModal()
}

const confirmarRechazo = () => {
  solicitudEnAtencion.value.estado = 'Rechazada'
  if (formRechazo.value.motivo) {
    solicitudEnAtencion.value.comentario += ` [REPORTE DE RECHAZO: ${formRechazo.value.motivo}]`
  }
  cerrarModal()
}

const confirmarEnvio = () => {
  solicitudEnAtencion.value.estado = 'En Camino'
  modalTipo.value = 'procesada'
}

const ejecutarCancelacion = () => {
  solicitudEnAtencion.value.estado = 'Cancelada'
  cerrarModal()
}
</script>

<style>
.modal-backdrop-custom { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.55); z-index: 1050; }
.modal-edit-card { max-width: 520px; border-radius: 8px; overflow: hidden; border: none; }
.modal-edit-header-yellow { background: #fbb03b; color: #fff; padding: 16px; text-align: center; font-weight: 700; font-size: 1.25rem; }

.modal-reject-card { max-width: 480px; border-radius: 6px; overflow: hidden; border: none; }
.modal-reject-header-black { background-color: #1e293b; color: #ffffff; padding: 14px 20px; text-align: center; font-weight: 700; font-size: 1.25rem; letter-spacing: -0.2px; }
.modal-confirm-title { font-size: 1.05rem; }

.btn-choice-yes { background-color: #68a341; color: #ffffff; font-weight: 600; border: none; border-radius: 3px; padding: 8px 34px; font-size: 0.95rem; transition: all 0.2s ease; }
.btn-choice-yes:hover { background-color: #558734; }
.btn-choice-yes.is-active { box-shadow: 0 0 0 3px rgba(104, 163, 65, 0.4); transform: scale(1.03); }

.btn-choice-no { background-color: #d0021b; color: #ffffff; font-weight: 600; border: none; border-radius: 3px; padding: 8px 34px; font-size: 0.95rem; transition: all 0.2s ease; }
.btn-choice-no:hover { background-color: #b00115; }

.label-reporte-rechazo { font-size: 0.98rem; color: #1e293b; }
.textarea-report-reject { background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 4px; padding: 12px; width: 100%; min-height: 120px; outline: none; color: #334155; font-size: 0.95rem; resize: vertical; transition: all 0.2s ease; }
.textarea-report-reject:focus:not(:disabled) { border-style: solid; border-color: #1e293b; background-color: #ffffff; }
.textarea-report-reject:disabled { background-color: #f8fafc; color: #94a3b8; cursor: not-allowed; }

.btn-submit-reject { background-color: #68a341; color: #ffffff; font-weight: 600; border: none; border-radius: 3px; padding: 9px 24px; font-size: 0.95rem; min-width: 150px; transition: all 0.2s ease; }
.btn-submit-reject:hover:not(:disabled) { background-color: #558734; }
.btn-submit-reject:disabled { background-color: #b4bcc8; color: #ffffff; opacity: 1; cursor: not-allowed; }

.btn-exit-reject { background-color: #d0021b; color: #ffffff; font-weight: 600; border: none; border-radius: 3px; padding: 9px 28px; font-size: 0.95rem; min-width: 130px; transition: all 0.2s ease; }
.btn-exit-reject:hover { background-color: #b00115; }

.modal-dialog-box { max-width: 480px; background: #fff; }
.modal-processed-box { background: #6ea344; color: #fff; border-radius: 4px; padding: 40px 50px; text-align: center; cursor: pointer; }
.service-select-btn { width: 90px; height: 75px; border-radius: 18px; display: flex; align-items: center; justify-content: center; font-size: 2rem; cursor: pointer; }
.service-select-btn.is-unselected { opacity: 0.35; filter: grayscale(40%); }
.service-select-btn.is-selected { opacity: 1; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.btn-icon-police { background-color: #243c5a; color: #fbbf24; }
.btn-icon-redcross { background-color: #ff0000; color: #ffffff; }
.btn-icon-traffic { background-color: #f59e0b; color: #ffffff; }
.btn-icon-pcivil { background-color: #ea580c; color: #ffffff; }
.btn-icon-fire { background-color: #94a3b8; color: #dc2626; }
.btn-icon-citizen { background-color: #10b981; color: #ffffff; }
</style>