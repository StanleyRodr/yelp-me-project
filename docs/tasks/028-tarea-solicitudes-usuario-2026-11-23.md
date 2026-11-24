# 028 — Tarea: Solicitudes del usuario

> Plan: `005-plan-backend-solicitudes-2026-11-23.md` · Rama: `feature/backend-solicitudes` · Estado: ✅ Completada

## Objetivo
El usuario de la app crea solicitudes con ubicación y servicios, las consulta y las modifica mientras están pendientes.

## Endpoints (rol `usuario`)

| Método | Ruta | Acción | Éxito |
|---|---|---|---|
| GET | `/api/solicitudes?estado=` | `index` | 200, más recientes primero |
| POST | `/api/solicitudes` | `store` | 201 "Solicitud enviada" |
| GET | `/api/solicitudes/{id}` | `show` | 200 |
| PUT/PATCH | `/api/solicitudes/{id}` | `update` | 200 "Solicitud actualizada" |

Sin `destroy` (405): las solicitudes nunca se borran; se cancelan (tarea 029).

## Pasos

### 1. `app/Support/Solicitudes.php` (compartido con el panel)
- `ESTADOS`: los 5 oficiales.
- `bloquear($id, $usuarioId = null)`: `SELECT … FOR UPDATE` dentro de transacción. Con `$usuarioId`, solo si es suya; si no, 404.
- `servicios($id)`, `reemplazarServicios($id, $ids)`.
- `reglasServicios()`: `servicios` array; cada uno entero, sin repetir, existente y activo.
- `exigirNoSuspendido($usuarioId)`: 403 "Tu cuenta está suspendida: solo puedes avisar a tus contactos."

### 2. `SolicitudController` (app)

| Campo | Reglas |
|---|---|
| `titulo` | required, máx. 150 |
| `descripcion` | opcional, máx. 2000 |
| `latitud` / `longitud` | required, numérico, −90..90 / −180..180 |
| `ubicacion_nombre` | opcional, máx. 255 |
| `servicios` | opcional (sin servicios = solo contactos) |

- `index`: filtro `estado` validado con `Rule::in`. Orden `created_at` desc.
- `store`: nace `Pendiente`. Con servicios y cuenta suspendida → 403. Inserción + servicios en una transacción. Textos con `trim`; vacíos → `null`.
- `show`: propia o 404.
- `update` (campos opcionales):
  1. Transacción + `bloquear($id, usuario)`.
  2. Estado ≠ Pendiente → 409 "La solicitud está en X: solo puedes modificarla mientras está pendiente."
  3. Servicios: suspendido puede **quitar**, no **agregar** (si la lista nueva trae algún id que no tenía → 403). Quitar todos = pasa a "solo contactos".
- Respuestas con `PresentadorSolicitud`.

### 3. Rutas y traducciones
- `Route::apiResource('solicitudes', SolicitudController::class)->except(['destroy'])->parameters(['solicitudes' => 'id'])->whereNumber('id')` en el grupo `rol:usuario`.
- `lang/es/validation.php`: atributos `servicios.*` (servicio) y `ubicacion_nombre`.

## Verificación
Script `tinker` con transacción revertida:

| Caso | Resultado |
|---|---|
| Catálogo | 6 servicios activos |
| Mis solicitudes (Carlos) | #1, #3, #6; `?estado=Pendiente` → 1; estado inválido → 422 |
| Crear vacío / coordenadas fuera de rango | 422 con mensajes en español |
| Servicio repetido o inexistente | 422 |
| Crear con servicios (título con espacios, descripción vacía, lat como texto) | 201, Pendiente, `descripcion: null`, `latitud` número |
| Luis suspendido con servicios / sin servicios | 403 / 201 |
| Luis agrega servicios al editar | 403 |
| Ver propia / ajena | 200 / 404 |
| Editar pendiente; quitar todos los servicios | 200; `servicios: []` |
| Editar Enviada | 409 |
| Editar ajena | 404 |
| DELETE | 405 |
| Admin en `/api/solicitudes` / en `/api/servicios` | 403 / 200 |

## Resultado
Commit: `feat(backend): agrega solicitudes de ayuda del usuario`.
