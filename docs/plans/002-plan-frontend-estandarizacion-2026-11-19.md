# 002 — Plan: Estandarización del frontend

> Fecha: 2026-11-19 · Rama: `feature/frontend-base` (apilada sobre `feature/backend-base`)
> Referencia: `docs/plans/principal-plan.md` (secciones 4.2, 7, 8, 10.3 y 10.5)

## Objetivo

Reestructurar el panel web del administrador con la arquitectura del plan principal (vue-router, Pinia, services) y un **diseño estandarizado**, conservando lo que sirve de la UI actual. Los datos siguen siendo de ejemplo, pero con la forma exacta de la BD: conectar la API real solo cambiará los *services*.

**No incluye:** conexión real a la API (depende del plan de autenticación del backend, ahora plan 003) ni consulta periódica del dashboard.

## Punto de partida

- Navegación manual con `currentView` en `App.vue`; sin login.
- Estado en `composables/useAlertas.js` con mocks de 6 estados obsoletos.
- Cada vista define colores propios; la navbar cambia de color por sección; radios inconsistentes.
- `style.css` es resto de la plantilla de Vite y no se importa.
- `UsuariosView` (712 líneas) mezcla tabla, perfil y modal.
- Modal "Cancelar envío de ayuda" contradice las reglas (solo el usuario cancela).

## Decisiones

### Arquitectura
- **Rutas:** `/login`, `/`, `/solicitudes`, `/solicitudes/:id`, `/usuarios`, `/usuarios/:id`, `/perfil`. Todas protegidas salvo `/login`.
- **Login simulado:** solo admins del seeder; token falso en Pinia + `localStorage`; guard de rutas.
- **Capa de datos:** vistas → stores (Pinia) → services → mocks. Los mocks replican la BD (`snake_case`, 5 estados, servicios, strikes, suspensiones) y los usuarios del seeder.
- Escritorio primero; bien en tablet; usable en celular.

### Componentes
- Carpetas por dominio: `comunes/`, `layout/`, `modals/`, `dashboard/`, `solicitudes/`, `usuarios/`, `perfil/`.
- **Vista = composición:** acomoda secciones y conecta el store. Sin tablas, formularios ni estilos propios salvo la rejilla.
- Solo vistas y modales usan stores; secciones y comunes son presentacionales (props + emits).
- `<style scoped>` usando solo tokens (`--ym-*`, `--bs-*`); ningún hex fuera del archivo de tokens.
- `v-for` con `:key` estable para todo lo repetido (navegación, estados, filas, servicios, acciones, línea de tiempo); `v-if` para estados de la vista; `v-show` para lo que se alterna seguido; nunca `v-if` y `v-for` en el mismo elemento.

### Diseño
- **Paleta:** marca azul tinta `#14213D`, fondo `#F3F5F8`, superficie `#FFFFFF`, texto `#1A2233`, borde `#D5DBE3`.
- **Estados (color + ícono):** Pendiente `#C62828` `emergency` · Enviado `#B26A00` `local_shipping` · Terminado `#2E7D32` `check_circle` · Rechazado `#37474F` `block` · Cancelado `#78909C` `cancel`. Se replican en `colors.xml` de Android.
- **Tipografía:** Atkinson Hyperlegible Next (npm, offline), escala 14/16/20/28, cifras tabulares para folios y horas, etiquetas en tipo oración.
- **Forma:** radio único de 6 px, bordes en vez de sombras, navbar fija con identidad única; el color fuerte solo significa estado.
- **Íconos:** Material Symbols (npm) para servicios y estados; Bootstrap Icons para el resto de la UI.
- **Elemento memorable:** el indicador de tiempo de espera de la cola de pendientes, que se intensifica con los minutos.
- **Modales:** confirmaciones simples con SweetAlert2 tematizado; formularios como componentes en `modals/`. Acciones del admin según plan principal 7.2: Enviar, Rechazar (motivo + casilla de strike), Terminar, editar servicios (Pendiente y Enviado).

