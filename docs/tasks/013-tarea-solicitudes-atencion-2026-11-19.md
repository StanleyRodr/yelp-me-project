# 013 — Tarea: Solicitudes y atención

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada

## Objetivo
Listado con filtros + detalle por folio con solo las acciones válidas del estado.

## Pasos

### 1. Utilidades
- `utils/eventosSolicitud.js`: `eventosDe(solicitud)` arma la línea de tiempo desde `created_at`, `enviado_en`, `cerrado_en` (autor, motivo, color de estado). Sin tabla de eventos.
- `utils/formato.js`: `normalizar()` quita acentos y mayúsculas para buscar ("policia" encuentra "Policía").

### 2. `components/solicitudes/FiltrosSolicitudes`
- `v-model:estado` (null = todas) y `v-model:busqueda`.
- Chips con `v-for`: "Todas" + 5 estados, ícono y conteo. Botones con `aria-pressed`. Activo: tinte y borde inferior del color del estado.
- `CampoBusqueda`: folio, título, usuario, ubicación o nombre de servicio ("policia" encuentra las que piden Policía).

### 3. `components/solicitudes/TablaSolicitudes`
- Columnas: folio, solicitud + ubicación, usuario, servicios, estado, fecha. `caption` oculto explica la interacción.
- Clic en fila → `seleccionar` (centra mapa). Doble clic → `abrir`.
- Folio = `RouterLink`: acceso por teclado. Al enfocarlo también selecciona.
- Fila seleccionada: tinte de marca + barra izquierda.
- `table-responsive` para pantallas angostas.

### 4. `views/SolicitudesView.vue`
- Rejilla 7/5: tabla + mapa fijo (`sticky`) al hacer scroll. < 1200 px: una columna.
- Filtro de estado en la URL (`?estado=Pendiente`): enlaces desde la franja del dashboard funcionan; valor inválido se ignora.
- Orden: más recientes primero.
- Estados de la vista: skeleton → error con "Reintentar" → sin coincidencias con "Quitar filtros" → tabla.

### 5. `components/solicitudes/DetalleSolicitud`
- Descripción destacada (o aviso si no hay). `<dl>` con usuario + teléfono (`tel:`), ubicación + coordenadas, recibida, atendida por, servicios con nombre, motivo.

### 6. `components/solicitudes/AccionesSolicitud`
- Botones `v-for` desde `accionesPermitidas()`. `BotonCarga` con círculo en la acción en curso; las demás deshabilitadas.
- Nota contextual:
  - Estado final → "La solicitud está cerrada y ya no admite acciones."
  - Pendiente sin servicios → explica que solo avisa a contactos y que se rechaza solo por mal uso.
- Redacción neutra: "quien la creó", no "él".

### 7. `views/AtencionView.vue`
- Rejilla: volver · cabecera (folio, título `h1`, badge grande) · principal (acciones, detalle) · lateral (mapa de un punto, historial).
- Folio inexistente o no numérico (router ya filtra `\d+`) → `EstadoVacio` "No existe la solicitud #0099" + "Ver solicitudes".
- Acciones:

| Acción | Interfaz | Éxito |
|---|---|---|
| Enviar ayuda | `confirmar()` SweetAlert, texto con servicios y ubicación, carga dentro del diálogo | "Ayuda enviada" |
| Marcar como terminada | `confirmar()` | "Solicitud terminada" |
| Editar servicios | `ModalEditarServicios` | "Servicios actualizados" |
| Rechazar | `ModalRechazar` | "Solicitud rechazada [y strike registrado]" |

- Concurrencia: error 409 (otro admin ya la atendió) → mensaje en el diálogo + recarga de la solicitud.

### 8. Store `solicitudes`
- `cargarUna(id, { conservar })`: con `conservar` refresca sin vaciar (sin skeleton detrás del diálogo). También actualiza la fila en el listado.

## Verificación
- `npm run build` sin errores.
- Sin hex, sin `v-if` + `v-for` juntos, vistas sin `<table>` ni `<form>`.
- Reglas de transiciones y strikes: 23/23 OK (script de tarea 009, re-ejecutado).
- **Pendiente en navegador** (extensión sin conexión): flujo completo enviar → terminar, rechazar con strike, foco de modales. Tarea 016.

## Resultado
Commit: `feat(frontend): agrega listado de solicitudes y vista de atención`.
