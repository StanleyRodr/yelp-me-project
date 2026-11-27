# 043 — Tarea: Apellidos en dos columnas

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ⬜ Pendiente

## Objetivo
`usuarios.apellidos` → `apellido_paterno` (obligatorio) + `apellido_materno` (opcional) en BD, API, pruebas y panel.

## Pasos

### 1. `database/script-bd.sql`
```sql
apellido_paterno  VARCHAR(60)   NOT NULL,
apellido_materno  VARCHAR(60)   NULL,
```
en lugar de `apellidos`. Admin inicial: `('Administrador', 'General', NULL, ...)`.

### 2. Seeder (`backend/database/seeders/DatabaseSeeder.php`)
- El helper `$usuario` recibe `paterno` y `materno`.
- 'Ramírez Soto' → `Ramírez`, `Soto`; igual con los demás. Laura admin igual.

### 3. Backend: buscar y cambiar todo `apellidos`
```bash
grep -rn "apellidos" backend/app backend/tests
```
| Lugar | Cambio |
|---|---|
| `Support/PresentadorUsuario::base` | `apellido_paterno`, `apellido_materno` (ya no `apellidos`). |
| `RegistroController` | Reglas `apellido_paterno` `required|string|max:60`, `apellido_materno` `nullable|string|max:60`; `trim`, vacío → `null`. |
| `PerfilController` | Mismas reglas con `sometimes`. |
| `VinculoController`, `BloqueoController` | Seleccionar y devolver ambos campos. |
| `Support/PresentadorSolicitud`, `PresentadorCuenta` | Nombre compuesto con helper nuevo. |
| `Admin/UsuarioController` | `orderBy('apellido_paterno')->orderBy('apellido_materno')`; búsqueda en ambos. |
| `Models/User` | `$fillable`. |

Helper en `App\Support\Normalizar` (o `PresentadorUsuario`):
```php
public static function nombreCompleto(object $u): string
{
    return trim(implode(' ', array_filter([$u->nombre, $u->apellido_paterno, $u->apellido_materno ?? null])));
}
```

### 4. Pruebas
- Recrear: `php artisan bd:pruebas`.
- Actualizar payloads de registro/perfil en `tests/Feature/*`.
- Casos nuevos: registro sin materno OK (`apellido_materno: null`); sin paterno 422; perfil cambia solo el materno.

### 5. Panel (`frontend/`)
| Archivo | Cambio |
|---|---|
| `utils/formato.js` `nombreCompleto` | `[p.nombre, p.apellido_paterno, p.apellido_materno].filter(Boolean).join(' ')`. |
| `components/perfil/FormPerfil.vue` | Dos campos: "Apellido paterno" (obligatorio, `family-name`) y "Apellido materno" (opcional). Validación y `emit` con ambos. |
| `services/perfilService.js` `actualizar` | Enviar `apellido_paterno`, `apellido_materno`. |
| `AppNavbar.vue`, `CabeceraUsuario.vue`, iniciales de `FormPerfil` | Usar `nombre` + `apellido_paterno`. |
| Filtro de usuarios (`FiltrosUsuarios`/store) | Buscar en ambos apellidos. |
```bash
grep -rn "apellidos" frontend/src
```
Debe quedar vacío.

### 6. Documentación
- `backend/README.md`: cuerpos de registro y perfil.
- Avisar en la tarea: recrear `YelpMe` con `script-bd.sql` + `php artisan db:seed`.

## Verificación
```bash
cd backend && php artisan bd:pruebas && php artisan test
cd frontend && npm run build
```
Panel: perfil guarda materno vacío; navbar muestra "Administrador General".

## Resultado
Commit: `refactor(usuarios): separa apellidos en paterno y materno`.
