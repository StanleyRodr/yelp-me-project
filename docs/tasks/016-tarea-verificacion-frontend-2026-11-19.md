# 016 — Tarea: Verificación del frontend

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: ✅ Completada (verificación automática) · Auditoría visual: ✅ hecha a mano

## Objetivo
Confirmar criterios del plan 002 antes del PR.

## Limitación
Extensión de Chrome sin conexión en la sesión. Sin capturas ni recorrido automatizado en navegador. Esa parte queda como auditoría manual (sección final).

## Pasos hechos

### 1. Búsquedas de reglas (todo `src/`)

| Regla | Resultado |
|---|---|
| Hex fuera de `tokens.css` | Ninguno (`mocks/datos.js` = dato de BD; comentario de folio) |
| `v-if` y `v-for` en el mismo elemento | Ninguno |
| `v-for` sin `:key` | Ninguno (avisos eran `:key` en la línea siguiente) |
| `<table>` / `<form>` en vistas | Ninguno |
| Estilos de vista que no sean rejilla | 1 encontrado: `.folio` en `AtencionView` → movido a `solicitudes/CabeceraSolicitud.vue` |
| `useAlertas` | Ninguno |
| `console.*` | Ninguno |
| `<img>` sin `alt` | Ninguno (avatar con `alt=""`: decorativo junto al nombre) |
| Componentes sin uso | Ninguno |

### 2. Limpieza
- Borrado `src/views/_legado/` (vistas viejas, `useAlertas`, `AppLegado`). Ya sin referencias.

### 3. Build y pruebas
- `npm run build`: sin advertencias. 269 módulos. Entrada 350 kB (117 kB gzip); vistas en chunks diferidos.
- Componentes `comunes/`, `modals/`, `layout/`: compilan 20/20 (script fuera del repo).
- Reglas de negocio de services: 23/23 OK. Perfil: 7/7 OK.

### 4. Accesibilidad revisada en código
- Contraste medido (tarea 008): textos ≥ 5.59:1; Enviado y Cancelado base solo en puntos/bordes (≥ 3:1).
- Foco visible global (`:focus-visible`), variante clara sobre navbar oscura.
- Enlace "Saltar al contenido".
- Navegación por teclado: folios y nombres son enlaces; chips con `aria-pressed`; marcadores del mapa enfocables.
- Modales: foco atrapado (Bootstrap), Esc, foco devuelto al disparador.
- Skeletons con `role="status"` + texto oculto; formularios con `aria-invalid` y `aria-describedby`.
- `prefers-reduced-motion`: animaciones y skeleton estáticos.

### 5. Documentación
- `CLAUDE.md`: arquitectura final del frontend, convenciones de texto, aviso de `504 Outdated Optimize Dep`.
- `principal-plan.md`: estado actual y orden de planes (002 ✅).

## Auditoría manual pendiente
Lista completa en el PR del plan 002 y en la respuesta de cierre. Resumen:
1. Recorrido funcional con datos de demo (login, dashboard, solicitudes, atención, usuarios, perfil).
2. Responsive en 1440 / 1024 / 390 px.
3. Recorrido solo con teclado.
4. Movimiento reducido activado.
5. Consola sin errores.

## Resultado
Commit: `chore(frontend): verifica reglas del plan 002 y elimina vistas legado`.
