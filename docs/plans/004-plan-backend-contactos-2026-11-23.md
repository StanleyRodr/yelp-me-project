# 004 — Plan: API de contactos de confianza

> Fecha: 2026-11-23 · Rama: `feature/backend-contactos` (apilada sobre `feature/backend-auth`)
> Referencia: `docs/plans/principal-plan.md` (secciones 3.1, 3.2 y 6)

## Objetivo

Implementar en la API el **CRUD de contactos de confianza** que consume Android (requisito académico 3.1), con vinculación automática por teléfono, verificación de número sin exponer datos, y el consentimiento mínimo: ver quién me tiene como contacto, desvincularme, bloquear y desbloquear.

**No incluye:** pantallas de Android (plan de mobile), notificaciones push (plan FCM) ni la vista de contactos en el panel (el admin no gestiona agendas).

## Punto de partida
- Tablas `contactos` y `bloqueos` creadas por `script-bd.sql` (UNIQUE `usuario_id + telefono`; UNIQUE `usuario_id + bloqueado_id`).
- Grupo de rutas `auth:sanctum` + `cuenta.activa` + `rol:usuario` listo (plan 003).
- Datos de demo: Carlos tiene a María y José vinculados y a "Mamá" sin vincular; José bloqueó a Ana.

## Decisiones

### Endpoints (todos con token de rol `usuario`)

| Método | Ruta | Controlador | Uso |
|---|---|---|---|
| GET | `/api/contactos` | `ContactoController@index` | Mi agenda |
| POST | `/api/contactos` | `ContactoController@store` | Agregar contacto |
| GET | `/api/contactos/{id}` | `ContactoController@show` | Ver uno |
| PUT | `/api/contactos/{id}` | `ContactoController@update` | Modificar |
| DELETE | `/api/contactos/{id}` | `ContactoController@destroy` | Eliminar |
| GET | `/api/telefonos/{telefono}` | `TelefonoController@show` | ¿Ese número se vinculará? |
| GET | `/api/vinculos` | `VinculoController@index` | Quién me tiene como contacto |
| DELETE | `/api/vinculos/{id}` | `VinculoController@destroy` | Desvincularme (borra el contacto de su agenda) |
| GET | `/api/bloqueos` | `BloqueoController@index` | A quién bloqueé |
| POST | `/api/bloqueos` | `BloqueoController@store` | Bloquear a quien me tiene (`contacto_id`) |
| DELETE | `/api/bloqueos/{id}` | `BloqueoController@destroy` | Desbloquear |

### Reglas
- **Propiedad:** el dueño sale del token. Un contacto, vínculo o bloqueo de otra persona responde **404** (no revela que existe).
- **Campos:** `nombre` (máx. 100), `telefono` (10 dígitos, se normaliza), `parentesco` (texto libre, máx. 50: "Pareja", "Mamá", "Vecina"…).
- **Sin duplicados:** un mismo teléfono una sola vez por agenda (422).
- **No agregarse a uno mismo:** el teléfono propio se rechaza (422).
- **Vinculación automática** al crear y al cambiar el teléfono: se vincula si el número pertenece a un usuario de rol `usuario` que **no** bloqueó al dueño. Si no, se guarda sin vincular (se puede llamar).
- **Respuesta del contacto:** `id, nombre, telefono, parentesco, vinculado (bool), created_at, updated_at`. No se expone el id ni los datos de la cuenta vinculada.
- **Verificación de teléfono:** responde solo `{ vinculable: bool }`. Límite de 30 consultas por minuto.
- **Vínculos:** cada fila muestra `id` del contacto y nombre, apellidos y teléfono de quien me agregó (su cuenta, no el alias que me puso).
- **Desvincular y bloquear** borran el contacto de la agenda de quien me agregó (plan principal 6.2). Bloquear además impide que me vuelva a vincular; desbloquear solo borra el bloqueo (no restaura el contacto).
- **Borrado físico** de contactos y bloqueos (son de las tablas que sí se borran).

### Pruebas
Archivo nuevo por recurso sobre `YelpMe_pruebas`, con los datos de demo.

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 022 | [Base de contactos](../tasks/022-tarea-base-contactos-2026-11-23.md) | Reglas de vinculación, presentador de contacto, normalización y límite de consultas. | ✅ |
| 023 | [CRUD de contactos](../tasks/023-tarea-crud-contactos-2026-11-23.md) | `index`, `store`, `show`, `update`, `destroy` con propiedad y vinculación. | ✅ |
| 024 | [Verificación de teléfono](../tasks/024-tarea-verificacion-telefono-2026-11-23.md) | Saber si un número se vinculará sin exponer datos. | Pendiente |
| 025 | [Vínculos y bloqueos](../tasks/025-tarea-vinculos-bloqueos-2026-11-23.md) | Quién me tiene, desvincularme, bloquear y desbloquear. | Pendiente |
| 026 | [Pruebas y documentación](../tasks/026-tarea-pruebas-contactos-2026-11-23.md) | Pruebas de todos los casos y endpoints en el README. | Pendiente |

---

## Criterios de terminado

- [ ] CRUD completo de contactos con formato estándar y mensajes en español.
- [ ] Un usuario no puede ver, modificar ni borrar contactos de otro (404).
- [ ] Vinculación automática al crear y al cambiar teléfono, respetando bloqueos.
- [ ] La verificación de teléfono no devuelve datos personales.
- [ ] Desvincular y bloquear borran el contacto de la agenda del otro; el bloqueo impide volver a vincular.
- [ ] Un admin no puede usar estos endpoints (403).
- [ ] `php artisan test` pasa sobre `YelpMe_pruebas`.
- [ ] Endpoints documentados en `backend/README.md`.
- [ ] Commits por tarea, en español, sin coautoría; PR al terminar.
