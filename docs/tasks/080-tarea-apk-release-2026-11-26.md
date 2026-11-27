# 080 — Tarea: APK de release

> Plan: `013-plan-despliegue-2026-11-26.md` · Rama: `feature/despliegue` · Estado: ⬜ Pendiente

## Objetivo
APK firmado `v1.0.0` que apunta a la API en Railway, publicado en GitHub Releases.

## Pasos

### 1. (Tú) Keystore
Android Studio → Build → Generate Signed App Bundle/APK → APK → Create new:
- Ruta **fuera del repo** (p. ej. `C:\Users\<tú>\keystores\yelpme.jks`), alias `yelpme`, validez 25 años.
- Guardar contraseñas en un gestor. **Si se pierde, no se pueden publicar actualizaciones con la misma firma.**

### 2. `mobile/keystore.properties` (ignorado, tarea 042)
```
storeFile=C:/Users/<tú>/keystores/yelpme.jks
storePassword=…
keyAlias=yelpme
keyPassword=…
```
`mobile/keystore.properties.example` (versionado) con las claves vacías.

### 3. `app/build.gradle.kts`
```kotlin
val firma = java.util.Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
signingConfigs {
    create("release") {
        if (firma.isNotEmpty()) {
            storeFile = file(firma.getProperty("storeFile"))
            storePassword = firma.getProperty("storePassword")
            keyAlias = firma.getProperty("keyAlias")
            keyPassword = firma.getProperty("keyPassword")
        }
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        buildConfigField("String", "API_URL", "\"https://<api>.up.railway.app/api/\"")
    }
}
defaultConfig { versionCode = 1; versionName = "1.0.0" }
```
Activar `optimization`/minify solo si se agregan reglas para Retrofit/Gson/Glide; para el MVP dejarlo apagado.

### 4. Construir
```bash
cd mobile && ./gradlew assembleRelease
# app/build/outputs/apk/release/app-release.apk
```
Requiere `app/google-services.json` (tarea 074).

### 5. Probar en teléfono físico
Instalar el APK (USB o descarga), con **datos móviles** y la PC apagada: registro, login, contacto, solicitud con GPS, foto, push.

### 6. Publicar (confirmar con la persona antes: es público)
```bash
gh release create v1.0.0 mobile/app/build/outputs/apk/release/app-release.apk \
  --title "YelpMe 1.0.0" --notes "APK de la demo. Instalar permitiendo orígenes desconocidos."
```
Actualizaciones: subir `versionCode`/`versionName`, misma keystore, nuevo release.

## Verificación
`apksigner verify --print-certs app-release.apk` muestra el certificado; app funcionando contra la nube.

## Resultado
Commit: `chore(mobile): configura firma y URL de producción del APK`.
