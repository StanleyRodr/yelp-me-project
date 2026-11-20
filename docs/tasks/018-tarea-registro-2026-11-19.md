# 018 — Tarea: Registro

> Plan: `003-plan-backend-autenticacion-2026-11-19.md` · Rama: `feature/backend-auth` · Estado: ✅ Completada

## Objetivo
`POST /api/registro`: alta pública de usuarios de la app. Devuelve token (sesión iniciada) y vincula contactos existentes con su teléfono.

## Petición

```json
{
  "nombre": "Rosa", "apellidos": "Prueba",
  "telefono": "2359990001", "correo": "rosa@correo.com",
  "password": "secreto123", "password_confirmation": "secreto123",
  "fcm_token": "opcional"
}
```

## Respuesta 201

```json
{ "ok": true, "mensaje": "Cuenta creada",
  "datos": { "token": "1|…", "usuario": { …PresentadorUsuario }, "contactos_vinculados": 1 } }
```

## Pasos

### 1. `app/Http/Controllers/Api/RegistroController@store`
1. Normalizar antes de validar (`$request->merge`):
   - correo: minúsculas, sin espacios. `Rosa@Correo.com` y `rosa@correo.com` son el mismo.
   - teléfono: solo dígitos. `235 999 0001` → `2359990001`.
2. Validar:

| Campo | Reglas |
|---|---|
| nombre, apellidos | required, string, max:100 |
| telefono | required, digits:10, unique:usuarios |
| correo | required, email, max:150, unique:usuarios |
| password | required, min:8, max:72, confirmed |
| fcm_token | nullable, max:255 |

   - `max:72`: bcrypt ignora lo que pase de 72 bytes.
3. Transacción (`DB::transaction`):
   - `DB::table('usuarios')->insertGetId(...)` con `Hash::make`, `rol = 'usuario'` fijo. El rol nunca viene de la petición.
   - Vincular: `contactos` con ese teléfono y `usuario_contacto_id NULL` de otros dueños → `usuario_contacto_id = id nuevo`.
4. Token: `User::findOrFail($id)->createToken('app')`. Único uso del modelo (Sanctum).
5. Responder con `PresentadorUsuario` (sin password ni fcm_token).

### 2. Ruta
`routes/api.php`, grupo `throttle:acceso`:

```php
Route::post('/registro', [RegistroController::class, 'store']);
```

## Verificación
Kernel HTTP en `tinker`:

| Caso | Resultado |
|---|---|
| Cuerpo vacío | 422, 5 campos obligatorios en español |
| Teléfono y correo de Carlos (correo en mayúsculas), confirmación distinta | 422: teléfono ya registrado, correo ya registrado, confirmación no coincide |
| Datos válidos con espacios en teléfono y mayúsculas en correo | 201, correo y teléfono normalizados, `rol: usuario`, token, `contactos_vinculados: 1` (contacto "Mamá" de Carlos) |
| Respuesta | Sin `password` |

Limpieza: contacto desvinculado, token y usuario de prueba borrados. Queda el autoincremento en 9 (sin efecto).

## Notas
- Usuario suspendido o deshabilitado no aplica: la cuenta es nueva.
- Contactos de dueños que el nuevo usuario bloquee después se manejan en el plan de contactos.

## Resultado
Commit: `feat(backend): agrega registro de usuarios de la app`.
