# 073 — Tarea: Envíos de notificaciones

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ✅ Terminada

## Objetivo
Push a contactos vinculados al crear una solicitud y al solicitante cuando el admin cambia el estado.

## Pasos

### 1. `App\Support\Avisos`
```php
public static function solicitudCreada(int $solicitudId): void
public static function estadoCambiado(int $solicitudId): void
```
- Se llaman dentro de la transacción con `DB::afterCommit(fn () => Avisos::…)` para no avisar si se revierte.
- `solicitudCreada`: creador + destinatarios:
  ```php
  DB::table('contactos as c')
      ->join('usuarios as u', 'u.id', '=', 'c.usuario_contacto_id')
      ->where('c.usuario_id', $creadorId)
      ->where('u.activo', 1)->whereNotNull('u.fcm_token')
      ->pluck('u.fcm_token')->unique();
  ```
  Título "<Nombre> pidió ayuda", cuerpo = `titulo`. Datos (todo string): `tipo=alerta_contacto`, `nombre` (completo), `telefono`, `titulo`, `latitud`, `longitud`, `ubicacion_nombre`.
- `estadoCambiado`: solo si el estado nuevo es Enviado, Rechazado o Terminado **y lo cambió un admin**; destino = `fcm_token` del creador (si activo). Textos del plan 012. Datos `tipo=estado`, `solicitud_id`, `estado`.
- Cada envío va en `try/catch` propio: uno que falla no frena a los demás.

### 2. Enganches
| Controlador | Llamada |
|---|---|
| `Api/SolicitudController@store` | `solicitudCreada` |
| `Api/Admin/EstadoSolicitudController@update` | `estadoCambiado` |
Cambios del propio usuario (cancelar, terminar) no avisan al usuario.

### 3. Pruebas `tests/Feature/NotificacionesTest.php` (con `NotificadorFalso`)
- Carlos crea → envío a cada vinculado con token; ninguno a no vinculados; ninguno a deshabilitados ni sin token.
- Datos de la alerta incluyen lat/lng como string.
- Admin envía/rechaza/termina → 1 envío al creador con el estado; usuario cancela → 0.
- Notificador que lanza excepción → la API responde 201/200 igual.
- Transacción revertida (409) → sin envíos.

### 4. `backend/README.md`
Sección "Notificaciones" con eventos y cómo configurar credenciales.

## Verificación
```bash
php artisan test
```
Con credenciales reales: crear una solicitud con curl y ver la llamada en Logcat del dispositivo (tarea 074).

## Resultado
Commit: `feat(notificaciones): avisa a contactos y al solicitante con push`.

Notas: `App\Support\Avisos` (`solicitudCreada`, `estadoCambiado`) enganchado con `DB::afterCommit` en `Api/SolicitudController@store` y `Api/Admin/EstadoSolicitudController@update`; si la transacción se revierte (p. ej. 409) no hay aviso. Canales Android: `alertas` (contactos) y `estado` (mis solicitudes), enviados en el mensaje. Cada envío en su `try/catch`. Pruebas en `NotificacionesTest` (6 nuevas; 108 en total): avisos solo a vinculados activos con token, datos como texto, Enviado/Terminado/Rechazado al solicitante con motivo, cancelar del usuario sin aviso, 409 sin aviso y notificador caído sin afectar la respuesta.
