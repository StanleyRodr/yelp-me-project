# 013 — Tarea: Solicitudes y atención

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Listado con filtros y detalle por folio con acciones válidas según el estado.

## Pasos
1. `components/solicitudes/FiltrosSolicitudes`: chips por estado con conteo (`v-for`) + `CampoBusqueda`. Filtro sincronizado con `?estado=` en la URL.
2. `components/solicitudes/TablaSolicitudes`: folio, solicitud y ubicación, usuario, servicios, estado, fecha. Clic selecciona (centra mapa); Enter o botón abre detalle.
3. `views/SolicitudesView.vue`: filtros + tabla + `MapaBase`.
4. `components/solicitudes/DetalleSolicitud`: título, descripción, usuario, ubicación, servicios.
5. `components/solicitudes/AccionesSolicitud`: botones desde `accionesPermitidas()` (`v-for`). Sin acciones en estados finales.
   - Enviar → `confirmar()` SweetAlert. Terminar → `confirmar()`. Rechazar → `ModalRechazar`. Editar servicios → `ModalEditarServicios`.
6. `LineaTiempo` con eventos derivados de `created_at`, `enviado_en`, `cerrado_en`, admin y motivo.
7. `views/AtencionView.vue`: composición; folio inexistente → `EstadoVacio` con enlace a solicitudes.

## Verificación
- Cada estado muestra solo sus acciones (plan principal 7.2).
- Rechazar con strike suma strike al usuario y, al tercero, lo suspende.

## Resultado
Pendiente.
