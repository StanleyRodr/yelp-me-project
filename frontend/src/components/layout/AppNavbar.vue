<template>
  <nav 
    class="navbar navbar-expand-lg navbar-dark navbar-yelpme shadow-sm sticky-top px-3"
    :class="navbarColorClass"
  >
    <div class="container-fluid p-0">
      <a class="navbar-brand text-white d-flex align-items-center cursor-pointer me-3 py-2" @click="$emit('cambiar-vista', 'inicio')">
        <i class="bi bi-bell-fill fs-3 me-2"></i>
        <span class="yelpme-brand-logo">YelpME</span>
      </a>

      <button class="navbar-toggler border-0 my-2" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse h-100" id="mainNav">
        <ul class="navbar-nav ms-auto align-items-lg-center">
          <!-- Inicio -->
          <li class="nav-item">
            <button 
              class="nav-link-yelpme btn text-decoration-none" 
              :class="{ 'active-red': currentView === 'inicio' }" 
              @click="$emit('cambiar-vista', 'inicio')"
            >
              Inicio
            </button>
          </li>

          <!-- Solicitudes -->
          <li class="nav-item">
            <button 
              class="nav-link-yelpme btn text-decoration-none" 
              :class="{ 'active-green': currentView === 'solicitudes' }" 
              @click="$emit('cambiar-vista', 'solicitudes')"
            >
              Solicitudes
            </button>
          </li>

          <!-- Atención -->
          <li class="nav-item">
            <button 
              class="nav-link-yelpme btn text-decoration-none" 
              :class="{ 'active-blue': currentView === 'atencion' }" 
              @click="$emit('cambiar-vista', 'atencion')"
            >
              Atención
            </button>
          </li>

          <!-- Usuarios -->
          <li class="nav-item">
            <button 
              class="nav-link-yelpme btn text-decoration-none" 
              :class="{ 'active-yellow': currentView === 'usuarios' }" 
              @click="$emit('cambiar-vista', 'usuarios')"
            >
              Usuarios
            </button>
          </li>

          <!-- Perfil (Naranja cálido exacto de la captura) -->
          <li class="nav-item">
            <button 
              class="nav-link-yelpme btn text-decoration-none" 
              :class="{ 'active-orange': currentView === 'perfil' }" 
              @click="$emit('cambiar-vista', 'perfil')"
            >
              Perfil
            </button>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentView: { type: String, required: true }
})

defineEmits(['cambiar-vista'])

const navbarColorClass = computed(() => {
  if (props.currentView === 'solicitudes') return 'bg-yelpme-green'
  if (props.currentView === 'atencion') return 'bg-yelpme-blue'
  if (props.currentView === 'usuarios') return 'bg-yelpme-yellow'
  if (props.currentView === 'perfil') return 'bg-yelpme-orange'
  return 'bg-yelpme-red'
})
</script>

<style scoped>
.bg-yelpme-red    { background-color: #ff0000 !important; transition: background-color 0.25s ease; }
.bg-yelpme-green  { background-color: #8ac249 !important; transition: background-color 0.25s ease; }
.bg-yelpme-blue   { background-color: #0284c7 !important; transition: background-color 0.25s ease; }
.bg-yelpme-yellow { background-color: #f5b842 !important; transition: background-color 0.25s ease; }

/* Tono naranja exacto de la captura para el perfil */
.bg-yelpme-orange { background-color: #f39200 !important; transition: background-color 0.25s ease; }

.navbar-yelpme { padding: 0; min-height: 56px; }
.yelpme-brand-logo { font-size: 1.6rem; letter-spacing: -0.5px; font-weight: 800; }

.nav-link-yelpme {
  color: #ffffff !important;
  font-weight: 500;
  font-size: 0.95rem;
  border: none !important;
  background: transparent;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 20px !important;
  border-radius: 4px;
  margin: 8px 3px;
  transition: all 0.2s ease;
}
.nav-link-yelpme:hover { background-color: rgba(0, 0, 0, 0.08); }

.nav-link-yelpme.active-red {
  background-color: #c40000 !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}
.nav-link-yelpme.active-green {
  background-color: #6b9e32 !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}
.nav-link-yelpme.active-blue {
  background-color: #0369a1 !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}
.nav-link-yelpme.active-yellow {
  background-color: #df9e26 !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

/* Recuadro activo de Perfil en la barra */
.nav-link-yelpme.active-orange {
  background-color: rgba(0, 0, 0, 0.12) !important;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  font-weight: 600;
}

.cursor-pointer { cursor: pointer; }
</style>