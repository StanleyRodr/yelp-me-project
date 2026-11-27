# 071 — Tarea: Verificación del plan 011

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ✅ Terminada

## Objetivo
Demostrar los criterios 5, 6, 7 y 10 del MVP con app y panel, y cerrar el plan.

## Pasos

### 1. Automáticas
```bash
cd backend && php artisan test
cd frontend && npm run build
cd mobile && ./gradlew testDebugUnitTest assembleDebug
```

### 2. Recorrido app + panel (`php artisan serve` + `npm run dev` + emulador)
| Criterio | Caso | Esperado |
|---|---|---|
| 5 | Carlos crea con GPS + Ambulancia | Marcador en el dashboard |
| 6 | Admin agrega Policía, envía y termina | App muestra cada estado; historial con admin |
| 7 | Ana: admin rechaza con strike → 3 strikes | App bloquea servicios; admin rehabilita → se desbloquean |
| 10 | Sin GPS → domicilio | Panel muestra "Enviada desde su domicilio" |
| — | Usuario cancela con motivo | Panel ve Cancelado y motivo |
| — | Solicitud sin servicios → terminar | Terminado en ambos |
| — | Propiedad: token de Carlos pide solicitud de María | 404 |

### 3. Documentación
- `CLAUDE.md`: espejo de transiciones en tres lados (backend, `frontend/src/constantes/transiciones.js`, `mobile/.../util/TransicionesSolicitud.java`); Mobile: `Ubicacion`, recarga cada 15 s.
- `principal-plan.md`: "✅ Plan 011", estado mobile.
- Plan 011: tareas ✅, criterios `[x]`.

### 4. PR
"Plan 011: solicitudes en Android".

## Resultado
Commit: `docs(solicitudes): documenta solicitudes en Android y cierra el plan 011`.

Notas: `php artisan test` 97 OK; `npm run build` OK; `./gradlew testDebugUnitTest assembleDebug assembleRelease` OK; sin `findViewById`, hex ni textos fijos en layouts. Recorrido con el emulador y el panel en Chrome (API local):
- Criterio 5: las solicitudes creadas en la app aparecen en el dashboard ("Por atender" y mapa) sin recargar.
- Criterio 6: #16 con Ambulancia → el admin agrega Policía, envía y termina → la app pasa a Enviado y a Terminado sola (≤ 15 s) con "Atendida por Administrador General" y las tres fechas.
- Criterio 7: #17 de Ana rechazada con strike → 3 strikes y suspensión → la app muestra el aviso y bloquea servicios; el admin la rehabilita → al volver a la app desaparece el aviso (se agregó el refresco del usuario en `onResume` de Inicio).
- Criterio 10: #14 creada sin GPS con el domicilio → el panel muestra "Enviada desde su domicilio" y la tarjeta del domicilio con mapa (revisión visual pendiente del plan 009 hecha).
- Cancelar con motivo (#18, Ana) → el panel la ve Cancelado con "Motivo: Ya encontre el camino". Sin servicios → terminar (#14) en ambos lados.
- Propiedad: con el token de Carlos, `GET` y `PUT /estado` de una solicitud de María → 404.
El detalle del panel no se recarga solo (solo las listas): al cambiar el estado desde la app hay que recargar esa página; queda anotado para el panel.
