import { hace } from './tiempo'

/*
 * Datos de ejemplo con la forma exacta de las tablas de database/script-bd.sql
 * y los mismos registros que backend/database/seeders/DatabaseSeeder.php.
 * Los IDs siguen el orden de inserción del script y del seeder.
 */

// Tabla servicios (datos iniciales del script)
export const servicios = [
  { id: 1, nombre: 'Policía', icono: 'local_police', color: '#243C5A', activo: true },
  { id: 2, nombre: 'Ambulancia', icono: 'ambulance', color: '#FF0000', activo: true },
  { id: 3, nombre: 'Bomberos', icono: 'fire_truck', color: '#DC2626', activo: true },
  { id: 4, nombre: 'Tránsito', icono: 'traffic', color: '#F59E0B', activo: true },
  { id: 5, nombre: 'Protección Civil', icono: 'health_and_safety', color: '#EA580C', activo: true },
  { id: 6, nombre: 'Asistencia Ciudadana', icono: 'support', color: '#10B981', activo: true },
]

const usuario = (id, nombre, apellidos, telefono, correo, rol, creadoDias) => ({
  id, nombre, apellidos, telefono, correo, rol,
  foto_perfil: null,
  activo: true,
  deshabilitado_en: null,
  created_at: hace({ dias: creadoDias }),
})

// Tabla usuarios (admin del script + seeder)
export const usuarios = [
  usuario(1, 'Administrador', 'General', '0000000000', 'admin@yelpme.com', 'admin', 60),
  usuario(2, 'Laura', 'Méndez Ruiz', '2350000001', 'laura.admin@yelpme.com', 'admin', 45),
  usuario(3, 'Carlos', 'Ramírez Soto', '2351000001', 'carlos@correo.com', 'usuario', 44),
  usuario(4, 'María', 'López Díaz', '2351000002', 'maria@correo.com', 'usuario', 44),
  usuario(5, 'José', 'Hernández Cruz', '2351000003', 'jose@correo.com', 'usuario', 43),
  usuario(6, 'Ana', 'Torres Vega', '2351000004', 'ana@correo.com', 'usuario', 42),
  usuario(7, 'Luis', 'García Mora', '2351000005', 'luis@correo.com', 'usuario', 41),
]

const solicitud = (datos) => ({
  descripcion: null,
  admin_id: null,
  cerrado_por_id: null,
  motivo: null,
  enviado_en: null,
  cerrado_en: null,
  servicios: [],
  updated_at: datos.created_at,
  ...datos,
})

const MOTIVO_BROMA = 'Falsa alarma: la patrulla no encontró ninguna emergencia.'
const BOQUERON = { latitud: 19.9215, longitud: -96.852, ubicacion_nombre: 'Calle Boquerón, Misantla' }

