# 059 — Tarea: Domicilio, contraseña y tema (app)

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
Formulario de domicilio con pin, cambio de contraseña y selector de tema.

## Pasos

### 1. `ui/perfil/DomicilioActivity` (`activity_domicilio.xml`)
- Reutiliza los campos del formulario de dirección del diseño original: calle, número, colonia, CP (`digits:5`), referencia (opcional). Sin "etiqueta".
- Vista previa del mapa (`MapaOsm.soloLectura`) + botón "Marcar en el mapa" → `ElegirUbicacionActivity` (tarea 056) centrado en el pin actual.
- Guardar exige pin: si falta, error "Marca tu domicilio en el mapa.".
- `PUT /api/perfil/domicilio`; errores por campo.
- Menú: "Quitar domicilio" (confirmación) → `DELETE`.

### 2. `ui/perfil/PasswordActivity` (o diálogo)
- Actual, nueva, confirmación (`password_toggle`).
- `PUT /api/perfil/password` con `actual`, `nueva`, `nueva_confirmation`. 422 en `actual` → "La contraseña actual no es correcta." bajo el campo.
- OK → Snackbar "Contraseña actualizada." y cerrar.

### 3. Tema
- En `PerfilActivity`: fila "Tema" → diálogo de opción única (Claro, Oscuro, Sistema).
- Guarda `ym_tema` y aplica `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO | MODE_NIGHT_YES | MODE_NIGHT_FOLLOW_SYSTEM)`.

## Verificación
- María agrega domicilio con pin → aparece en su perfil y en el panel (tarea 055).
- Quitarlo → perfil muestra la invitación a agregarlo.
- Cambiar contraseña y volver a entrar.
- Tema oscuro persiste al cerrar la app.

## Resultado
Commit: `feat(mobile): agrega domicilio, cambio de contraseña y tema`.

Notas: `DomicilioActivity` (calle, número, colonia, CP, referencia; vista previa del mapa y "Marcar en el mapa"/"Mover el punto" con `ElegirUbicacionActivity`; sin pin → "Marca tu domicilio en el mapa."; "Quitar domicilio" con confirmación). `PasswordActivity` (actual, nueva, confirmación; 422 bajo su campo). El perfil recibe el resultado y muestra el Snackbar. **Tema:** sin diálogo Claro/Oscuro/Sistema; por decisión del 2026-11-27 solo claro y oscuro: botón en la tarjeta "Ajustes" que hace lo mismo que el menú lateral (`Tema.alternar`). Probado en emulador con María: CP de 4 dígitos y falta de pin, pin elegido y guardado (mapa en el perfil), contraseña actual incorrecta bajo el campo, cambio OK y login con la nueva (restaurada después a la de demo), tema oscuro persiste tras cerrar la app (mapa con filtro oscuro), quitar domicilio → invitación a agregarlo. 5 ciclos abrir/cerrar domicilio + selector sin avisos de StrictMode.
