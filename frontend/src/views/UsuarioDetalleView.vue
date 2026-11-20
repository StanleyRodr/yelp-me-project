<template>
  <div class="rejilla">
    <nav class="rejilla-volver" aria-label="Ruta">
      <RouterLink :to="{ name: 'usuarios' }"><i class="bi bi-arrow-left" aria-hidden="true"></i> Usuarios</RouterLink>
    </nav>

    <template v-if="mostrarSkeleton">
      <TarjetaSeccion class="rejilla-cabecera"><SkeletonDetalle :campos="3" etiqueta="Cargando usuario…" /></TarjetaSeccion>
    </template>

    <EstadoVacio
      v-else-if="!store.cargandoActual && !u"
      class="rejilla-cabecera"
      icono="person_search"
      titulo="No existe este usuario"
      mensaje="Revisa el enlace o vuelve al listado de usuarios."
    >
      <RouterLink :to="{ name: 'usuarios' }" class="btn btn-outline-primary">Ver usuarios</RouterLink>
    </EstadoVacio>

    <template v-else-if="u">
      <CabeceraUsuario class="rejilla-cabecera" :usuario="u" :en-curso="enCurso" @accion="ejecutar" />

      <TarjetaSeccion class="rejilla-solicitudes" titulo="Solicitudes" sin-relleno>
        <HistorialSolicitudes :solicitudes="u.solicitudes" />
      </TarjetaSeccion>

      <div class="rejilla-lateral">
        <TarjetaSeccion titulo="Strikes" sin-relleno>
          <HistorialStrikes :strikes="u.strikes" @anular="pedirAnulacion" />
        </TarjetaSeccion>
        <TarjetaSeccion titulo="Suspensiones" sin-relleno>
          <HistorialSuspensiones :suspensiones="u.suspensiones" />
        </TarjetaSeccion>
      </div>

      <ModalMotivo v-if="modal" v-model:abierto="modalAbierto" v-bind="modal" />
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import CabeceraUsuario from '../components/usuarios/CabeceraUsuario.vue'
import HistorialSolicitudes from '../components/usuarios/HistorialSolicitudes.vue'
import HistorialStrikes from '../components/usuarios/HistorialStrikes.vue'
import HistorialSuspensiones from '../components/usuarios/HistorialSuspensiones.vue'
import TarjetaSeccion from '../components/comunes/TarjetaSeccion.vue'
import EstadoVacio from '../components/comunes/EstadoVacio.vue'
import SkeletonDetalle from '../components/comunes/SkeletonDetalle.vue'
import ModalMotivo from '../components/modals/ModalMotivo.vue'
import { useCargaDiferida } from '../composables/useCargaDiferida'
import { useUsuariosStore } from '../stores/usuarios'
import { confirmar, exito } from '../plugins/alertas'
import { nombreCompleto } from '../utils/formato'

const props = defineProps({ id: { type: String, required: true } })

const store = useUsuariosStore()
const u = computed(() => store.actual)
const enCurso = ref('')
const modal = ref(null)
const modalAbierto = ref(false)

const cargaDiferida = useCargaDiferida(() => store.cargandoActual)
const mostrarSkeleton = computed(() => cargaDiferida.value && !u.value)

watch(() => props.id, (id) => store.cargarUno(id), { immediate: true })

const nombre = () => nombreCompleto(u.value)

function abrirModal(config) {
  modal.value = config
  modalAbierto.value = true
}

function pedirAnulacion(strike) {
  abrirModal({
    titulo: 'Anular strike',
    descripcion: `El strike deja de contar para la suspensión de ${nombre()}, pero queda en el historial.`,
    etiqueta: 'Motivo de la anulación',
    textoBoton: 'Anular strike',
    mensajeExito: 'Strike anulado',
    accion: (motivo) => store.anularStrike(strike.id, motivo),
  })
}

async function confirmarAccion(clave, dialogo) {
  enCurso.value = clave
  try {
    if (await confirmar(dialogo)) exito(dialogo.exito)
  } finally {
    enCurso.value = ''
  }
}

function ejecutar(clave) {
  const id = u.value.id
  switch (clave) {
    case 'reportar':
      return abrirModal({
        titulo: 'Reportar usuario',
        descripcion: `Suma un strike a ${nombre()}. Con 3 strikes activos la cuenta se suspende para pedir servicios.`,
        etiqueta: 'Motivo del reporte',
        textoBoton: 'Registrar strike',
        variante: 'danger',
        mensajeExito: 'Strike registrado',
        accion: (motivo) => store.reportar(id, motivo),
      })
    case 'suspender':
      return abrirModal({
        titulo: 'Suspender cuenta',
        descripcion: `${nombre()} no podrá pedir servicios de emergencia hasta que un admin rehabilite la cuenta. Podrá seguir avisando a sus contactos.`,
        etiqueta: 'Motivo de la suspensión',
        textoBoton: 'Suspender cuenta',
        variante: 'danger',
        mensajeExito: 'Cuenta suspendida',
        accion: (motivo) => store.suspender(id, motivo),
      })
    case 'rehabilitar':
      return confirmarAccion(clave, {
        titulo: 'Rehabilitar cuenta',
        texto: `Se levanta la suspensión de ${nombre()} y sus strikes activos vuelven a 0. El historial se conserva.`,
        confirmar: 'Rehabilitar cuenta',
        exito: 'Cuenta rehabilitada',
        accion: () => store.rehabilitar(id),
      })
    case 'deshabilitar':
      return confirmarAccion(clave, {
        titulo: 'Deshabilitar cuenta',
        texto: `${nombre()} no podrá iniciar sesión ni recibir avisos. Sus datos se conservan y puedes reactivar la cuenta después.`,
        confirmar: 'Deshabilitar cuenta',
        peligro: true,
        exito: 'Cuenta deshabilitada',
        accion: () => store.deshabilitar(id),
      })
    case 'reactivar':
      return confirmarAccion(clave, {
        titulo: 'Reactivar cuenta',
        texto: `${nombre()} podrá volver a iniciar sesión.`,
        confirmar: 'Reactivar cuenta',
        exito: 'Cuenta reactivada',
        accion: () => store.reactivar(id),
      })
  }
}
</script>

<style scoped>
.rejilla {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
  grid-template-areas:
    'volver volver'
    'cabecera cabecera'
    'solicitudes lateral';
  gap: var(--ym-espacio-4);
  align-items: start;
}

.rejilla-volver { grid-area: volver; }
.rejilla-cabecera { grid-area: cabecera; }
.rejilla-solicitudes { grid-area: solicitudes; }

.rejilla-lateral {
  grid-area: lateral;
  display: flex;
  flex-direction: column;
  gap: var(--ym-espacio-4);
  min-width: 0;
}

@media (max-width: 991.98px) {
  .rejilla {
    grid-template-columns: minmax(0, 1fr);
    grid-template-areas: 'volver' 'cabecera' 'solicitudes' 'lateral';
  }
}
</style>
