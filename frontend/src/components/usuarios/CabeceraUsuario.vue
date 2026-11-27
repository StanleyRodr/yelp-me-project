<template>
  <header class="cabecera">
    <div class="identidad">
      <img v-if="usuario.foto_perfil" :src="usuario.foto_perfil" alt="" class="avatar" />
      <span v-else class="avatar avatar-iniciales" aria-hidden="true">{{ iniciales }}</span>
      <div>
        <h1 class="nombre">{{ nombreCompleto(usuario) }}</h1>
        <p class="contacto">
          <a :href="`tel:${usuario.telefono}`" class="ym-cifras"><i class="bi bi-telephone" aria-hidden="true"></i> {{ usuario.telefono }}</a>
          <a :href="`mailto:${usuario.correo}`"><i class="bi bi-envelope" aria-hidden="true"></i> {{ usuario.correo }}</a>
        </p>
      </div>
    </div>

    <dl class="resumen">
      <div>
        <dt>Cuenta</dt>
        <dd><EstadoCuenta :estado="usuario.estado_cuenta" /></dd>
      </div>
      <div>
        <dt>Strikes activos</dt>
        <dd><MedidorStrikes :activos="usuario.strikes_activos" :limite="usuario.limite_strikes" /></dd>
      </div>
      <div>
        <dt>Veces suspendido</dt>
        <dd class="ym-cifras">{{ usuario.veces_suspendido }}</dd>
      </div>
      <div>
        <dt>Registro</dt>
        <dd class="ym-cifras">{{ fecha(usuario.created_at) }}</dd>
      </div>
    </dl>

    <div class="acciones">
      <BotonCarga
        v-for="accion in acciones"
        :key="accion.clave"
        :variante="accion.variante"
        :icono="accion.icono"
        :cargando="enCurso === accion.clave"
        :deshabilitado="Boolean(enCurso)"
        @click="$emit('accion', accion.clave)"
      >
        {{ accion.etiqueta }}
      </BotonCarga>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import EstadoCuenta from './EstadoCuenta.vue'
import MedidorStrikes from './MedidorStrikes.vue'
import BotonCarga from '../comunes/BotonCarga.vue'
import { accionesCuenta } from '../../constantes/cuentas'
import { fecha, nombreCompleto } from '../../utils/formato'

const props = defineProps({
  usuario: { type: Object, required: true },
  enCurso: { type: String, default: '' },
})

defineEmits(['accion'])

const acciones = computed(() => accionesCuenta(props.usuario))
const iniciales = computed(() => [props.usuario.nombre, props.usuario.apellido_paterno].map((p) => p?.[0] ?? '').join('').toUpperCase())
</script>

<style scoped>
.cabecera {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--ym-espacio-4) var(--ym-espacio-5);
  padding: var(--ym-espacio-5);
  background: var(--ym-superficie);
  border: 1px solid var(--ym-borde);
  border-radius: var(--ym-radio);
}

.identidad {
  display: flex;
  align-items: center;
  gap: var(--ym-espacio-4);
  min-width: 0;
}

.avatar {
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.avatar-iniciales {
  display: grid;
  place-items: center;
  background: var(--ym-marca);
  color: var(--ym-sobre-marca);
  font-size: var(--ym-texto-lg);
  font-weight: var(--ym-peso-fuerte);
}

.nombre {
  margin: 0 0 var(--ym-espacio-1);
}

.contacto {
  display: flex;
  flex-wrap: wrap;
  gap: var(--ym-espacio-1) var(--ym-espacio-4);
  margin: 0;
}

.resumen {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, max-content));
  gap: var(--ym-espacio-2) var(--ym-espacio-6);
  margin: 0;
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

.acciones {
  grid-column: 2;
  grid-row: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-content: flex-start;
  gap: var(--ym-espacio-2);
}

@media (max-width: 991.98px) {
  .cabecera {
    grid-template-columns: minmax(0, 1fr);
  }

  .acciones {
    grid-column: 1;
    grid-row: auto;
    justify-content: flex-start;
  }

  .resumen {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
