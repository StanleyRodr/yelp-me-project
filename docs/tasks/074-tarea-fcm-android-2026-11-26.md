# 074 — Tarea: FCM en Android

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ⬜ Pendiente

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