// Tabla solicitudes + solicitud_servicio (servicios: ids)
export const solicitudes = [
  solicitud({
    id: 1, usuario_id: 3, estado: 'Pendiente',
    titulo: 'Incendio en pastizal cerca de viviendas',
    descripcion: 'El fuego avanza hacia las casas de la periferia.',
    latitud: 19.9325, longitud: -96.853, ubicacion_nombre: 'Col. Buenos Aires, Misantla',
    servicios: [3, 5], created_at: hace({ minutos: 38 }),
  }),
  solicitud({
    id: 2, usuario_id: 4, estado: 'Pendiente',
    titulo: 'Me siento insegura, necesito que alguien venga por mí',
    latitud: 19.9298, longitud: -96.8525, ubicacion_nombre: 'Parque Central, Misantla',
    created_at: hace({ minutos: 14 }),
  }),
  solicitud({
    id: 3, usuario_id: 3, estado: 'Enviado',
    titulo: 'Persona lesionada por caída',
    descripcion: 'Posible fractura de pierna, no puede moverse.',
    latitud: 19.9265, longitud: -96.8505, ubicacion_nombre: 'Av. Ignacio Zaragoza, Misantla',
    admin_id: 1, enviado_en: hace({ dias: 1, horas: 3 }),
    servicios: [2, 1], created_at: hace({ dias: 1, horas: 4 }),
  }),
  solicitud({
    id: 4, usuario_id: 5, estado: 'Terminado',
    titulo: 'Choque entre motocicleta y automóvil',
    latitud: 19.934, longitud: -96.849, ubicacion_nombre: 'Calle 5 de Mayo, Centro',
    admin_id: 2, cerrado_por_id: 2,
    enviado_en: hace({ dias: 3, horas: 5 }), cerrado_en: hace({ dias: 3, horas: 3 }),
    servicios: [4, 2], created_at: hace({ dias: 3, horas: 6 }),
  }),
  solicitud({
    id: 5, usuario_id: 4, estado: 'Terminado',
    titulo: 'Se descompuso mi auto en carretera',
    latitud: 19.924, longitud: -96.854, ubicacion_nombre: 'Entronque carretera estatal',
    cerrado_por_id: 4, cerrado_en: hace({ dias: 4, horas: 1 }),
    created_at: hace({ dias: 4, horas: 3 }),
  }),
  solicitud({
    id: 6, usuario_id: 3, estado: 'Cancelado',
    titulo: 'Perro agresivo en la calle',
    latitud: 19.9355, longitud: -96.855, ubicacion_nombre: 'Col. Manuel Villaraus',
    cerrado_por_id: 3, motivo: 'El dueño ya recogió al perro.',
    cerrado_en: hace({ dias: 5, horas: 2 }),
    servicios: [5], created_at: hace({ dias: 5, horas: 3 }),
  }),
  ...[
    [7, 6, 1, 'Riña en la plaza', 6],
    [8, 6, 2, 'Robo a casa habitación', 2],
    [9, 7, 1, 'Incendio en escuela', 9],
    [10, 7, 2, 'Persona armada', 8],
    [11, 7, 1, 'Explosión de gas', 7],
  ].map(([id, usuarioId, adminId, titulo, dias]) => solicitud({
    id, usuario_id: usuarioId, estado: 'Rechazado', titulo, ...BOQUERON,
    admin_id: adminId, cerrado_por_id: adminId, motivo: MOTIVO_BROMA,
    cerrado_en: hace({ dias, horas: 1 }),
    servicios: [1], created_at: hace({ dias, horas: 2 }),
  })),
]

const strike = (id, usuarioId, adminId, solicitudId, motivo, activo, dias) => ({
  id, usuario_id: usuarioId, admin_id: adminId, solicitud_id: solicitudId, motivo, activo,
  anulado_por_id: null, motivo_anulacion: null, anulado_en: null,
  created_at: hace({ dias, horas: 1 }),
})

const BROMA = 'Broma / mal uso del servicio.'
const MANUAL = 'Reporte manual: llamadas de broma reiteradas.'

// Tabla strikes
export const strikes = [
  strike(1, 6, 1, 7, BROMA, true, 6),
  strike(2, 6, 2, 8, BROMA, true, 2),
  strike(3, 7, 1, null, MANUAL, false, 40),
  strike(4, 7, 1, null, MANUAL, false, 38),
  strike(5, 7, 1, null, MANUAL, false, 35),
  strike(6, 7, 1, 9, BROMA, true, 9),
  strike(7, 7, 2, 10, BROMA, true, 8),
  strike(8, 7, 1, 11, BROMA, true, 7),
]

// Tabla suspensiones
export const suspensiones = [
  {
    id: 1, usuario_id: 7, suspendido_por_id: null,
    motivo: 'Suspensión automática por 3 strikes.',
    fecha_suspension: hace({ dias: 35 }),
    rehabilitado_por_id: 1, fecha_rehabilitacion: hace({ dias: 30 }),
  },
  {
    id: 2, usuario_id: 7, suspendido_por_id: null,
    motivo: 'Suspensión automática por 3 strikes.',
    fecha_suspension: hace({ dias: 7, horas: 1 }),
    rehabilitado_por_id: null, fecha_rehabilitacion: null,
  },
]
