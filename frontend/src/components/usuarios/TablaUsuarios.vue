<template>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <caption class="visually-hidden">Usuarios de la app con su estado de cuenta y strikes activos.</caption>
      <thead>
        <tr>
          <th scope="col">Usuario</th>
          <th scope="col">Teléfono</th>
          <th scope="col">Cuenta</th>
          <th scope="col">Strikes</th>
          <th scope="col" class="text-end">Registro</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in usuarios" :key="u.id" @click="$emit('abrir', u.id)">
          <td>
            <RouterLink :to="{ name: 'usuario', params: { id: u.id } }" class="nombre" @click.stop>
              {{ nombreCompleto(u) }}
            </RouterLink>
            <span class="secundario">{{ u.correo }}</span>
          </td>
          <td class="ym-cifras">{{ u.telefono }}</td>
          <td><EstadoCuenta :estado="u.estado_cuenta" /></td>
          <td><MedidorStrikes :activos="u.strikes_activos" :limite="u.limite_strikes" /></td>
          <td class="text-end secundario ym-cifras"><time :datetime="u.created_at">{{ fecha(u.created_at) }}</time></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import EstadoCuenta from './EstadoCuenta.vue'
import MedidorStrikes from './MedidorStrikes.vue'
import { fecha, nombreCompleto } from '../../utils/formato'

defineProps({
  usuarios: { type: Array, required: true },
})

defineEmits(['abrir'])
</script>

<style scoped>
tbody tr {
  cursor: pointer;
}

.nombre {
  display: block;
  font-weight: var(--ym-peso-medio);
  color: var(--ym-marca-texto);
}

.secundario {
  display: block;
  font-size: var(--ym-texto-sm);
  color: var(--ym-texto-secundario);
}

th:first-child,
td:first-child {
  padding-left: var(--ym-espacio-4);
}

th:last-child,
td:last-child {
  padding-right: var(--ym-espacio-4);
}
</style>
