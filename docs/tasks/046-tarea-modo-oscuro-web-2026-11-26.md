# 046 — Tarea: Modo oscuro en la web

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Panel con tema claro, oscuro o del sistema. Paleta oscura definida aquí; la tarea 048 la replica en Android.

## Pasos

### 1. Tokens oscuros en `styles/tokens.css`
Bloque `:root[data-theme="dark"]` con **los mismos nombres** `--ym-*`. Propuesta (validar contraste ≥ 4.5:1 en texto):
| Token | Oscuro |
|---|---|
| `--ym-fondo` | `#0F1420` |
| `--ym-superficie` | `#182031` |
| `--ym-texto` | `#E6EAF2` |
| `--ym-texto-secundario` | `#A3ADBF` |
| `--ym-borde` / `--ym-borde-fuerte` | `#2C3649` / `#46526A` |
| `--ym-marca` | `#14213D` (navbar igual) · `--ym-marca-suave` `#22304F` |
| `--ym-foco` | `#7FA2FF` |
| `--ym-skeleton` | `#232C3F` |
| Estados `-texto` | versiones claras (p. ej. pendiente `#FF8A80`, enviado `#FFB74D`, terminado `#81C784`, rechazado `#B0BEC5`, cancelado `#CFD8DC`) |
| Estados `-fondo` | tintes oscuros (p. ej. pendiente `#3A1A1D`, enviado `#3A2A12`, terminado `#1B3020`, rechazado `#263238`, cancelado `#2A3236`) |
Recalcular los `-rgb`. Sigue siendo el único archivo con hex.

### 2. `composables/useTema.js`
- Valores `'claro' | 'oscuro' | 'sistema'`, guardado en `localStorage['ym_tema']` (con `try/catch`). Por defecto `'sistema'`.
- Aplica `document.documentElement.dataset.theme = 'dark'|'light'` y `data-bs-theme` (Bootstrap 5.3).
- Con `'sistema'`: `matchMedia('(prefers-color-scheme: dark)')` + listener de cambio.
- Llamar en `main.js` antes de montar (evita parpadeo); también script mínimo en `index.html` que lee `ym_tema`.

### 3. Selector
- `AppNavbar.vue`: menú del usuario → "Tema" con 3 opciones (radio, ícono `bi-sun`, `bi-moon`, `bi-circle-half`).
- Login: botón pequeño de tema en una esquina (para recuperar/entrar de noche).

### 4. Ajustes por librería
- `tema-bootstrap.css`: variables `--bs-*` apuntan a tokens en ambos temas (`--bs-body-bg`, `--bs-body-color`, `--bs-border-color`, `--bs-secondary-color`, `--bs-tertiary-bg`). Revisar tablas, `form-control`, `dropdown`, `modal`.
- `sweetalert.css`: fondo `--ym-superficie`, texto `--ym-texto`.
- `MapaBase.vue` (Leaflet): en oscuro, `filter: brightness(.85) contrast(1.1)` sobre `.leaflet-tile-pane` (las teselas OSM no tienen versión oscura gratuita). Marcadores y popups con tokens.
- Skeletons y badges: ya usan tokens; revisar.

### 5. Recorrido
Login, dashboard, solicitudes, atención, usuarios, detalle, perfil y modales en ambos temas. Ningún texto ilegible ni fondo blanco suelto.

## Verificación
- `npm run build`.
- Cambiar tema del sistema con "Sistema" elegido → el panel cambia sin recargar.
- Recargar conserva la elección.

## Resultado
Commit: `feat(frontend): agrega modo oscuro con selector de tema`.

Notas: token nuevo `--ym-marca-texto` (marca como texto/acento; en oscuro `#A9BFFF`) y `--ym-marca-contorno`, porque el navy sobre fondo oscuro no se lee. `--ym-peligro`/`--ym-exito` conservan el tono claro en oscuro (botones con texto blanco). Selector en `components/comunes/SelectorTema.vue`. **Pendiente:** recorrido visual en navegador (sin extensión de Chrome en esta sesión).

Ajuste posterior (`fix(panel)`): selector cambiado a un solo botón de ícono que cicla Claro → Oscuro → Sistema (`title`/`aria-label` con el actual y el siguiente), en la navbar junto al menú del usuario (se quitaron las opciones del menú) y en login/recuperación. "Sistema oscuro con Windows en claro" no era fallo: el navegador tenía su propio modo fijo en oscuro y `prefers-color-scheme` lo reporta así.

Segundo ajuste (`refactor(panel)`): se quitó la opción "Sistema"; el botón alterna Claro ↔ Oscuro. Sin elección guardada el panel sigue al sistema; un valor viejo `sistema` en `ym_tema` se trata como sin elección.
