# 026 — Tarea: Pruebas y documentación de contactos

> Plan: `004-plan-backend-contactos-2026-11-23.md` · Rama: `feature/backend-contactos` · Estado: ✅ Completada

## Objetivo
Pruebas automáticas de todo el plan 004 y documentación de los endpoints.

## Pasos

### 1. Ayudante `como($correo)` en `tests/TestCase.php`
- `Sanctum::actingAs(User::where('correo', ...))` tras `reiniciarAuth()`.
- Cambia de usuario dentro de una prueba sin pasar por el login (más rápido, sin límite de intentos).

### 2. `tests/Feature/ContactosTest.php` (11)
| Prueba | Comprueba |
|---|---|
| Lista | solo mi agenda, orden por nombre, `vinculado`, sin `usuario_contacto_id` |
| Crear con cuenta | normaliza nombre y teléfono, vincula |
| Crear sin cuenta | sin vínculo |
| Cuenta que me bloqueó | sin vínculo |
| Teléfono de admin | sin vínculo |
| Validación | campos obligatorios, duplicado, número propio (mensajes exactos) |
| Modificar | PATCH conserva vínculo; PUT con otro teléfono lo recalcula; mismo teléfono OK; duplicado 422 |
| Eliminar | 200, luego 404, fila borrada |
| Ajenos | ver, modificar y borrar → 404; el contacto sigue existiendo |
| Roles | sin token 401, admin 403 |
| Verificar teléfono | respuesta exacta, `en_agenda`, formato 422, bloqueo = sin cuenta |

### 3. `tests/Feature/VinculosTest.php` (6)
| Prueba | Comprueba |
|---|---|
| Quién me tiene | José y María con datos de su cuenta |
| Desvincularme | me borra de su agenda; puede volver a agregarme vinculado |
| Bloquear | borra el contacto; al reagregarme queda sin vínculo; `vinculable: false` |
| Desbloquear | el contacto guardado durante el bloqueo queda vinculado |
| Ajenos | vínculo y bloqueo de otro → 404; bloquear sin `contacto_id` → 422 |
| Roles | admin 403 en vínculos y bloqueos |

### 4. Documentación
- `backend/README.md`: sección "Contactos de confianza" (endpoints, forma, reglas, errores).
- `CLAUDE.md`: endpoints del plan 004, `ReglasContactos`, `Normalizar`, ayudante `como()`; estado del backend.
- `principal-plan.md`: plan 004 ✅ y estado actual.

## Verificación

```bash
php artisan test   # 37 passed (136 assertions)
```

## Resultado
Commit: `test(backend): agrega pruebas de contactos, vínculos y bloqueos`.
