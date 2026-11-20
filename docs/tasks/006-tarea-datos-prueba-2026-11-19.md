# 006 — Tarea: Datos de prueba

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
Datos de demo que cubren todos los casos del plan principal. Sin modelos ni factories.

## Reglas
- Solo `DB::table(...)` y `Hash::make`. Nada de `User::factory()`.
- No duplicar datos del script: admin inicial (id 1) y servicios ya existen.
- Todo dentro de `DB::transaction`: falla uno, no queda nada a medias.
- Idempotente: si existe `laura.admin@yelpme.com`, salir sin insertar.

## Pasos

### 1. Reescribir `database/seeders/DatabaseSeeder.php`
Quitar `use App\Models\User` y la llamada a factory.

### 2. Usuarios
Helper `$usuario(...)` → `insertGetId` en `usuarios`.

| Usuario | Rol | Teléfono | Uso en demo |
|---|---|---|---|
| Laura | admin | 2350000001 | Segundo admin |
| Carlos | usuario | 2351000001 | Usuario principal |
| María | usuario | 2351000002 | Solicitudes sin servicios |
| José | usuario | 2351000003 | Bloquea a Ana |
| Ana | usuario | 2351000004 | 2 strikes |
| Luis | usuario | 2351000005 | Suspendido |

Contraseñas: admins `Admin123*`, usuarios `Usuario123*`. Calcular hash una vez, reutilizar.

### 3. Contactos
- Vinculado: teléfono de usuario registrado → `usuario_contacto_id` con su id.
- No vinculado: teléfono externo (`23599900xx`) → `usuario_contacto_id = null`.
- Resultado: 4 vinculados, 3 no vinculados.

### 4. Bloqueo
José bloquea a Ana: fila en `bloqueos` (`usuario_id` = José, `bloqueado_id` = Ana).

### 5. Solicitudes
- Servicios por nombre: `DB::table('servicios')->pluck('id', 'nombre')`.
- Helper `$solicitud($datos, $servicios)`: inserta solicitud y filas de `solicitud_servicio`.
- Coordenadas reales de Misantla, Ver. (~19.93, -96.85).
- Fechas relativas a `now()` (`subDays`, `subHours`). Demo siempre se ve reciente.

| Estado | Usuario | Servicios | Campos clave |
|---|---|---|---|
| Pendiente | Carlos | Bomberos, Protección Civil | — |
| Pendiente | María | ninguno | Solo contactos |
| Enviado | Carlos | Ambulancia, Policía | `admin_id`, `enviado_en` |
| Terminado | José | Tránsito, Ambulancia | Cerrada por admin |
| Terminado | María | ninguno | Cerrada por la usuaria |
| Cancelado | Carlos | Protección Civil | `motivo`, cerrada por el usuario |
| Rechazado ×5 | Ana ×2, Luis ×3 | Policía | `motivo`, cerrada por admin + strike |

Coherencia: cada estado lleva solo los campos que su transición llena (ver plan principal 7.2).

### 6. Strikes y suspensiones
- Helper `$rechazo(...)`: solicitud `Rechazado` + strike activo ligado (`solicitud_id`).
- Ana: 2 rechazos → 2 strikes activos. No suspendida.
- Luis, historial anterior:
  - 3 strikes manuales (`solicitud_id = null`, `activo = false`).
  - Suspensión automática (`suspendido_por_id = null`) rehabilitada por admin 1.
- Luis, actual: 3 rechazos → 3 strikes activos + suspensión vigente (`fecha_rehabilitacion = null`).

### 7. Documentar
README del backend: tabla de credenciales y caso de demo de cada usuario.

## Verificación

```bash
php artisan db:seed   # "Datos de demo cargados."
php artisan db:seed   # "Datos de demo ya cargados; no se hace nada."
```

En `tinker`, conteos esperados:

| Consulta | Esperado |
|---|---|
| Usuarios por rol | admin 2, usuario 5 |
| Solicitudes por estado | Pendiente 2, Enviado 1, Rechazado 5, Cancelado 1, Terminado 2 |
| Solicitudes sin servicios | 2 |
| Contactos vinculados / no | 4 / 3 |
| Strikes activos | Ana 2, Luis 3 |
| Suspensión vigente | Luis |
| Veces suspendido Luis | 2 |
| `Hash::check('Usuario123*', ...)` | true |

## Notas
- Reiniciar demo: `DROP DATABASE YelpMe;`, correr `database/script-bd.sql` y luego `db:seed`. El script no borra tablas: sobre una BD existente falla con "table already exists". Borrar BD es irreversible.
- Consultas con `join`: calificar columnas repetidas (`strikes.activo`), si no MySQL da error `1052 ambiguous`.

## Resultado
Todo verificado. Commit: `feat(backend): agrega datos de prueba para la demo`.
