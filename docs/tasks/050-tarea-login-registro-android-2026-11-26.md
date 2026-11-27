# 050 — Tarea: Login, registro y sesión (app)

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Entrada automática con el token guardado, login con correo o teléfono, registro completo y cierre de sesión contra la API.

## Pasos

### 1. `SplashActivity` (launcher)
- Splash Screen API (`Theme.YelpMe.Splash`, logo sobre `ym_marca`).
- Sin token → `LoginActivity`.
- Con token → `GET /api/sesion`: OK → `Sesion.actualizarUsuario` + `InicioActivity`; 401/403 → limpiar + login; sin conexión → `InicioActivity` con usuario guardado y aviso (Snackbar) "Sin conexión".

### 2. `LoginActivity` (`activity_login.xml`, ViewBinding)
- Campo "Correo o teléfono" (`etIdentificador`): si son solo dígitos → `telefono`, si no → `correo`.
- Contraseña con `endIconMode="password_toggle"`.
- Botón con indicador de carga (deshabilitado mientras espera).
- Errores: 401 "Correo, teléfono o contraseña incorrectos." bajo contraseña; 403 (admin/deshabilitada) y 429 en Snackbar con el mensaje de la API.
- Extra `expirada` → aviso "Tu sesión terminó. Inicia sesión de nuevo.".
- Enlaces: "Regístrate" → `RegistroActivity`; "¿Olvidaste tu contraseña?" → `RecuperarContrasenaActivity`.
- `fcm_token`: aún no (plan 012).

### 3. `RegistroActivity` (`activity_registro.xml`)
- Campos: nombre, apellido paterno, apellido materno (opcional), teléfono (10 dígitos, `inputType="phone"`), correo, contraseña, confirmación. Quitar `etDireccion`.
- `ScrollView` + `adjustResize` para que el teclado no tape.
- Validación local mínima (vacíos, 10 dígitos, confirmación igual); la real es de la API: cada `ErrorApi.campo()` en su `TextInputLayout.setError`.
- OK → `Sesion.guardar` y `InicioActivity`. Si `contactos_vinculados > 0`: Snackbar "Te vinculamos con N contactos.".

### 4. Cerrar sesión
- `util/CerrarSesion.ejecutar(Activity)`: `DELETE /api/sesion` (ignora error) → `Sesion.cerrar()` → `LoginActivity` con `CLEAR_TASK`.
- Confirmación con `MaterialAlertDialogBuilder` ("¿Cerrar sesión?").

### 5. Saludo en Inicio
`tvSaludoUsuario` = `getString(R.string.inicio_saludo, usuario.nombre)` ("¡Hola, %1$s!").

## Verificación
Backend con `php artisan serve`; emulador:
- Registro nuevo → entra; repetir teléfono → error bajo el campo.
- Login con `carlos@correo.com` y con `2351000001` (`Usuario123*`).
- Login con `admin@yelpme.com` → mensaje de cuenta de administrador.
- Cerrar app y abrir → entra directo. Borrar tokens en BD → abrir → login con aviso.
- Cerrar sesión → login; Atrás no regresa.

## Resultado
Commit: `feat(mobile): conecta login, registro y sesión a la API`.

Notas: probado en emulador (API 37) contra `php artisan serve`: login por teléfono con contraseña mala (error bajo el campo) y buena, entrada automática, tokens borrados → login con aviso, registro con teléfono repetido (error bajo el campo) y registro nuevo con vinculación. En este equipo el emulador no alcanzó `10.0.2.2:8000`; se usó `adb reverse tcp:8000 tcp:8000` + `api.url` en `local.properties`. Guarda contra doble envío (Enter + botón). `Margenes.bordeABorde` deja íconos claros sobre el header. El saludo y el menú con "Cerrar sesión" quedan en el header de `BaseActivity` (tarea 052).
