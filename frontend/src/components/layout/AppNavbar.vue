<template>
  <nav class="navbar navbar-expand-md barra" aria-label="Navegación principal">
    <div class="container-fluid barra-contenedor">
      <RouterLink :to="{ name: 'inicio' }" class="navbar-brand marca">
        <i class="bi bi-bell-fill" aria-hidden="true"></i>
        <span>YelpMe</span>
      </RouterLink>

      <button
        class="navbar-toggler barra-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navPrincipal"
        aria-controls="navPrincipal"
        aria-expanded="false"
        aria-label="Abrir menú"
      >
        <i class="bi bi-list" aria-hidden="true"></i>
      </button>

      <div id="navPrincipal" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto">
          <li v-for="enlace in enlaces" :key="enlace.nombre" class="nav-item">
            <RouterLink
              :to="{ name: enlace.nombre }"
              class="nav-link enlace"
              :class="{ 'enlace-activo': esActivo(enlace.nombre) }"
              :aria-current="esActivo(enlace.nombre) ? 'page' : undefined"
            >
              <i :class="['bi', enlace.icono]" aria-hidden="true"></i>
              {{ enlace.etiqueta }}
              <span
                v-if="enlace.contador"
                class="contador ym-cifras"
                :aria-label="`${enlace.contador} pendientes`"
              >{{ enlace.contador }}</span>
            </RouterLink>
          </li>
        </ul>

        <div v-if="admin" class="dropdown">
          <button
            class="btn usuario dropdown-toggle"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <span class="avatar" aria-hidden="true">{{ iniciales }}</span>
            <span class="usuario-nombre">{{ [admin.nombre, admin.apellido_paterno].filter(Boolean).join(' ') }}</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <RouterLink :to="{ name: 'perfil' }" class="dropdown-item">
                <i class="bi bi-person me-2" aria-hidden="true"></i>Mi perfil
              </RouterLink>
            </li>
            <li><hr class="dropdown-divider" /></li>
            <SelectorTema modo="items" />
            <li><hr class="dropdown-divider" /></li>
            <li>
              <button type="button" class="dropdown-item" @click="$emit('cerrar-sesion')">
                <i class="bi bi-box-arrow-right me-2" aria-hidden="true"></i>Cerrar sesión
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SelectorTema from '../comunes/SelectorTema.vue'

const props = defineProps({
  enlaces: { type: Array, required: true },
  admin: { type: Object, default: null },
})

defineEmits(['cerrar-sesion'])

const route = useRoute()

// "Solicitudes" sigue activo dentro del detalle /solicitudes/:id (igual con usuarios).
const SECCION = { solicitud: 'solicitudes', usuario: 'usuarios' }
const esActivo = (nombre) => (SECCION[route.name] ?? route.name) === nombre

const iniciales = computed(() =>
  [props.admin?.nombre, props.admin?.apellido_paterno]
    .filter(Boolean)
    .map((p) => p[0])
    .join('')
    .toUpperCase(),
)
</script>

<style scoped>
.barra {
  background: var(--ym-marca);
  min-height: var(--ym-alto-navbar);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 1030;
}

.barra-contenedor {
  max-width: 1440px;
  padding: 0 var(--ym-espacio-5);
}

.marca {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  color: var(--ym-sobre-marca);
  font-weight: var(--ym-peso-fuerte);
  font-size: var(--ym-texto-lg);
  margin-right: var(--ym-espacio-6);
}

.marca:hover,
.marca:focus-visible {
  color: var(--ym-sobre-marca);
}

.barra-toggler {
  color: var(--ym-sobre-marca);
  border-color: var(--ym-sobre-marca-tenue);
  font-size: var(--ym-texto-lg);
}

.enlace {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  color: var(--ym-sobre-marca-tenue);
  font-weight: var(--ym-peso-medio);
  padding: 0 var(--ym-espacio-3) !important;
  min-height: var(--ym-alto-navbar);
  border-bottom: 3px solid transparent;
  border-top: 3px solid transparent;
}

.enlace:hover {
  color: var(--ym-sobre-marca);
}

.enlace-activo {
  color: var(--ym-sobre-marca);
  border-bottom-color: var(--ym-sobre-marca);
}

.contador {
  min-width: 1.5rem;
  padding: 0 var(--ym-espacio-1);
  border-radius: var(--ym-radio);
  background: var(--ym-estado-pendiente);
  color: var(--ym-sobre-marca);
  font-size: var(--ym-texto-sm);
  line-height: 1.5rem;
  text-align: center;
}

.usuario {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-2);
  color: var(--ym-sobre-marca);
  border: 1px solid transparent;
}

.usuario:hover,
.usuario[aria-expanded='true'] {
  color: var(--ym-sobre-marca);
  border-color: var(--ym-sobre-marca-tenue);
}

.avatar {
  display: inline-grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: var(--ym-sobre-marca);
  color: var(--ym-marca);
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-fuerte);
}

/* La navbar es oscura: el foco usa la variante clara */
.barra :focus-visible {
  outline-color: var(--ym-foco-claro);
}

@media (max-width: 767.98px) {
  .barra-contenedor {
    padding: var(--ym-espacio-2) var(--ym-espacio-3);
  }

  .enlace {
    min-height: 2.75rem;
    border: 0;
    border-left: 3px solid transparent;
  }

  .enlace-activo {
    border-left-color: var(--ym-sobre-marca);
  }

  .dropdown {
    padding: var(--ym-espacio-2) 0;
  }
}
</style>
