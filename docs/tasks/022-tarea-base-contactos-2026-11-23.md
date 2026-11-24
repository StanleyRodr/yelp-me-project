# 022 — Tarea: Base de contactos

> Plan: `004-plan-backend-contactos-2026-11-23.md` · Rama: `feature/backend-contactos` · Estado: ✅ Completada

## Objetivo
Piezas compartidas del plan: normalización, reglas de vinculación, forma pública del contacto y límite de consultas de teléfono.

## Pasos

### 1. `app/Support/Normalizar.php`
- `telefono()`: solo dígitos. `correo()`: minúsculas sin espacios. `fecha()`: MySQL → ISO 8601 con zona.
- Vacío o null → null.
- Refactor: `RegistroController`, `SesionController`, `Admin\SesionController` y `PresentadorUsuario` ya lo usan. Antes repetían `preg_replace` / `mb_strtolower` / `Carbon::parse`.

### 2. `app/Support/ReglasContactos.php`
- `usuarioVinculable($telefono, $duenoId)` → id o null. Vincula si:
  - teléfono de cuenta con `rol = 'usuario'` (admins nunca),
  - no es el propio dueño,
  - esa cuenta **no** bloqueó al dueño (`NOT EXISTS` en `bloqueos` con `usuario_id = cuenta`, `bloqueado_id = dueño`).
- `esTelefonoPropio($telefono, $duenoId)`.
- Una sola consulta con `DB::table` + subconsulta.

### 3. `app/Support/PresentadorContacto.php`

```json
{ "id", "nombre", "telefono", "parentesco", "vinculado": true, "created_at", "updated_at" }
```

- No expone `usuario_contacto_id` ni datos de la cuenta vinculada (plan 6.1: no exponer datos personales).

### 4. Límite `telefonos` (`AppServiceProvider`)
- 30 consultas por minuto por usuario (o IP sin sesión). Frena recorrer números para descubrir quién tiene cuenta.

## Verificación
- `php artisan test`: 20/20 tras el refactor (sin cambios de comportamiento).
- `ReglasContactos` con datos de demo:

| Caso | Resultado |
|---|---|
| Carlos agrega a Ana | 6 (vincula) |
| Ana agrega a José (José bloqueó a Ana) | null |
| Carlos agrega a José | 5 |
| Carlos agrega su propio número | null, `esTelefonoPropio = true` |
| Carlos agrega el teléfono del admin | null |
| Número sin cuenta | null |

## Resultado
Commit: `feat(backend): agrega reglas de vinculación y presentador de contactos`.