### Carga: skeleton y círculo
Sin dependencias nuevas: placeholders y spinners de Bootstrap 5.3.

| Situación | Indicador |
|---|---|
| Carga inicial de vista o sección (tabla, cola, detalle, perfil) | Skeleton con la forma del contenido |
| Mapa mientras llegan los datos | Skeleton de bloque del tamaño del mapa |
| Botón de acción (guardar, enviar, iniciar sesión) | Círculo pequeño dentro del botón, botón deshabilitado |
| Recarga periódica (futuro) | Ninguno: se conservan los datos y se reemplazan al llegar |
| Confirmación con SweetAlert2 | `Swal.showLoading()` |

- Estado `cargando` en cada store; las vistas eligen skeleton o contenido con `v-if` / `v-else`.
- Los services simulan ~400 ms de latencia; el skeleton no aparece si la carga dura menos de ~150 ms.
- Accesibilidad: `aria-busy`, texto oculto "Cargando…", bloques con `aria-hidden`, sin animación con `prefers-reduced-motion`.

---

## Tareas

> Resumen de cada tarea. Los pasos detallados están en `docs/tasks/`.

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 008 | [Base de diseño](../tasks/008-tarea-base-diseno-2026-11-19.md) | Tokens CSS, fuente, Material Symbols, tema de Bootstrap y SweetAlert2; quitar `style.css` viejo. | ✅ |
| 009 | [Arquitectura](../tasks/009-tarea-arquitectura-frontend-2026-11-19.md) | vue-router con guard, layout, stores Pinia, services con mocks de la BD; quitar `useAlertas`. | ✅ |
| 010 | [Componentes comunes y modales](../tasks/010-tarea-componentes-comunes-2026-11-19.md) | Badge de estado, ícono de servicio, mapa base, línea de tiempo, búsqueda, skeletons y modales de acción. | ✅ |
| 011 | [Login](../tasks/011-tarea-login-2026-11-19.md) | Pantalla de inicio de sesión simulado, logout y sesión persistente. | ✅ |
| 012 | [Dashboard](../tasks/012-tarea-dashboard-2026-11-19.md) | Franja de estados, cola de pendientes con tiempo de espera y mapa de incidentes. | ✅ |
| 013 | [Solicitudes y atención](../tasks/013-tarea-solicitudes-atencion-2026-11-19.md) | Tabla + mapa con filtros; detalle por folio con línea de tiempo y acciones según transición. | ✅ |
| 014 | [Usuarios](../tasks/014-tarea-usuarios-2026-11-19.md) | Tabla con estado de cuenta y perfil con historial, strikes, suspensiones y acciones. | ✅ |
| 015 | [Perfil del admin](../tasks/015-tarea-perfil-admin-2026-11-19.md) | Datos editables, foto, cambio de contraseña y solicitudes atendidas. | Pendiente |
| 016 | [Verificación](../tasks/016-tarea-verificacion-frontend-2026-11-19.md) | Revisión visual con capturas, teclado, contraste, responsive y build. | Pendiente |

---

## Criterios de terminado

- [ ] Todas las rutas funcionan y las protegidas redirigen a `/login` sin sesión.
- [ ] Ningún componente usa `useAlertas`; los datos salen de stores → services → mocks.
- [ ] Solo existen los 5 estados oficiales y las acciones permitidas por estado.
- [ ] Ningún color hexadecimal fuera del archivo de tokens.
- [ ] Ninguna vista contiene tablas, formularios o estilos propios salvo la rejilla.
- [ ] Skeleton en cargas de sección y círculo solo en acciones.
- [ ] Foco visible, navegación por teclado y `prefers-reduced-motion` respetado.
- [ ] `npm run build` sin errores ni advertencias nuevas.
- [ ] Commits por tarea, en español, sin coautoría; PR al terminar.
