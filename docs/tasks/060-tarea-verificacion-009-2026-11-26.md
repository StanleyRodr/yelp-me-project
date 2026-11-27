# 060 — Tarea: Verificación del plan 009

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
Probar el plan, documentar y abrir el PR.

## Pasos

### 1. Automáticas
```bash
cd backend && php artisan bd:pruebas && php artisan test
cd frontend && npm run build
cd mobile && ./gradlew testDebugUnitTest assembleDebug
```

### 2. Recorrido (emulador + panel)
| Caso | Esperado |
|---|---|
| Perfil de Luis | Suspendido, "Suspendido 2 veces", banner |
| Editar datos / foto | Se ve en app y panel |
| Domicilio nuevo, editar, quitar | App y panel coinciden |
| Contraseña | Cambia; la anterior ya no entra |
| Tema | Claro, oscuro y sistema |
| Mapa en scroll | Se arrastra sin mover la pantalla |

### 3. Documentación
- `CLAUDE.md`: endpoints de domicilio; Mobile: `MapaOsm`, `ElegirUbicacionActivity`, compresión de foto.
- `principal-plan.md`: estado y "✅ Plan 009".
- Plan 009: tareas ✅, criterios `[x]`.

### 4. PR
"Plan 009: perfil y domicilio". Cómo probar: recrear `YelpMe` (tabla nueva) o ejecutar solo el `CREATE TABLE domicilios` + `db:seed`.

## Resultado
Commit: `docs(perfil): documenta perfil y domicilio y cierra el plan 009`.

Notas: `php artisan test` 93 OK; `npm run build` OK; `./gradlew testDebugUnitTest assembleDebug assembleRelease` OK; sin `findViewById`, colores hex ni textos fijos en layouts. Recorrido en emulador (API local con `adb reverse`): Carlos con domicilio y mapa; Luis suspendido ("Suspendido 2 veces", banner, 3 de 3) en oscuro; editar datos (teléfono ajeno con error bajo el campo, materno vacío), foto de 7.8 MB subida comprimida y quitada; María agrega, ve y quita domicilio con pin; contraseña cambiada (la nueva entra; tras restaurar la de demo, la otra ya no entra); tema oscuro persiste. Los mapas del perfil y del formulario son de solo lectura (no hay arrastre dentro del scroll); el arrastre es en `ElegirUbicacionActivity` a pantalla completa. **Pendiente:** revisión visual del panel en navegador (extensión de Chrome sin conexión). Datos de demo restaurados (materno de Luis, contraseña y domicilio de María).
