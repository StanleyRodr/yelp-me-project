<template>
  <div class="detalle">
    <p v-if="solicitud.descripcion" class="descripcion">{{ solicitud.descripcion }}</p>
    <p v-else class="descripcion sin-dato">El usuario no agregó descripción.</p>

    <dl class="datos">
      <div>
        <dt>Usuario</dt>
        <dd>
          {{ nombreCompleto(solicitud.usuario) }}
          <a v-if="solicitud.usuario?.telefono" :href="`tel:${solicitud.usuario.telefono}`" class="d-block ym-cifras">
            <i class="bi bi-telephone" aria-hidden="true"></i> {{ solicitud.usuario.telefono }}
          </a>
        </dd>
      </div>
      <div>
        <dt>Ubicación</dt>
        <dd>
          {{ solicitud.ubicacion_nombre || 'Sin referencia' }}
          <span v-if="desdeDomicilio" class="d-block">
            <span class="etiqueta-domicilio">
              <span class="material-symbols-outlined" aria-hidden="true">home</span>Enviada desde su domicilio
            </span>
          </span>
          <span class="d-block secundario ym-cifras">{{ solicitud.latitud }}, {{ solicitud.longitud }}</span>
        </dd>
      </div>
      <div>
        <dt>Recibida</dt>
        <dd class="ym-cifras"><time :datetime="solicitud.created_at">{{ fechaHora(solicitud.created_at) }}</time></dd>
      </div>
      <div>
        <dt>Atendida por</dt>
        <dd>{{ solicitud.admin ? nombreCompleto(solicitud.admin) : 'Sin asignar' }}</dd>
      </div>
      <div class="datos-ancho">
        <dt>Servicios requeridos</dt>
        <dd><ListaServicios :ids="solicitud.servicios" :catalogo="catalogo" con-nombre /></dd>
      </div>
      <div v-if="solicitud.motivo" class="datos-ancho">
        <dt>Motivo</dt>
        <dd>{{ solicitud.motivo }}</dd>
      </div>
    </dl>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ListaServicios from '../comunes/ListaServicios.vue'
import { fechaHora, nombreCompleto } from '../../utils/formato'

const props = defineProps({
  solicitud: { type: Object, required: true },
  catalogo: { type: Object, required: true },
})

// La app pone "Domicilio: …" en ubicacion_nombre cuando usa el domicilio en lugar del GPS (plan 011).
const desdeDomicilio = computed(() => (props.solicitud.ubicacion_nombre ?? '').startsWith('Domicilio:'))
</script>

<style scoped>
.descripcion {
  font-size: var(--ym-texto-lg);
  max-width: 60ch;
  margin-bottom: var(--ym-espacio-5);
}

.sin-dato {
  font-size: var(--ym-texto-base);
  color: var(--ym-texto-secundario);
}

.datos {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ym-espacio-4) var(--ym-espacio-5);
  margin: 0;
}

.datos-ancho {
  grid-column: 1 / -1;
}

dt {
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-medio);
  color: var(--ym-texto-secundario);
  margin-bottom: var(--ym-espacio-1);
}

dd {
  margin: 0;
}

.secundario {
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

@media (max-width: 575.98px) {
  .datos {
    grid-template-columns: minmax(0, 1fr);
  }
}
.etiqueta-domicilio {
  display: inline-flex;
  align-items: center;
  gap: var(--ym-espacio-1);
  margin-top: var(--ym-espacio-1);
  padding: 0 var(--ym-espacio-2);
  border-radius: var(--ym-radio);
  background: var(--ym-marca-suave);
  color: var(--ym-marca-texto);
  font-size: var(--ym-texto-sm);
  font-weight: var(--ym-peso-medio);
}

.etiqueta-domicilio .material-symbols-outlined {
  font-size: 1rem;
}
</style>
