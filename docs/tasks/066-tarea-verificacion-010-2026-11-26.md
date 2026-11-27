# 066 — Tarea: Verificación del plan 010

> Plan: `010-plan-mobile-contactos-2026-11-26.md` · Rama: `feature/mobile-contactos` · Estado: ✅ Terminada

## Objetivo
Demostrar el CRUD obligatorio, la propiedad de registros y cerrar el plan.

## Pasos

### 1. Automáticas
```bash
cd backend && php artisan bd:pruebas && php artisan test
cd mobile && ./gradlew testDebugUnitTest assembleDebug
```

### 2. CRUD académico (criterio 3.1), grabar pantalla del emulador
| Paso | Acción | Comprobar en BD (`SELECT * FROM contactos WHERE usuario_id = …`) |
|---|---|---|
| Crear | Agregar "Pruebas CRUD" 2359999999 Amistad | Fila nueva |
| Consultar | Aparece en la lista y en el detalle | — |
| Modificar | Parentesco → Pareja | Columna cambiada |
| Eliminar | Eliminar | Fila borrada |

### 3. Propiedad (criterio 3.2)
Con el token de Carlos (Logcat o curl): `GET /api/contactos/{id de María}` → 404; `PUT` y `DELETE` → 404.

### 4. Recorrido
Vinculación automática, favorito y barra, llamada concedida/negada, vínculos con domicilio, desvincular, bloquear, desbloquear. Tema oscuro.

### 5. Documentación
- `CLAUDE.md`: favorito y domicilio en vínculos (backend); Mobile: `Llamadas`, pantallas de contactos.
- `principal-plan.md`: "✅ Plan 010", estado.
- Plan 010: tareas ✅, criterios `[x]`.

### 6. PR
"Plan 010: contactos en Android". Cómo probar: columna `favorito` nueva (recrear `YelpMe` o `ALTER TABLE contactos ADD favorito BOOLEAN NOT NULL DEFAULT FALSE`).

## Resultado
Commit: `docs(contactos): documenta contactos en Android y cierra el plan 010`.

Notas: `php artisan test` 97 OK; `./gradlew testDebugUnitTest assembleDebug assembleRelease` OK; sin `findViewById`, colores hex ni textos fijos en layouts. CRUD en emulador con Carlos, comprobado en la BD en cada paso: crear "Pruebas CRUD" 2359999999 Amistad (fila nueva, sin vínculo) → consultar en lista y detalle → modificar parentesco a Pareja (columna cambiada) → eliminar (fila borrada). Grabación local `crud-plan010.mp4` (no se sube al repo). Propiedad con curl y el token de Carlos sobre un contacto de María: `GET`, `PUT`, `DELETE` y `PUT /favorito` → 404. Recorrido de vinculación, favorito y barra, llamada concedida/negada, vínculos con domicilio, desvincular, bloquear y desbloquear en las tareas 062–065; tema oscuro revisado en lista y "Quién me tiene".
