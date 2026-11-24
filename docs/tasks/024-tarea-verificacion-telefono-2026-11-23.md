# 024 — Tarea: Verificación de teléfono

> Plan: `004-plan-backend-contactos-2026-11-23.md` · Rama: `feature/backend-contactos` · Estado: ✅ Completada

## Objetivo
Android pregunta, antes de guardar, si un número se vinculará y si ya está en la agenda. Sin exponer datos de nadie (plan principal 6.1).

## Endpoint
`GET /api/telefonos/{telefono}` · rol `usuario` · `throttle:telefonos` (30/min por usuario).

```json
{ "telefono": "2351000004", "vinculable": true, "en_agenda": false }
```

## Pasos

### 1. `app/Http/Controllers/Api/TelefonoController@show`
1. `Normalizar::telefono` (acepta `235-100-0004`, espacios).
2. `Validator::make(...)->validate()`: `digits:10`. Falla → 422.
3. `vinculable` = `ReglasContactos::usuarioVinculable(...) !== null`.
4. `en_agenda` = existe contacto propio con ese teléfono. Android avisa "ya lo tienes" antes de enviar.

### 2. Privacidad
- `vinculable: false` igual para: sin cuenta, cuenta de admin, número propio y cuenta que me bloqueó. Un bloqueo no se distingue de "sin cuenta".
- Nunca nombre, id ni foto de la cuenta.
- Límite de 30/min frena recorrer números.

### 3. Ruta
Grupo `rol:usuario`:

```php
Route::get('/telefonos/{telefono}', [TelefonoController::class, 'show'])->middleware('throttle:telefonos');
```

## Verificación
Script `tinker` con transacción revertida:

| Caso | vinculable | en_agenda |
|---|---|---|
| Carlos → Ana (`235-100-0004`) | true | false |
| Carlos → María | true | true |
| Carlos → Mamá (sin cuenta) | false | true |
| Carlos → su número | false | false |
| Carlos → admin | false | false |
| Ana → José (la bloqueó) | false | false |
| Ana → número sin cuenta | false | false |

- Bloqueo y sin cuenta: respuesta idéntica.
- `123` → 422. Consulta 31 en un minuto → 429.

## Resultado
Commit: `feat(backend): agrega verificación de teléfono sin exponer datos`.
