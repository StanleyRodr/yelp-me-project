# 063 — Tarea: Crear y editar contacto (app)

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ⬜ Pendiente

## Objetivo
**Crear** y **Modificar** del CRUD con un mismo formulario.

## Pasos

### 1. `ui/contactos/FormContactoActivity` (`activity_form_contacto.xml`)
- Extra opcional `id`: sin `id` = crear ("Agregar contacto"); con `id` = editar ("Editar contacto", precarga con `obtener(id)`).
- Campos:
  - Nombre (`textPersonName|textCapWords`).
  - Teléfono (`phone`, 10 dígitos).
  - Parentesco: `MaterialAutoCompleteTextView` (exposed dropdown) con `R.array.parentescos` (Mamá, Papá, Pareja, Hermano/a, Hijo/a, Amistad, Otro); acepta texto libre.
- Botones "Guardar" y "Cancelar".

### 2. Verificación del teléfono
- `TextWatcher` (clase anónima) + `Handler.postDelayed` 600 ms; solo con 10 dígitos y si cambió respecto al original.
- `GET /api/telefonos/{telefono}`:
  - `vinculable` → helper "Tiene cuenta en YelpMe: recibirá tus alertas." (color `ym_exito`).
  - `en_agenda` → error "Ya tienes este número en tu agenda.".
  - Ninguno → helper "Sin cuenta en YelpMe: solo podrás llamarle.".
  - 429 → no mostrar nada (el guardado igual valida).
- Cancelar la verificación pendiente al salir (`removeCallbacks`).

### 3. Guardar
- Crear `POST`, editar `PUT` (solo campos cambiados).
- Errores por campo con `ErrorApi.campo()` (p. ej. "No puedes agregarte a ti mismo.", "Ya tienes este número en tu agenda.").
- OK → `setResult(RESULT_OK)` y `finish()`; Snackbar en la pantalla que vuelve ("Contacto guardado." / "Vinculado con su cuenta de YelpMe.").

## Verificación
- Crear contacto con `2351000002` (María) → vinculado.
- Crear con número sin cuenta → no vinculado.
- Repetir número → error bajo el campo.
- Editar parentesco → lista actualizada.
- Rotar la pantalla a mitad: los datos se conservan.

## Resultado
Commit: `feat(mobile): crea y edita contactos con verificación de teléfono`.
