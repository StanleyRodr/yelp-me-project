# 071 — Tarea: Verificación del plan 011

> Plan: `011-plan-mobile-solicitudes-2026-11-26.md` · Rama: `feature/mobile-solicitudes` · Estado: ⬜ Pendiente

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
