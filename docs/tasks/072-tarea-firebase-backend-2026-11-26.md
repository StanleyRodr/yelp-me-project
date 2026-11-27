# 072 — Tarea: Firebase en el backend

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ⬜ Pendiente

## Objetivo
Conectar Laravel con FCM (API v1) detrás de una interfaz que permite funcionar sin Firebase.

## Pasos

### 1. Firebase (lo hace la persona dueña del proyecto)
1. Consola de Firebase → crear proyecto "YelpMe" (Analytics opcional, desactivado).
2. Agregar app Android con paquete `com.yelpme.app` → descargar `google-services.json` (tarea 074).
3. Configuración → Cuentas de servicio → "Generar nueva clave privada" → JSON.
4. Guardar el JSON en `backend/storage/app/firebase/credenciales.json`. **No subirlo.**

### 2. Librería
```bash
composer require kreait/laravel-firebase
php artisan vendor:publish --provider="Kreait\Laravel\Firebase\ServiceProvider" --tag=config
```
`.env.example`:
```
FIREBASE_CREDENTIALS=storage/app/firebase/credenciales.json
# Producción (plan 013): JSON en base64; tiene prioridad sobre FIREBASE_CREDENTIALS
FIREBASE_CREDENTIALS_BASE64=
```
`.gitignore` del backend: `/storage/app/firebase/`.

### 3. `App\Support\Notificador` (interfaz)
```php
interface Notificador
{
    /** @param array<string,string> $datos */
    public function enviar(string $token, string $titulo, string $cuerpo, array $datos = []): bool;
}
```
- `App\Support\NotificadorFcm`: usa `Kreait\Firebase\Contract\Messaging` con `CloudMessage::new()->toToken($token)->withNotification(...)->withData($datos)` y `AndroidConfig` prioridad `high`. Captura `MessagingException`: si el token no existe/inválido → `DB::table('usuarios')->where('fcm_token', $token)->update(['fcm_token' => null])`; registra con `Log::warning` y devuelve `false`.
- `App\Support\NotificadorNulo`: no hace nada, devuelve `false`.
- `AppServiceProvider::register`: si hay `FIREBASE_CREDENTIALS_BASE64`, decodificar a `storage/app/firebase/credenciales.json` (si no existe) o pasar el array a config; si no hay credenciales legibles → `NotificadorNulo`, si hay → `NotificadorFcm`.

### 4. Endpoint del token
`PUT /api/sesion/fcm` (grupo `rol:usuario`) `{ fcm_token: required|string|max:255 }` → actualiza `usuarios.fcm_token` del token; si otro usuario tenía el mismo token (dispositivo compartido), ponerlo en `null` allá. Respuesta `null`, mensaje "Token de notificaciones actualizado.". `SesionController@store`/`RegistroController` aplican la misma limpieza.

### 5. Pruebas
- `tests/Support/NotificadorFalso.php` (guarda envíos en un array); `TestCase::setUp` lo registra en el contenedor.
- `PUT /api/sesion/fcm` guarda el token; token duplicado se mueve al nuevo usuario; admin → 403.

## Verificación
```bash
php artisan test
php artisan tinker   # app(\App\Support\Notificador::class) → NotificadorFcm con credenciales, NotificadorNulo sin ellas
```

## Resultado
Commit: `feat(notificaciones): integra Firebase Cloud Messaging en el backend`.
