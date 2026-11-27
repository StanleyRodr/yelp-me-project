# 045 — Tarea: Recuperar contraseña (panel)

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
"¿Olvidaste tu contraseña?" en el login del panel, con los 3 pasos de la API (tarea 044).

## Pasos

### 1. Service `services/recuperacionService.js`
```js
pedirCodigo(correo)                 // POST /recuperacion
verificar(correo, codigo)           // POST /recuperacion/verificacion
cambiar({ correo, codigo, password }) // PUT /recuperacion + password_confirmation
```
Mismo `pedir()` de `http.js`. El interceptor de 401 no aplica (rutas públicas; agregar `/recuperacion` a las excluidas si hace falta).

### 2. Ruta pública `/recuperar` → `views/RecuperarView.vue`
- Misma composición que `LoginView` (tarjeta centrada).
- Componentes en `components/recuperacion/`:
  - `PasoCorreo.vue`: campo correo + "Enviar código".
  - `PasoCodigo.vue`: 6 dígitos (`inputmode="numeric"`, `autocomplete="one-time-code"`), "Verificar", enlace "Enviar otro código".
  - `PasoPassword.vue`: nueva + confirmación (mismas reglas que `FormPassword`), "Guardar contraseña".
- Estado del paso en la vista (`ref`); `v-if` por paso.
- Al terminar: `router.push('/login?recuperada=1')`; `LoginView` muestra aviso "Contraseña actualizada. Inicia sesión.".
- Errores por campo con `ErrorApi.campo()`; 429 muestra el mensaje de la API.

### 3. Enlace en `FormLogin.vue`
"¿Olvidaste tu contraseña?" → `/recuperar`. Botón tipo enlace, foco visible.

### 4. Guard del router
`/recuperar` pública; con sesión activa redirige a `/`.

## Verificación
- `npm run build`.
- Con `php artisan serve` (MAIL_MAILER=log): pedir código para `admin@yelpme.com`, leerlo en `storage/logs/laravel.log`, cambiar contraseña, entrar. Restaurar `Admin123*` después (o `db:seed` tras recrear).
- Código malo → error bajo el campo.

## Resultado
Commit: `feat(frontend): agrega recuperación de contraseña al panel`.

Notas: `AvisoFormulario` para avisos/errores de los pasos. Si el código vence en el paso 3 se regresa al paso 2 con el error. `meta.soloSinSesion` en el router para `/login` y `/recuperar`.
