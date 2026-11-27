# 069 — Tarea: Detalle y acciones de la solicitud (app)

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ⬜ Pendiente

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
