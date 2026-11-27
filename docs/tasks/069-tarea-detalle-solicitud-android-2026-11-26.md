# 069 — Tarea: Detalle y acciones de la solicitud (app)

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ✅ Terminada

## Objetivo
Tarjeta de solicitud activa en Inicio, detalle completo y acciones del usuario: editar (Pendiente), cancelar con motivo y terminar.

## Pasos

### 1. `util/TransicionesSolicitud.java` (espejo del backend)
```java
// Espejo de backend/app/Support/TransicionesSolicitud.php (lado usuario). Cambiar ambos.
public static List<Accion> delUsuario(Solicitud s)
//  Con servicios:  Pendiente → EDITAR, CANCELAR · Enviado → TERMINAR, CANCELAR
//  Sin servicios:  Pendiente → EDITAR, TERMINAR, CANCELAR
//  Finales (Rechazado, Cancelado, Terminado): ninguna
```
Prueba unitaria `TransicionesSolicitudTest` con los 5 estados × con/sin servicios.

### 2. `ui/comunes/BadgeEstado`
`Chip` no clicable o `TextView` con fondo redondeado: color `ym_estado_<x>_fondo`, texto `ym_estado_<x>_texto`, ícono por estado (mismo que la web). Textos `R.string.estado_pendiente`… ("Pendiente", "Enviado", "Rechazado", "Cancelado", "Terminado").

### 3. Tarjeta activa en Inicio (`cardTuSolicitud`)
- `listar(null)` → primera con estado Pendiente o Enviado.
- Muestra badge, título, servicios (íconos), "Hace 5 min", mensaje por estado: Pendiente "Esperando a que un administrador la atienda.", Enviado "La ayuda va en camino.".
- Tocar → `DetalleSolicitudActivity`.
- Recarga en `onResume` y cada 15 s mientras la activity está en primer plano (`Handler` + `Runnable` anónimo, se detiene en `onPause`). Sin skeleton en recargas.

### 4. `ui/solicitudes/DetalleSolicitudActivity` (`activity_detalle_solicitud.xml`, estilo del detalle del historial del diseño)
- Badge, título, descripción, lugar, mapa solo lectura, servicios con ícono y nombre, fechas (creada, enviada, cerrada), "Atendida por <admin>" si hay, motivo si Rechazado/Cancelado ("Motivo: …").
- Botones según `TransicionesSolicitud.delUsuario`:
  - **Editar** → `FormSolicitudActivity` (mismo formulario que Inicio, precargado; `PUT /api/solicitudes/{id}`; puede cambiar ubicación y servicios).
  - **Cancelar** → diálogo con campo de motivo obligatorio (`dialog_motivo.xml`, `TextInputLayout`, error si vacío) → `PUT /estado {estado: "Cancelado", motivo}`.
  - **Terminar** → confirmación "¿La emergencia terminó?" → `PUT /estado {estado: "Terminado"}`.
- 409 (el admin cambió el estado antes) → Snackbar con el mensaje de la API y recargar.
- Recarga cada 15 s (mismo patrón que el paso 3).

### 5. Borrar del diseño
Ciclo de estado al tocar el banner, colores de fondo por estado (`#FF3D00`…), `SharedPreferences SolicitudActiva`.

## Verificación (con el panel abierto en otra ventana)
- Crear → Pendiente; admin envía → la app muestra Enviado en ≤ 15 s; usuario termina → panel la ve Terminado.
- Admin rechaza con motivo → la app muestra Rechazado y el motivo; sin botones.
- Editar en Pendiente OK; en Enviado no aparece el botón.
- Cancelar sin motivo → error bajo el campo.
- Sin servicios: Pendiente → Terminar disponible.

## Resultado
Commit: `feat(mobile): agrega detalle y acciones de la solicitud`.

Notas: `util/TransicionesSolicitud` (espejo del lado usuario del backend) con `TransicionesSolicitudTest` (5 estados × con/sin servicios). `ui/comunes/BadgeEstado` (color, fondo e ícono por estado como `EstadoBadge` del panel; estilo `YelpMe.Badge`). Los campos del formulario se extrajeron a `layout_form_solicitud` + `ui/inicio/FormularioSolicitud` (ubicación con respaldo, servicios, suspensión), que usan Inicio y `FormSolicitudActivity` (editar; al editar, un suspendido puede quitar servicios pero no agregar). Inicio muestra arriba la solicitud activa (Pendiente o Enviado) y la recarga cada 15 s mientras está visible ("Hace N min", en español aunque el teléfono esté en otro idioma). `DetalleSolicitudActivity`: badge, mensaje por estado, motivo, datos, mapa, fechas y quién atendió; botones según las transiciones; cancelar pide motivo (no cierra el diálogo si está vacío); 409 → aviso y recarga; se recarga cada 15 s. Se quitaron del diseño el ciclo de estados al tocar y los colores inventados. Probado con el panel en Chrome: #14 sin servicios → Editar/Terminar/Cancelar, cancelar sin motivo da error, terminar → Terminado sin botones; #12 enviada desde el panel → la app la muestra Enviado (Terminar/Cancelar, sin Editar) y el usuario la termina → el panel la ve Terminado al recargar; #15 editada en Pendiente (título y comentario; servicios intactos); rechazada en el panel con motivo → la app muestra Rechazado y "Motivo: …" sola en ≤ 15 s. Cancelar con motivo usa el mismo camino que terminar (`PUT /estado`); su envío exitoso se prueba en la tarea 071. Observación del panel (fuera de este plan): el detalle de Atención no se recarga solo, solo las listas.
