# 035 — Tarea: Perfil propio

> Plan: `006-plan-backend-usuarios-perfil-2026-11-23.md` · Rama: `feature/backend-usuarios` · Estado: ✅ Completada

## Objetivo
Editar datos, foto y contraseña propios (app y panel) y consultar la actividad del admin.

## Endpoints

| Método | Ruta | Quién | Controlador | Éxito |
|---|---|---|---|---|
| PUT/PATCH | `/api/perfil` | Cualquier rol | `PerfilController@update` | "Perfil actualizado" + usuario |
| POST | `/api/perfil/foto` | Cualquier rol | `FotoPerfilController@store` | "Foto actualizada" + usuario |
| DELETE | `/api/perfil/foto` | Cualquier rol | `FotoPerfilController@destroy` | "Foto eliminada" + usuario |
| PUT | `/api/perfil/password` | Cualquier rol | `PasswordController@update` | "Contraseña actualizada" |
| GET | `/api/admin/actividad` | Admin | `Admin\ActividadController@index` | `{ solicitudes, strikes_dados }` |

Consultar el perfil = `GET /api/sesion` (ya existe).

## Pasos

### 1. `PerfilController@update`
- Campos opcionales: `nombre`, `apellidos`, `telefono` (10 dígitos), `correo`. Normalizados (`Normalizar::correo|telefono`) y `trim`.
- Únicos con `Rule::unique(...)->ignore(id propio)`: su mismo correo en mayúsculas no es duplicado.
- **Teléfono nuevo (rol usuario)** → `actualizarAgendas`:
  1. Contactos que me tienen vinculado → número nuevo, salvo dueños que ya guardaron ese número aparte (UNIQUE `usuario_id + telefono`).
     - MySQL error 1093: no se puede consultar `contactos` dentro del `UPDATE` de `contactos`. Se calculan antes los dueños con conflicto (`pluck`).
  2. Contactos sueltos que ya tenían mi número nuevo → se vinculan, salvo dueños que bloqueé (como al registrarse).
  - El vínculo sigue por `usuario_id` (plan 6.1).

### 2. `FotoPerfilController`
- `foto`: `file`, `image`, `mimes:jpg,jpeg,png,webp`, `max:1024` (KB). Mensajes: "La foto debe ser JPG, PNG o WEBP." / "La foto pesa más de 1 MB…".
- `store('', 'perfiles')`: nombre aleatorio. En BD la ruta; en la respuesta la URL (`PresentadorUsuario`).
- Al reemplazar o quitar: se borra el archivo anterior (sin fotos huérfanas).
- Requiere extensión `fileinfo` (activa).

### 3. `PasswordController@update`
- `actual` + `nueva` (mín. 8, máx. 72, `confirmed`, `different:actual`).
- `Hash::check` manual (la regla `current_password` usa el guard `web`, no Sanctum). Incorrecta → 422 en `actual`.
- `ReglasCuenta::cerrarSesiones($id, token actual)`: otros dispositivos vuelven a iniciar sesión; este sigue.

### 4. `Admin\ActividadController@index`
- `PresentadorCuenta::solicitudesBreves(solicitudes con admin_id = yo)` + `COUNT` de `strikes.admin_id`.
- `solicitudesBreves` extraído del detalle de cuenta: lo comparten historial de usuario y actividad.

### 5. Rutas y traducciones
- Perfil en el grupo `auth:sanctum` + `cuenta.activa` (cualquier rol); actividad en el grupo admin.
- Atributos: `foto`, `actual` (contraseña actual), `nueva` (nueva contraseña).

## Verificación
Script `tinker` con transacción revertida y `Storage::fake('perfiles')`:

| Caso | Resultado |
|---|---|
| Teléfono de otro / correo inválido | 422 |
| Su correo en mayúsculas | 200 |
| Nombre y teléfono nuevo | agendas de María y José con el número nuevo |
| José cambia a un número que María ya tenía como "Otro" | Carlos: número nuevo; María: el viejo conserva su número, "Otro" se vincula; Ana (bloqueada por José): no se vincula |
| Foto no imagen / > 1 MB | 422 con mensaje propio |
| Foto válida, reemplazo, quitar | URL; archivo anterior borrado; `foto_perfil: null` |
| Contraseña actual mala / corta y sin confirmar / igual | 422 |
| Contraseña correcta | sesión actual sigue, otra → 401, login con la nueva OK |
| Actividad admin 1 | #3, #7, #9, #11; 6 strikes |
| Actividad con usuario de la app | 403 |

**Sin GD en este PHP:** `UploadedFile::fake()->image()` falla. Se usa un PNG real de 1×1 en bytes (y relleno para > 1 MB). Las pruebas formales hacen lo mismo.

## Resultado
Commit: `feat(backend): agrega perfil propio con foto, contraseña y actividad del admin`.
