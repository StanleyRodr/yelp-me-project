# 003 — Plan: Autenticación del backend

> Fecha: 2026-11-19 · Rama: `feature/backend-auth` (apilada sobre `feature/frontend-base`)
> Referencia: `docs/plans/principal-plan.md` (secciones 3.2, 5 y 8)

## Objetivo

Implementar en la API el registro y el inicio de sesión de usuarios de la app, el inicio de sesión de administradores, el cierre de sesión y la protección de endpoints por rol y por estado de cuenta, con tokens de Sanctum. Dejar pruebas automáticas sobre una base de datos de pruebas aislada.

**No incluye:** conectar el frontend ni Android a estos endpoints (planes posteriores), recuperación de contraseña (fuera del MVP) ni notificaciones FCM (solo se guarda el token del dispositivo).

## Punto de partida

- Sanctum instalado; `App\Models\User` sobre `usuarios` con `HasApiTokens` (plan 001).
- Respuestas y errores JSON en español; 401 sin redirección (plan 001).
- `phpunit.xml` usa SQLite en memoria: no sirve para pruebas con BD (el esquema vive en `script-bd.sql`, MySQL).

## Decisiones

### Endpoints

| Método | Ruta | Acceso | Controlador |
|---|---|---|---|
| POST | `/api/registro` | Público, con límite de intentos | `RegistroController@store` |
| POST | `/api/login` | Público, con límite de intentos (solo rol `usuario`) | `SesionController@store` |
| POST | `/api/admin/login` | Público, con límite de intentos (solo rol `admin`) | `Admin\SesionController@store` |
| GET | `/api/sesion` | Con token | `SesionController@show` |
| DELETE | `/api/sesion` | Con token | `SesionController@destroy` |

- Los controladores siguen la convención de recurso: la sesión es el recurso (`store` = iniciar, `show` = consultar, `destroy` = cerrar).
- Grupos de rutas listos para planes siguientes: `auth:sanctum` + `cuenta.activa` + `rol:usuario` (app) y `rol:admin` (panel).

### Reglas
- **Registro:** nombre, apellidos, teléfono (10 dígitos, único), correo (único), contraseña (mín. 8, con confirmación). Siempre crea rol `usuario`. Devuelve token: la app queda con sesión iniciada.
- **Vinculación al registrarse:** los contactos de otros usuarios que tengan ese teléfono se vinculan al nuevo usuario (plan principal 6.1).
- **Login de la app:** por correo **o** teléfono + contraseña. Acepta `fcm_token` opcional del dispositivo.
- **Login del panel:** por correo + contraseña.
- **Rol equivocado:** admin en `/api/login` o usuario en `/api/admin/login` → 403 con mensaje que indica dónde entrar.
- **Cuenta deshabilitada:** no inicia sesión (403). Si ya tenía token, el middleware `cuenta.activa` responde 403 en cualquier ruta protegida.
- **Cuenta suspendida:** sí inicia sesión; `/api/sesion` informa `suspendido: true` para que la app desactive pedir servicios.
- **Credenciales incorrectas:** 401 con el mismo mensaje exista o no la cuenta (no revela qué correos están registrados).
- **Límite de intentos:** 5 por minuto por IP + identificador en registro y logins → 429.
- **Cierre de sesión:** borra solo el token actual; en la app también limpia `fcm_token`.
- **Tokens sin expiración** en el MVP (`sanctum.expiration = null`).
- **Sin modelos:** `User` solo para tokens; inserciones y consultas con `DB::table`.

### Pruebas
- BD aislada `YelpMe_pruebas`, creada desde `script-bd.sql` con un comando de artisan que **solo** acepta nombres terminados en `_pruebas` (no puede borrar `YelpMe`).
- Cada prueba corre en una transacción que se revierte (`DatabaseTransactions`).

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 017 | [Base de autenticación](../tasks/017-tarea-base-autenticacion-2026-11-19.md) | Middlewares `rol` y `cuenta.activa`, límite de intentos, presentador de usuario y traducciones. | Pendiente |
| 018 | [Registro](../tasks/018-tarea-registro-2026-11-19.md) | `POST /api/registro` con validación, token y vinculación de contactos por teléfono. | Pendiente |
| 019 | [Sesión de la app](../tasks/019-tarea-sesion-app-2026-11-19.md) | Login por correo o teléfono, consultar y cerrar sesión, token FCM. | Pendiente |
| 020 | [Sesión del panel](../tasks/020-tarea-sesion-panel-2026-11-19.md) | Login de admin y grupos de rutas protegidas por rol. | Pendiente |
| 021 | [Pruebas de autenticación](../tasks/021-tarea-pruebas-autenticacion-2026-11-19.md) | BD de pruebas desde el script y pruebas de todos los casos. | Pendiente |

---

## Criterios de terminado

- [ ] Los 5 endpoints responden con el formato estándar y mensajes en español.
- [ ] Un usuario no puede entrar al panel ni un admin a la app.
- [ ] Una cuenta deshabilitada no inicia sesión ni usa un token previo.
- [ ] El registro vincula contactos existentes con el mismo teléfono.
- [ ] El sexto intento de login en un minuto devuelve 429.
- [ ] `php artisan test` pasa sobre `YelpMe_pruebas` sin tocar `YelpMe`.
- [ ] Endpoints documentados en `backend/README.md`.
- [ ] Commits por tarea, en español, sin coautoría; PR al terminar.
