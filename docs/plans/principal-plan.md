# Plan principal — YelpMe

> Sistema de contactos y solicitudes de emergencia.
> Creado: 2026-11-19 · Última actualización: 2026-11-19
> Tipo: prototipo académico (MVP). **No se conecta con servicios reales de emergencia.**

Este documento reúne el contexto general del proyecto: objetivo, alcance, reglas de negocio, stack y decisiones de diseño. Los planes específicos (`docs/plans/`) y sus tareas (`docs/tasks/`) se derivan de aquí. **No contiene tareas.**

---

## 1. Objetivo

Desarrollar una aplicación que permita a las personas **administrar contactos de emergencia** y **registrar solicitudes de ayuda con su ubicación**, y a un equipo de administradores **atender esas solicitudes** desde un panel web.

Una emergencia puede requerir:

- **Servicios de emergencia:** Policía, Ambulancia, Bomberos, Protección Civil, Tránsito, etc. Los atiende un administrador.
- **Contactos de confianza:** amistades, pareja, familia. Para situaciones que no ameritan un servicio de emergencias, pero sí el apoyo de alguien cercano. Se les avisa con notificación push y se les puede llamar directamente.

---

## 2. Metas del proyecto

1. Usuarios de Android se registran, inician sesión y gestionan su agenda de contactos de confianza (**CRUD completo contra la API**).
2. Usuarios crean solicitudes de ayuda con su ubicación GPS y, opcionalmente, los servicios que necesitan.
3. Los contactos de confianza vinculados reciben una notificación push cuando el usuario crea una solicitud; el usuario recibe una notificación cuando su solicitud cambia de estado.
4. Usuarios llaman directamente a sus contactos desde la app.
5. Administradores atienden las solicitudes desde la web: ven ubicación, corrigen servicios, cambian estado y consultan el historial con el administrador que atendió cada una.
6. Administradores sancionan el mal uso con strikes; 3 strikes suspenden la cuenta para pedir servicios.
7. Toda la información está protegida por autenticación con token y cada registro pertenece a su usuario.

---

## 3. Reglas obligatorias (requisitos académicos)

### 3.1 CRUD móvil completo
La app Android debe realizar al menos un CRUD completo (**Crear → Consultar → Modificar → Eliminar**) enviando las operaciones a la API. Un CRUD solo en la web **no** cumple.

**Entidad elegida:** Contactos de confianza (agenda del usuario).

### 3.2 Autenticación y propiedad de registros
- Login, registro, token, autorización y endpoints protegidos.
- Cada registro (contacto, solicitud) pertenece al usuario autenticado.
- El `usuario_id` se obtiene **del token**, nunca del cuerpo de la petición.
- El usuario A **no puede** consultar, modificar ni eliminar registros del usuario B.

---

## 4. Actores y funcionalidades

### 4.1 Usuario (Android)
- **Cuenta:** registrarse (teléfono obligatorio), iniciar sesión, cerrar sesión, editar perfil (nombre, foto, teléfono).
- **Contactos de confianza:** agregar, consultar, modificar, eliminar; llamar con un botón.
- **Quién me tiene como contacto:** ver la lista y desvincularse o bloquear.
- **Solicitudes de ayuda:**
  - Crear solicitud con ubicación GPS y servicios (opcionales).
  - Consultar sus solicitudes y su historial.
  - Modificar solicitud **solo mientras esté `Pendiente`**.
  - Cancelar solicitud (con motivo).
  - Darla por `Terminado` (según las transiciones de la sección 7.2).
- **Notificaciones:** recibir alertas cuando un contacto vinculado crea una solicitud y cuando su propia solicitud cambia de estado.
- **Perfil:** ver sus strikes activos y cuántas veces ha sido suspendido.

### 4.2 Administrador (Web)
- Iniciar y cerrar sesión (sin registro público).
- Dashboard con solicitudes y su ubicación en mapa; actualización automática por consulta periódica.
- Consultar incidentes y su detalle.
- Editar los servicios requeridos (en `Pendiente` y `Enviado`).
- Cambiar estado: `Enviado`, `Rechazado` (con motivo y casilla de strike), `Terminado`.
- Consultar historial, incluyendo qué administrador atendió cada solicitud.
- **Gestión de usuarios:** ver perfil e historial (solicitudes, strikes, suspensiones), dar strike / reportar, anular strike por error (con motivo), suspender / rehabilitar, deshabilitar / reactivar cuenta.
- Perfil propio.

