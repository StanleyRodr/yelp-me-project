# 061 — Tarea: Favorito y vínculos con domicilio (API)

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ⬜ Pendiente

## Objetivo
Contacto favorito único por usuario y domicilio en `GET /api/vinculos`.

## Pasos

### 1. `database/script-bd.sql`
En `contactos`: `favorito BOOLEAN NOT NULL DEFAULT FALSE,` + `INDEX idx_contactos_usuario_favorito (usuario_id, favorito)`. Seeder: Carlos con un favorito.

### 2. `PresentadorContacto::datos`
Agregar `'favorito' => (bool) $c->favorito`.

### 3. `ContactoController@index`
`orderByDesc('favorito')->orderBy('nombre')`.

### 4. `Api/FavoritoController` (rol `usuario`)
| Método | Ruta | Efecto |
|---|---|---|
| PUT | `/api/contactos/{id}/favorito` | En `DB::transaction`: `lockForUpdate` sobre los contactos del usuario; todos `favorito = 0`; el `{id}` `favorito = 1`. Devuelve el contacto. |
| DELETE | `/api/contactos/{id}/favorito` | `favorito = 0`. Devuelve el contacto. |
Contacto ajeno o inexistente → 404 (misma regla de propiedad del plan 004). `whereNumber('id')`.

### 5. Vínculos con domicilio
`VinculoController@index`: cargar ids de los dueños (`c.usuario_id`) y `PresentadorDomicilio::porUsuarios($ids)`; cada vínculo agrega `domicilio` (o `null`). Bloqueos sin cambios.

### 6. Pruebas
- `tests/Feature/ContactosTest.php`: marcar A → favorito; marcar B → A deja de serlo (solo 1 con `favorito=1`); quitar; lista con favorito primero; contacto ajeno 404; borrar el favorito → usuario sin favorito.
- `tests/Feature/VinculosTest.php`: B ve el domicilio de A (A guardó a B); C (no vinculado) no aparece; tras bloquear, A ya no está en la lista de B.

### 7. `backend/README.md`
Filas nuevas en "Contactos" y campo `domicilio` en vínculos.

## Verificación
```bash
php artisan bd:pruebas && php artisan test
```

## Resultado
Commit: `feat(contactos): agrega contacto favorito y domicilio en vínculos`.
