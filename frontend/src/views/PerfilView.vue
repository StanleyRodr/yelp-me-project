<template>
  <div class="admin-profile-container py-3">
    <div class="admin-content-wrapper mx-auto">
      
      <div class="row g-5 align-items-start">
        
        <!-- ==========================================
             COLUMNA IZQUIERDA: PERFIL DEL ADMINISTRADOR
             ========================================== -->
        <div class="col-12 col-lg-5 text-center">
          <h2 class="section-title-orange mb-4">Perfil</h2>

          <!-- Foto circular con botón de cambio -->
          <div class="profile-photo-wrapper mb-3 mx-auto shadow-sm position-relative">
            <img 
              :src="adminData.avatar" 
              :alt="adminData.nombre"
              class="profile-big-avatar"
            >
            <label v-if="editando" class="btn-change-photo" title="Cambiar fotografía">
              <i class="bi bi-camera-fill"></i>
              <input type="file" class="d-none" accept="image/*" @change="onCambiarFoto">
            </label>
          </div>

          <h3 class="profile-user-name mb-1">{{ adminData.nombre }}</h3>
          <div class="profile-user-id mb-2">#{{ adminData.folioAdmin }}</div>

          <!-- Badge de Rol -->
          <div class="mb-4">
            <span class="badge badge-role-admin px-3 py-2">
              <i class="bi bi-shield-lock-fill me-1"></i> Administrador General · YelpME
            </span>
          </div>

          <!-- MODO LECTURA -->
          <div v-if="!editando" class="profile-contact-info text-start mx-auto mb-4">
            <div class="contact-item d-flex align-items-center mb-3">
              <i class="bi bi-telephone-fill me-3 contact-icon"></i>
              <span class="text-dark">{{ adminData.telefono }}</span>
            </div>
            <div class="contact-item d-flex align-items-center mb-3">
              <i class="bi bi-envelope-fill me-3 contact-icon"></i>
              <span class="text-dark">{{ adminData.correo }}</span>
            </div>
            <div class="contact-item d-flex align-items-start mb-3">
              <i class="bi bi-geo-alt-fill me-3 contact-icon mt-1"></i>
              <span class="text-dark">{{ adminData.direccion }}</span>
            </div>
            <div class="contact-item d-flex align-items-center">
              <i class="bi bi-key-fill me-3 contact-icon"></i>
              <span class="text-muted">•••••••••••• (Contraseña segura)</span>
            </div>

            <div class="text-center mt-4">
              <button class="btn-profile-edit w-100" @click="activarEdicion">
                <i class="bi bi-pencil-square me-2"></i> Editar información de cuenta
              </button>
            </div>
          </div>

          <!-- MODO EDICIÓN -->
          <div v-else class="profile-edit-form text-start mx-auto mb-4">
            <div class="mb-3">
              <label class="form-label small fw-bold text-secondary mb-1">Nombre completo</label>
              <input type="text" class="form-control form-control-sm" v-model="formTemp.nombre">
            </div>

            <div class="mb-3">
              <label class="form-label small fw-bold text-secondary mb-1">Teléfono</label>
              <input type="text" class="form-control form-control-sm" v-model="formTemp.telefono">
            </div>

            <div class="mb-3">
              <label class="form-label small fw-bold text-secondary mb-1">Correo electrónico</label>
              <input type="email" class="form-control form-control-sm" v-model="formTemp.correo">
            </div>

            <div class="mb-3">
              <label class="form-label small fw-bold text-secondary mb-1">Dirección / Sede</label>
              <input type="text" class="form-control form-control-sm" v-model="formTemp.direccion">
            </div>

            <div class="mb-4">
              <label class="form-label small fw-bold text-secondary mb-1">Nueva contraseña (opcional)</label>
              <input type="password" class="form-control form-control-sm" v-model="formTemp.password" placeholder="En blanco para mantener actual">
            </div>

            <div class="d-flex gap-2">
              <button class="btn btn-success flex-grow-1" @click="guardarCambios">
                <i class="bi bi-check2-circle me-1"></i> Guardar cambios
              </button>
              <button class="btn btn-danger px-3" @click="cancelarEdicion">
                Cancelar
              </button>
            </div>
          </div>

        </div>

        <!-- ==========================================
             COLUMNA DERECHA: HISTORIAL DE ACTIVIDADES
             ========================================== -->
        <div class="col-12 col-lg-7">
          
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="section-title-orange mb-0">Historial de solicitudes</h2>
            <span class="text-muted small">
              {{ historialAcciones.length }} acciones registradas
            </span>
          </div>

          <!-- Tabla con bordes y fondo naranja suave -->
          <div class="history-table-container shadow-sm mb-4">
            <table class="table table-history mb-0">
              <tbody>
                <tr 
                  v-for="(item, idx) in historialAcciones" 
                  :key="idx"
                  class="history-row"
                >
                  <td class="history-folio-cell ps-3">#{{ item.folio }}</td>
                  <td class="history-desc-cell pe-3">
                    <div class="fw-semibold text-dark">{{ item.accion }}</div>
                    <div class="text-muted" style="font-size: 0.85rem;">
                      {{ item.detalle }} · <span class="fst-italic">{{ item.fecha }}</span>
                    </div>
                  </td>
                </tr>

                <tr v-if="historialAcciones.length === 0">
                  <td colspan="2" class="text-center py-5 text-muted fst-italic">
                    Sin registros de actividad reciente.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Métricas del Administrador -->
          <div class="row g-3">
            <div class="col-12 col-sm-4">
              <div class="metric-admin-box p-3 text-center bg-white shadow-sm rounded-3">
                <div class="metric-title text-muted small">Alertas canalizadas</div>
                <div class="metric-value text-primary fw-bold fs-4">24</div>
              </div>
            </div>
            <div class="col-12 col-sm-4">
              <div class="metric-admin-box p-3 text-center bg-white shadow-sm rounded-3">
                <div class="metric-title text-muted small">Strikes emitidos</div>
                <div class="metric-value text-warning-emphasis fw-bold fs-4">3</div>
              </div>
            </div>
            <div class="col-12 col-sm-4">
              <div class="metric-admin-box p-3 text-center bg-white shadow-sm rounded-3">
                <div class="metric-title text-muted small">Cuentas bloqueadas</div>
                <div class="metric-value text-danger fw-bold fs-4">1</div>
              </div>
            </div>
          </div>

        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const editando = ref(false)

