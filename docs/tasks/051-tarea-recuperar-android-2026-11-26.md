# 051 — Tarea: Recuperar contraseña (app)

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ⬜ Pendiente

## Objetivo
`RecuperarContrasenaActivity` con los 3 pasos del diseño conectados a la API (tarea 044).

## Pasos

### 1. Layout `activity_recuperar_contrasena.xml`
- Conservar las 3 secciones del diseño (correo, código, nueva contraseña) como 3 contenedores; solo uno visible (`setVisibility`).
- `TextInputLayout` en todos; código con `inputType="number"`, `maxLength=6`, `autofillHints="smsOTPCode"` no aplica (es correo) → solo numérico.
- Botones: "Enviar código", "Verificar código", "Guardar contraseña"; enlace "Enviar otro código".
- Botón atrás (toolbar) vuelve al paso anterior; en el paso 1 cierra.

### 2. `ServicioAuth`
```java
@POST("recuperacion") Call<Respuesta<Void>> pedirCodigo(@Body Map<String, String> cuerpo);
@POST("recuperacion/verificacion") Call<Respuesta<Map<String, Boolean>>> verificar(@Body Map<String, String> cuerpo);
@PUT("recuperacion") Call<Respuesta<Void>> cambiar(@Body Map<String, String> cuerpo);
```

### 3. Lógica
- Paso 1 → mensaje neutro de la API en Snackbar; pasa al paso 2 aunque el correo no exista (no revela nada).
- Paso 2 → 422 bajo el campo código.
- Paso 3 → envía `correo`, `codigo`, `password`, `password_confirmation`; OK → `LoginActivity` con aviso "Contraseña actualizada. Inicia sesión.".
- Estado (paso, correo, código) en `onSaveInstanceState` para sobrevivir a la rotación.

## Verificación
Emulador + `MAIL_MAILER=log`: pedir código para `maria@correo.com`, leer en `storage/logs/laravel.log`, cambiar, entrar con la nueva. Código malo 5 veces → pide uno nuevo.

## Resultado
Commit: `feat(mobile): conecta recuperación de contraseña`.
