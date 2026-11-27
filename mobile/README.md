# YelpMe — app Android

App del usuario final (Java, Android Studio). Consume la API Laravel de `backend/`.

## Compilar

```bash
./gradlew assembleDebug        # APK de desarrollo
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew assembleRelease      # APK de producción
```

- **API:** debug usa `http://10.0.2.2:8000/api/` (la PC desde el emulador). Si no la alcanza: `adb reverse tcp:8000 tcp:8000` y en `local.properties` `api.url=http://127.0.0.1:8000/api/`.
- **Ubicación en el emulador:** Extended controls → Location (o `adb emu geo fix <lng> <lat>`).

## Notificaciones push (Firebase)

Sin `app/google-services.json` la app compila y funciona igual, sin push (`BuildConfig.FCM_ACTIVO = false`). Para activarlas:

1. [Consola de Firebase](https://console.firebase.google.com) → proyecto de YelpMe (o uno nuevo) → **Agregar app → Android** con el paquete `com.yelpme.app`.
2. Descargar **`google-services.json`** y guardarlo en `mobile/app/google-services.json`. **No se sube a git** (está en `mobile/.gitignore`).
3. El backend necesita la cuenta de servicio del **mismo proyecto** (ver `backend/README.md`, "Notificaciones push").
4. Probar en un emulador con imagen **Google Play** (las imágenes AOSP no tienen Google Play Services).

Qué hace la app:
- Al iniciar sesión, registrarse o abrir la app con sesión manda su token (`PUT /api/sesion/fcm`); si Firebase lo rota, `ServicioMensajes.onNewToken` lo vuelve a mandar.
- Los mensajes llegan **solo con datos** y la app arma la notificación en el canal `alertas` (un contacto pidió ayuda → `AlertaContactoActivity` con mapa, "Llamar" y "Cómo llegar") o `estado` (cambió el estado de mi solicitud → su detalle).
- Pide el permiso de notificaciones (Android 13+) una vez, con explicación; si se niega, Perfil lo recuerda con un botón a los ajustes.
