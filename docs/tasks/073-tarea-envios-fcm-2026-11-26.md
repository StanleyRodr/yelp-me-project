# 073 — Tarea: Envíos de notificaciones

> Plan: `012-plan-fcm-2026-11-26.md` · Rama: `feature/fcm` · Estado: ⬜ Pendiente

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
