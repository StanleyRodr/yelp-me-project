# 059 — Tarea: Domicilio, contraseña y tema (app)

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ⬜ Pendiente

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
