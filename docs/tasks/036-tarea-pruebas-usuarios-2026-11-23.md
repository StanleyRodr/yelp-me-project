# 036 — Tarea: Pruebas y documentación de usuarios y perfil

> Plan: `006-plan-backend-usuarios-perfil-2026-11-23.md` · Rama: `feature/backend-usuarios` · Estado: ✅ Completada

## Objetivo
Pruebas automáticas del plan 006 y documentación de los endpoints.

## Pasos

### 1. Ayudante `imagenPng($nombre, $rellenoKb)` en `tests/TestCase.php`
- PNG real de 1×1 en base64 → archivo temporal → `UploadedFile` en modo prueba.
- `$rellenoKb`: bytes extra para probar el límite de 1 MB.
- Motivo: este PHP **no tiene GD**; `UploadedFile::fake()->image()` lanza `GD extension is not installed`.

### 2. `tests/Feature/UsuariosPanelTest.php` (9)
| Prueba | Comprueba |
|---|---|
| Listado | solo usuarios de la app, orden, Luis Suspendido/3/2 |
| Detalle | estructura del frontend, sin password ni fcm_token |
| Admins / roles | id de admin → 404 en detalle, cuenta y strikes; usuario → 403 |
| Cuenta | reactivar activa 409 (mensaje exacto); deshabilitar cierra sesiones y su token → 401; reportar deshabilitado 409; reactivar |
| Reportar | motivo obligatorio; 3.º strike "…y cuenta suspendida"; strike manual sin solicitud; suspender ya suspendida 409 |
| Anular | motivo obligatorio; baja a 2 activos pero sigue Suspendido; guarda motivo y quién; repetir 409; inexistente 404 |
| Rehabilitar | Activo, 0 activos, veces 2, sin vigente; 6 strikes conservados sin anulación; repetir 409 |
| Suspensión manual | `suspendido_por_nombre`; pedir servicios → 403 |
| Actividad | 4 solicitudes atendidas, 6 strikes dados |

### 3. `tests/Feature/PerfilTest.php` (6)
| Prueba | Comprueba |
|---|---|
| Datos | teléfono ajeno 422; su correo en mayúsculas OK; nombre con `trim` |
| Teléfono | todas las agendas que lo tienen quedan con el número nuevo |
| Conflictos | contacto viejo de María conserva número; "Otro" (ya tenía el nuevo) se vincula; contacto de Ana no (José la bloqueó) |
| Foto | PDF 422; > 1 MB 422; subir, reemplazar (borra anterior), quitar (borra archivo) con `Storage::fake` |
| Contraseña | actual mala 422; igual 422; cambio OK; sesión actual sigue, otra 401; login con la nueva |
| Admin | también edita su perfil |

### 4. Errores encontrados por las pruebas
- **Foto anterior no se borraba** al reemplazar: el controlador leía `foto_perfil` de `$request->user()`, desactualizado (en pruebas `actingAs` reutiliza el objeto). Ahora se lee de la BD.
- Mismo patrón corregido en teléfono del perfil (con `lockForUpdate`), contraseña actual y desbloqueo.
- Regla agregada a `CLAUDE.md`.

### 5. Documentación
- `backend/README.md`: secciones "Gestión de usuarios" y "Perfil propio".
- `CLAUDE.md`: endpoints del plan 006, regla de `$request->user()`, ayudante sin GD; estado del backend.
- `principal-plan.md`: plan 006 ✅ y estado actual.

## Verificación

```bash
php artisan test   # 73 passed (459 assertions)
```

## Resultado
Commit: `test(backend): agrega pruebas de gestión de usuarios y perfil`.