---

## 5. Cuentas y autenticación

- **Una sola tabla `usuarios`** con columna `rol` (`admin` / `usuario`).
- Autenticación por token con **Laravel Sanctum**.
- **Excepción a la regla "sin modelos":** se conserva `App\Models\User` **solo para autenticación**, porque Sanctum (`HasApiTokens` / `createToken()`) lo requiere. Todo lo demás se maneja con `DB::table`.

| Tipo          | Cliente | Login | Registro |
|---------------|---------|-------|----------|
| Usuario       | Android | Sí    | Sí, desde la app (endpoint público). |
| Administrador | Web     | Sí    | **No.** Un encargado da de alta las credenciales directamente en la BD o mediante un seeder. |

- Contraseñas siempre con hash (`Hash::make` / bcrypt), incluidas las de administradores dadas de alta a mano.
- Endpoints de administración solo aceptan tokens con rol `admin`; endpoints de usuario solo operan sobre registros propios.
- **Logout** elimina el token de Sanctum y el token FCM del dispositivo.
- Rutas de login con **límite de intentos** (`throttle`).
- Las cuentas **no se eliminan, se deshabilitan** (`activo = 0`), y solo un admin puede hacerlo y reactivarlas. Una cuenta deshabilitada no inicia sesión, no recibe notificaciones y conserva su teléfono y correo reservados; sus solicitudes y vínculos se mantienen.

---

## 6. Contactos de confianza

### 6.1 Modelo mixto
- Cada contacto de la agenda guarda **nombre, teléfono y parentesco** (como una agenda normal). Esto da un CRUD completo con datos reales que modificar.
- Si el teléfono **coincide con un usuario registrado**, el contacto se **vincula automáticamente** (`usuario_contacto_id`).
  - Contacto **vinculado:** recibe notificaciones push de emergencia y se le puede llamar.
  - Contacto **no vinculado:** solo se le puede llamar.
- Al registrarse un usuario nuevo, se vinculan automáticamente los contactos existentes de otros usuarios que tengan su teléfono.
- La búsqueda por teléfono **no expone datos personales**; solo indica si el número tiene cuenta y se vinculará.
- El vínculo se basa en `usuario_id`, así que se conserva aunque el contacto cambie de teléfono.

### 6.2 Consentimiento (mínimo)
- El usuario B puede ver **quién lo tiene como contacto**.
- B puede **desvincularse** o **bloquear** a A: en ambos casos **el contacto se elimina de la agenda de A**.
- Si B bloquea a A, A no puede volver a vincularlo. B puede **desbloquear** a A (el registro del bloqueo se borra). El contacto borrado no vuelve, pero si A ya había guardado de nuevo a B, ese contacto se vincula.

### 6.3 Llamada directa
- Botón que llama al teléfono del contacto: `Intent ACTION_CALL` con permiso `CALL_PHONE`; si no se concede, `ACTION_DIAL`.

---

## 7. Solicitudes de ayuda

### 7.1 Estados

| Estado      | Quién lo asigna       | Significado |
|-------------|-----------------------|-------------|
| `Pendiente` | Usuario (al crearla)  | Solicitud enviada, sin atender. Único estado editable por el usuario. |
| `Enviado`   | Administrador         | El administrador despachó los servicios de emergencia. |
| `Rechazado` | Administrador         | El admin la rechaza (p. ej. broma). Motivo obligatorio. |
| `Cancelado` | Usuario               | El usuario la cancela. Motivo obligatorio. |
| `Terminado` | Administrador o usuario | La atención concluyó. |

`Rechazado`, `Cancelado` y `Terminado` son **estados finales**.

### 7.2 Transiciones permitidas

**Solicitud con servicios:**

| Desde       | Hacia       | Quién | Condición |
|-------------|-------------|-------|-----------|
| `Pendiente` | `Enviado`   | Admin | — |
| `Pendiente` | `Rechazado` | Admin | Motivo; casilla opcional de strike. |
| `Pendiente` | `Cancelado` | Usuario | Motivo. |
| `Enviado`   | `Terminado` | Admin o usuario | — |
| `Enviado`   | `Cancelado` | Usuario | Motivo. |

**Solicitud sin servicios (solo contactos, informativa para el admin):**

