# 054 — Tarea: Domicilio en la API

> Plan: `009-plan-perfil-2026-11-26.md` · Rama: `feature/perfil-domicilio` · Estado: ✅ Terminada

## Objetivo
Domicilio único por usuario y `veces_suspendido` en el usuario.

## Pasos

### 1. `database/script-bd.sql`
```sql
CREATE TABLE domicilios (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id     BIGINT UNSIGNED NOT NULL,
    calle          VARCHAR(120)    NOT NULL,
    numero         VARCHAR(20)     NOT NULL,
    colonia        VARCHAR(100)    NOT NULL,
    codigo_postal  CHAR(5)         NOT NULL,
    referencia     VARCHAR(255)    NULL,
    latitud        DECIMAL(10, 8)  NOT NULL,
    longitud       DECIMAL(11, 8)  NOT NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_domicilios_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_domicilios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT
) ENGINE=InnoDB;
```
Seeder: domicilio para Carlos y Ana (Misantla, coordenadas cercanas a 19.9311, -96.8519). María sin domicilio.

### 2. `App\Support\PresentadorDomicilio`
```php
public static function deUsuario(int $usuarioId): ?array   // null si no tiene
public static function datos(object $d): array             // calle, numero, colonia, codigo_postal, referencia, latitud (float), longitud (float), texto, updated_at
public static function porUsuarios(array $ids): Collection // en lote, para listados (plan 010)
```
`texto` = "Calle Número, Colonia, CP" (lo usa la app en `ubicacion_nombre`).

### 3. `Api/DomicilioController` (rol `usuario`)
| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/perfil/domicilio` | domicilio o `null` |
| PUT | `/api/perfil/domicilio` | domicilio (crea o reemplaza; `updateOrInsert` por `usuario_id`) |
| DELETE | `/api/perfil/domicilio` | `null` (404 "No tienes un domicilio registrado." si no hay) |

Reglas: `calle` `required|string|max:120`, `numero` `required|string|max:20`, `colonia` `required|string|max:100`, `codigo_postal` `required|digits:5`, `referencia` `nullable|string|max:255`, `latitud` `required|numeric|between:-90,90`, `longitud` `required|numeric|between:-180,180`. Mensajes en español. `usuario_id` del token.

Rutas dentro del grupo `rol:usuario` de `routes/api.php`.

### 4. `veces_suspendido`
`PresentadorUsuario::datos` agrega `'veces_suspendido' => DB::table('suspensiones')->where('usuario_id', $u->id)->count()`. Mover a `ReglasCuenta::vecesSuspendido()`.

### 5. Admin
- `PresentadorCuenta::detalle` agrega `domicilio` (o `null`).
- `PresentadorSolicitud` en `Admin/SolicitudController@show`: `usuario.domicilio`. Listados no (evitar N+1).

### 6. Pruebas `tests/Feature/DomicilioTest.php`
- Sin domicilio → `null`; PUT crea; PUT otra vez reemplaza (sigue 1 fila); DELETE borra; DELETE sin domicilio → 404.
- CP de 4 dígitos → 422; sin coordenadas → 422.
- Admin no usa la ruta (403). Usuario B no ve el de A (cada uno ve el suyo).
- Detalle admin de usuario y de solicitud incluye domicilio.
- `GET /api/sesion` de Luis → `veces_suspendido` = 2.

### 7. `backend/README.md`
Sección "Domicilio".

## Verificación
```bash
php artisan bd:pruebas && php artisan test
```

## Resultado
Commit: `feat(perfil): agrega domicilio del usuario y veces suspendido`.

Notas: 93 pruebas OK (6 nuevas en `DomicilioTest`). El 404 "No tienes un domicilio registrado." va con `throw new HttpException(404, ...)`: el render de `bootstrap/app.php` cambia toda `NotFoundHttpException` (también `abort(404, ...)`) por "Recurso no encontrado.". `GET /api/sesion` devuelve el usuario en `datos` (no `datos.usuario`). En la BD `YelpMe` se agregó la tabla y los domicilios de demo sin recrearla.
