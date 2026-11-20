# 021 — Tarea: Pruebas de autenticación

> Plan: `003-plan-backend-autenticacion-2026-11-19.md` · Rama: `feature/backend-auth` · Estado: ✅ Completada

## Objetivo
Pruebas automáticas de todo el plan 003 sobre una BD aislada, sin tocar `YelpMe`.

## Problema
- `phpunit.xml` venía con SQLite en memoria. El esquema vive en `script-bd.sql` (MySQL) y no hay migraciones: SQLite queda vacío.
- Probar sobre `YelpMe` mezclaría datos de prueba con desarrollo.

## Pasos

### 1. Comando `php artisan bd:pruebas` (`app/Console/Commands/PrepararBdPruebas.php`)
1. Opción `--nombre` (por defecto `YelpMe_pruebas`). **Solo** acepta `^[A-Za-z0-9_]+_pruebas$`. `--nombre=YelpMe` → error, no hace nada.
2. Lee `../database/script-bd.sql` y cambia solo `CREATE DATABASE IF NOT EXISTS YelpMe` y `USE YelpMe;`. Si no encuentra exactamente uno de cada → error (script cambió de forma).
3. `DB::unprepared("DROP DATABASE IF EXISTS `<nombre>`;" . script)`.
4. Cambia la conexión a la BD nueva y ejecuta `db:seed` (datos de demo).
- Correrlo de nuevo tras cambiar el script o el seeder.

### 2. `phpunit.xml`
- `DB_CONNECTION=mysql`, `DB_DATABASE=YelpMe_pruebas`. Usuario y contraseña salen del `.env`.
- `CACHE_STORE=array`: el límite de intentos se reinicia en cada prueba.
- `BCRYPT_ROUNDS=4`: registros rápidos. Los hashes del seeder (cost 12) se verifican igual.

### 3. `tests/TestCase.php`
- `use DatabaseTransactions`: cada prueba se revierte.
- `setUp()`: si la BD no termina en `_pruebas` → `fail()`. Segunda barrera contra tocar desarrollo.
- `reiniciarAuth()`: `forgetGuards()` entre peticiones de una misma prueba. Sin esto, el guard recuerda al usuario y un token cerrado parece válido.

### 4. `tests/Feature/AutenticacionTest.php` (16 pruebas)

| Grupo | Pruebas |
|---|---|
| Registro | crea usuario + token + vincula contacto (normaliza correo y teléfono, sin password ni fcm_token) · ignora `rol=admin` · duplicados y confirmación distinta con mensajes exactos |
| App | login por correo guarda FCM · login por teléfono con guiones · mismo mensaje con cuenta inexistente · pide correo o teléfono · admin no entra · suspendido entra con `suspendido: true` · deshabilitado no entra ni usa token previo · consultar y cerrar (borra FCM, invalida solo ese token) · sin token → 401 JSON aunque pida HTML |
| Panel | admin entra (token `panel`) · usuario no entra |
| Roles | matriz admin/usuario × zona admin/app con rutas temporales |
| Límite | 6º intento en un minuto → 429 |

### 5. Documentación
- `backend/README.md`: sección "Pruebas" (comandos, una sola prueba, protecciones).
- `CLAUDE.md`: comandos del backend, reglas de pruebas y de rutas protegidas.

## Verificación

```bash
php artisan bd:pruebas --nombre=YelpMe   # rechazado
php artisan bd:pruebas                   # crea YelpMe_pruebas + demo
php artisan test                         # 20 passed (66 assertions)
php artisan test                         # 20 passed otra vez: transacciones se revierten
```

Después de las pruebas, en ambas BD: 7 usuarios, 0 tokens, sin la cuenta de prueba "rosa".

## Resultado
Commit: `test(backend): agrega BD de pruebas y pruebas de autenticación`.