| Desde       | Hacia       | Quién | Condición |
|-------------|-------------|-------|-----------|
| `Pendiente` | `Terminado` | Usuario | — |
| `Pendiente` | `Cancelado` | Usuario | Motivo. |
| `Pendiente` | `Rechazado` | Admin | Solo por mal uso; motivo y casilla de strike. |

Cualquier otra transición se rechaza en el backend.

### 7.3 Servicios requeridos
- Catálogo fijo de servicios (Policía, Ambulancia, Bomberos, Tránsito, Protección Civil, etc.) cargado con un seeder.
- Relación **muchos a muchos** solicitud ↔ servicio. Los servicios son **opcionales**.
- El usuario los elige en el formulario de Android.
- El administrador puede **corregir los servicios** de solicitudes con servicios en `Pendiente` y `Enviado`.
  - Ej.: piden solo ambulancia para una persona asesinada → se agrega Policía.
  - Ej.: piden solo Policía en un accidente con heridos → se agregan Tránsito y Ambulancia.
- **Representación visual:** íconos de **Material Symbols** (el mismo ícono en web y Android). El catálogo guarda el nombre del ícono (p. ej. `local_police`, `ambulance`, `fire_truck`, `traffic`), no una imagen.
  - Web: paquete npm de Material Symbols.
  - Android: Vector Asset de Android Studio.

### 7.4 Concurrencia y trazabilidad
- Antes de aplicar una acción, el backend **valida el estado actual**; si ya cambió, la rechaza. Así dos administradores no atienden la misma solicitud.
- Cada solicitud registra **qué administrador la atendió** (el que la despacha o la rechaza) y el historial lo muestra.
- Se guarda quién cerró la solicitud y el motivo, cuando aplica.

---

## 8. Strikes y suspensiones

- **Origen de un strike:**
  - Al rechazar una solicitud, el admin marca la casilla **"Es broma / mal uso"**.
  - Desde la vista Usuarios, el admin puede **reportar** al usuario manualmente.
  - Cada strike guarda motivo, admin que lo dio, fecha y solicitud relacionada (si aplica).
- **Anulación:** un strike dado por error se anula con motivo; deja de contar, pero se conserva con quién lo anuló y cuándo.
- **3 strikes activos → suspensión automática.** El admin también puede suspender manualmente.
- **Usuario suspendido:**
  - Puede iniciar sesión, gestionar contactos, llamar y crear solicitudes **sin servicios** (avisar a sus contactos).
  - **No puede** crear solicitudes con servicios ni agregar servicios a una solicitud.
- **Rehabilitación:** solo un admin. El contador de strikes activos vuelve a **0**.
- **Historial:** se conservan todos los strikes y suspensiones. En el perfil (web y Android) se ve cuántas veces ha sido suspendido el usuario.

---

## 9. Notificaciones

- **Push con Firebase Cloud Messaging (FCM)**; se implementa en la **fase final**, y el sistema debe funcionar completo sin ellas.
  - Al crear una solicitud → a los contactos **vinculados** del usuario.
  - Al cambiar el estado de una solicitud → al usuario que la creó (p. ej. "Tu ayuda va en camino").
- El backend guarda el token FCM de cada dispositivo y lo borra al cerrar sesión. Envío con la API v1 de FCM (librería sugerida: `kreait/laravel-firebase`).
- **Dashboard web:** sin push; consulta periódica a la API cada 10–15 s para detectar solicitudes nuevas.

---

## 10. Stack tecnológico

### 10.1 Base de datos
- **MySQL**, puerto `3306`, base de datos **`YelpMe`**.
- **El esquema se define en `database/script-bd.sql`** (fuente oficial), no con migraciones de Laravel. Incluye tablas, llaves, índices y datos iniciales (admin inicial y catálogo de servicios). Cualquier cambio de esquema se hace en ese script.
- Consecuencias en Laravel:
  - `App\Models\User` usa `protected $table = 'usuarios'`.
  - `.env` con `SESSION_DRIVER=file` y `CACHE_STORE=file` (no se crean las tablas de sesiones/caché de Laravel; `throttle` funciona con caché en archivos).
  - La tabla `personal_access_tokens` de Sanctum viene en el script.
- Datos de prueba para la demo (usuarios, contactos, solicitudes en todos los estados): seeder o script aparte.

