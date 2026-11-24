# 023 — Tarea: CRUD de contactos

> Plan: `004-plan-backend-contactos-2026-11-23.md` · Rama: `feature/backend-contactos` · Estado: ✅ Completada

## Objetivo
CRUD de la agenda (requisito académico 3.1): crear, consultar, modificar y eliminar contactos propios desde Android.

## Endpoints (rol `usuario`)

| Método | Ruta | Acción | Éxito |
|---|---|---|---|
| GET | `/api/contactos` | `index` | 200, lista por nombre |
| POST | `/api/contactos` | `store` | 201 "Contacto agregado" |
| GET | `/api/contactos/{id}` | `show` | 200 |
| PUT/PATCH | `/api/contactos/{id}` | `update` | 200 "Contacto actualizado" |
| DELETE | `/api/contactos/{id}` | `destroy` | 200 "Contacto eliminado" |

## Pasos

### 1. Rutas
Dentro del grupo `rol:usuario` de `routes/api.php`:

```php
Route::apiResource('contactos', ContactoController::class)
    ->parameters(['contactos' => 'id'])
    ->whereNumber('id');
```

- `whereNumber`: `/api/contactos/abc` → 404 sin llegar al controlador.

### 2. `ContactoController`
- `propio($request, $id)`: `WHERE id = ? AND usuario_id = token`. Si no → `abort(404)`.
  - Ajeno e inexistente dan el mismo 404 genérico: no revela que el contacto existe.
- `validar($request, $duenoId, $ignorarId)`:
  - Teléfono normalizado (`Normalizar::telefono`) antes de validar.
  - `store`: todo `required`. `update`: `sometimes` → se envían solo los campos que cambian (PATCH) o todos (PUT).
  - `Rule::unique('contactos','telefono')->where('usuario_id', dueño)->ignore($id)`. Mensaje: "Ya tienes un contacto con ese teléfono."
  - Teléfono propio → 422 "No puedes agregarte a ti mismo como contacto."
  - `trim` de textos.
- `store`: `usuario_contacto_id = ReglasContactos::usuarioVinculable(...)`.
- `update`: si el teléfono **cambia**, se recalcula el vínculo (otro número = otra persona). Si no cambia, el vínculo se conserva.
- `destroy`: borrado físico (contactos sí se borran).
- Todas las respuestas con `PresentadorContacto`.

## Verificación
Script `tinker` dentro de `DB::beginTransaction()` / `rollBack()`: no deja datos en `YelpMe`.

| Caso | Resultado |
|---|---|
| Agenda de Carlos | José y María vinculados, Mamá no; orden por nombre |
| Crear vacío | 422 con 3 campos |
| Teléfono repetido en su agenda | 422 "Ya tienes un contacto…" |
| Teléfono propio con espacios | 422 "No puedes agregarte…" |
| Crear a Ana (`235-100-0004`, nombre con espacios) | 201, vinculado, nombre y teléfono limpios |
| Ana agrega a José (José la bloqueó) | 201, **no** vinculado |
| Ver propio / ajeno / inexistente | 200 / 404 / 404 |
| PATCH solo parentesco | 200, vínculo se conserva |
| PUT con teléfono sin cuenta | 200, deja de estar vinculado |
| Cambiar a un teléfono ya en la agenda | 422 |
| Mismo teléfono que ya tiene (ignora su id) | 200 |
| Modificar / borrar ajeno | 404 |
| Admin en `/api/contactos` | 403 |
| Sin token | 401 |
| Borrar propio y volver a pedirlo | 200, luego 404 |
| `/api/contactos/abc` | 404 |

`php artisan test`: 20/20. `route:list`: 5 rutas.

## Resultado
Commit: `feat(backend): agrega CRUD de contactos de confianza`.
