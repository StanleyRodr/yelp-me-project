# 006 — Plan: Gestión de usuarios y perfil

> Fecha: 2026-11-23 · Rama: `feature/backend-usuarios` (desde `main`)
> Referencia: `docs/plans/principal-plan.md` (secciones 4, 5, 8 y 10.2)

## Objetivo

Completar la API que el panel ya muestra con datos de ejemplo: gestión de los usuarios de la app (estado de cuenta, historial, reportar, anular strikes, suspender y rehabilitar, deshabilitar y reactivar) y el perfil propio para ambos roles (datos, foto, contraseña y, para el admin, su actividad). Con esto el backend cubre todas las pantallas del panel y del perfil de Android.

**No incluye:** conectar el frontend o Android (planes posteriores) ni notificaciones push.

## Punto de partida
- `ReglasCuenta` ya tiene `strikesActivos`, `estaSuspendido`, `suspender` y `darStrike` (planes 003 y 005).
- Disco `perfiles` listo (plan 001).
- El frontend define la forma esperada en `frontend/src/services/usuariosService.js` y las acciones por estado en `constantes/cuentas.js`.

## Decisiones

### Endpoints

| Método | Ruta | Quién | Controlador | Uso |
|---|---|---|---|---|
| GET | `/api/admin/usuarios` | Admin | `Admin\UsuarioController@index` | Usuarios de la app con estado de cuenta |
| GET | `/api/admin/usuarios/{id}` | Admin | `Admin\UsuarioController@show` | Perfil con historial |
| PUT | `/api/admin/usuarios/{id}/cuenta` | Admin | `Admin\CuentaController@update` | `activo: false` deshabilita, `true` reactiva |
| POST | `/api/admin/usuarios/{id}/strikes` | Admin | `Admin\StrikeController@store` | Reportar (strike manual) |
| PUT | `/api/admin/strikes/{id}/anulacion` | Admin | `Admin\AnulacionStrikeController@update` | Anular un strike con motivo |
| POST | `/api/admin/usuarios/{id}/suspension` | Admin | `Admin\SuspensionController@store` | Suspender |
| DELETE | `/api/admin/usuarios/{id}/suspension` | Admin | `Admin\SuspensionController@destroy` | Rehabilitar (cierra la vigente; no borra historial) |
| GET | `/api/admin/actividad` | Admin | `Admin\ActividadController@index` | Solicitudes que atendí y strikes que di |
| PUT | `/api/perfil` | Cualquier rol | `PerfilController@update` | Nombre, apellidos, teléfono, correo |
| POST | `/api/perfil/foto` | Cualquier rol | `FotoPerfilController@store` | Subir foto |
| DELETE | `/api/perfil/foto` | Cualquier rol | `FotoPerfilController@destroy` | Quitar foto |
| PUT | `/api/perfil/password` | Cualquier rol | `PasswordController@update` | Cambiar contraseña |

### Reglas
- **Solo usuarios de la app:** las rutas de gestión solo operan sobre rol `usuario`; un id de admin responde 404.
- **Acciones por estado de cuenta** (igual a `constantes/cuentas.js`):

| Cuenta | Acciones |
|---|---|
| Activo | reportar, suspender, deshabilitar |
| Suspendido | rehabilitar, reportar, deshabilitar |
| Deshabilitado | reactivar |

  Otra acción → 409. Anular un strike activo se permite en cualquier estado.
- **Reportar:** motivo obligatorio; `darStrike` sin solicitud; al tercero, suspensión automática.
- **Anular:** solo strikes activos; motivo obligatorio; deja de contar y guarda quién, cuándo y por qué. **No** levanta una suspensión ya aplicada (solo un admin rehabilita).
- **Rehabilitar:** cierra la suspensión vigente (quién y cuándo) y deja los strikes activos en 0 (reiniciados, no anulados).
- **Deshabilitar:** `activo = 0`, `deshabilitado_en`, y cierra todas sus sesiones (borra sus tokens). **Reactivar:** `activo = 1`.
- **Perfil:** correo y teléfono únicos (sin contar el propio). Si un usuario cambia su teléfono, se actualiza el número en las agendas de quienes lo tienen vinculado (el vínculo es por `usuario_id`); si alguien ya tiene ese número guardado aparte, se respeta su registro.
- **Foto:** JPG, PNG o WEBP de hasta 1 MB en el disco `perfiles`; al reemplazar o quitar se borra el archivo anterior.
- **Contraseña:** pide la actual; nueva de mín. 8 con confirmación. Al cambiarla se cierran las demás sesiones (se conserva la actual).
- **Forma de respuesta** igual al frontend: resumen = usuario + `estado_cuenta`, `veces_suspendido`; detalle = resumen + `suspension_vigente`, `solicitudes`, `strikes` (con `admin_nombre`, `anulado_por_nombre`), `suspensiones` (con `suspendido_por_nombre`, `rehabilitado_por_nombre`). Listado sin consultas por fila.

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 032 | [Base de cuentas](../tasks/032-tarea-base-cuentas-2026-11-23.md) | Presentador de cuenta (resumen y detalle sin N+1), estado de cuenta y acciones permitidas. | ✅ |
| 033 | [Usuarios del panel](../tasks/033-tarea-usuarios-panel-2026-11-23.md) | Listado, detalle, deshabilitar y reactivar. | ✅ |
| 034 | [Strikes y suspensiones](../tasks/034-tarea-strikes-suspensiones-2026-11-23.md) | Reportar, anular, suspender y rehabilitar. | Pendiente |
| 035 | [Perfil propio](../tasks/035-tarea-perfil-propio-2026-11-23.md) | Datos, teléfono con agendas, foto, contraseña y actividad del admin. | Pendiente |
| 036 | [Pruebas y documentación](../tasks/036-tarea-pruebas-usuarios-2026-11-23.md) | Pruebas de todos los casos y endpoints en el README. | Pendiente |

---

## Criterios de terminado

- [ ] Las acciones por estado de cuenta coinciden con el frontend; las demás responden 409.
- [ ] Reportar, anular, suspender y rehabilitar conservan todo el historial.
- [ ] Deshabilitar cierra las sesiones de la cuenta.
- [ ] Las rutas de gestión no operan sobre admins.
- [ ] Cambiar el teléfono mantiene los vínculos y actualiza las agendas.
- [ ] La forma de respuesta coincide con la del frontend.
- [ ] `php artisan test` pasa sobre `YelpMe_pruebas`.
- [ ] Endpoints documentados en `backend/README.md`.
- [ ] Commits por tarea, en español, sin coautoría; PR a `main` al terminar.
