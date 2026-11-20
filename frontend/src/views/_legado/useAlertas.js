import { ref, computed } from 'vue'

export const ESTILOS_ESTADO = {
  'Pendiente': { color: '#ef4444' }, // Rojo
  'Asignada':  { color: '#0284c7' }, // Azul
  'En Camino': { color: '#f59e0b' }, // Amarillo / Ámbar
  'Atendida':  { color: '#15803d' }, // Verde
  'Rechazada': { color: '#1e293b' }, // Negro Carbón
  'Cancelada': { color: '#64748b' }  // Gris
}

export const catalogoServicios = [
  { key: 'policia', nombre: 'Policía Municipal', icono: 'bi bi-shield-shaded', clase: 'icon-police', btnClass: 'btn-icon-police' },
  { key: 'cruzroja', nombre: 'Cruz Roja / Médica', icono: 'bi bi-plus-square-fill', clase: 'icon-redcross', btnClass: 'btn-icon-redcross' },
  { key: 'transito', nombre: 'Tránsito y Vialidad', icono: 'bi bi-cone-striped', clase: 'icon-traffic', btnClass: 'btn-icon-traffic' },
  { key: 'pcivil', nombre: 'Protección Civil', icono: 'bi bi-shield-fill-check', clase: 'icon-pcivil', btnClass: 'btn-icon-pcivil' },
  { key: 'bomberos', nombre: 'Cuerpo de Bomberos', icono: 'bi bi-truck-front-fill', clase: 'icon-fire', btnClass: 'btn-icon-fire' },
  { key: 'ciudadana', nombre: 'Asistencia Ciudadana', icono: 'bi bi-person-fill', clase: 'icon-citizen', btnClass: 'btn-icon-citizen' }
]

const todasLasAlertas = ref([
  {
    id: '00001',
    estado: 'Pendiente',
    titulo: 'Incendio en pastizal / conato',
    comentario: 'Fuego extendiéndose cerca de viviendas sobre la periferia. Se solicita unidad de ataque rápido.',
    fecha: '09/11/2026',
    hora: '08:15 a.m.',
    lat: 19.9325,
    lng: -96.8530,
    ubicacionNombre: 'Col. Buenos Aires / Periferia',
    ayudas: ['bomberos', 'pcivil']
  },
  {
    id: '00002',
    estado: 'Pendiente',
    titulo: 'Persona lesionada por caída',
    comentario: 'Ciudadano con posible fractura tras accidente cerca de plaza comercial. Requiere traslado.',
    fecha: '09/11/2026',
    hora: '08:40 a.m.',
    lat: 19.9298,
    lng: -96.8525,
    ubicacionNombre: 'Parque Central José María Morelos',
    ayudas: ['cruzroja', 'ciudadana']
  },
  {
    id: '00003',
    estado: 'Pendiente',
    titulo: 'Colisión vehicular en crucero',
    comentario: 'Impacto entre motocicleta y automóvil. Tránsito detenido con derrame menor.',
    fecha: '08/11/2026',
    hora: '02:20 p.m.',
    lat: 19.9265,
    lng: -96.8505,
    ubicacionNombre: 'Av. Ignacio Zaragoza esq. Doblado',
    ayudas: ['transito', 'cruzroja', 'policia']
  },
  {
    id: '00004',
    estado: 'Asignada',
    titulo: 'Fuga de gas en restaurante',
    comentario: 'Olor fuerte en cocina comercial, unidad de protección civil asignada.',
    fecha: '08/11/2026',
    hora: '11:05 a.m.',
    lat: 19.9340,
    lng: -96.8490,
    ubicacionNombre: 'Calle 5 de Mayo, Centro',
    ayudas: ['pcivil', 'bomberos']
  },
  {
    id: '00005',
    estado: 'En Camino',
    titulo: 'Auxilio vial por falla mecánica',
    comentario: 'Vehículo varado bloqueando carril de incorporación a carretera.',
    fecha: '07/11/2026',
    hora: '06:10 p.m.',
    lat: 19.9240,
    lng: -96.8540,
    ubicacionNombre: 'Entronque Carretera Estatal',
    ayudas: ['transito']
  },
  {
    id: '00006',
    estado: 'En Camino',
    titulo: 'Emergencia médica hipertensión',
    comentario: 'Ambulancia de Cruz Roja en ruta para valoración de paciente.',
    fecha: '07/11/2026',
    hora: '03:45 p.m.',
    lat: 19.9310,
    lng: -96.8570,
    ubicacionNombre: 'Col. Los Pinos',
    ayudas: ['cruzroja']
  },
  {
    id: '00007',
    estado: 'Atendida',
    titulo: 'Retiro de árbol caído',
    comentario: 'Ramas removidas exitosamente de la vía pública por Protección Civil.',
    fecha: '06/11/2026',
    hora: '09:20 a.m.',
    lat: 19.9360,
    lng: -96.8470,
    ubicacionNombre: 'Colonia Aviación',
    ayudas: ['pcivil']
  },
  {
    id: '00008',
    estado: 'Atendida',
    titulo: 'Atención prehospitalaria en escuela',
    comentario: 'Estudiante con crisis convulsiva estabilizado y entregado a familiares.',
    fecha: '06/11/2026',
    hora: '10:30 a.m.',
    lat: 19.9275,
    lng: -96.8565,
    ubicacionNombre: 'Av. Carlos R. Smith',
    ayudas: ['cruzroja', 'ciudadana']
  },
  {
    id: '00009',
    estado: 'Rechazada',
    titulo: 'Falsa alarma de riña',
    comentario: 'Patrulla acudió al sitio y comprobó reporte infundado o broma.',
    fecha: '05/11/2026',
    hora: '11:50 p.m.',
    lat: 19.9215,
    lng: -96.8520,
    ubicacionNombre: 'Calle Boquerón',
    ayudas: ['policia']
  },
  {
    id: '00010',
    estado: 'Cancelada',
    titulo: 'Reporte de animal peligroso',
    comentario: 'Cancelado por el propio reportante al resguardar la mascota.',
    fecha: '04/11/2026',
    hora: '04:15 p.m.',
    lat: 19.9355,
    lng: -96.8550,
    ubicacionNombre: 'Col. Manuel Villaraus',
    ayudas: ['pcivil', 'ciudadana']
  }
])

const solicitudEnAtencion = ref(todasLasAlertas.value[0])

export function useAlertas() {
  const totalAlertas = computed(() => todasLasAlertas.value.length)
  const alertasActivasCount = computed(() => {
    return todasLasAlertas.value.filter(a => ['Pendiente', 'Asignada', 'En Camino'].includes(a.estado)).length
  })
  const atendidasCount = computed(() => {
    return todasLasAlertas.value.filter(a => a.estado === 'Atendida').length
  })
  const alertasPendientes = computed(() => {
    return todasLasAlertas.value.filter(a => a.estado === 'Pendiente')
  })

  const textoEstado = (estado) => {
    if (estado === 'Pendiente') return 'SIN ATENDER'
    if (estado === 'En Camino') return 'EN CAMINO'
    if (estado === 'Cancelada') return 'CANCELADA'
    if (estado === 'Rechazada') return 'RECHAZADA'
    return (estado || '').toUpperCase()
  }

  const conteoPorEstado = (estado) => {
    return todasLasAlertas.value.filter(a => a.estado === estado).length
  }

  return {
    todasLasAlertas,
    solicitudEnAtencion,
    totalAlertas,
    alertasActivasCount,
    atendidasCount,
    alertasPendientes,
    textoEstado,
    conteoPorEstado
  }
}