const adminData = ref({
  nombre: 'Kimberly Heredia',
  folioAdmin: 'ADM-00001',
  telefono: '+52 235 114 3616',
  correo: 'kimberher2205@gmail.com',
  direccion: 'Miguel Hidalgo, Carlos R. Smith, sn, 93827, Misantla, Ver.',
  avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80'
})

const formTemp = ref({ ...adminData.value, password: '' })

const historialAcciones = ref([
  {
    folio: '00001',
    accion: 'Despacho de auxilio autorizado',
    detalle: 'Se envió unidad de Protección Civil a conato en pastizal',
    fecha: 'Hoy, 08:25 a.m.'
  },
  {
    folio: '00003',
    accion: 'Edición de requerimientos',
    detalle: 'Se añadió apoyo de Tránsito y Vialidad a choque vehicular',
    fecha: 'Ayer, 02:40 p.m.'
  },
  {
    folio: '00004',
    accion: 'Canalización prioritaria',
    detalle: 'Atención a fuga de gas en establecimiento zona centro',
    fecha: '08/11/2026, 11:12 a.m.'
  },
  {
    folio: '00009',
    accion: 'Solicitud rechazada con reporte',
    detalle: 'Se levantó reporte y se aplicó strike a reporte infundado',
    fecha: '05/11/2026, 11:58 p.m.'
  }
])

const activarEdicion = () => {
  formTemp.value = { ...adminData.value, password: '' }
  editando.value = true
}

const cancelarEdicion = () => {
  editando.value = false
}

const guardarCambios = () => {
  adminData.value.nombre = formTemp.value.nombre
  adminData.value.telefono = formTemp.value.telefono
  adminData.value.correo = formTemp.value.correo
  adminData.value.direccion = formTemp.value.direccion
  editando.value = false
}

const onCambiarFoto = (e) => {
  const file = e.target.files[0]
  if (file) {
    adminData.value.avatar = URL.createObjectURL(file)
    formTemp.value.avatar = adminData.value.avatar
  }
}
</script>

<style scoped>
.admin-content-wrapper {
  max-width: 1060px;
  width: 100%;
}

/* Título en Naranja Corporativo */
.section-title-orange {
  color: #f39200;
  font-weight: 900;
  font-size: 2.2rem;
  letter-spacing: -0.5px;
}

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

.btn-change-photo {
  position: absolute;
  bottom: 8px;
  right: 12px;
  background-color: #f39200;
  color: #ffffff;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease;
}
.btn-change-photo:hover {
  transform: scale(1.08);
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

.badge-role-admin {
  background-color: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
  font-size: 0.82rem;
  border-radius: 20px;
}

.profile-contact-info,
.profile-edit-form {
  max-width: 340px;
  font-size: 0.95rem;
  color: #334155;
}

.contact-icon {
  color: #94a3b8;
  font-size: 1.15rem;
  flex-shrink: 0;
}

.btn-profile-edit {
  background-color: #f39200;
  color: #ffffff;
  font-weight: 600;
  border: none;
  border-radius: 4px;
  padding: 9px 18px;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}
.btn-profile-edit:hover {
  background-color: #d97706;
}

/* Tabla beige con acentos naranja */
.history-table-container {
  border: 1.5px solid #fbd38d;
  background-color: #fffaf0;
}

.table-history {
  width: 100%;
  margin-bottom: 0;
}

.history-row {
  border-bottom: 1px solid #fbd38d;
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
  padding: 14px 12px;
  vertical-align: middle;
}

.metric-admin-box {
  border: 1px solid #e2e8f0;
}
</style>