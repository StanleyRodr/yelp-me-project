# 053 — Tarea: Verificación del plan 008

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ⬜ Pendiente

## Objetivo
Probar el plan completo, actualizar documentación y abrir el PR.

## Pasos

### 1. Automáticas
```bash
cd backend && php artisan bd:pruebas && php artisan test
cd frontend && npm run build
cd mobile && ./gradlew testDebugUnitTest assembleDebug assembleRelease
```

### 2. Revisión de reglas Android
```bash
grep -rn "findViewById" mobile/app/src/main/java            # vacío
grep -rnE '#[0-9A-Fa-f]{6}' mobile/app/src/main/res/layout  # vacío
grep -rn 'android:text="[^@]' mobile/app/src/main/res/layout # vacío
```

### 3. Recorrido manual (emulador API 35+, `php artisan serve`)
| Caso | Esperado |
|---|---|
| Registro con materno vacío | Entra; panel muestra el usuario sin espacio extra |
| Login correo / teléfono | Entra |
| Abrir app con token | Entra sin login |
| Recuperar contraseña (app y panel) | Código en log, cambio OK, sesiones previas cerradas |
| Tema oscuro (sistema y selector web) | Legible en todas las pantallas tocadas |
| Barra: servicio | Diálogo simulado |
| Barra: favorito | Lleva a Contactos |
| Cerrar sesión | Login, sin volver con Atrás |

### 4. Documentación
- `CLAUDE.md`: tabla de estado (mobile "Base conectada a la API (plan 008)"); sección Mobile con estructura de paquetes, `BaseActivity`, `ClienteApi`/`Llamada`, `BuildConfig.API_URL`, `adb reverse`, espejo `colors.xml` ↔ `tokens.css`; backend con recuperación y apellidos.
- `principal-plan.md` sección 11 y 15.3 (✅ Plan 008).
- Plan 008: tareas ✅ y criterios `[x]`.

### 5. PR
Título "Plan 008: base mobile y ajustes transversales", cuerpo con resumen, tabla de tareas y commits, cómo probar (incluye recrear `YelpMe` por el cambio de apellidos). Sin atribución.

## Resultado
Commit: `docs(mobile): documenta la base mobile y cierra el plan 008`.
