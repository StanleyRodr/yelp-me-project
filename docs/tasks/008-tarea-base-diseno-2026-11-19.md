# 008 — Tarea: Base de diseño

> Plan: `002-plan-frontend-estandarizacion-2026-11-19.md` · Rama: `feature/frontend-base` · Estado: Pendiente

## Objetivo
Un solo sistema visual para todo el panel: tokens, fuente, íconos, tema de Bootstrap y de SweetAlert2. Ninguna vista define colores propios.

## Pasos

### 1. Instalar fuente e íconos (offline)

```bash
cd frontend
npm i @fontsource-variable/atkinson-hyperlegible-next material-symbols
```

- Fuente variable: un archivo cubre todos los pesos.
- `material-symbols`: importar solo `outlined.css`. Clase `.material-symbols-outlined`.

### 2. Quitar `src/style.css`
Resto de plantilla Vite (acento morado, modo oscuro). `main.js` no lo importa. Borrar.

### 3. Crear `src/styles/tokens.css`
Único archivo con colores hex. Prefijo `--ym-`.

- Marca, fondo, superficie, texto, texto secundario, borde.
- Por estado, 3 variables:
  - `--ym-estado-X`: color aprobado. Puntos, marcadores, bordes.
  - `--ym-estado-X-texto`: variante oscura. Texto sobre fondo claro, contraste ≥ 4.5:1.
  - `--ym-estado-X-fondo`: tinte claro para fondo de badge.
- Tipografía: familia, escala 14/16/20/28 (`--ym-texto-sm/base/lg/xl`).
- Forma: `--ym-radio: 6px`, espaciados.

Contraste: `#B26A00` (Enviado) y `#78909C` (Cancelado) quedan < 4.5:1 sobre blanco. Se usan solo como color de punto/borde; el texto usa su variante `-texto`.

### 4. Crear `src/styles/tema-bootstrap.css`
Mapear tokens a Bootstrap sin Sass:

- `:root`: `--bs-body-font-family`, `--bs-body-bg`, `--bs-body-color`, `--bs-border-color`, `--bs-border-radius*`, `--bs-primary`, `--bs-primary-rgb`, `--bs-link-color`, `--bs-focus-ring-color`.
- Bootstrap compila colores de componentes en cada clase. Sobrescribir variables locales:
  - `.btn-primary`, `.btn-outline-primary`: `--bs-btn-*`.
  - `.form-control:focus`, `.form-select:focus`, `.form-check-input:checked`: borde y anillo de foco con marca.
  - `.table`: `--bs-table-*` neutros.
  - `.placeholder`: color de skeleton.
- Sin sombras en `.card`, `.dropdown-menu`; usar borde.

### 5. Crear `src/styles/base.css`
- `html { font-size: 16px }`, cuerpo con familia del token.
- `.ym-cifras`: `font-variant-numeric: tabular-nums` para folios y horas.
- `.material-symbols-outlined`: tamaño alineado al texto, `vertical-align`.
- Foco visible: `:focus-visible` con anillo de marca de 2 px en todo elemento interactivo.
- `@media (prefers-reduced-motion: reduce)`: sin animaciones ni transiciones; skeleton estático.
- `.visually-hidden` ya viene en Bootstrap; no duplicar.

### 6. Tema de SweetAlert2
- `src/styles/sweetalert.css`: tipografía, radio, colores de botón con tokens.
- `src/plugins/alertas.js`: `Swal.mixin` con textos en español y `buttonsStyling: false` + clases de Bootstrap. Exportar:
  - `confirmar({ titulo, texto, confirmar })` → `Promise<boolean>`
  - `exito(mensaje)`, `error(mensaje)` (toast arriba a la derecha)
- Botón de confirmar siempre con verbo concreto ("Enviar ayuda", no "Aceptar").

### 7. Registrar en `main.js`
Orden: Bootstrap CSS → Bootstrap Icons → Leaflet CSS → fuente → Material Symbols → `tokens.css` → `tema-bootstrap.css` → `base.css` → `sweetalert.css`. Tokens después de Bootstrap para que ganen.

### 8. `index.html`
`lang="es"`, título `Yelp Me | Panel de administración`.

## Verificación
- `npm run build` sin errores.
- `grep -rE '#[0-9a-fA-F]{3,8}' src --include=*.css` solo encuentra `tokens.css` (vistas viejas se limpian en tareas 012–015).
- En el navegador: fuente Atkinson aplicada, `<span class="material-symbols-outlined">ambulance</span>` muestra ícono.

## Resultado
Pendiente.
