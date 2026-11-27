# 048 — Tarea: Tema y recursos Android

> Plan: `008-plan-mobile-base-2026-11-26.md` · Rama: `feature/mobile-base` · Estado: ✅ Terminada

## Objetivo
Identidad igual a la web: paleta `--ym-*` clara y oscura, Atkinson Hyperlegible Next, Material Symbols, dimensiones y textos en recursos.

## Pasos

### 1. `res/values/colors.xml` y `res/values-night/colors.xml`
Mismos nombres que los tokens, con `_` en vez de `-`:
```xml
<color name="ym_marca">#14213D</color>
<color name="ym_fondo">#F3F5F8</color>
<color name="ym_superficie">#FFFFFF</color>
<color name="ym_texto">#1A2233</color>
<color name="ym_texto_secundario">#556070</color>
<color name="ym_borde">#D5DBE3</color>
<color name="ym_estado_pendiente">#C62828</color>
<!-- …todos los de tokens.css (marca, superficies, foco, 5 estados × 3, peligro, éxito) -->
```
`values-night`: valores del bloque oscuro de la tarea 046. Comentario arriba: "Espejo de frontend/src/styles/tokens.css: cambiar ambos". Actualizar el comentario de `tokens.css` igual.

### 2. Tema (`res/values/themes.xml`)
- `Theme.YelpMe` hereda de `Theme.Material3.DayNight.NoActionBar`.
- `colorPrimary` = `ym_marca`, `colorOnPrimary` = `ym_sobre_marca`, `android:colorBackground` = `ym_fondo`, `colorSurface` = `ym_superficie`, `colorOnSurface` = `ym_texto`, `colorOutline` = `ym_borde`, `colorError` = `ym_peligro`.
- `fontFamily` / `android:fontFamily` = `@font/atkinson`.
- Estilos: `Widget.YelpMe.Boton` (MaterialButton, `minHeight` 48dp, radio 6dp como la web), `Widget.YelpMe.Campo` (TextInputLayout OutlinedBox), `Widget.YelpMe.Tarjeta` (MaterialCardView), `TextAppearance.YelpMe.Titulo/Cuerpo/Pequeno` (28/16/14sp, pesos 700/400).
- Borrar `values-night/themes.xml` si solo repite (el DayNight toma los colores night).
- Renombrar `Theme.YelpME` → `Theme.YelpMe`; `app_name` = "YelpMe".

### 3. Fuente
- Atkinson Hyperlegible Next (licencia OFL): copiar los `.ttf` Regular, SemiBold y Bold a `res/font/atkinson_regular.ttf`, etc. (minúsculas y `_`), desde el paquete `@fontsource` del frontend o Google Fonts.
- `res/font/atkinson.xml` con `<font-family>` (pesos 400/600/700).
- Borrar `lilita_one.xml`, `raleway_*.xml`, `font_certs.xml`, `preloaded_fonts.xml`.

### 4. `res/values/dimens.xml`
`espacio_1..6` (4/8/12/16/24/32dp), `toque_min` 48dp, `radio` 6dp, `icono` 24dp, `icono_servicio` 32dp. Sustituir los dp literales de los layouts que se toquen.

### 5. Íconos Material Symbols (Vector Asset, estilo Outlined, 24dp)
- Servicios (mismos nombres que `servicios.icono`): `ic_local_police`, `ic_ambulance`, `ic_fire_truck`, `ic_traffic`, `ic_health_and_safety`, `ic_support`.
- UI: `ic_menu`, `ic_close`, `ic_arrow_back`, `ic_call`, `ic_star`, `ic_star_border`, `ic_add`, `ic_edit`, `ic_delete`, `ic_person`, `ic_group`, `ic_history`, `ic_logout`, `ic_my_location`, `ic_home`, `ic_block`, `ic_link_off`, `ic_visibility`, `ic_dark_mode`.
- `android:tint="?attr/colorOnSurface"` para que funcionen en ambos temas.
- Util `IconosServicio.de(String icono)` → `R.drawable.*` (fallback `ic_support`).

### 6. Limpiar drawables
- Borrar los 20 PNG (`bomb`, `bomberos`, `poli`, `cr`, `transi`, `pc`, `llave*`, `paloma*`, `palomita`, `men_`, `contac*`, …). Logo: convertir `dise_o_sin_t_tulo__18_` a `logo_yelpme.webp` ≤ 100 KB (512 px) o vector; el ícono del login con Material Symbols.
- `bg_*`, `input_bg`: borrar o pasar a colores de tema (`?attr/colorSurface`).
- Launcher: Image Asset con el logo sobre `ym_marca`.

### 7. Textos
- Todo `android:text`/`hint` de los layouts → `res/values/strings.xml` (prefijo por pantalla: `login_`, `registro_`, `inicio_`, `contactos_`, `historial_`, `perfil_`, `comun_`).
- Corregir: "Regístrate", "Restablecer", "YelpMe".
- `contentDescription` en todo ImageButton/ImageView accionable (`@string/cd_*`).

### 8. Aplicar a layouts
En cada layout: fondo `?android:colorBackground`, tarjetas `MaterialCardView`, campos `TextInputLayout` + `TextInputEditText`, botones con estilos del paso 2, sin hex. Conservar orden de secciones del diseño.

## Verificación
```bash
grep -rnE '#[0-9A-Fa-f]{6}' app/src/main/res/layout   # vacío
grep -rn 'android:text="[^@]' app/src/main/res/layout # vacío
./gradlew assembleDebug
```
Emulador: tema claro y oscuro (Ajustes → Pantalla) sin textos ilegibles.

## Resultado
Commit: `style(mobile): aplica paleta, tipografía e íconos de YelpMe`.

Notas: Atkinson como TTF variable (`res/font/atkinson_variable.ttf`, 79 KB) convertido del woff2 del frontend con fontTools; licencia OFL en `mobile/LICENCIA-atkinson.txt`. Íconos generados desde la fuente Material Symbols del frontend (mismos glifos que la web), `ic_star` = variante rellena. `colorPrimary` = `ym_marca_texto` para que botones y foco se vean en oscuro; color solo Android `ym_sobre_marca_texto`. Logo blanco sobre transparente (`logo_yelpme.webp`, 6 KB) e ícono adaptable sobre `ym_marca`. Layouts de contenido sin header ni barra (los pone `BaseActivity`, tarea 052); `menu_drawer.xml` borrado.
