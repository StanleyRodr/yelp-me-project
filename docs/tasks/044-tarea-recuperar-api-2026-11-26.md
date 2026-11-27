# 044 — Tarea: Recuperar contraseña (API)

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Recuperación con código de 6 dígitos por correo, para cualquier rol. Reglas: plan principal 5.1.

## Pasos

### 1. Tabla en `database/script-bd.sql`
```sql
CREATE TABLE codigos_recuperacion (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT UNSIGNED NOT NULL,
    codigo      VARCHAR(255)    NOT NULL,   -- hash (Hash::make)
    intentos    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    expira_en   TIMESTAMP       NOT NULL,
    usado_en    TIMESTAMP       NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_codrec_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_codrec_usuario (usuario_id, usado_en)
) ENGINE=InnoDB;
```
Historial: no se borran; "vigente" = `usado_en IS NULL AND expira_en > NOW() AND intentos < 5`. Pedir otro marca los anteriores como usados (`usado_en = now()`).

### 2. `App\Support\Recuperacion`
- `const VIGENCIA_MIN = 15; const MAX_INTENTOS = 5;`
- `emitir(object $usuario): void` → anula vigentes, genera `random_int(0, 999999)` con `str_pad(…, 6, '0')`, guarda hash, envía correo.
- `verificar(string $correo, string $codigo): object` → busca vigente; si falla suma intento; 422 `codigo` "El código no es válido o ya venció." Devuelve fila.

### 3. Correo
- `App\Mail\CodigoRecuperacion` (Mailable, vista `resources/views/correos/codigo.blade.php`): asunto "Tu código de YelpMe", texto con código y vigencia.
- `.env.example`: `MAIL_MAILER=log`, `MAIL_FROM_ADDRESS="no-responder@yelpme.local"`, `MAIL_FROM_NAME="YelpMe"`; comentario con ejemplo SMTP (Gmail con contraseña de aplicación). Sin credenciales reales.

### 4. Controlador `Api/RecuperacionController` + rutas (grupo `throttle:acceso`)
| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/recuperacion` | `correo` | 200 neutro "Si el correo está registrado, te enviamos un código." (exista o no, o esté deshabilitada) |
| POST | `/api/recuperacion/verificacion` | `correo`, `codigo` | 200 `{ valido: true }` o 422 |
| PUT | `/api/recuperacion` | `correo`, `codigo`, `password`, `password_confirmation` | 200 "Contraseña actualizada. Inicia sesión." |

PUT, en `DB::transaction`:
1. `verificar` (con `lockForUpdate`).
2. Reglas de contraseña de `App\Support\Credenciales` (mismas que registro).
3. `password = Hash::make`, `usado_en = now()`.
4. Borrar **todos** los tokens: `DB::table('personal_access_tokens')->where('tokenable_type', User::class)->where('tokenable_id', $id)->delete()`.

Correo normalizado con `Normalizar` (minúsculas, trim).

### 5. Pruebas `tests/Feature/RecuperacionTest.php` (con `Mail::fake()`)
- Correo existente → mail enviado; inexistente → misma respuesta, sin mail.
- Deshabilitada → sin mail.
- Código correcto → verificación OK; incorrecto → 422 y `intentos` +1; al 5.º falla aunque sea correcto.
- Vencido (`travel(16)->minutes()`) → 422.
- Pedir dos veces → el primero ya no sirve.
- Cambio OK → login con la nueva; tokens previos borrados (GET `/api/sesion` con token viejo → 401).
- Contraseña sin confirmación → 422.
- Para conocer el código en la prueba: `Mail::assertSent(CodigoRecuperacion::class, fn ($m) => $codigo = $m->codigo)`.

### 6. `backend/README.md`
Sección "Recuperación de contraseña" con la tabla y cómo leer el código en `storage/logs/laravel.log`.

## Verificación
```bash
php artisan bd:pruebas && php artisan test
```

## Resultado
Commit: `feat(auth): agrega recuperación de contraseña con código por correo`.

Notas: la verificación vive en `VerificacionRecuperacionController@store` (convención de métodos REST). `Recuperacion::comprobar` suma el intento y el 422 se lanza fuera de la transacción para no revertirlo. `Credenciales::REGLAS_PASSWORD` compartida con el registro. BD `YelpMe` recreada.
