# 042 — Tarea: Importar proyecto Android

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ⬜ Pendiente

## Objetivo
Proyecto Android de la otra persona a git **tal como llegó**, sin datos personales ni archivos de máquina. Base para ver en el historial qué se cambió después.

## Pasos

### 1. Rama
```bash
git checkout main && git pull
git checkout -b feature/mobile-base
```

### 2. Quitar datos personales (solo eso; nada de refactor)
| Archivo | Cambio |
|---|---|
| `app/src/main/java/com/yelpme/app/SesionManager.java` | Valores por defecto `"Kimberly"`, `"Kimberly Heredia García"`, correo real → `""`. |
| `app/src/main/java/com/yelpme/app/MainActivity.java` | Quitar el `if` que compara con el correo real; dejar solo la navegación. |
| `app/src/main/res/layout/activity_perfil.xml` | Nombre, teléfono, correo y dirección reales → texto de ejemplo ("Nombre Apellido", "2351000000", "correo@ejemplo.com", "Calle 1, Centro"). |

Buscar restos:
```bash
grep -rniE "kimber|heredia|3616|gmail" mobile/app/src
```
Debe salir vacío.

### 3. `.gitignore` de `mobile/`
Agregar:
```
/.idea/deploymentTargetSelector.xml
/.idea/deviceManager.xml
/.idea/misc.xml
/app/release/
keystore.properties
*.jks
*.keystore
```
Comprobar:
```bash
git check-ignore -v mobile/local.properties mobile/build mobile/app/build mobile/.idea/deviceManager.xml
```
Todos deben aparecer ignorados. `gradle/wrapper/gradle-wrapper.jar` **sí** se versiona.

### 4. Quitar `mobile/.gitkeep`
Ya no hace falta: `git rm mobile/.gitkeep`.

### 5. Compila igual que antes
```bash
cd mobile && ./gradlew assembleDebug
```
(Windows: `gradlew.bat assembleDebug`.) Sin errores; no se cambia lógica.

### 6. Revisar lo que entra
```bash
git add mobile && git status --short mobile | head -80
```
Nada de `build/`, `local.properties` ni `.idea/workspace.xml`.

## Verificación
- `grep` del paso 2 vacío.
- `assembleDebug` OK.

## Resultado
Commit: `chore(mobile): agrega proyecto Android base del diseño`.
