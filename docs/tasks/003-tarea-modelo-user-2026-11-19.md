# 003 — Tarea: Adaptar el modelo `User` a `usuarios`

> Plan: `001-plan-backend-base-2026-11-19.md` · Rama: `feature/backend-base` · Estado: ✅ Completada

## Objetivo
`App\Models\User` funciona sobre tabla `usuarios` y emite tokens Sanctum.

## Por qué existe este modelo
Regla del proyecto: sin modelos Eloquent. Excepción única: Sanctum necesita modelo con `HasApiTokens` para `createToken()`. Solo autenticación. Resto del código usa `DB::table`.

## Pasos

Editar `backend/app/Models/User.php`:

### 1. Tabla y traits

```php
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'usuarios';
```

- Quitar `HasFactory` y `Notifiable`. Factory ya no existe. Notificaciones van por FCM, no por Laravel.

### 2. Desactivar `remember_token`
Tabla no tiene esa columna. API usa tokens, no cookies "recordarme".

```php
protected $rememberTokenName = false;
```

### 3. Campos asignables

```php
protected $fillable = [
    'nombre', 'apellidos', 'telefono', 'correo', 'password',
    'rol', 'foto_perfil', 'fcm_token', 'activo',
];
```

### 4. Campos ocultos en JSON
`password` y `fcm_token` nunca salen en respuestas.

```php
protected $hidden = ['password', 'fcm_token'];
```

### 5. Casts

```php
protected function casts(): array
{
    return [
        'password' => 'hashed',
        'activo' => 'boolean',
        'deshabilitado_en' => 'datetime',
    ];
}
```

- `hashed`: asignar contraseña en texto plano la cifra sola. No cifrar dos veces.

### 6. Comentario en la clase
Dejar docblock: único modelo, solo para Sanctum.

## Verificación
En `php artisan tinker`:

```php
$u = App\Models\User::where('correo', 'admin@yelpme.com')->first(); // admin
Hash::check('Admin123*', $u->password);                              // true
$u->createToken('prueba');                                           // fila en personal_access_tokens
$u->toArray();                                                       // sin password ni fcm_token
$u->tokens()->delete();                                              // limpiar token de prueba
```

## Notas
- `config/auth.php` ya apunta a `App\Models\User`. No cambiar.
- Login compara `correo` o `telefono`, no `email`. Se define en plan de autenticación.

## Resultado
Todo verificado. Commit: `feat(backend): adapta modelo User a tabla usuarios con Sanctum`.
