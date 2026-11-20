# 009 — Tarea: Arquitectura del frontend

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Flujo de datos vistas → stores → services → mocks, con rutas y layout. Quitar `useAlertas` y la navegación por `currentView`.

## Pasos

### 1. Mocks con forma de la BD (`src/mocks/`)
- `servicios.js`, `usuarios.js`, `solicitudes.js`, `strikes.js`, `suspensiones.js`.
- Mismos datos del seeder del backend: Carlos, María, José, Ana, Luis; admins 1 y Laura.
- Campos `snake_case` iguales a las columnas. Solicitudes con `servicios: [ids]`.
- Fechas relativas a "ahora" para que la demo se vea reciente.

### 2. Constantes de dominio (`src/constantes/`)
- `estados.js`: los 5 estados con clave, etiqueta, ícono y token CSS.
- `transiciones.js`: tabla del plan principal 7.2. Función `accionesPermitidas(solicitud)` → acciones del admin.

### 3. Services (`src/services/`)
- `http.js`: instancia de axios (`baseURL` de `VITE_API_URL`) con token en `Authorization`. Lista para plan futuro.
- Un service por recurso (`authService`, `solicitudesService`, `usuariosService`, `serviciosService`).
- Firma igual a la API futura; hoy leen mocks con latencia simulada (~400 ms) y devuelven copias.

### 4. Stores Pinia (`src/stores/`)
- `auth`, `solicitudes`, `usuarios`, `catalogos`.
- Cada store: `cargando`, `error`, datos y acciones que llaman al service.
- Getters derivados: conteo por estado, pendientes por antigüedad, strikes activos, suspensión vigente.

### 5. Router (`src/router/index.js`)
- Rutas del plan con `meta.titulo` y `meta.publica`.
- Guard: sin sesión → `/login`; con sesión en `/login` → `/`.
- Título del documento desde `meta.titulo`.
- Rutas con carga diferida (`() => import(...)`).

### 6. Layout
- `components/layout/AppShell.vue`: navbar + `<RouterView>`.
- `components/layout/AppNavbar.vue`: identidad fija; enlaces con `v-for` sobre un arreglo; `RouterLink` con estado activo; menú de usuario con cerrar sesión.
- `App.vue`: solo `<RouterView>`; AppShell envuelve las rutas protegidas.

### 7. Limpieza
- Borrar `composables/useAlertas.js` y los modales de `App.vue`.
- Vistas viejas quedan conectadas a stores de forma mínima hasta rehacerse en 012–015.

## Verificación
- `npm run build` sin errores.
- Navegar entre rutas; recarga en `/solicitudes/3` abre directo.
- Ningún import de `useAlertas`.

## Resultado
Pendiente.
