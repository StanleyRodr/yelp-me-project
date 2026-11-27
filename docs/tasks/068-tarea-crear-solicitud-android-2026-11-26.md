# 068 — Tarea: Crear solicitud (app)

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ⬜ Pendiente

## Objetivo
Formulario de Inicio conectado a `POST /api/solicitudes` con servicios del catálogo.

## Pasos

### 1. `api/ServicioSolicitudes`
```java
@GET("solicitudes")              Call<Respuesta<List<Solicitud>>> listar(@Query("estado") String estado); // null = todas
@GET("solicitudes/{id}")         Call<Respuesta<Solicitud>> obtener(@Path("id") long id);
@POST("solicitudes")             Call<Respuesta<Solicitud>> crear(@Body NuevaSolicitud s);
@PUT("solicitudes/{id}")         Call<Respuesta<Solicitud>> actualizar(@Path("id") long id, @Body NuevaSolicitud s);
@PUT("solicitudes/{id}/estado")  Call<Respuesta<Solicitud>> cambiarEstado(@Path("id") long id, @Body Map<String, String> cuerpo);
```
Modelos: `Solicitud` (id, titulo, descripcion, latitud, longitud, ubicacionNombre, estado, motivo, servicios `List<Long>`, admin `Persona`, cerradoPor `Persona`, enviadoEn, cerradoEn, createdAt), `NuevaSolicitud` (titulo, descripcion, latitud, longitud, ubicacionNombre, servicios), `Persona` (id, nombre, apellidoPaterno, apellidoMaterno).

### 2. Layout `activity_inicio.xml` (estructura del diseño)
- Saludo + tarjeta de solicitud activa (tarea 069, oculta si no hay).
- Tarjeta "Pedir ayuda":
  - "¿Qué pasa?" (`etEmergencia` → `titulo`, obligatorio, máx. 150).
  - "¿Dónde?" (`etLugar` → `ubicacion_nombre`, opcional) + fila con ícono y texto del origen ("Tu ubicación actual" / "Tu domicilio" / "Punto en el mapa") + botones "Usar mi domicilio" y "Cambiar en el mapa". Mini mapa solo lectura con el punto.
  - "¿Qué servicios necesitas?" `ChipGroup cgServicios` (`Widget.Material3.Chip.Filter`, ícono del servicio, `checkedIconVisible`). Texto de ayuda "Si no eliges ninguno, solo avisaremos a tus contactos.".
  - "Comentarios" (`etComentarios` → `descripcion`, opcional, multilínea).
  - Botones "Limpiar" y "Pedir ayuda".
- Fuera: los 6 `ImageButton` con PNG.

### 3. Lógica de `InicioActivity`
- Al abrir: ubicación en segundo plano (`Ubicacion.obtener`, tarea 067) para tenerla lista; chips desde `Catalogo.servicios()`.
- `Sesion.usuario().suspendido` → chips deshabilitados + banner "Tu cuenta está suspendida: solo puedes avisar a tus contactos.".
- "Pedir ayuda":
  1. Valida título y ubicación (si falta → `DialogoSinUbicacion`).
  2. Muestra `dialog_resumen.xml` (ya en el diseño): título, lugar, servicios con íconos (o "Solo tus contactos"), mini mapa, "Estado: Pendiente". Botones "Enviar" / "Corregir".
  3. `POST` → Snackbar "Solicitud enviada." y la tarjeta activa aparece (tarea 069); limpiar formulario.
- Errores: 422 por campo; 403 de suspensión → Snackbar con el mensaje y refrescar usuario (`GET /sesion`) para deshabilitar chips.
- Doble toque: botón deshabilitado mientras envía.

## Verificación
- Carlos con GPS y Policía + Ambulancia → aparece en el dashboard del panel con su ubicación.
- María sin servicios → panel la muestra como "solo contactos".
- Luis (suspendido) → chips deshabilitados; envía sin servicios.
- Sin GPS → usar domicilio → `ubicacion_nombre` "Domicilio: …" y badge en el panel (tarea 055).

## Resultado
Commit: `feat(mobile): crea solicitudes con ubicación y servicios`.
