# 010 — Plan: Contactos en Android

> Fecha: 2026-11-26 · Rama: `feature/mobile-contactos` (desde `main`, tras fusionar el plan 009)
> Referencia: `docs/plans/principal-plan.md` (secciones 3.1, 6, 6.4, 6.5) · plan 004 (API de contactos)

## Objetivo

Cumplir el **CRUD obligatorio desde Android**: el usuario agrega, consulta, modifica y elimina contactos de confianza contra la API, ve si están vinculados, marca un favorito y lo llama desde la barra. También ve quién lo tiene como contacto (con el domicilio de esas personas), se desvincula, bloquea y desbloquea.

**No incluye:** notificaciones a los contactos (plan 012).

## Punto de partida
- API del plan 004: `apiResource /api/contactos`, `GET /api/telefonos/{telefono}`, `/api/vinculos`, `/api/bloqueos`.
- Domicilio en la API (plan 009) con `PresentadorDomicilio::porUsuarios`.
- App: `ContactosActivity` del diseño (3 tarjetas que se alternan, lista con `TextView` a mano, campos apodo/ID/dirección/mapa que no existen en la API). `BaseActivity` y barra con botón de favorito provisional (plan 008).

## Decisiones
- **Favorito en la API:** columna `contactos.favorito` (BOOLEAN). Máximo uno por usuario, garantizado en transacción (marcar uno desmarca los demás). Endpoints `PUT|DELETE /api/contactos/{id}/favorito`. La lista sale con el favorito primero.
- **Vínculos con domicilio:** `GET /api/vinculos` agrega `domicilio` de cada persona que me tiene (ella me eligió como contacto de confianza, así que comparte su domicilio conmigo). Bloqueos no lo incluyen.
- **Campos del contacto:** solo nombre, teléfono y parentesco (modelo de la API). Se quitan apodo, nombre de usuario, ID, dirección y mapa del diseño.
- **Pantallas:** lista (`ContactosActivity`), detalle (`DetalleContactoActivity`), formulario crear/editar (`FormContactoActivity`), `VinculosActivity`, `BloqueosActivity`. Se conserva el estilo de tarjetas del diseño.
- Parentesco: campo libre con sugerencias (Mamá, Papá, Pareja, Hermano/a, Hijo/a, Amistad, Otro).
- Verificación de teléfono mientras escribe (debounce 600 ms, solo con 10 dígitos) con `GET /api/telefonos/{telefono}`.
- **Llamada:** `util/Llamadas.llamar(Activity, telefono)`: `ACTION_CALL` si hay `CALL_PHONE`; si se niega, `ACTION_DIAL`. Usado en detalle y en la barra.

---

## Tareas

| # | Tarea | Resumen | Estado |
|---|---|---|---|
| 061 | [Favorito y vínculos con domicilio (API)](../tasks/061-tarea-favorito-api-2026-11-26.md) | Columna, endpoints, orden, domicilio en vínculos, pruebas. | ✅ |
| 062 | [Lista de contactos](../tasks/062-tarea-lista-contactos-2026-11-26.md) | RecyclerView, búsqueda, favorito primero, vinculado. | ✅ |
| 063 | [Crear y editar contacto](../tasks/063-tarea-form-contacto-2026-11-26.md) | Formulario con verificación de teléfono y errores de la API. | ✅ |
| 064 | [Detalle, llamada y favorito](../tasks/064-tarea-detalle-contacto-2026-11-26.md) | Detalle, eliminar, marcar favorito, llamar; barra activa. | ⬜ |
| 065 | [Vínculos y bloqueos](../tasks/065-tarea-vinculos-bloqueos-android-2026-11-26.md) | Quién me tiene (con domicilio), desvincular, bloquear, desbloquear. | ⬜ |
| 066 | [Verificación del plan 010](../tasks/066-tarea-verificacion-010-2026-11-26.md) | CRUD de punta a punta, propiedad, documentación. | ⬜ |

---

## Criterios de terminado

- [ ] Crear → consultar → modificar → eliminar contactos desde Android contra la API.
- [ ] Vinculación automática visible (contacto con cuenta muestra "Vinculado").
- [ ] Favorito único; la barra llama al favorito con `ACTION_CALL`/`ACTION_DIAL`.
- [ ] Vínculos con domicilio; desvincular, bloquear y desbloquear funcionan.
- [ ] Un usuario no ve ni toca contactos ajenos (404).
- [ ] `php artisan test` y `./gradlew assembleDebug` sin errores.
- [ ] Commits por tarea, sin atribución; PR a `main`.
