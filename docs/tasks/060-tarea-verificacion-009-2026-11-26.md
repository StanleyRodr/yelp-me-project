# 060 — Tarea: Verificación del plan 009

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ⬜ Pendiente

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
