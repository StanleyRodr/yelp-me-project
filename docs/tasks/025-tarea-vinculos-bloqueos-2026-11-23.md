# 025 — Tarea: Vínculos y bloqueos

> Plan: `004-plan-backend-contactos-2026-11-23.md` · Rama: `feature/backend-contactos` · Estado: ✅ Completada

## Objetivo
Consentimiento mínimo (plan principal 6.2): ver quién me tiene como contacto, desvincularme, bloquear y desbloquear.

## Conceptos
- **Vínculo:** contacto **ajeno** con `usuario_contacto_id = yo`. Su `id` es el del contacto en la agenda del otro.
- **Bloqueo:** fila `bloqueos (usuario_id = yo, bloqueado_id = otro)`.

## Endpoints (rol `usuario`)

| Método | Ruta | Acción | Respuesta |
|---|---|---|---|
| GET | `/api/vinculos` | quién me tiene | `[{ id, nombre, apellidos, telefono, desde }]` |
| DELETE | `/api/vinculos/{id}` | desvincularme | 200 "Te desvinculaste de este contacto" |
| GET | `/api/bloqueos` | a quién bloqueé | `[{ id, nombre, apellidos, telefono, desde }]` |
| POST | `/api/bloqueos` | bloquear `{ contacto_id }` | 201 "Usuario bloqueado" |
| DELETE | `/api/bloqueos/{id}` | desbloquear | 200 "Usuario desbloqueado" |

## Pasos

### 1. `VinculoController`
- `index`: `contactos JOIN usuarios (dueño)` donde `usuario_contacto_id = yo`. Datos de la **cuenta** del dueño, no el alias que me puso.
- `duenoDelVinculo($request, $contactoId)` (estático, lo usa el bloqueo): dueño del contacto que apunta a mí, o `abort(404)`. Vínculo ajeno = 404.
- `destroy`: borra **todos** los contactos de ese dueño que apuntan a mí. Caso: me guardó dos veces (número viejo y nuevo).
- Desvincular **no** bloquea: puede volver a agregarme y se vincula.

### 2. `BloqueoController`
- `store`: valida `contacto_id`; `duenoDelVinculo` → 404 si no es un vínculo mío. Transacción:
  1. `insertOrIgnore` en `bloqueos` (UNIQUE evita duplicado).
  2. Borra los contactos del dueño que apuntan a mí.
- Con el bloqueo, `ReglasContactos::usuarioVinculable` excluye mi cuenta: si me agrega de nuevo, se guarda **sin** vínculo y la verificación de teléfono responde `vinculable: false`.
- `destroy` (desbloquear): transacción:
  1. Borra el bloqueo.
  2. Vincula los contactos del otro que ya tengan mi teléfono y estén sin vínculo.
  - El contacto borrado al bloquear no vuelve.
  - **Hueco corregido:** sin el paso 2, un contacto guardado durante el bloqueo quedaba sin vínculo para siempre (el vínculo solo se recalcula al cambiar teléfono).
- `buscar($yo, $id)`: bloqueo propio o 404.

### 3. Rutas
Grupo `rol:usuario`:

```php
Route::apiResource('vinculos', VinculoController::class)->only(['index', 'destroy'])
    ->parameters(['vinculos' => 'id'])->whereNumber('id');
Route::apiResource('bloqueos', BloqueoController::class)->only(['index', 'store', 'destroy'])
    ->parameters(['bloqueos' => 'id'])->whereNumber('id');
```

### 4. Plan principal 6.2 actualizado
Regla de desbloqueo ampliada con el paso 2.

## Verificación
Script `tinker` con transacción revertida (demo: Carlos está en las agendas de María y José):

| Caso | Resultado |
|---|---|
| Carlos ve quién lo tiene | José y María con nombre completo y teléfono |
| Ana desvincula un vínculo de Carlos | 404 |
| Carlos se desvincula de María | 200, ya no está en su agenda |
| María lo agrega de nuevo | vinculado |
| Bloquear sin `contacto_id` / con vínculo ajeno | 422 / 404 |
| Carlos bloquea a José | 201, borrado de la agenda de José |
| José lo agrega de nuevo | sin vínculo; verificación `vinculable: false, en_agenda: true` |
| José borra el bloqueo de Carlos | 404 |
| Carlos desbloquea | 200; contacto que José ya tenía queda vinculado |
| Admin en `/api/vinculos` | 403 |

Tras revertir: 7 contactos, 1 bloqueo (demo), 0 tokens. `php artisan test`: 20/20.

## Resultado
Commit: `feat(backend): agrega vínculos, desvinculación y bloqueos`.
