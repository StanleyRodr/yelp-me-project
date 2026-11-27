# 074 — Tarea: FCM en Android

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ✅ Terminada

## Objetivo
Recibir y mostrar las notificaciones; mandar el token al backend.

## Pasos

### 1. Configuración
- Copiar `google-services.json` (tarea 072) a `mobile/app/`. Agregar `app/google-services.json` a `mobile/.gitignore`.
- Catálogo: plugin `com.google.gms.google-services`, `firebase-bom`, `firebase-messaging`.
- `app/build.gradle.kts`: `alias(libs.plugins.google.services)`; `implementation(platform(libs.firebase.bom))`, `implementation(libs.firebase.messaging)`.
- `mobile/README.md`: cómo descargar el archivo desde la consola de Firebase.
- Emulador con imagen **Google Play** (no AOSP).

### 2. Token
- `util/TokenFcm.obtener(Consumer<String>)` con `FirebaseMessaging.getInstance().getToken()`.
- Login y registro (tarea 050) envían `fcm_token`.
- `api/ServicioAuth`: `@PUT("sesion/fcm") Call<Respuesta<Void>> actualizarFcm(@Body Map<String, String> cuerpo);`
- `SplashActivity` con sesión: obtener token y mandarlo (por si rotó).

### 3. `notificaciones/ServicioMensajes extends FirebaseMessagingService`
- `onNewToken` → si `Sesion.activa()`, `PUT /api/sesion/fcm` (llamada síncrona `execute()` está bien aquí, ya es hilo de fondo).
- `onMessageReceived` → según `data.tipo`:
  - `alerta_contacto` → canal `alertas` (IMPORTANCE_HIGH), título y cuerpo del mensaje, `PendingIntent` a `AlertaContactoActivity` con los datos; acción "Llamar" (`tel:` con `ACTION_DIAL` desde la notificación).
  - `estado` → canal `estado`, `PendingIntent` a `DetalleSolicitudActivity` con `id`.
- Íconos de notificación monocromos (`ic_notificacion`) con color `ym_marca`.
- Manifest: `<service android:name=".notificaciones.ServicioMensajes" android:exported="false">` con `intent-filter com.google.firebase.MESSAGING_EVENT`; meta-data de canal y color por defecto.

### 4. Canales y permiso
- `YelpMeApp.onCreate` crea los 2 canales (`NotificationChannel`, textos en `strings.xml`).
- Tras el primer login (`InicioActivity`): pedir `POST_NOTIFICATIONS` con explicación previa ("Para avisarte cuando un contacto pida ayuda."). Si lo niega, aviso en Perfil con botón a ajustes.

### 5. `ui/alertas/AlertaContactoActivity`
- Muestra: "<Nombre> pidió ayuda", título, lugar, `MapaOsm.soloLectura` con el punto, botón "Llamar a <Nombre>" (`Llamadas.llamar`) y "Cómo llegar" (`geo:lat,lng?q=lat,lng` con `ACTION_VIEW`).
- Funciona aunque la app estuviera cerrada (datos por extras; requiere sesión: si no hay → login y luego vuelve).

## Verificación
- Dos emuladores (o emulador + teléfono): Carlos y un contacto vinculado. Carlos crea solicitud → el otro recibe la alerta; tocar abre el mapa.
- Admin envía la solicitud de Carlos → Carlos recibe "Tu ayuda va en camino"; tocar abre el detalle.
- App en primer plano, en segundo plano y cerrada.

## Resultado
Commit: `feat(mobile): recibe notificaciones push de alertas y estados`.

Notas: catálogo con `google-services` 4.4.4 y `firebase-bom` 34.6.0 (`firebase-messaging`). El plugin se aplica **solo si existe** `app/google-services.json` (`BuildConfig.FCM_ACTIVO`); sin el archivo compila y corre sin push. **Cambio respecto al plan:** los mensajes son solo de datos (`aviso_titulo`, `aviso_cuerpo`, `canal` + datos) y la app arma siempre la notificación; con bloque `notification`, en segundo plano Android la mostraba solo y al tocarla abría el launcher. Paquete `notificaciones`: `TokenFcm` (manda el token al iniciar sesión, registrarse y abrir la app con sesión), `Canales` (`alertas` alta, `estado`), `ServicioMensajes`. `AlertaContactoActivity` (quién, qué pasa, lugar, mapa, llamar, cómo llegar con `geo:`; sin sesión → login). Permiso `POST_NOTIFICATIONS` una vez con explicación desde Inicio; aviso con botón a ajustes en Perfil. `mobile/README.md` nuevo con los pasos de Firebase. Probado con dos emuladores (segundo AVD temporal, borrado después por falta de disco): Carlos pide ayuda → María recibe "Carlos pidió ayuda" con acción Llamar (app en primer plano) y al tocar abre la alerta con mapa; el admin rechaza la solicitud de Carlos con su app en segundo plano → "Tu solicitud fue rechazada" con el motivo y al tocar abre el detalle; el admin envía otra → "Tu ayuda va en camino". El caso "app cerrada" no quedó probado con rigor (`am kill` no terminó el proceso). Nota: cerrar sesión en otro dispositivo borra el token FCM del usuario (un dispositivo por usuario, plan 003); el teléfono lo vuelve a mandar al abrir la app.
