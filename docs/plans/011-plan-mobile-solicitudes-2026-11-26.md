# 011 — Plan: Solicitudes en Android

> Fecha: 2026-11-26 · Rama: `feature/mobile-solicitudes` (desde `main`, tras fusionar el plan 010)
> Referencia: `docs/plans/principal-plan.md` (secciones 4.1, 7, 7.5, 8) · plan 005 (API de solicitudes)

## Objetivo

El usuario crea solicitudes de ayuda con su ubicación GPS (o su domicilio si el GPS falla) y los servicios que necesita, sigue su estado, la edita mientras está `Pendiente`, la cancela con motivo o la da por terminada, y consulta su historial. El admin la ve en el mapa del panel.

**No incluye:** notificaciones push (plan 012). Mientras tanto, la app recarga la solicitud al volver a la pantalla y cada 15 s mientras está visible.

## Punto de partida
- API del plan 005: `apiResource /api/solicitudes` (sin destroy), `PUT /api/solicitudes/{id}/estado`, `GET /api/servicios`. Suspendido con servicios → 403.
- Domicilio (plan 009), `MapaOsm` y `ElegirUbicacionActivity` (tarea 056), barra y catálogo (plan 008).
- App: `InicioActivity` (antes `SolicitudesActivity`) con formulario del diseño: emergencia, lugar, mapa fijo, 6 `ImageButton` con PNG y alpha, modal de resumen, tarjeta "Tu solicitud" con estados inventados (SIN ATENDER, EN PROCESO, ATENDIDA) que ciclan al tocar. `HistorialActivity` con datos fijos y `Spinner`.

## Decisiones
- **Mapeo de campos:** "¿Qué pasa?" → `titulo`, "Lugar" → `ubicacion_nombre`, "Comentarios" → `descripcion`, coordenadas del GPS → `latitud`/`longitud`, servicios elegidos → `servicios` (ids).
- **Servicios:** `ChipGroup` con chips filtrables (ícono Material Symbols + nombre) desde `GET /api/servicios`; selección con check visible (no solo alpha). "Sin servicios" es válido: se explica "Solo avisaremos a tus contactos.". Se quitan "Cruz Roja" y "Contacto" del diseño.
- **Suspendido:** chips deshabilitados + banner con el mensaje de la API; puede enviar sin servicios.
- **Ubicación:** `util/Ubicacion` con `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY)` y tiempo límite 10 s; respaldo según 7.5.
- **Estados:** los 5 reales, con badge (color + texto) igual que la web; se elimina el cambio de color de toda la pantalla y el ciclo al tocar.
- **Transiciones del usuario** en `util/TransicionesSolicitud.java`: espejo de `backend/app/Support/TransicionesSolicitud.php` (y de `frontend/src/constantes/transiciones.js` para el admin). Solo deciden qué botones mostrar; la API valida.
- **Una "solicitud activa"** en Inicio: la más reciente en `Pendiente` o `Enviado`. Se puede crear otra aunque haya una activa (la API lo permite); Inicio muestra la activa arriba y el formulario abajo.
- **Historial** sin paginación (la API devuelve la lista completa; volumen del MVP pequeño). Filtro por estado con chips.

---

## Tareas

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 067 | [Ubicación con respaldo](../tasks/067-tarea-ubicacion-android-2026-11-26.md) | GPS, permisos, diálogo con domicilio / reintentar / mapa. | ✅ |
| 068 | [Crear solicitud](../tasks/068-tarea-crear-solicitud-android-2026-11-26.md) | Formulario, servicios del catálogo, suspensión, resumen y envío. | ✅ |
| 069 | [Detalle y acciones](../tasks/069-tarea-detalle-solicitud-android-2026-11-26.md) | Solicitud activa, detalle, editar, cancelar con motivo, terminar. | ✅ |
| 070 | [Historial](../tasks/070-tarea-historial-android-2026-11-26.md) | Lista con filtro por los 5 estados y detalle. | ✅ |
| 071 | [Verificación del plan 011](../tasks/071-tarea-verificacion-011-2026-11-26.md) | Criterios 5–7 y 10 del MVP con el panel; documentación. | ⬜ |

---

## Criterios de terminado

- [ ] Crear solicitud con GPS y servicios; aparece en el mapa del dashboard en ≤ 15 s.
- [ ] Sin GPS: usar domicilio, reintentar o marcar en el mapa.
- [ ] Editar solo en `Pendiente`; cancelar con motivo; terminar según transiciones.
- [ ] Cambios del admin (Enviado, Rechazado con motivo, Terminado) se ven en la app.
- [ ] Usuario suspendido no puede elegir servicios; sí avisar a contactos.
- [ ] Historial con los 5 estados.
- [ ] `./gradlew assembleDebug` sin errores; commits por tarea, sin atribución; PR a `main`.
