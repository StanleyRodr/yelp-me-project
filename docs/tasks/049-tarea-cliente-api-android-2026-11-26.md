# 049 — Tarea: Cliente API Android

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Retrofit con token Bearer, errores uniformes (como `ErrorApi` de la web), 401 global y sesión guardada. Base de todas las pantallas.

## Pasos

### 1. Paquete `com.yelpme.app.api`
| Clase | Rol |
|---|---|
| `Respuesta<T>` | `{ boolean ok; String mensaje; T datos; Map<String, List<String>> errores; }` (formato de la API). |
| `ErrorApi` | `extends Exception`: `codigo` (HTTP, 0 sin conexión), `mensaje`, `errores`; `campo(String)` → primer mensaje o `null`. Sin conexión: `R.string.error_sin_conexion` "No se pudo conectar con el servidor. Revisa tu conexión.". |
| `ClienteApi` | Singleton `Retrofit` con `BuildConfig.API_URL`, Gson, OkHttp (timeout 15 s, `HttpLoggingInterceptor` solo en debug). |
| `InterceptorToken` | Agrega `Authorization: Bearer <token>` y `Accept: application/json`. |
| `InterceptorSesion` | Si responde 401 y la ruta no es `login`/`registro`/`recuperacion`: borra sesión y lanza `LoginActivity` con `FLAG_ACTIVITY_NEW_TASK | CLEAR_TASK` + extra `expirada=true`. |
| `Llamada<T>` | Envuelve `Call<Respuesta<T>>`: `enqueue(Callback<T>)` con `alExito(T)` y `alError(ErrorApi)` en hilo principal; parsea el cuerpo de error con Gson. Uso con clases internas anónimas. |
| `ServicioAuth` (interfaz Retrofit) | `POST registro`, `POST login`, `GET sesion`, `DELETE sesion`, `POST/PUT recuperacion`, `POST recuperacion/verificacion`. |
| `ServicioCatalogo` | `GET servicios`. |
Las demás interfaces (`ServicioPerfil`, `ServicioContactos`, `ServicioSolicitudes`) llegan en sus planes.

### 2. Modelos (`com.yelpme.app.modelos`)
`Usuario` (id, nombre, apellidoPaterno, apellidoMaterno, telefono, correo, fotoPerfil, suspendido, strikesActivos, limiteStrikes) con `@SerializedName` en snake_case; `nombreCompleto()`. `Servicio` (id, nombre, icono, color). `RespuestaSesion` (token, usuario).

### 3. `util/Sesion`
- `SharedPreferences` `ym_sesion`: `token`, `usuario` (JSON).
- `guardar(token, usuario)`, `token()`, `usuario()`, `actualizarUsuario(u)`, `cerrar()`, `activa()`.
- Constructor con `Context` (aplicación). Sin datos por defecto.

### 4. `YelpMeApp extends Application`
- Inicializa `ClienteApi`, `Sesion` y el tema guardado (`AppCompatDelegate.setDefaultNightMode`, preferencia `ym_tema`: `claro`/`oscuro`/`sistema`).
- Declarar en manifest `android:name=".YelpMeApp"`.

### 5. Prueba unitaria
`app/src/test/.../ErrorApiTest.java`: parsea `{"ok":false,"mensaje":"Los datos no son válidos.","errores":{"telefono":["El teléfono ya está registrado."]}}` → `campo("telefono")`. Borrar `ExampleUnitTest`.

## Verificación
```bash
./gradlew testDebugUnitTest assembleDebug
```
Logcat en debug muestra peticiones con el header Bearer.

## Resultado
Commit: `feat(mobile): agrega cliente Retrofit con sesión y errores uniformes`.

Notas: `Llamada.Respuestas<T>` (clase abstracta para usar con clases internas anónimas) entrega `datos` o `ErrorApi` en el hilo principal. Logging en debug solo de encabezados (el cuerpo llevaría contraseñas). `DELETE sesion` recibe el token explícito para no disparar el 401 global. Tema en `util/Tema` (`ym_ajustes`, no se borra al cerrar sesión).