### 10.1.1 Política de datos: deshabilitar, no borrar
Los datos importantes **no se borran físicamente, se deshabilitan**, para conservar historial. Todas las FK usan `ON DELETE RESTRICT` para que la BD lo impida.

| Tabla | Qué pasa al "eliminar" |
|---|---|
| `usuarios` | Se deshabilita (`activo = 0`); solo un admin reactiva. |
| `servicios` | Se deshabilita (`activo = 0`). |
| `solicitudes` | Nunca se borran; terminan en un estado final. |
| `strikes` | Se anulan (`activo = 0` + quién, cuándo y por qué). |
| `suspensiones` | Historial; nunca se borran. |
| `contactos` | **Se borran** (decisión del usuario sobre su agenda). |
| `bloqueos` | **Se borran** al desbloquear. |
| `solicitud_servicio` | **Se borra** la fila al quitar un servicio. |
| `personal_access_tokens` | Se borran al cerrar sesión (Sanctum). |

### 10.2 Backend — PHP Laravel (`backend/`)
- **Controladores sin modelos Eloquent** (excepto `User` para Sanctum): acceso a datos con `DB::table(...)` / Query Builder.
- Controladores con métodos `index`, `show`, `store`, `update`, `destroy`.
- Rutas en `routes/api.php` (se genera con `php artisan install:api`, que instala Sanctum).
- **Disco de almacenamiento** en `config/filesystems.php` para fotos de perfil; `php artisan storage:link` para servirlas.
- Listados paginados (`paginate()`), sobre todo el historial.
- FCM para notificaciones push.

### 10.3 Frontend — Vue 3 + Vite (`frontend/`)
- **Bootstrap 5** + Bootstrap Icons (interfaz general).
- **Material Symbols** para los íconos de servicios.
- **SweetAlert2** para alertas y confirmaciones.
- **axios** como cliente HTTP.
- **Pinia** para estado global.
- **vue-router** con protección de rutas (sin token → `/login`).
- **Leaflet** para mapas.
- Vistas (`src/views/`) compuestas por componentes reutilizables (`src/components/`).

### 10.4 Mobile — Android Studio, Java (`mobile/`)
- **Los diseños los crea otra persona.** Nuestro trabajo: darles la lógica, ajustarlos al MVP y estandarizarlos.
- Plantilla **Empty Views Activity**.
- **ViewBinding** obligatorio.
- **RecyclerView** para listados, con adapters que usan ViewBinding.
- **Glide** para imágenes.
- **Retrofit** para consumir la API (token en header `Authorization: Bearer <token>`).
- **Firebase Cloud Messaging** para recibir notificaciones push.
- GPS con `FusedLocationProviderClient`; permisos en tiempo de ejecución y manejo del caso en que el usuario los niega.
- Estilo: clases internas anónimas cuando sea posible y constructores.
- Colores en `colors.xml` y textos en `strings.xml`; nada hardcodeado.

### 10.5 Identidad visual común
- Web y Android comparten **paleta de colores, tipografía e íconos** (Material Symbols para servicios).
- La paleta parte de los colores ya usados en la web (secciones y estados) y se replica en `colors.xml`. Las diferencias que aparezcan se corrigen.

---

## 11. Estado actual del proyecto

| Parte    | Estado |
|----------|--------|
| Frontend | Planes 002 y 007: panel completo (rutas con login, dashboard con recarga cada 15 s, solicitudes, usuarios, perfil) conectado a la API real. Sin datos de ejemplo. |
| Backend  | Planes 001 y 003–006: base, autenticación, contactos, solicitudes, gestión de usuarios (strikes, suspensiones, cuentas) y perfil propio. Pruebas sobre `YelpMe_pruebas`. Falta FCM. |
| Mobile   | Vacío; diseños en proceso por otra persona. |
| BD       | Creada con `database/script-bd.sql`: 9 tablas, admin inicial y catálogo de servicios. |

### Pendiente en el frontend
- Auditoría visual y de teclado en navegador del plan 002 (ver tarea 016).
- Fuente Material Symbols ≈ 4 MB: recortar a los íconos usados si el peso molesta.

---

## 12. Entidades

> Detalle completo de columnas, llaves e índices en `database/script-bd.sql`.

