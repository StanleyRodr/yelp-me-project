# 047 — Tarea: Configuración Android

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ⬜ Pendiente

## Objetivo
Gradle, dependencias, BuildConfig, manifest y red listos para todo el MVP. Renombrar activities.

## Pasos

### 1. `gradle/libs.versions.toml`
Agregar (versiones estables vigentes; verificar en Maven Central al hacerlo):
| Alias | Artefacto |
|---|---|
| `retrofit`, `retrofit-gson` | `com.squareup.retrofit2:retrofit`, `converter-gson` |
| `okhttp-logging` | `com.squareup.okhttp3:logging-interceptor` |
| `glide` | `com.github.bumptech.glide:glide` |
| `play-services-location` | `com.google.android.gms:play-services-location` |
| `recyclerview` | `androidx.recyclerview:recyclerview` |
| `swiperefreshlayout` | `androidx.swiperefreshlayout:swiperefreshlayout` |
| `osmdroid` | `org.osmdroid:osmdroid-android:6.1.18` (mover el literal al catálogo) |
Quitar `cardview` (se usa `MaterialCardView`).

### 2. `app/build.gradle.kts`
```kotlin
buildFeatures { viewBinding = true; buildConfig = true }
buildTypes {
    debug   { buildConfigField("String", "API_URL", "\"http://10.0.2.2:8000/api/\"") }
    release { buildConfigField("String", "API_URL", "\"https://yelpme-api.up.railway.app/api/\"") }
}
```
Para teléfono físico en debug: documentar `adb reverse tcp:8000 tcp:8000` y usar `http://127.0.0.1:8000/api/` vía propiedad opcional `api.url` en `local.properties`:
```kotlin
val local = java.util.Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
debug { buildConfigField("String", "API_URL", "\"${local.getProperty("api.url", "http://10.0.2.2:8000/api/")}\"") }
```

### 3. Red
- `app/src/debug/res/xml/network_security_config.xml`: `cleartextTrafficPermitted="true"` solo para `10.0.2.2`, `127.0.0.1`, `localhost`.
- `app/src/debug/AndroidManifest.xml`: `android:networkSecurityConfig="@xml/network_security_config"` (merge con `tools:replace` no necesario si main no lo declara).
- Release: sin cleartext.

### 4. `AndroidManifest.xml`
- Permisos: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `CALL_PHONE`, `POST_NOTIFICATIONS`.
- `<uses-feature android:name="android.hardware.telephony" android:required="false"/>`.
- Quitar meta-data `preloaded_fonts`.
- Launcher: `SplashActivity` (tarea 050). `android:label="@string/app_name"`.

### 5. Renombrar (Refactor → Rename, actualiza manifest)
| Antes | Después |
|---|---|
| `MainActivity` | `LoginActivity` (layout `activity_login.xml`) |
| `Registro` | `RegistroActivity` |
| `RecuperarContrasena` | `RecuperarContrasenaActivity` |
| `SolicitudesActivity` | `InicioActivity` (pantalla de crear solicitud; layout `activity_inicio.xml`) |
Paquetes: `com.yelpme.app.ui.<pantalla>`, `com.yelpme.app.api`, `com.yelpme.app.modelos`, `com.yelpme.app.util`.

### 6. Borrar lo que no entra al MVP
- `dialog_direccion_predeterminada.xml`, `dialog_contacto_emergencia.xml` y su lógica.
- CRUD de direcciones en `PerfilActivity` (el domicilio único llega en el plan 009).
- `SesionManager` viejo (lo reemplaza la tarea 049).
Las pantallas que aún no se conectan (contactos, historial, perfil) quedan compilando con su layout y sin datos fijos.

## Verificación
```bash
./gradlew assembleDebug assembleRelease
```
`BuildConfig.API_URL` distinto en cada variante (ver `app/build/generated/source/buildConfig`).

## Resultado
Commit: `chore(mobile): configura dependencias, BuildConfig y red`.
