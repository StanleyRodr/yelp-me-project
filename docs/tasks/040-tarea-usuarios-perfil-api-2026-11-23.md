# 040 — Tarea: Usuarios y perfil contra la API

> Plan: `007-plan-frontend-api-2026-11-23.md` · Rama: `feature/frontend-api` · Estado: ✅ Completada

## Objetivo
Gestión de cuentas, strikes y suspensiones, y perfil del admin (datos, foto, contraseña, actividad) contra la API.

## Pasos

### 1. `services/usuariosService.js` (sin mocks)
| Función | Endpoint |
|---|---|
| `listar()` / `obtener(id)` | `GET /api/admin/usuarios[/{id}]` |
| `reportar(id, { motivo })` | `POST …/{id}/strikes` |
| `anularStrike(strikeId, { motivo })` | `PUT /api/admin/strikes/{id}/anulacion` |
| `suspender(id, { motivo })` / `rehabilitar(id)` | `POST` / `DELETE …/{id}/suspension` |
| `deshabilitar(id)` / `reactivar(id)` | `PUT …/{id}/cuenta` `{ activo }` |

Todas responden el detalle de la cuenta; el store lo aplica sin otra petición.

### 2. `services/perfilService.js` (nuevo)
| Función | Endpoint |
|---|---|
| `actividad()` | `GET /api/admin/actividad` |
| `actualizar(datos)` | `PUT /api/perfil` (solo nombre, apellidos, teléfono, correo) |
| `subirFoto(archivo)` | `POST /api/perfil/foto` con `FormData` (multipart) |
| `quitarFoto()` | `DELETE /api/perfil/foto` |
| `cambiarPassword({ actual, nueva })` | `PUT /api/perfil/password` + `nueva_confirmation` |

### 3. Stores
- `usuarios`: sin `adminId` ni import de `auth`.
- `perfil.guardarDatos({ datos, foto, quitarFoto })`: datos → (subir **o** quitar foto) → `auth.actualizarAdmin(usuario)` con la última respuesta (navbar y foto se actualizan).

### 4. Formularios
- `FormPerfil`:
  - Guarda el `File` elegido (la vista previa sigue local con `FileReader`).
  - Botón "Quitar foto".
  - Emite `{ datos, foto, quitarFoto }`.
  - Prop `erroresApi`: cada mensaje de la API va bajo su campo ("El teléfono ya está registrado.").
- `FormPassword`: prop `erroresApi` (p. ej. "La contraseña actual no es correcta." bajo "Contraseña actual").
- `PerfilView`: con errores por campo muestra "Revisa los campos marcados." arriba; si no, el mensaje de la API.

## Verificación contra la API real
`YelpMe_pruebas` recreada; servidor en 8010. Script fuera del repo:

| Caso | Resultado |
|---|---|
| Listado | 5 usuarios con estado de cuenta |
| Botones de Luis (`accionesCuenta`) | rehabilitar, reportar, deshabilitar = API |
| Detalle | forma del frontend con nombres de admins |
| Reportar sin motivo / a Ana | 422 / Suspendido con 3 strikes |
| Suspender ya suspendida | 409 "La cuenta está suspendida…" |
| Anular / rehabilitar | 2 strikes y sigue suspendida / Activo con 0 |
| Deshabilitar / reactivar a José | OK |
| Actividad | solicitudes atendidas y strikes dados |
| Teléfono ajeno | 422, `campo('telefono')` |
| Subir foto (multipart desde el service) | URL `…/storage/perfiles/…png`; el backend la sirve (200, `image/png`) |
| Foto PDF | 422 |
| Quitar foto | `foto_perfil: null`; disco `perfiles` vacío al final |
| Contraseña actual mala / cambio | 422 en `actual` / login con la nueva |

`npm run build` sin errores.

## Resultado
Commit: `feat(frontend): conecta usuarios y perfil a la API`.
