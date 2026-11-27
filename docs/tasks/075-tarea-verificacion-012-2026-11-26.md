# 075 — Tarea: Verificación del plan 012

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ✅ Terminada

## Objetivo
Demostrar el criterio 8 del MVP y cerrar el plan.

## Pasos

### 1. Automáticas
```bash
cd backend && php artisan test
cd mobile && ./gradlew testDebugUnitTest assembleDebug
```

### 2. Recorrido con dos dispositivos
| Caso | Esperado |
|---|---|
| Carlos crea solicitud | Push al contacto vinculado; ninguno al no vinculado |
| Contacto deshabilitado por el admin | No recibe |
| Admin: Enviado / Rechazado / Terminado | Push al solicitante con el texto correcto |
| Usuario cancela | Sin push |
| Cerrar sesión | Ya no recibe (token borrado) |
| Backend sin `credenciales.json` | Todo funciona; log con aviso |

### 3. Secretos
```bash
git ls-files | grep -iE "credenciales|google-services"   # vacío
```

### 4. Documentación
- `CLAUDE.md`: estado backend "API completa del MVP con FCM"; `Notificador`/`Avisos`; Mobile: `ServicioMensajes`, canales, `google-services.json` fuera de git.
- `principal-plan.md`: "✅ Plan 012", estado.
- Plan 012: tareas ✅, criterios `[x]`.

### 5. PR
"Plan 012: notificaciones push con FCM". Cómo probar: pasos de Firebase de la tarea 072.

## Resultado
Commit: `docs(notificaciones): documenta FCM y cierra el plan 012`.

Notas: `php artisan test` 108 OK; `npm run build` OK; `./gradlew testDebugUnitTest assembleDebug assembleRelease` OK, y `assembleDebug` también sin `google-services.json` (`FCM_ACTIVO = false`). `git ls-files` no tiene `credenciales.json` ni `google-services.json`. Con dos emuladores y Firebase real (tarea 074): alerta al contacto vinculado (María) y avisos de Rechazado y Enviado al solicitante (Carlos), con apertura de la pantalla correcta al tocar. Los demás casos del recorrido están cubiertos por pruebas automáticas con `NotificadorFalso`: no vinculado y deshabilitado sin aviso, Terminado al solicitante, cancelar del usuario sin aviso, 409 sin aviso, notificador caído sin afectar la respuesta, sin credenciales → `NotificadorNulo`; cerrar sesión borra el token (plan 003). El caso "app cerrada" no quedó probado con rigor.
