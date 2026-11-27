# 055 — Tarea: Domicilio en el panel

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
El admin ve el domicilio del usuario en su detalle y en el detalle de cada solicitud.

## Pasos

### 1. Componente `components/comunes/TarjetaDomicilio.vue` (presentacional)
- Props: `domicilio` (objeto o `null`), `titulo` (default "Domicilio").
- Con domicilio: texto (`domicilio.texto`), referencia si existe, `MapaBase` pequeño (alto 180px) con un marcador (ícono `home`).
- Sin domicilio: `EstadoVacio` "No registró domicilio.".
- Solo tokens; funciona en tema oscuro (tarea 046).

### 2. `UsuarioDetalleView.vue`
Agregar `TarjetaDomicilio` en la rejilla junto a la cabecera / estado de cuenta.

### 3. Detalle de solicitud (`DetalleSolicitud.vue` / `AtencionView`)
- Debajo de los datos del solicitante: "Domicilio del solicitante" con `TarjetaDomicilio`.
- Si la solicitud se envió desde el domicilio (`ubicacion_nombre` empieza con "Domicilio:"), badge "Enviada desde su domicilio".

### 4. Perfil del admin
Sin domicilio (los admins no tienen).

## Verificación
- `npm run build`.
- Panel contra la API: detalle de Carlos con mapa; María "No registró domicilio."; solicitud de Carlos muestra su domicilio.

## Resultado
Commit: `feat(frontend): muestra el domicilio en usuarios y solicitudes`.

Notas: `TarjetaDomicilio` en el lateral del detalle de usuario y debajo del detalle en Atención. `MapaBase` acepta marcadores con `icono` propio (color de marca, sin estado). Las acciones del panel (`/estado`, `/servicios`) devuelven `PresentadorSolicitud::detalle` con `usuario.domicilio`: el store reemplaza la solicitud con la respuesta y sin esto el domicilio desaparecía tras actuar (prueba en `DomicilioTest`). `npm run build` OK. **Pendiente:** revisión visual en navegador (extensión de Chrome sin conexión).