| Tabla | Función | Relaciones |
|---|---|---|
| `usuarios` | Cuentas de usuarios y admins (`rol`, `activo`). | — |
| `contactos` | Agenda del usuario (CRUD móvil). | usuarios 1:M (dueño) · usuarios 1:M (vinculado, opcional) |
| `bloqueos` | B bloquea a A; impide volver a vincular. | usuarios M:N usuarios |
| `servicios` | Catálogo con nombre, ícono Material Symbols y color. | — |
| `solicitudes` | Petición de ayuda con ubicación, estado, admin que atendió, quién cerró, motivo y fechas. | usuarios 1:M (creador / admin / cerró) |
| `solicitud_servicio` | Servicios requeridos por solicitud. | solicitudes M:N servicios |
| `strikes` | Sanciones; activas, reiniciadas o anuladas. | usuarios 1:M (sancionado / admin / anuló) · solicitudes 1:M (opcional) |
| `suspensiones` | Historial de suspensiones; vigente si `fecha_rehabilitacion` es NULL. | usuarios 1:M (suspendido / suspendió / rehabilitó) |
| `personal_access_tokens` | Tokens de Sanctum. | Polimórfica, sin FK |

Datos derivados (no se guardan como columna):
- **Suspendido** = existe una suspensión con `fecha_rehabilitacion IS NULL`.
- **Strikes activos** = `COUNT(*)` de strikes con `activo = 1`.
- **Veces suspendido** = número de filas en `suspensiones`.
- **Solicitud con servicios** = tiene filas en `solicitud_servicio`.

---

## 13. Criterios de aceptación del MVP

El día de la presentación debe poder demostrarse:

1. Registro e inicio de sesión de un usuario en Android; login de admin en la web.
2. CRUD completo de contactos desde Android, con vinculación automática por teléfono.
3. Que el usuario A no pueda ver ni modificar contactos o solicitudes del usuario B.
4. Llamada directa a un contacto.
5. Crear una solicitud con GPS y servicios, y verla en el mapa del dashboard web.
6. El admin corrige servicios, despacha y termina una solicitud; el historial muestra quién la atendió.
7. Rechazo por broma con strike; al tercer strike el usuario no puede pedir servicios, y el admin lo rehabilita.
8. Notificación push al contacto vinculado y al solicitante.

---

## 14. Fuera de alcance (MVP)

- Conexión con servicios reales de emergencia.
- Registro de administradores desde la web o la API.
- Seguimiento de ubicación en tiempo real (la ubicación se toma al crear la solicitud).
- Recuperación de contraseña.

---

## 15. Metodología de trabajo

### 15.1 Documentación
- **Planes:** `docs/plans/NNN-plan-x-AAAA-MM-DD.md` — un plan por función o meta, con las tareas (y subtareas) que lo componen.
- **Tareas:** `docs/tasks/NNN-tarea-x-AAAA-MM-DD.md` — cada tarea concreta para cumplir un plan.
- Numeración consecutiva de 3 dígitos.
- Este archivo (`principal-plan.md`) es el contexto raíz; no contiene tareas.

### 15.2 Git
- Una rama por tarea o sección (p. ej. `feature/backend-auth`, `feature/mobile-contactos`).
- Commits con Conventional Commits (`/conventional-commit`, `/caveman-commit`): tipo en inglés (`feat`, `fix`, `docs`…), descripción y cuerpo en español.
- **Sin línea de coautoría de Claude** en commits ni PRs.

### 15.3 Orden sugerido de planes
1. ✅ Plan 001 — Backend base: proyecto Laravel, conexión a `YelpMe`, datos de prueba.
2. ✅ Plan 002 — Frontend: estandarización de estructura y diseño con datos de ejemplo (vue-router, Pinia, services, 5 estados, Material Symbols).
3. ✅ Plan 003 — Autenticación: registro/login/logout de usuarios, login de admin, protección por rol, throttle.
4. ✅ Plan 004 — API de contactos (CRUD, vinculación por teléfono, desvincular/bloquear).
5. ✅ Plan 005 — API de solicitudes y servicios (estados, transiciones, motivos, admin que atiende).
6. ✅ Plan 006 — Gestión de usuarios (strikes, suspensiones, cuentas) y perfil propio.
7. ✅ Plan 007 — Frontend: conectar los services a la API con axios y consulta periódica del dashboard.
8. Mobile: integración de diseños, Retrofit, CRUD de contactos, solicitudes con GPS, llamadas.
9. Notificaciones FCM (backend + Android).
