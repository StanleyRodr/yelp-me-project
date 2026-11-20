# 009 — Tarea: Arquitectura del frontend

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Flujo vistas → stores → services → mocks, con rutas y layout. Quitar `useAlertas` y navegación por `currentView`.

## Estructura resultante

```
src/
  constantes/   estados.js, transiciones.js
  mocks/        datos.js, bd.js, tiempo.js
  services/     http.js, simulacion.js, authService.js, serviciosService.js,
                solicitudesService.js, usuariosService.js, reglasUsuarios.js
  stores/       auth.js, catalogos.js, solicitudes.js, usuarios.js
  router/       index.js
  components/layout/  AppShell.vue, AppNavbar.vue
  views/        vistas provisionales + _legado/ (referencia, fuera del build)
```

## Pasos

### 1. Vistas viejas a `views/_legado/`
- Vistas viejas acopladas a `useAlertas`, eventos de `App.vue` y datos viejos (6 estados, `ubicacionNombre`, `ayudas`).
- Adaptarlas = trabajo tirado: se rehacen en 012–015.
- Mover con `git mv` las 5 vistas, `useAlertas.js` y `App.vue` (→ `AppLegado.vue`, tiene los modales viejos como referencia).
- No se importan: Vite no las incluye en el build. Se borran al cerrar el plan.

### 2. Mocks (`src/mocks/`)
- `datos.js`: tablas `servicios`, `usuarios`, `solicitudes`, `strikes`, `suspensiones`. Columnas iguales al script. IDs en orden de inserción del script + seeder.
- `solicitudes[].servicios`: ids (equivale a `solicitud_servicio`).
- `tiempo.js`: `hace({ dias, horas, minutos })` → ISO. Demo siempre reciente.
- Pendientes con 38 y 14 min de espera (seeder usa horas) para mostrar el indicador de espera.
- `bd.js`: copia mutable en memoria (`structuredClone`), `siguienteId()`, `CONTRASENA_DEMO_ADMIN`. Se reinicia al recargar.
- `servicios.color` viene de la BD con hex. Dato, no estilo. La UI no lo usa: íconos de servicio en color neutro para no competir con estados.

### 3. Constantes de dominio (`src/constantes/`)
- `estados.js`: 5 estados con `clave`, `icono` (Material Symbols), `token` (→ `--ym-estado-<token>`), `final`.
- `transiciones.js`: `ACCIONES` (etiqueta, ícono, variante de botón) y `accionesPermitidas(solicitud)` según plan 7.2:

| Estado | Con servicios | Sin servicios |
|---|---|---|
| Pendiente | enviar, editar servicios, rechazar | rechazar |
| Enviado | terminar, editar servicios | — |
| Finales | — | — |

UI y services usan la misma tabla. Una sola fuente.

### 4. Services (`src/services/`)
- `http.js`: axios con `VITE_API_URL` (por defecto `http://localhost:8000/api`), `Accept: application/json`, token desde `localStorage['ym_sesion']`. Aún sin uso.
- `simulacion.js`: `responder()` espera 400 ms y devuelve copia; `ErrorApi(mensaje, codigo)`.
- Cada service documenta su endpoint futuro. Forma de respuesta = la de la API.
- `solicitudesService`: listar/obtener con `usuario`, `admin`, `cerrado_por` expandidos. `enviar`, `terminar`, `rechazar` (motivo obligatorio, strike opcional), `actualizarServicios` (mín. 1). Valida con `puede()`: estado ya cambió → error 409 (concurrencia entre admins, plan 7.4).
- `reglasUsuarios.js`: strikes activos, suspensión vigente, estado de cuenta (Deshabilitado > Suspendido > Activo), `darStrike` (3 activos → suspensión automática).
- `usuariosService`: listar (solo rol `usuario`, con resumen), obtener (historial de solicitudes, strikes, suspensiones con nombres), reportar, anular strike (motivo), suspender, rehabilitar (cierra suspensión + strikes activos a 0), deshabilitar, reactivar, perfil, contraseña.
- `authService`: login solo admins activos con `Admin123*`.

### 5. Stores (`src/stores/`, setup stores)
- `auth`: sesión persistida en `localStorage` (try/catch: puede estar bloqueado). `iniciarSesion`, `cerrarSesion`, `actualizarAdmin`.
- `catalogos`: servicios, se cargan una vez; `servicioPorId`.
- `solicitudes`: `lista`, `actual`, `cargando`, `cargandoActual`, `error`. Getters: `conteoPorEstado`, `pendientesPorAntiguedad`, `activas`. Acciones reemplazan la solicitud en detalle y listado.
- `usuarios`: igual patrón; acciones de strikes y cuenta.
- `adminId` sale de `auth`, nunca de la vista.

### 6. Router (`src/router/index.js`)
- `/login` pública. Resto hijas de `AppShell`.
- `:id(\\d+)`: solo números. Ruta desconocida → inicio.
- Guard: sin sesión → `/login?redirigir=<ruta>`; con sesión en `/login` → inicio.
- `document.title` = `<meta.titulo> | Yelp Me`.
- Vistas con carga diferida.

### 7. Layout
- `App.vue`: solo `<RouterView />`.
- `AppShell`: enlace "Saltar al contenido", navbar, `<main>`. Arma enlaces (con contador de pendientes) y pasa props. Carga solicitudes si no hay.
- `AppNavbar`: presentacional. Identidad fija azul tinta. Enlaces con `v-for`. Activo con barra inferior; detalle `/solicitudes/:id` mantiene "Solicitudes" activo. Contador de pendientes. Menú: Mi perfil, Cerrar sesión. Foco claro sobre fondo oscuro. Colapsa en < 768 px.

### 8. Vistas provisionales
Mínimas, con datos del store para probar el flujo. Comentario indica tarea que las rehace (011–015). Login provisional funcional.

## Verificación

```bash
npm run build                                   # sin errores
grep -rn useAlertas src | grep -v _legado        # vacío
```

Reglas de negocio probadas cargando services con `vite.ssrLoadModule` (script fuera del repo): **23/23 OK**.
- Acciones por estado (con y sin servicios, finales).
- Login: usuario rechazado, contraseña mala, admin correcto.
- Enviar; segundo admin recibe error de concurrencia.
- Servicios vacíos rechazados; editar en Enviado; terminar.
- Motivo obligatorio en rechazar y anular.
- Rechazo sin strike no suma; tercer strike suspende; rehabilitar reinicia y conserva historial; anular guarda quién.
- Deshabilitado gana sobre suspendido; listado solo usuarios de la app.

No verificado aún en navegador (guard, recarga en `/solicitudes/3`). Se revisa en tarea 016.

## Resultado
Commit: `feat(frontend): agrega arquitectura con router, stores y services`.
