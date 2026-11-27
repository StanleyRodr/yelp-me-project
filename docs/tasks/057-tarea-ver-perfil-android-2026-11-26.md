# 057 — Tarea: Ver perfil (app)

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ⬜ Pendiente

## Objetivo
`PerfilActivity` con datos reales de la API, conservando la estructura del diseño.

## Pasos

### 1. `api/ServicioPerfil`
```java
@GET("sesion")              Call<Respuesta<RespuestaUsuario>> sesion();
@PUT("perfil")              Call<Respuesta<Usuario>> actualizar(@Body Map<String, String> datos);
@Multipart @POST("perfil/foto") Call<Respuesta<Usuario>> subirFoto(@Part MultipartBody.Part foto);
@DELETE("perfil/foto")      Call<Respuesta<Usuario>> quitarFoto();
@PUT("perfil/password")     Call<Respuesta<Void>> cambiarPassword(@Body Map<String, String> datos);
@GET("perfil/domicilio")    Call<Respuesta<Domicilio>> domicilio();
@PUT("perfil/domicilio")    Call<Respuesta<Domicilio>> guardarDomicilio(@Body Domicilio d);
@DELETE("perfil/domicilio") Call<Respuesta<Void>> quitarDomicilio();
```
Modelo `Domicilio` (calle, numero, colonia, codigoPostal, referencia, latitud, longitud, texto). `Usuario` agrega `vecesSuspendido`.

### 2. Layout `activity_perfil.xml` (estructura del diseño)
- Tarjeta superior: foto (`ivFotoPerfil`, 96dp, `contentDescription`), nombre corto, nombre completo, teléfono, correo. `tvPerfilFolio` → "Cuenta #<id>".
- Tarjeta "Mi cuenta": strikes con `LinearProgressIndicator` (`strikes_activos / limite_strikes`), texto "1 de 3 strikes", "Suspendido N veces"; si `suspendido`: banner `ym_estado_pendiente_fondo` "Tu cuenta está suspendida: solo puedes avisar a tus contactos.".
- Tarjeta "Domicilio": texto + mini mapa (`MapaOsm.soloLectura(true)`); sin domicilio: "Agrega tu domicilio para usarlo si el GPS falla." + botón "Agregar domicilio".
- Botones: "Editar datos", "Cambiar contraseña", "Tema" (tareas 058–059).
- `SwipeRefreshLayout` para recargar.

### 3. Lógica
- `onResume` → `GET /sesion` + `GET /perfil/domicilio` (en paralelo, cada uno con su `Llamada`); actualiza `Sesion.actualizarUsuario`.
- Foto con Glide: `Glide.with(this).load(url).circleCrop().placeholder(R.drawable.bg_avatar_iniciales).into(…)`; sin foto → iniciales (`nombre[0] + paterno[0]`) en `tvAvatarInicial`.
- Errores en Snackbar con "Reintentar".

## Verificación
Emulador: Carlos (con domicilio), María (sin), Luis (suspendido, 2 veces), Ana (2/3 strikes). Tema oscuro legible.

## Resultado
Commit: `feat(mobile): muestra el perfil con strikes y domicilio`.
